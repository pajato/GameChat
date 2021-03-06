/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.exp.model;

import com.google.firebase.database.Exclude;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.database.model.Base;
import com.pajato.android.gamechat.exp.Board;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.ExpType.tttET;

/** Provide a Firebase model class for a tictactoe game experience. */
public class TicTacToe extends Base implements Experience {

    // Public class constants.

    /** The game is still active. */
    public final static int ACTIVE = 0;

    /** The game has been won by player using X. */
    public final static int X_WINS = 1;

    /** The game has been won by player using O. */
    public final static int O_WINS = 2;

    /** The game has ended in a tie. */
    public final static int TIE = 3;

    /** The game has ended, been celebrated and is pending a new game. */
    public final static int PENDING = 4;

    // Public instance variables.

    /** A POJO encapsulating the board moves and wining tallies. */
    public TTTBoard board;

    /** The group push key. */
    public String groupKey;

    /** The list of players, for tictactoe, two of them. */
    public List<Player> players;

    /** The room push key. */
    public String roomKey;

    /** The game state. */
    public int state;

    /** The current turn. */
    public boolean turn;

    /** The experience type name. */
    public String type;

    /** A list of joined users (by account id), who have not yet seen the experience. */
    private List<String> unseenList = new ArrayList<>();

    /** The experience icon url. */
    public String url;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public TicTacToe() {
        super();
    }

    /** Build a default TicTacToe using the given parameters and defaulting the rest. */
    public TicTacToe(final String key, final String owner, final String name, final long createTime,
                     final String groupKey, final String roomKey, final List<Player> players) {
        super(key, owner, name, createTime);
        this.groupKey = groupKey;
        this.players = players;
        this.roomKey = roomKey;
        state = ACTIVE;
        turn = true;
        type = tttET.name();
        url = "android.resource://com.pajato.android.gamechat/drawable/ic_tictactoe_red";
    }

    // Public instance methods.

    /** Implement the interface by returning null (for a non-compliant board.) */
    @Override public Board getBoard() {
        return null;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude @Override public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("board", board);
        result.put("groupKey", groupKey);
        result.put("players", players);
        result.put("roomKey", roomKey);
        result.put("state", state);
        result.put("turn", turn);
        result.put("type", type);
        result.put("unseenList", unseenList);
        result.put("url", url);

        return result;
    }

    /** Return the create time to satisfy the Experience contract. */
    @Override public long getCreateTime() {
        return createTime;
    }

    /** Return the experience push key. */
    @Exclude @Override public String getExperienceKey() {
        return key;
    }

    /** Return the fragment type value or null if no such fragment type exists. */
    @Exclude @Override public ExpType getExperienceType() {
        return ExpType.valueOf(type);
    }

    /** Return the group push key. */
    @Exclude @Override public String getGroupKey() {
        return groupKey;
    }

    /** Return the experience name. */
    @Exclude @Override public String getName() {
        return name;
    }

    /** Return the experience modification time. */
    @Exclude @Override public long getModTime() {
        return modTime;
    }

    /** Return the room push key. */
    @Exclude @Override public String getRoomKey() {
        return roomKey;
    }

    /** Return the room push key. */
    @Exclude @Override public boolean getTurn() { return turn; }

    /** Return the unseen list. */
    @Override public List<String> getUnseenList() { return unseenList; }

    /** Return the winning player's name or null if the game is active or ended in a tie. */
    @Exclude public String getWinningPlayerName() {
        switch (state) {
            case X_WINS:
                return players.get(0).name;
            case O_WINS:
                return players.get(1).name;
            case ACTIVE:
            case TIE:
            default:
                return null;
        }
    }

    /** Implement the interface by returning null. */
    @Exclude @Override public State getStateType() {
        return null;
    }

    /** Return the value associated with the current player: 1 == X, 2 == O. */
    @Exclude public int getSymbolValue() {
        // This implies that player 1 is always X and player 2 is always O.
        return turn ? 1 : 4;
    }

    /** Return the symbol text value for the player whose turn is current. */
    @Exclude public String getSymbolText() {
        return turn ? players.get(0).symbol : players.get(1).symbol;
    }

    /** Implement the interface by returning null. */
    @Exclude @Override public Player getWinningPlayer() {
        return null;
    }

    /** Always reset the game. Return true to indicate reset completed. */
    @Exclude @Override public boolean reset(BaseFragment fragment) {
        board = new TTTBoard();
        state = ACTIVE;
        return true;
    }

    /** Set the experience key to satisfy the Experience contract. */
    @Exclude @Override public void setExperienceKey(final String key) {
        this.key = key;
    }

    /** Set the group key to satisfy the Experience contract. */
    @Exclude @Override public void setGroupKey(final String key) {
        this.groupKey = key;
    }

    /** Return a list of players for this experience. */
    @Exclude @Override public List<Player> getPlayers() {
        return players;
    }

    /** Set the experience name */
    @Exclude @Override public void setName(final String name) {
        this.name = name;
    }

    /** Set the room key to satisfy the Experience contract. */
    @Exclude @Override public void setRoomKey(final String key) {
        this.roomKey = key;
    }

    /** Set the modification timestamp. */
    @Exclude @Override public void setModTime(final long value) {
        modTime = value;
    }

    /** Implement the interface by providing a nop. */
    @Exclude @Override public void setStateType(final State value) {}

    /** Provide a mutator for Firebase. */
    @SuppressWarnings("unused")
    public void setUnseenList(final List<String> unseenList) {
        this.unseenList = unseenList;
    }

    /** Update the win count based on the current state. */
    @Exclude @Override public void setWinCount() {
        switch (state) {
            case X_WINS:
                players.get(0).winCount++;
                break;
            case O_WINS:
                players.get(1).winCount++;
                break;
            default:
                break;
        }
    }

    /** Toggle the turn state. */
    @Exclude @Override public boolean toggleTurn() {
        turn = !turn;
        return turn;
    }
}
