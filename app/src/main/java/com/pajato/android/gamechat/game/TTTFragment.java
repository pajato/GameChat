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
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.database.DatabaseManager;
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

import static com.pajato.android.gamechat.R.id.player1WinCount;
import static com.pajato.android.gamechat.R.id.player2WinCount;
import static com.pajato.android.gamechat.account.AccountManager.SIGNED_OUT_EXPERIENCE_KEY;
import static com.pajato.android.gamechat.account.AccountManager.SIGNED_OUT_OWNER_ID;
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

    // Private instance variables.

    /** A map used to help manage the board... */
    private HashMap<String, Integer> mLayoutMap;

    // Public instance methods.

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_ttt;}

    /** Placeholder while message handler stays relevant for chess and checkers. */
    @Override public void messageHandler(final String msg) {}

    /** Handle a tile click event by sending a message to the current tic-tac-toe fragment. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Determine if the payload exists and is a string. Abort if not.  Handle it as a tile click
        // if it does.
        Object payload = event.view.getTag();
        if (!(payload instanceof String)) return;
        handleTileClick((String) payload);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.ttt_menu, menu);
    }

    /** Handle taking the foreground by updating the UI based on the current expeience. */
    @Override public void onResume() {
        // Determine if there is an experience ready to be enjoyed.  If not, hide the layout and
        // present a spinner.  When an experience is posted by the app event manager, the game can
        // be shown
        super.onResume();
        if (mExperience == null) {
            // Disable the layout and startup the spinner.
            mLayout.setVisibility(View.GONE);
            String title = "TicTacToe";
            String message = "Waiting for the database to provide the game...";
            ProgressManager.instance.show(getContext(), title, message);
        }
    }

    /** Return an experience for a given dispatcher instance. */
    @Override protected void createExperience(@NonNull final Context context,
                                              @NonNull final Dispatcher dispatcher) {
        // Set up the players and persist the game.
        List<Account> players = getPlayers(dispatcher);
        createExperience(context, players);
    }

    // Private instance methods.

    /**
     * Evaluates the state of the board, determining if there are three in a row of either X or O.
     *
     * @return false if a player has won or if the full number of turns has occurred, true otherwise.
     */
    private boolean checkNotFinished(@NonNull final TicTacToe model) {
        // First, we need to check on the buttons' states.
        int[][] boardValues = evaluateBoard(model);

        // Evaluate all possible lines of 3.
        int topRow = boardValues[0][0] + boardValues[0][1] + boardValues[0][2];
        int midRow = boardValues[1][0] + boardValues[1][1] + boardValues[1][2];
        int botRow = boardValues[2][0] + boardValues[2][1] + boardValues[2][2];
        int startCol = boardValues[0][0] + boardValues[1][0] + boardValues[2][0];
        int centerCol = boardValues[0][1] + boardValues[1][1] + boardValues[2][1];
        int endCol = boardValues[0][2] + boardValues[1][2] + boardValues[2][2];
        int leftDiag = boardValues[0][0] + boardValues[1][1] + boardValues[2][2];
        int rightDiag = boardValues[2][0] + boardValues[1][1] + boardValues [0][2];

        // If any lines of 3 are equal to 3, X wins.
        boolean xWins = (topRow == 3 || midRow == 3 || botRow == 3 || startCol == 3
                || centerCol == 3 || endCol == 3 || leftDiag == 3 || rightDiag == 3);

        // If any lines of 3 are equal to 6, O wins.
        boolean oWins = (topRow == 6 || midRow == 6 || botRow == 6 || startCol == 6
                || centerCol == 6 || endCol == 6 || leftDiag == 6 || rightDiag == 6);

        // If we have a win condition, reveal the winning messages.
        if (xWins || oWins || mLayoutMap.size() == 10) {
            // Setup the winner TextView and snackbar messages.
            TextView Winner = (TextView) super.getActivity().findViewById(R.id.winner);
            Winner.setText("");
            Winner.setVisibility(View.VISIBLE);

            // If there is a winner, output winning messages and ensure that the appropriate
            // player's icon has been highlighted.
            if (xWins) {
                updateWinCount(0);
                Winner.setText(R.string.winner_x);
                handlePlayerIcons(true);
            } else if (oWins) {
                updateWinCount(1);
                Winner.setText(R.string.winner_o);
                handlePlayerIcons(false);

            // If no one has won, the turn timer has run out. End the game.
            } else {
                // Reveal Tie Messages
                Winner.setText(R.string.winner_tie);
                GameManager.instance.notify(mLayout, "It's a Tie!", -1, true);
            }
            return false;
        }
        // If none of the conditions are met, the game has not yet ended, and we can continue it.
        return true;
    }

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

        // Lastly, if there is a valid key, use it to cache an offline experience.  Abort if no
        // such key is available.
        //if (key == null  || exp == null) return null;
        //exp.setExperienceKey(key);
        //DatabaseListManager.instance.experienceMap.put(key, exp);
        //return exp;
    }

    /**
     * Evaluates the current state of the individual tiles of the board and stores them as a HashMap
     * in the Firebase Database.
     */
    private int[][] evaluateBoard(@NonNull final TicTacToe model) {
        int[][] boardValues = new int[3][3];
        if (mLayoutMap == null) mLayoutMap = new HashMap<>();
        // Go through all the buttons.
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String text = "button" + Integer.toString(i) + Integer.toBinaryString(j);
                Button currTile = (Button) mLayout.findViewWithTag(text);
                String tileValue = currTile.getText().toString();

                // Assign each possible state for each tile as a value. The only possible values
                // of each row (that indicate and endgame state) are 3 in a row of X or O. There
                // is no way to get a value of 3 without getting 3 Xs, and there's no way to get a
                // value of 6 without getting 3 Os.

                // If there's an X in this button, store a 1
                if (tileValue.equals(model.getSymbolValue(0))) {
                    boardValues[i][j] = 1;
                    if(!(mLayoutMap.containsKey(i + "-" + j))) mLayoutMap.put(i + "-" + j, 1);
                    // If there's an O in this button, store a 2
                } else if (tileValue.equals(model.getSymbolValue(1))) {
                    boardValues[i][j] = 2;
                    if(!(mLayoutMap.containsKey(i + "-" + j))) mLayoutMap.put(i + "-" + j, 2);
                    // Otherwise, there's a space. -5 was chosen arbitrarily to keep row values unique.
                } else {
                    boardValues[i][j] = -5;
                }
            }
        }
        //mRef.setValue(mLayoutMap);
        return boardValues;
    }

    // Return either a null placeholder key value or a sentinel value as the experience key. */
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
        name = getPlayerName(getPlayer(players, 0), "Player2");
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
    private void handlePlayerIcons(final boolean turn) {
        // Alternate the decorations on each player sigil/symbol.
        if (turn) {
            // Make player1's decorations the more prominent.
            handlePlayerIcons(R.id.player1Sigil, R.id.leftIndicator1, R.id.rightIndicator1,
                              R.id.player2Sigil, R.id.leftIndicator2, R.id.rightIndicator2);
        } else {
            // Make player2's decorations the more prominent.
            handlePlayerIcons(R.id.player2Sigil, R.id.leftIndicator2, R.id.rightIndicator2,
                              R.id.player1Sigil, R.id.leftIndicator1, R.id.rightIndicator1);
        }
    }

    /** Manage a particular player's sigil decorations. */
    private void handlePlayerIcons(final int large, final int largeLeft, final int largeRight,
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

    /** Handle a click on a given tile by updating the value on the tile and start the next turn. */
    private void handleTileClick(final String buttonTag) {
        // Ensure that a model object exists, the given button exists, is empty and the game is
        // still in progress.  Abort otherwise.
        Button button = (Button) mLayout.findViewWithTag(buttonTag);
        boolean invalidButton = button == null || button.getText().length() > 0;
        TicTacToe model = getModel();
        if (model == null || invalidButton || !(checkNotFinished(model))) return;

        // The move is valid. Update the board and toggle the turn.
        button.setText(model.getSymbolValue());
        handlePlayerIcons(model.toggleTurn());
        checkNotFinished(model);
    }

    /** Handle the recreation of the board based on the values stored in the BoardMap. */
    private void recreateExistingBoard(@NonNull final TicTacToe model) {
        // If the board map is size 1, we know that there is only the turn stored in it, and send a
        // new game out.
        if(mLayoutMap.size() == 1) {
            GameManager.instance.startNextFragment(getActivity(), mFragmentType);
            return;
        }

        // Comb through the board and replace the remaining pieces.
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                // If our keys are present, then put their corresponding piece onto the board.
                String currKey = i + "-" + j;
                Button currButton = (Button) mLayout.findViewWithTag("button" + i + j);
                if(mLayoutMap.containsKey(currKey)) {
                    if(mLayoutMap.get(currKey) == 1) {
                        currButton.setText(model.getSymbolValue(0));
                    } else {
                        currButton.setText(model.getSymbolValue(1));
                    }
                } else {
                    currButton.setText("");
                }
            }
        }
    }

    /** Set the name for a given player index. */
    private void setPlayerName(final int resId, final int index, final TicTacToe model) {
        // Ensure that the name text view exists. Abort if not.  Set the value from the model if it
        // does.
        TextView name = (TextView) mLayout.findViewById(resId);
        if (name == null) return;
        name.setText(model.players.get(index).name);
    }

    /** Set the name for a given player index. */
    private void setPlayerWinCount(final int resId, final int index, final TicTacToe model) {
        // Ensure that the win count text view exists. Abort if not.  Set the value from the model
        // if it does.
        TextView winCount = (TextView) mLayout.findViewById(resId);
        if (winCount == null) return;
        winCount.setText(model.players.get(index).winCount);
    }

    /** Set the sigil (X or O) for a given player. */
    private void setPlayerSigil(final int resId, final int index, final TicTacToe model) {
        // Ensure that the sigil text view exists.  Abort if not, set the value from the data
        // model if it does.
        TextView sigil = (TextView) mLayout.findViewById(resId);
        if (sigil == null) return;
        sigil.setText(model.players.get(index).symbol);
    }

    /**
     * Empties the instructions, board tiles, and outputs a new game message.
     */
    private void setupGameBoard(@NonNull final TicTacToe model) {
        // Reset the board map and update the Firebase database's copy.
        if (mLayoutMap == null) mLayoutMap = new HashMap<>();
        //mRef.setValue(mLayoutMap);

        // Hide our winning messages and ensure the turn display is working properly.
        TextView Winner = (TextView) super.getActivity().findViewById(R.id.winner);
        Winner.setVisibility(View.INVISIBLE);
        handlePlayerIcons(model.turn);

        // Set values for each tile to empty.
        ((Button) mLayout.findViewWithTag("button00")).setText("");
        ((Button) mLayout.findViewWithTag("button01")).setText("");
        ((Button) mLayout.findViewWithTag("button02")).setText("");
        ((Button) mLayout.findViewWithTag("button10")).setText("");
        ((Button) mLayout.findViewWithTag("button11")).setText("");
        ((Button) mLayout.findViewWithTag("button12")).setText("");
        ((Button) mLayout.findViewWithTag("button20")).setText("");
        ((Button) mLayout.findViewWithTag("button21")).setText("");
        ((Button) mLayout.findViewWithTag("button22")).setText("");

        // Output New Game Messages
        String newTurn = "New Game! Player " + (model.turn
                ? "1 (" + model.getSymbolValue(0) + ")"
                : "2 (" + model.getSymbolValue(1) + ")") + "'s Turn";
        GameManager.instance.notify(mLayout, newTurn, ContextCompat.getColor(getActivity(),
                R.color.colorPrimaryDark), false);

        checkNotFinished(model);
    }

    /** Update the UI with either a default or a persisted experience. */
    private void updateExperience() {
        // Ensure that a valid experience exists.  Abort if not.
        //if (mExperience == null) setupTicTacToeExperience();
        if (mExperience == null || !(mExperience instanceof TicTacToe)) return;

        // A valid experience is available. Use the data model to populate the UI.
        TicTacToe model = (TicTacToe) mExperience;
        setPlayerName(R.id.player1Name, 0, model);
        setPlayerName(R.id.player2Name, 1, model);
        setPlayerWinCount(player1WinCount, 0, model);
        setPlayerWinCount(player2WinCount, 1, model);
        setPlayerSigil(R.id.player1Sigil, 0, model);
        setPlayerSigil(R.id.player2Sigil, 1, model);

        setupGameBoard(model);
    }

    /** Update the win count model. */
    private void updateWinCount(final int index) {
        TicTacToe model = getModel();
        if (model == null) return;

        // Update the data model.
        model.players.get(index).winCount++;
        //DatabaseManager.instance.updateExperience(mExperience);
    }

}
