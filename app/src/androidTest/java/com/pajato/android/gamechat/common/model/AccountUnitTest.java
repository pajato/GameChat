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

package com.pajato.android.gamechat.common.model;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AccountUnitTest {
    /** Test the Account constructors. */
    @Test public void testAccount() {

        // Test the copy constructor base operations.
        Account account = new Account();
        account.key = "testKey";
        account.owner = "FredsKey";
        account.name = "Fred";
        account.createTime = new Date().getTime();
        Account testAccount = new Account(account);
        Assert.assertEquals("Key is wrong!", account.key, testAccount.key);
        Assert.assertEquals("Owner is wrong!", account.key, testAccount.owner);
        Assert.assertEquals("Name is wrong!", account.name, testAccount.name);
        Assert.assertTrue("Create time is wrong!", account.createTime <= testAccount.createTime);
    }
}
