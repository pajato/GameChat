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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provide an interface that models the use of shared preferences within GameChat to facilitate
 * testing.
 *
 * @author Paul Michael Reilly on 7/29/2017
 */

public interface PreferencesProvider {

    /** Return a string set associated with the given key. If none, return a default set. */
    Set<String> getStringSet(String key, Set<String> defaultValues);

    /** Return a boolean flag associated with the given key. In none, return a default value. */
    boolean getBoolean(String key, boolean defaultValue);

    /** Return a map of all preference values. */
    Map<String, Preference> getAll();

    /** Persist a given list of preference values.  Clear the values when list is null. */
    void persist(List<Preference> list);
}
