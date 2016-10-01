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

import java.util.HashMap;
import java.util.Map;

/** Provide a Firebase model class repesenting a tictactoe game experience, an icon, a name and text. */
@IgnoreExtraProperties
public class Player {

    /** The player's display name. */
    public String name;

    /** The player's sigil (for lack of a better term). */
    public int sigilId;

    /** The player's win count. */
    public int winCount;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    public Player() {}

    /** Build a default TicTacToe using all the parameters. */
    public Player(final String name, final int sigilId, final int winCount) {
        this.name = name;
        this.sigilId = sigilId;
        this.winCount = winCount;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("sigilId", sigilId);
        result.put("winCount", winCount);

        return result;
    }

}
