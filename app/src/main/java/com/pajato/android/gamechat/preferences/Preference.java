/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
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

package com.pajato.android.gamechat.preferences;

import java.util.Set;

import static com.pajato.android.gamechat.preferences.Preference.Type.bool;
import static com.pajato.android.gamechat.preferences.Preference.Type.stringset;

/**
 * Provide an data class that models a preference value.
 *
 * @author Paul Michael Reilly on 7/31/2017
 */

public class Preference {

    // Public enum.

    /** Identify the supported types. */
    public enum Type {
        bool, stringset
    }

    // Public constructors.

    /** Build an string set instance. */
    public Preference(final String key, final Set<String> value) {
        this.key = key;
        type = stringset;
        stringSetValue = value;
    }

    /** Build an bool instance. */
    public Preference(final String key, final boolean value) {
        this.key = key;
        type = bool;
        booleanValue = value;
    }

    // Public instance variables.

    /** The preference key property. */
    public String key;

    /** The preference type property. */
    public Type type;

    /** The boolean data value property. */
    boolean booleanValue;

    /** The string set data value property. */
    Set<String> stringSetValue;
}
