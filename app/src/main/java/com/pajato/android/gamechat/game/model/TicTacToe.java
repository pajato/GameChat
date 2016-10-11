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

package com.pajato.android.gamechat.game.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.game.ExpType;
import com.pajato.android.gamechat.game.Experience;
import com.pajato.android.gamechat.game.FragmentType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provide a Firebase model class for a tictactoe game experience. */
@IgnoreExtraProperties public class TicTacToe implements Experience {

    // Public class constants.

    /** The level types. */
    public final static int EASY = 0;
    public final static int INTERMEDIATE = 1;
    public final static int IMPOSSIBLE = 2;

    /** A list of board positions that have been filled by either an X or an O. */
    public List<String> board;

    /** The creation timestamp. */
    public long createTime;

    /** The experience push key. */
    public String key;

    /** The group push key. */
    public String groupKey;

    /** The game level. */
    public int level;

    /** The last modification timestamp. */
    public long modTime;

    /** The experience display name. */
    public String name;

    /** The member account identifer who created the experience. */
    public String owner;

    /** The list of players, for tictactoe, two of them. */
    public List<Player> players;

    /** The room push key. */
    public String roomKey;

    /** The experience icon url. */
    public String url;

    /** The current turn. */
    public boolean turn;

    /** The experience type. */
    public int type = -1;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    public TicTacToe() {}

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
        turn = true;
        type = ExpType.ttt.ordinal();
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
        result.put("turn", turn);
        result.put("type", type);
        result.put("url", url);

        return result;
    }

    /** Return the fragment type value or null if no such fragment type exists. */
    @Exclude @Override public FragmentType getFragmentType() {
        if (type < 0 || type >= FragmentType.values().length) return null;

        return FragmentType.values()[type];
    }

    /** Return the experience push key. */
    @Exclude @Override public String getExperienceKey() {
        return key;
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

    /** Return the symbol text value for the player at the given index. */
    @Exclude public String getSymbolValue(final int index) {
        return players.get(index).symbol;
    }

    /** Return the symbol text value for the player whose turn is current. */
    @Exclude public String getSymbolValue() {
        return turn ? players.get(0).symbol : players.get(1).symbol;
    }

    /** Return the type to satisfy the Experience contract. */
    @Exclude @Override public int getType() {
        return type;
    }

    /** Set the experience key to satisfy the Experience contract. */
    @Exclude @Override public void setExperienceKey(final String key) {
        this.key = key;
    }

    /** Toggle the turn state. */
    @Exclude public boolean toggleTurn() {
        turn = !turn;
        return turn;
    }

}
