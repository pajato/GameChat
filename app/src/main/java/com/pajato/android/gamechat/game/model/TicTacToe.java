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

import android.content.Context;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
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
    public String expKey;

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

    /** Build a default TicTacToe using all the parameters. */
    public TicTacToe(final String key, final String owner, final String name, final String url,
                     final long createTime, final long modTime, final boolean turn, final int type,
                     final String groupKey, final String roomKey, final List<String> board,
                     final List<Player> players) {
        this.board = board;
        this.createTime = createTime;
        this.expKey = key;
        this.groupKey = groupKey;
        this.modTime = modTime;
        this.name = name;
        this.owner = owner;
        this.players = players;
        this.roomKey = roomKey;
        this.turn = turn;
        this.type = type;
        this.url = url;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("board", board);
        result.put("createTime", createTime);
        result.put("expKey", expKey);
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
    @Exclude @Override public FragmentType getFragType() {
        if (type < 0 || type >= FragmentType.values().length) return null;

        return FragmentType.values()[type];
    }

    /** Return the experience push key. */
    @Exclude @Override public String getExpKey() {
        return expKey;
    }

    /** Return the group push key. */
    @Exclude @Override public String getGroupKey() {
        return groupKey;
    }

    /** Return the room push key. */
    @Exclude @Override public String getRoomKey() {
        return roomKey;
    }

    /** Return the sigil text value for the given player. */
    @Exclude public String getSigilValue(final Context context, final int index) {
        // Ensure that the index is valid. Return the sentinal value "?" if not.
        if (index < 0 || index > 1) return "?";

        // The index is valid. Return the sigil value.
        int resId = players.get(index).sigilId;
        return context.getString(resId);
    }

    /** Return the sigil text value for the player whose turn is current. */
    @Exclude public String getSigilValue(final Context context) {
        int resId = turn ? players.get(0).sigilId : players.get(1).sigilId;
        return context.getString(resId);
    }

    /** Toggle the turn state. */
    @Exclude public boolean toggleTurn() {
        turn = !turn;
        return turn;
    }

}
