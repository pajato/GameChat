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

package com.pajato.android.gamechat.exp;

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
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.model.Board;
import com.pajato.android.gamechat.exp.model.ExpProfile;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.exp.model.TicTacToe;
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
import static com.pajato.android.gamechat.exp.ExpType.ttt;
import static com.pajato.android.gamechat.exp.ExpFragmentType.checkers;
import static com.pajato.android.gamechat.exp.ExpFragmentType.chess;
import static com.pajato.android.gamechat.exp.ExpFragmentType.tictactoe;
import static com.pajato.android.gamechat.exp.model.Board.BEG_COL;
import static com.pajato.android.gamechat.exp.model.Board.BOT_ROW;
import static com.pajato.android.gamechat.exp.model.Board.END_COL;
import static com.pajato.android.gamechat.exp.model.Board.LEFT_DIAG;
import static com.pajato.android.gamechat.exp.model.Board.MID_COL;
import static com.pajato.android.gamechat.exp.model.Board.MID_ROW;
import static com.pajato.android.gamechat.exp.model.Board.RIGHT_DIAG;
import static com.pajato.android.gamechat.exp.model.Board.TOP_ROW;
import static com.pajato.android.gamechat.exp.model.TicTacToe.ACTIVE;
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
public class TTTFragment extends BaseGameFragment implements View.OnClickListener {

    // Public constants.

    /** The lookup key for the FAB tictactoe memu. */
    public static final String TIC_TAC_TOE_FAM_KEY = "TicTacToeFamKey";

    // Private constants.

    /** The logcat TAG. */
    private static final String TAG = TTTFragment.class.getSimpleName();

    // Public instance methods.

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_game_ttt;}

    /** Placeholder while message handler stays relevant for chess and checkers. */
    @Override public void messageHandler(final String msg) {}

    /** Handle a FAM or Snackbar TicTacToe click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Determine if this event is for this fragment.  Abort if not.
        if (GameManager.instance.getCurrent() != tictactoe.ordinal()) return;

        // The event is either a snackbar action (start a new game) or a FAM menu entry.  Detect and
        // handle a snackbar action first.
        Object tag = event.view.getTag();
        if (isPlayAgain(tag, TAG)) {
            // Dismiss the FAB (assuming it was the source of the click --- being wrong is ok, and
            // setup a new game.
            FabManager.game.dismissMenu(this);
            handleNewGame();
        }
    }

    /** Handle a click on the tictactoe board by verifying the click and handing it off. */
    @Override public void onClick(final View view) {
        Object tag = view.getTag();
        if (tag instanceof String && ((String) tag).startsWith("button"))
            handleTileClick((String) tag);
    }

    /** Handle an experience posting event to see if this is a tictactoe experience. */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Check the payload to see if this is not tictactoe.  Abort if not.
        if (event.experience == null || event.experience.getExperienceType() != ttt) return;

        // The experience is a tictactoe experience.  Start the game.
        mExperience = event.experience;
        resume();
    }

    /** Initialie by setting up tile click handlers on the board. */
    @Override public void onInitialize() {
        // Place an click listener on all nine buttons by iterating over all nine buttons.
        super.onInitialize();
        FabManager.game.setMenu(TIC_TAC_TOE_FAM_KEY, getTTTMenu());
        final String format = "Invalid tag found on button with tag {%s}";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String tag = String.format(Locale.US, "button%s%s", i, j);
                View view = mLayout.findViewWithTag(tag);
                if (view != null)
                    view.setOnClickListener(this);
                else
                    Log.e(TAG, String.format(Locale.US, format, tag));
            }
        }
    }

    /** Handle taking the foreground by updating the UI based on the current expeience. */
    @Override public void onResume() {
        // Determine if there is an experience ready to be enjoyed.  If not, hide the layout and
        // present a spinner.  When an experience is posted by the app event manager, the game can
        // be shown
        super.onResume();
        FabManager.game.setMenu(this, TIC_TAC_TOE_FAM_KEY);
        resume();
    }

    // Protected instance methods.

    /** Return an experience for a given dispatcher instance. */
    @Override
    protected void createExperience(@NonNull final Context context,
                                    @NonNull final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
        // Set up the players and persist the game.
        List<Account> players = getPlayers(dispatcher);
        createExperience(context, players);
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
        String name = model.getWinningPlayerName();
        if (name == null) return getString(R.string.TieMessageNotification);

        // There was a winner.  Return a congratulatory message.
        String format = getString(R.string.WinMessageNotificationFormat);
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
    private List<Account> getPlayers(final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
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

    /** Return the game state after applying the given button move to the data model. */
    private int getState(@NonNull final TicTacToe model, String buttonTag) {
        // Check to see if the game is a tie, with all moves exhausted.
        if (model.state == ACTIVE && model.board.grid.size() == 9) return TicTacToe.TIE;

        // Not a tie.  Determine the winner state based on the current play given by the button tag.
        int value = model.getSymbolValue();
        if (model.state == ACTIVE) return getState(model, value, buttonTag);

        return model.state;
    }

    /** Return the game state after applying the given button move to the data model. */
    private int getState(@NonNull final TicTacToe model, final int value, final String buttonTag) {
        // Case on the button tag to update the appropriate tallies.
        switch (buttonTag) {
            case "button00": return getState(model, value, TOP_ROW, BEG_COL, LEFT_DIAG);
            case "button01": return getState(model, value, TOP_ROW, MID_COL);
            case "button02": return getState(model, value, TOP_ROW, END_COL, RIGHT_DIAG);
            case "button10": return getState(model, value, MID_ROW, BEG_COL);
            case "button11": return getState(model, value, MID_ROW, MID_COL, LEFT_DIAG, RIGHT_DIAG);
            case "button12": return getState(model, value, MID_ROW, END_COL);
            case "button20": return getState(model, value, BOT_ROW, BEG_COL, RIGHT_DIAG);
            case "button21": return getState(model, value, BOT_ROW, MID_COL);
            case "button22": return getState(model, value, BOT_ROW, END_COL, LEFT_DIAG);
            default: break;
        }

        return model.state;
    }

    /** Return the game state after updating the given tallies. */
    private int getState(@NonNull final TicTacToe model, final int value, final String... tallyKeys) {
        // Ensure that the tallies map exists, crearing it if it doesn't and apply the value to each
        // of the keys and return the state.
        final int X_WIN_VALUE = 3;
        final int O_WIN_VALUE = 12;
        if (model.board.tallies == null) model.board.tallies = new HashMap<>();
        for (String key : tallyKeys) {
            // Ensure that the entry exists.
            int tally = model.board.tallies.containsKey(key) ? model.board.tallies.get(key) : 0;
            tally += value;
            model.board.tallies.put(key, tally);
            if (tally == X_WIN_VALUE) return TicTacToe.X_WINS;
            if (tally == O_WIN_VALUE) return TicTacToe.O_WINS;
        }

        return ACTIVE;
    }

    /** Handle a new game by resetting the data model. */
    private void handleNewGame() {
        // Ensure that the data model exists and is valid.
        TicTacToe model = getModel();
        if (model == null) {
            Log.e(TAG, "Null TTT data model.", new Throwable());
            return;
        }

        // Reset the data model, update the database and clear the notification manager one-shot.
        model.board = null;
        model.state = ACTIVE;
        DatabaseManager.instance.updateExperience(mExperience);
    }

    /** Handle a click on a given tile by updating the value on the tile and start the next turn. */
    private void handleTileClick(final String buttonTag) {
        // Ensure that the click occurred on a grid button and that the data model is not empty.
        // Abort if not.
        View view = mLayout.findViewWithTag(buttonTag);
        TicTacToe model = getModel();
        if (view == null || !(view instanceof Button) || model == null) return;

        // Handle the button click based on the current state.
        Button button = (Button) view;
        switch (model.state) {
            case ACTIVE:
                // Ensure that the click occurred on an empty button.
                if (button.getText().length() != 0) {
                    // The click occurred on a played button.  Warn the User and ignore the play.
                    NotificationManager.instance.notify(this, R.string.InvalidButton);
                    return;
                }
                break;
            default:
                // In all other cases, clear the board to start a new game.
                initBoard(model);
                model.board = null;
                model.state = ACTIVE;
                NotificationManager.instance.notify(this, R.string.StartNewGame);
                break;
        }

        // Update the database with the collected changes.
        if (model.board == null) model.board = new Board();
        model.board.grid.put(buttonTag, model.getSymbolText());
        model.state = getState(model, buttonTag);
        model.setWinCount();
        model.toggleTurn();
        DatabaseManager.instance.updateExperience(mExperience);
    }

    /** Initialize the board model and values and clear the winner text. */
    private void initBoard(@NonNull final TicTacToe model) {
        // Ensure that the layout has been established. Abort if not.
        if (mLayout == null) return;

        // Clear the board, the winner text, the board evaluation support and all current X's and
        // O's.
        if (model.board != null) model.board.clear();
        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        if (winner != null) winner.setText("");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // Seed the array with a value that will guarantee the X vs O win and a tie will be
                // calculated correctly; clear the grid button.
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
            // Start the game and update the views using the current state of the experience.
            mLayout.setVisibility(View.VISIBLE);
            setTitles(mExperience.getGroupKey(), mExperience.getRoomKey());
            ProgressManager.instance.hide();
            updateExperience();
        }
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

    /** Manage a particular player's symbol decorations. */
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
        // Generate a message string appropriate for a win or tie, or nothing if the game is active.
        String message = null;
        switch (model.state) {
            case TicTacToe.X_WINS:
            case TicTacToe.O_WINS:
                // do other stuff.
                String name = model.getWinningPlayerName();
                String format = getString(R.string.WinMessageFormat);
                message = String.format(Locale.getDefault(), format, name);
                break;
            case TicTacToe.TIE:
                // Reveal a tie game.
                message = getString(R.string.TieMessage);
                break;
            default:
                // keeep playing or waiting for a new game.
                break;
        }

        // Determine if the game has ended (winner or tie).  Abort if not.
        if (message == null) return;

        // Update the UI to celebrate the winner or a tie and update the database game state to
        // pending.
        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        winner.setText(message);
        winner.setVisibility(View.VISIBLE);
        NotificationManager.instance.notify(this, getDoneMessage(model), true);
        model.state = TicTacToe.PENDING;
        DatabaseManager.instance.updateExperience(mExperience);
    }

    /** Set up the game board based on the data model state. */
    private void setGameBoard(@NonNull final TicTacToe model) {
        // Determine if the model has any pieces to put on the board.  If not reset the board.
        if (model.board == null)
            // Initialize the board state.
            initBoard(model);
        else
            // Place the X and O symbols on the grid.
            for (String tag : model.board.grid.keySet()) {
                // Determine if the position denoted by the suffix is valid and has mot yet been
                // updated.  If so, then update the position.
                String value = model.board.grid.get(tag);
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

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getTTTMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getEntry(R.string.PlayCheckers, R.mipmap.ic_checkers, checkers));
        menu.add(getEntry(R.string.PlayChess, R.mipmap.ic_chess, chess));
        menu.add(getTintEntry(R.string.MyRooms, R.drawable.vd_casino_black_24px));
        menu.add(getNoTintEntry(R.string.PlayAgain, R.mipmap.ic_tictactoe_red));
        return menu;
    }

}