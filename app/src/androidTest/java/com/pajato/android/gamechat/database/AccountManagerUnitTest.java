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
