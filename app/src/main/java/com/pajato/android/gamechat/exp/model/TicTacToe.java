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
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.ExpType.ttt;

/** Provide a Firebase model class for a tictactoe game experience. */
@IgnoreExtraProperties public class TicTacToe implements Experience {

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
    public Board board;

    /** The creation timestamp. */
    private long createTime;

    /** The experience push key. */
    public String key;

    /** The group push key. */
    public String groupKey;

    /** The last modification timestamp. */
    private long modTime;

    /** The experience display name. */
    public String name;

    /** The member account identifer who created the experience. */
    public String owner;

    /** The list of players, for tictactoe, two of them. */
    public List<Player> players;

    /** The room push key. */
    public String roomKey;

    /** The game state. */
    public int state;

    /** The current turn. */
    public boolean turn;

    /** The experience type ordinal value. */
    public int type = -1;

    /** The experience icon url. */
    public String url;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    @SuppressWarnings("unused") public TicTacToe() {}

    /** Build a default TicTacToe using the given parameters and defaulting the rest. */
    public TicTacToe(final String key, final String id, final String name, final long createTime,
                     final String groupKey, final String roomKey, final List<Player> players) {
        this.createTime = createTime;
        this.key = key;
        this.groupKey = groupKey;
        this.modTime = 0;
        this.name = name;
        this.owner = id;
        this.players = players;
        this.roomKey = roomKey;
        state = ACTIVE;
        turn = true;
        type = ttt.ordinal();
        url = "android.resource://com.pajato.android.gamechat/drawable/ic_tictactoe_red";
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude @Override public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("board", board);
        result.put("createTime", createTime);
        result.put("key", key);
        result.put("groupKey", groupKey);
        result.put("modTime", modTime);
        result.put("name", name);
        result.put("owner", owner);
        result.put("players", players);
        result.put("roomKey", roomKey);
        result.put("state", state);
        result.put("turn", turn);
        result.put("type", type);
        result.put("url", url);

        return result;
    }

    /** Return the experience push key. */
    @Exclude @Override public String getExperienceKey() {
        return key;
    }

    /** Return the fragment type value or null if no such fragment type exists. */
    @Exclude @Override public ExpType getExperienceType() {
        if (type < 0 || type >= ExpType.values().length) return null;

        return ExpType.values()[type];
    }

    /** Return the group push key. */
    @Exclude @Override public String getGroupKey() {
        return groupKey;
    }

    /** Return the experience name. */
    @Exclude @Override public String getName() {
        return name;
    }

    /** Return the room push key. */
    @Exclude @Override public String getRoomKey() {
        return roomKey;
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

    /** Set the experience key to satisfy the Experience contract. */
    @Exclude @Override public void setExperienceKey(final String key) {
        this.key = key;
    }

    /** Set the modification timestamp. */
    @Exclude @Override public void setModTime(final long value) {
        modTime = value;
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
    @Exclude public boolean toggleTurn() {
        turn = !turn;
        return turn;
    }

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

}
