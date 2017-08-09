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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implement the preferences provider interface for use with the app shared preferences.
 *
 * @author Paul Michael Reilly on 7/31/2017
 */

public class TestPreferencesProvider implements PreferencesProvider {

    // Private instance variables.

    /** The test preferences being managed. */
    private Map<String, Preference> mPrefs = new HashMap<>();

    // Public constructor.

    /** Build an instance accepting the Activity containing the shared preferences. */
    public TestPreferencesProvider() {}

    // Public instance methods.

    /** Return a map of the preferences. */
    @Override public Map<String, Preference> getAll() {
        return mPrefs;
    }

    /** Return a boolean flag associated with the given key. In none, return a default value. */
    @Override public boolean getBoolean(final String key, final boolean defaultValue) {
        Preference pref = mPrefs.get(key);
        return pref != null ? pref.booleanValue : defaultValue;
    }

    /** Return a string set associated with the given key. If none, return a default set. */
    @Override public Set<String> getStringSet(final String key, final Set<String> defaultValues) {
        Preference pref = mPrefs.get(key);
        return pref != null ? pref.stringSetValue : defaultValues;
    }

    /** Persist a list of preference values. */
    @Override public void persist(final List<Preference> values) {
        if (values == null) {
            mPrefs.clear();
            return;
        }

        for (Preference item : values) {
            mPrefs.put(item.key, item);
        }
    }
}
