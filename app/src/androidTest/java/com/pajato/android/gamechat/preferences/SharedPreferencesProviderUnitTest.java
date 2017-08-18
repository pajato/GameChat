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
import android.content.Context;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.intro.IntroActivity;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Provide a unit test for the SharedPreferencesProvider class.
 *
 * Created by pmr on 8/4/17.
 */
@RunWith(AndroidJUnit4.class)
public class SharedPreferencesProviderUnitTest {

    // Public instance variables.

    /** Set up the rule instance variable to allow for having intent extras passed in. */
    @Rule
    public ActivityTestRule<IntroActivity> mRule =
        new ActivityTestRule<>(IntroActivity.class, true, false);

    /** Launch the intro activity at the start of every test. */
    @Before public void setUp() {
        Intent intent = new Intent();
        mRule.launchActivity(intent);
    }

    /** Gracefully shut down the test (intro) activity. */
    @After public void tearDown() {
        mRule.getActivity().finish();
    }

    @Test public void testAll() throws Exception {
        final Activity activity = mRule.getActivity();
        final String name = "IntroPrefs";
        final int mode = Context.MODE_PRIVATE;
        PreferencesProvider prefs = new SharedPreferencesProvider(activity, name, mode);
        prefs.persist(null);
        Map<String, Preference> map = prefs.getAll();
        Assert.assertTrue("The shared preferences file is not empty!", map.size() == 0);

        // Next test...
        Set<String> set = new TreeSet<>();
        set.add("first");
        set.add("second");
        set.add("third");
        final String BOOL_KEY = "boolKey";
        final String STRING_SET_KEY = "stringSetKey";
        List<Preference> list = new ArrayList<>();
        list.add(new Preference(BOOL_KEY, true));
        list.add(new Preference(STRING_SET_KEY, set));
        Preference pref = new Preference(BOOL_KEY, false);
        pref.type = null;
        list.add(pref);
        prefs.persist(list);
        Assert.assertEquals("The shared preferences file size is wrong!", 2, map.size());
        Assert.assertEquals("The boolean value is wrong.", true, prefs.getBoolean(BOOL_KEY, false));

        // Another test to get more constructor coverage.
        prefs = new SharedPreferencesProvider(activity, name, mode);
        Assert.assertEquals("The shared preferences file size is wrong!", 2, map.size());
        Assert.assertEquals("The boolean value is wrong.", true, prefs.getBoolean(BOOL_KEY, false));
    }
}
