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
