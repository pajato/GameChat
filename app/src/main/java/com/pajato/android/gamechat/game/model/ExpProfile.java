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

/**
 * Provide a Firebase model class repesenting an experience profile: a key to the full experience, a
 * display name for displaying to the User and an experience type, an integer denoting tictactoe,
 * chess or checkers, for example.
 *
 * @author Paul Michael Reilly
 */
@IgnoreExtraProperties
public class ExpProfile {

    /** The experience key. */
    public String key;

    /** The experience's display name. */
    public String name;

    /** The experience type value. */
    public int type;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    public ExpProfile() {}

    /** Build a default TicTacToe using all the parameters. */
    public ExpProfile(final String key, final String name, final int type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("name", name);
        result.put("type", type);

        return result;
    }

}
