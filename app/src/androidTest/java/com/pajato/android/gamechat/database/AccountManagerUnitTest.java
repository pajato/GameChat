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

package com.pajato.android.gamechat.database;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AccountManagerUnitTest {

    // Private instance variables.

    //private PreferencesProvider mPrefs;

    // Public instance methods.

    /** Initialize the credentials manager instance under test using the test prefs provider  */
    @Before public void setup() {
        // tbd
    }

    @After public void teardown() {}

    /** Cover the enum "extra" methods. */
    @Test public void testEnum() {
        Assert.assertEquals("Enumeration size is wrong!", 1, AccountManager.values().length);
        boolean value = AccountManager.valueOf("instance") == AccountManager.instance;
        Assert.assertTrue("Enumeration value is wrong!", value);
    }
}
