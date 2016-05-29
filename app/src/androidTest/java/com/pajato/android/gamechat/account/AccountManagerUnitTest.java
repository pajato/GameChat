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

package com.pajato.android.gamechat.stocklist;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;
import com.pajato.android.gamechat.account.AccountManager;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Provide a set of device based unit tests for the basic stocklist stock model class.
 *
 * @author Paul Michael Reilly
 */
@RunWith(AndroidJUnit4.class)
public class AccountManagerUnitTest {

    /** The rule used to launch the activity under test. */
    @Rule public ActivityTestRule<MainActivity> activityRule =
        new ActivityTestRule<>(MainActivity.class);

    @Test public void unitTest() {
        // Test that the instance value is accessible and correct; the adapter exists and that there
        // is one element in the values collection.
        AccountManager instance = AccountManager.instance;
        Assert.assertTrue("Bad instance value.", AccountManager.valueOf("instance") == instance);
        Assert.assertTrue("There is an account!", AccountManager.instance.hasAccount() != true);
        AccountManager.instance.register("fred@gmail.com");
        Assert.assertTrue("There is not a single value.", AccountManager.values().length == 1);
    }

}
