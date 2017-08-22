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

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.preferences.Preference.Type.bool;
import static com.pajato.android.gamechat.preferences.Preference.Type.stringset;

/**
 * Implement the preferences provider interface for use with the app shared preferences.
 *
 * @author Paul Michael Reilly on 7/31/2017
 */

public class SharedPreferencesProvider implements PreferencesProvider {

    // Private instance variables.

    /** The shared preferences being managed. */
    private SharedPreferences mPrefs;

    /** The cache providing immediate access to the preference values. */
    private Map<String, Preference> mCache;

    // Public constructor.

    /** Build an instance accepting the Activity containing the shared preferences. */
    public SharedPreferencesProvider(final Activity activity, final String name, final int mode) {
        // Load the shared preferences and cache the values.
        mPrefs = activity.getSharedPreferences(name, mode);
        mCache = new HashMap<>();
        Map<String, ?> prefs = mPrefs.getAll();
        for (String key : prefs.keySet()) {
            Object item = prefs.get(key);
            Preference pref = getPreference(key, item);
            if (pref != null)
                mCache.put(key, pref);
        }
        mCache = getAll();
    }

    // Public instance methods.

    /** Return a possibly empty map of all the preferences. */
    @Override public Map<String, Preference> getAll() {
        return mCache;
    }

    /** Return a boolean flag associated with the given key. In none, return a default value. */
    @Override public boolean getBoolean(final String key, final boolean defaultValue) {
        return mPrefs.getBoolean(key, defaultValue);
    }

    /** Return a string set associated with the given key. If none, return a default set. */
    @Override public Set<String> getStringSet(final String key, final Set<String> defaultValues) {
        return mPrefs.getStringSet(key, defaultValues);
    }

    /** Persist a list of preference values. */
    @Override public void persist(final List<Preference> values) {
        SharedPreferences.Editor editor = mPrefs.edit();
        if (values == null) {
            editor.clear().apply();
            mCache.clear();
            return;
        }

        // Persist the elements of the list.
        for (Preference item : values) {
            if (item.type == bool) {
                editor.putBoolean(item.key, item.booleanValue);
                mCache.put(item.key, new Preference(item.key, item.booleanValue));
            } else if (item.type == stringset) {
                editor.putStringSet(item.key, item.stringSetValue);
                mCache.put(item.key, new Preference(item.key, item.stringSetValue));
            } else
                continue;
        }
        editor.apply();
    }

    // Private instance methods.

    /** Return null or a Preference object with the given key and value. */
    private Preference getPreference(String key, Object value) {
        if (value instanceof Boolean)
            return new Preference(key, (Boolean) value);
        else if (value instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<String> stringSet = (Set<String>) value;
            return new Preference(key, stringSet);
        } else
            return null;
    }
}
