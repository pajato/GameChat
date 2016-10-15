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

import java.util.HashMap;
import java.util.Map;

/** Provide a pojo repesenting a tictactoe player: a name, a symbol (sigil), and a count. */
public class Player {

    /** The player's display name. */
    public String name;

    /** The player's symbol (either X or O). */
    public String symbol;

    /** The player's win count. */
    public int winCount;

    // Public constructors.

    /** Build an empty args constructor for the database. */
    public Player() {}

    /** Build a default game player using all the parameters. */
    public Player(final String name, final String symbol) {
        this.name = name;
        this.symbol = symbol;
        this.winCount = 0;
    }

    /** Build an instance accepting Object values for all fields. */
    public Player(final Object name, final Object symbol, final Object winCount) {
        this.name = name instanceof String ? (String) name : "anonymous";
        this.symbol = symbol instanceof String ? (String) symbol : "?";
        this.winCount = winCount instanceof Integer ? (Integer) winCount : 0;
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("symbol", symbol);
        result.put("winCount", winCount);

        return result;
    }

}
