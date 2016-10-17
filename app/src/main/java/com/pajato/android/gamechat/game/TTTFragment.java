/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.game.model.Player;
import com.pajato.android.gamechat.game.model.TicTacToe;
import com.pajato.android.gamechat.main.NetworkManager;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.account.AccountManager.SIGNED_OUT_EXPERIENCE_KEY;
import static com.pajato.android.gamechat.account.AccountManager.SIGNED_OUT_OWNER_ID;
import static com.pajato.android.gamechat.game.ExpType.ttt;
import static com.pajato.android.gamechat.main.NetworkManager.OFFLINE_EXPERIENCE_KEY;
import static com.pajato.android.gamechat.main.NetworkManager.OFFLINE_OWNER_ID;

/**
 * A Tic-Tac-Toe game that stores its current state on Firebase, allowing for cross-device play.
 *
 * TODO: Abstract out a player: name, win count, association (X/O/black/white/etc.), type (creator,
 * online, offline, gamechat, other?)
 *
 * @author Bryan Scott
 */
public class TTTFragment extends BaseGameFragment {

    // Private constants.

    /** The logcat TAG. */
    private static final String TAG = TTTFragment.class.getSimpleName();

    // Private instance variables.

    /* A convenience array that simplifies win determination. */
    private int[][] mBoardValues = new int[3][3];

    // Public instance methods.

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_game_ttt;}

    /** Placeholder while message handler stays relevant for chess and checkers. */
    @Override public void messageHandler(final String msg) {}

    /** Handle a tile click event by sending a message to the current tic-tac-toe fragment. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Determine if the payload exists and is not a string, in which case, abort as the event is
        // of no interest here.
        Object payload = event.view.getTag();
        if (!(payload instanceof String)) return;

        // Determine if the payload is a board position encoding.
        String tag = (String) payload;
        if (tag.startsWith("button")) {
            // It is an encoded button position.  Deal with it.
            handleTileClick(tag);
            return;
        }

        // Determine if the tag is this fragment's classname, in which case we play another game.
        if (this.getClass().getSimpleName().equals(tag)) handleNewGame();
    }

    /** Handle an experience posting event to see if this is a tictactoe experience. */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Check the payload to see if this is not a tictactoe.  Abort if not.
        if (event.experience == null || event.experience.getExperienceType() != ttt) return;

        // The experience is a tictactoe experience.  Start the game.
        mExperience = event.experience;
        resume();
    }

    /** Handle taking the foreground by updating the UI based on the current expeience. */
    @Override public void onResume() {
        // Determine if there is an experience ready to be enjoyed.  If not, hide the layout and
        // present a spinner.  When an experience is posted by the app event manager, the game can
        // be shown
        super.onResume();
        resume();
    }

    // Protected instance methods.

    /** Return an experience for a given dispatcher instance. */
    @Override protected void createExperience(@NonNull final Context context,
                                              @NonNull final Dispatcher dispatcher) {
        // Set up the players and persist the game.
        List<Account> players = getPlayers(dispatcher);
        createExperience(context, players);
    }

    /** Handle a requrest to setup the experience by initializing the convenience board values. */
    @Override protected void setupExperience(final Context context, final Dispatcher dispatcher) {
        super.setupExperience(context, dispatcher);
        initBoard();
    }

    // Private instance methods.

    /** Return a default, partially populated, TicTacToe experience. */
    private void createExperience(final Context context, final List<Account> playerAccounts) {
        // Setup the default key, players, and name.
        String key = getExperienceKey();
        List<Player> players = getDefaultPlayers(context, playerAccounts);
        String name1 = players.get(0).name;
        String name2 = players.get(1).name;
        long tstamp = new Date().getTime();
        String name = String.format(Locale.US, "%s vs %s on %s", name1, name2, tstamp);

        // Set up the default group and room keys, the owner id and return the value.
        String groupKey = DatabaseListManager.instance.getGroupKey();
        String roomKey = DatabaseListManager.instance.getRoomKey();
        String id = getOwnerId();
        TicTacToe model = new TicTacToe(key, id, name, tstamp, groupKey, roomKey, players);
        DatabaseManager.instance.createExperience(model);
    }

    /** Return a done message text to show in a snackbar.  The given model provides the state. */
    private String getDoneMessage(final TicTacToe model) {
        // Determine if there is a winner.  If not, return the "tie" message.
        String name = model.getWiningPlayerName();
        if (name == null) return getString(R.string.TieMessage);

        // There was a winner.  Return a congratulatory message.
        String format = getString(R.string.WinMessage);
        return String.format(Locale.getDefault(), format, name);
    }

    /** Return either a null placeholder key value or a sentinel value as the experience key. */
    private String getExperienceKey() {
        // Determine if there is a signed in account.  If so use the null placeholder.
        String accountId = AccountManager.instance.getCurrentAccountId();
        if (accountId != null) return null;

        // There is no signed in User.  Return one of the two sentinel values associated with being
        // either signed out or without access to a network.
        final boolean ONLINE = NetworkManager.instance.isConnected();
        return ONLINE ? SIGNED_OUT_EXPERIENCE_KEY : OFFLINE_EXPERIENCE_KEY;
    }

    /** Return the TicTacToe model class, null if it does not exist. */
    private TicTacToe getModel() {
        if (mExperience == null || !(mExperience instanceof TicTacToe)) return null;
        return (TicTacToe) mExperience;
    }

    // Return either a signed in User id or a sentinel value as the owner id. */
    private String getOwnerId() {
        // Determine if there is a signed in account.  If so return it.
        String accountId = AccountManager.instance.getCurrentAccountId();
        if (accountId != null) return accountId;

        // There is no signed in User.  Return one of the two sentinel values associated with being
        // either signed out or without access to a network.
        return NetworkManager.instance.isConnected() ? SIGNED_OUT_OWNER_ID : OFFLINE_OWNER_ID;
    }

    /** Return a possibly null list of player information for a two participant experience. */
    private List<Account> getPlayers(final Dispatcher dispatcher) {
        // Determine if this is an offline experience in which no accounts are provided.
        Account player1 = AccountManager.instance.getCurrentAccount();
        if (player1 == null) return null;

        // This is an online experience.  Use the current signed in User as the first player.
        List<Account> players = new ArrayList<>();
        players.add(player1);

        // Determine the second account, if any, based on the room.
        String key = dispatcher.roomKey;
        Room room = key != null ? DatabaseListManager.instance.roomMap.get(key) : null;
        int type = room != null ? room.type : -1;
        switch (type) {
            //case MEMBER:
                // Handle another User by providing their account.
            //    break;
            default:
                // Only one online player.  Just return.
                break;
        }

        return players;
    }

    /** Return a list of default TicTacToe players. */
    private List<Player> getDefaultPlayers(final Context context, final List<Account> players) {
        List<Player> result = new ArrayList<>();
        String name = getPlayerName(getPlayer(players, 0), "Player1");
        String symbol = context.getString(R.string.xValue);
        result.add(new Player(name, symbol));
        name = getPlayerName(getPlayer(players, 1), context.getString(R.string.friend));
        symbol = context.getString(R.string.oValue);
        result.add(new Player(name, symbol));

        return result;
    }

    /** Return the account associated with the given index, null if there is no such account. */
    private Account getPlayer(final List<Account> players, final int index) {
        // Determine if there is such an account, returning null if not.
        if (players == null || index < 0 || index >= players.size()) return null;

        // There is an account so return it.
        return players.get(index);
    }

    /** Return a name for the player by using the given account or a default. */
    private String getPlayerName(final Account player, final String defaultName) {
        // Determine if there is an account to use.  Return the default name if not.
        if (player == null) return defaultName;

        // There is an account.  Use the first name for the game.
        return player.getFirstName(defaultName);
    }

    /** Handle the turn indicator management by manipulating the turn icon size and decorations. */
    private void setPlayerIcons(final boolean turn) {
        // Alternate the decorations on each player symbol.
        if (turn)
            // Make player1's decorations the more prominent.
            setPlayerIcons(R.id.player1Symbol, R.id.leftIndicator1, R.id.rightIndicator1,
                           R.id.player2Symbol, R.id.leftIndicator2, R.id.rightIndicator2);
        else
            // Make player2's decorations the more prominent.
            setPlayerIcons(R.id.player2Symbol, R.id.leftIndicator2, R.id.rightIndicator2,
                           R.id.player1Symbol, R.id.leftIndicator1, R.id.rightIndicator1);
    }

    /**
     * Evaluates the state of the board, determining if there are three in a row of either X or O.
     *
     * @return false if a player has won or if the full number of turns has occurred, true otherwise.
     */
    private int getState(@NonNull final TicTacToe model, String buttonTag) {
        // Update the array of moves with the value of button tag from the model.  The button tag
        // has been vetted and will cause no exceptions.
        int value = model.getSymbolValue();
        int i = Integer.parseInt(buttonTag.substring(6, 7));
        int j = Integer.parseInt(buttonTag.substring(7, 8));
        mBoardValues[i][j] = value;

        // Evaluate all possible lines of 3.
        int topRow = mBoardValues[0][0] + mBoardValues[0][1] + mBoardValues[0][2];
        int midRow = mBoardValues[1][0] + mBoardValues[1][1] + mBoardValues[1][2];
        int botRow = mBoardValues[2][0] + mBoardValues[2][1] + mBoardValues[2][2];
        int startCol = mBoardValues[0][0] + mBoardValues[1][0] + mBoardValues[2][0];
        int centerCol = mBoardValues[0][1] + mBoardValues[1][1] + mBoardValues[2][1];
        int endCol = mBoardValues[0][2] + mBoardValues[1][2] + mBoardValues[2][2];
        int leftDiag = mBoardValues[0][0] + mBoardValues[1][1] + mBoardValues[2][2];
        int rightDiag = mBoardValues[2][0] + mBoardValues[1][1] + mBoardValues[0][2];

        // If any lines of 3 are equal to 3, X wins.
        boolean xWins = (topRow == 3 || midRow == 3 || botRow == 3 || startCol == 3
                || centerCol == 3 || endCol == 3 || leftDiag == 3 || rightDiag == 3);
        if (xWins) return TicTacToe.X_WINS;

        // If any lines of 3 are equal to 6, O wins.
        boolean oWins = (topRow == 6 || midRow == 6 || botRow == 6 || startCol == 6
                || centerCol == 6 || endCol == 6 || leftDiag == 6 || rightDiag == 6);
        if (oWins) return TicTacToe.O_WINS;

        // Determine if there is a tie.
        if (model.board.size() == 9) return TicTacToe.TIE;

        return TicTacToe.ACTIVE;
    }

    /** Handle a new game by resetting the data model. */
    private void handleNewGame() {
        // Ensure that the data model exists and is valid.
        TicTacToe model = getModel();
        if (model == null) {
            Log.e(TAG, "Null TTT data model.", new Throwable());
            return;
        }

        // Reset the data model and update the database.
        model.board = null;
        model.state = TicTacToe.ACTIVE;
        DatabaseManager.instance.updateExperience(mExperience);
    }

    /** Handle a click on a given tile by updating the value on the tile and start the next turn. */
    private void handleTileClick(final String buttonTag) {
        // Ensure that the click occurred on a grid button.  Abort if not.
        View view = mLayout.findViewWithTag(buttonTag);
        if (view == null || !(view instanceof Button)) return;

        // The click occurred on a grid button.  Ensure that the data model exists, aborting if not.
        TicTacToe model = getModel();
        if (model == null) {
            Log.e(TAG, "Null experience model detected", new Throwable());
            return;
        }

        // Ensure that the game is still active, providing a "Play again?" snackbar message if not.
        if (model.state != TicTacToe.ACTIVE) {
            // Use the coordinator view to manage the FAB button movement and notify the User via a
            // snackbar that the game is over.
            NotificationManager.instance.notify(this, getDoneMessage(model), true);
            return;
        }

        // Update the database with the collected changes.
        if (model.board == null) model.board = new HashMap<>();
        model.board.put(buttonTag, model.getSymbolText());
        model.state = getState(model, buttonTag);
        model.setWinCount();
        model.toggleTurn();
        DatabaseManager.instance.updateExperience(mExperience);
    }

    /** Initialize the board values and clear the winner text. */
    private void initBoard() {
        // Ensure that the layout has been established. Abort if not.
        if (mLayout == null) return;

        // Clear the winner text and the notification snackbar message.
        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        if (winner != null) winner.setText("");
        NotificationManager.instance.dismiss();

        // Clear the board evaluation support and all the X's and O's from the button grid.
        mBoardValues = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // Seed the array with a value that will guarantee the X vs O win and a tie will be
                // calculated correctly.
                mBoardValues[i][j] = -5;

                // Determine if the grid button needs to be reset.  Continue if not.
                if (mLayout == null) continue;

                // Reset the grid button.
                String tag = String.format(Locale.US, "button%d%d", i, j);
                TextView button = (TextView) mLayout.findViewWithTag(tag);
                if (button != null) button.setText("");
            }
        }
    }

    /** Process a resumption by testing and waiting for the experience. */
    private void resume() {
        if (mExperience == null) {
            // Disable the layout and startup the spinner.
            mLayout.setVisibility(View.GONE);
            String title = "TicTacToe";
            String message = "Waiting for the database to provide the game...";
            ProgressManager.instance.show(getContext(), title, message);
        } else {
            // Hide the spinner, start the game and update the views using the current state of the
            // experience.
            ProgressManager.instance.hide();
            mLayout.setVisibility(View.VISIBLE);
            setTitles(mExperience.getGroupKey(), mExperience.getRoomKey());
            updateExperience();
        }
    }

    /** Manage a particular player's sigil decorations. */
    private void setPlayerIcons(final int large, final int largeLeft, final int largeRight,
                                final int small, final int smallLeft, final int smallRight) {
        final float LARGE = 60.0f;
        final float SMALL = 45.0f;

        // Collect all the pertinent textViews.
        TextView tvLarge = (TextView) getActivity().findViewById(large);
        TextView tvLargeLeft = (TextView) getActivity().findViewById(largeLeft);
        TextView tvLargeRight = (TextView) getActivity().findViewById(largeRight);
        TextView tvSmall = (TextView) getActivity().findViewById(small);
        TextView tvSmallLeft = (TextView) getActivity().findViewById(smallLeft);
        TextView tvSmallRight = (TextView) getActivity().findViewById(smallRight);

        // Deal with the tvLarger symbol's decorations.
        tvLarge.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE);
        tvLarge.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        tvLargeLeft.setVisibility(View.VISIBLE);
        tvLargeRight.setVisibility(View.VISIBLE);

        // Deal with the tvSmall symbol's decorations.
        tvSmall.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL);
        tvSmall.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        tvSmallLeft.setVisibility(View.INVISIBLE);
        tvSmallRight.setVisibility(View.INVISIBLE);
    }

    /** Set the name for a given player index. */
    private void setPlayerName(final int resId, final int index, final TicTacToe model) {
        // Ensure that the name text view exists. Abort if not.  Set the value from the model if it
        // does.
        TextView name = (TextView) mLayout.findViewById(resId);
        if (name == null) return;
        name.setText(model.players.get(index).name);
    }

    /** Set the sigil (X or O) for a given player. */
    private void setPlayerSymbol(final int resId, final int index, final TicTacToe model) {
        // Ensure that the sigil text view exists.  Abort if not, set the value from the data
        // model if it does.
        TextView symbol = (TextView) mLayout.findViewById(resId);
        if (symbol == null) return;
        symbol.setText(model.players.get(index).symbol);
    }

    /** Set the name for a given player index. */
    private void setPlayerWinCount(final int resId, final int index, final TicTacToe model) {
        // Ensure that the win count text view exists. Abort if not.  Set the value from the model
        // if it does.
        TextView winCount = (TextView) mLayout.findViewById(resId);
        if (winCount == null) return;
        winCount.setText(String.valueOf(model.players.get(index).winCount));
    }

    /** Update the game state. */
    private void setState(final TicTacToe model) {
        String value = null;
        switch (model.state) {
            case TicTacToe.X_WINS:
                // do some stuff.
                value = getString(R.string.winner_x);
                break;
            case TicTacToe.O_WINS:
                // do other stuff.
                value = getString(R.string.winner_o);
                break;
            case TicTacToe.TIE:
                // Reveal Tie Messages
                value = getString(R.string.winner_tie);
                break;
            default:
                // keeep playing.
                break;
        }

        // Determine if the game has ended (winner or tie).  Abort if not.
        if (value == null) return;

        // Update the winner text view.
        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        winner.setText(value);
        winner.setVisibility(View.VISIBLE);
        NotificationManager.instance.notify(this, getDoneMessage(model), true);
    }

    /** Set up the game board based on the data model state. */
    private void setGameBoard(@NonNull final TicTacToe model) {
        // Determine if the model has any pieces to put on the board.  If not reset the board.
        if (model.board == null)
            // Initialize the board state.
            initBoard();
        else
            // Place the X and O symbols on the grid.
            for (String tag : model.board.keySet()) {
                // Determine if the position denoted by the suffix is valid and has mot yet been
                // updated.  If so, then update the position.
                String value = model.board.get(tag);
                Button button = (Button) mLayout.findViewWithTag(tag);
                if (button != null && button.getText().equals("")) button.setText(value);
            }
    }

    /** Update the UI using the current experience state from the database. */
    private void updateExperience() {
        // Ensure that a valid experience exists.  Abort if not.
        if (mExperience == null || !(mExperience instanceof TicTacToe)) return;

        // A valid experience is available. Use the data model to populate the UI and check if the
        // game is finished.
        TicTacToe model = (TicTacToe) mExperience;
        setPlayerName(R.id.player1Name, 0, model);
        setPlayerName(R.id.player2Name, 1, model);
        setPlayerWinCount(R.id.player1WinCount, 0, model);
        setPlayerWinCount(R.id.player2WinCount, 1, model);
        setPlayerSymbol(R.id.player1Symbol, 0, model);
        setPlayerSymbol(R.id.player2Symbol, 1, model);
        setPlayerIcons(model.turn);
        setGameBoard(model);
        setState(model);
    }

}
