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

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a unit test class to achieve acceptable coverage on the TestPreferencesProvider class.
 *
 * Created by pmr on 8/4/17.
 */
public class TestPreferencesProviderUnitTest {
    @Test public void testGetAndPersist() throws Exception {
        final String KEY = "key";
        TestPreferencesProvider prefs = new TestPreferencesProvider();
        boolean value = prefs.getBoolean("noValue", false);

        // Test the persist() with a list.
        Assert.assertFalse("Get boolean did not provide the default!", value);
        List<Preference> list = new ArrayList<>();
        list.add(new Preference(KEY, true));
        prefs.persist(list);
        value = prefs.getBoolean(KEY, false);
        Assert.assertEquals("Persisted the wrong number of values!", 1, prefs.getAll().size());
        Assert.assertTrue("Persisted boolean value is wrong!", value);

        // Test the persist() with a null.
        prefs.persist(null);
        value = prefs.getBoolean(KEY, false);
        Assert.assertFalse("Defaulted value is wrong!", value);
    }
}
