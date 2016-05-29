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

package com.pajato.android.gamechat.account;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.pajato.android.gamechat.main.MainActivity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Provide a set of device based unit tests for the message event class.
 *
 * @author Paul Michael Reilly
 */
@RunWith(AndroidJUnit4.class)
public class AccountUnitTest {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = AccountUnitTest.class.getSimpleName();

    // Private instance methods.

    // Accounts used for testing.

    /** An account that is unique and populated. */
    private Account a;

    /** An account that is different than the unique account. */
    private Account b;

    /** A copy of b. */
    private Account c;

    /** An empty account. */
    private Account d;

    /** The rule used to launch the activity under test. */
    @Rule public ActivityTestRule<MainActivity> activityRule =
        new ActivityTestRule<>(MainActivity.class);

    /** Build the test objects. */
    @Before public void setUp() {
        // Setup a unique (for this test) account.
        a = Account.builder()
            .accountId("888-555-1212")
            .displayName("Fred C. Jones")
            .accountUrl("http://path.to.a.URL")
            .token("#3338903345")
            .providerName("Google")
            .providerId("com.google")
            .avatar("Conor", getNewURL("file://server/conor.png")).build();

        // Setup two duplicate accounts.
        b = Account.builder()
            .accountId("800-555-1212")
            .displayName("George C. Jones")
            .accountUrl("http://path.to.b.URL")
            .token("#3338903678")
            .providerName("Facebook")
            .providerId("com.facebook").build();
        c = b.toBuilder().build();

        // Setup an empty account.
        d = Account.builder().build();
        d.setAvatars(null);
    }

    /** Test the Lombok equals() method for basic equality operations. */
    @Test public void testBasicEquality() {
        // Test basic equality
        assertTrue("An account is not equal to itself!", a.equals(a));
        assertTrue("Two accounts with identical data are not equal!", b.equals(c));
        assertFalse("Two different accounts are equal!", a.equals(c));
        assertFalse("Two different but equal objects are the same object!", b == c);
        assertFalse("An account and another object are equal!",a.equals("GOOG"));
    }

    /** Test the various hashcode possibilities. */
    @Test public void testHashCode() {
        // Test hashCode.
        int ha = a.hashCode();
        int hb = b.hashCode();
        int hc = c.hashCode();
        int hd = d.hashCode();
        assertTrue("hashCode() is not a pure function.", ha == a.hashCode());
        assertTrue("The hash code for two different, equal objects are different.", hb == hc);
        assertFalse("The hash code for two different, unequal objects are the same!.", ha == hc);
    }

    /** Test the equals() method. */
    @Test public void testEquals() {
        // Test the account id equality and prepare to test the display name.
        runEqualsAsserts("account id");
        b.setAccountId(a.getAccountId());
        d.setAccountId(a.getAccountId());

        // Test the display name equality and prepare to test the account url.
        runEqualsAsserts("display name");
        b.setDisplayName(a.getDisplayName());
        d.setDisplayName(a.getDisplayName());

        // Test the account url equality and prepare to test the token.
        runEqualsAsserts("account url");
        b.setAccountUrl(a.getAccountUrl());
        d.setAccountUrl(a.getAccountUrl());

        // Test the token equality and prepare to test the provider name.
        runEqualsAsserts("token");
        b.setToken(a.getToken());
        d.setToken(a.getToken());

        // Test the provider name equality and prepare to test the provider id.
        runEqualsAsserts("provider name");
        b.setProviderName(a.getProviderName());
        d.setProviderName(a.getProviderName());

        // Test the provider id equality and prepare to test the provider avatars..
        runEqualsAsserts("provider id");
        b.setProviderId(a.getProviderId());
        d.setProviderId(a.getProviderId());

        // Test the avatar equality.
        runEqualsAsserts("avatars");
    }

    /** Test setters */
    @Test public void testSetters() {
        Account test = d;
        assertTrue("The test account id is not empty!", test.getAccountId() == null);
        test.setAccountId("888-555-1212");
        assertTrue("The test account id is empty!", test.getAccountId() == "888-555-1212");

        assertTrue("The test display name is not empty!", test.getDisplayName() == null);
        test.setDisplayName("The Shovel");
        assertTrue("The test display name is empty!", test.getDisplayName() == "The Shovel");

        assertTrue("The test token is not empty!", test.getToken() == null);
        test.setToken("#a12345678");
        assertTrue("The test token is empty!", test.getToken() == "#a12345678");

        assertTrue("The test provider name is not empty!", test.getProviderName() == null);
        test.setProviderName("Google");
        assertTrue("The test provider name is empty!", test.getProviderName() == "Google");

        assertTrue("The test provider id is not empty!", test.getProviderId() == null);
        test.setProviderId("com.google");
        assertTrue("The test provider id is empty!", test.getProviderId() == "com.google");
    }

    /** Test the avatars operations. */
    @Test public void testAvatars() {
        // Test the avatars add operation.
        Account test = b;
        assertTrue("The test avatar collection is not empty!", test.getAvatars().size() == 0);
        test = test.toBuilder().avatar("fred", getNewURL("http://server0/path")).build();
        assertFalse("The test avatar collection is empty!", test.getAvatars().size() == 0);
        assertTrue("The test avatar collection size is not 1!", test.getAvatars().size() == 1);

        // Test the clear operation.
        test = test.toBuilder().clearAvatars().build();
        assertTrue("The test avatar collection is not empty!", test.getAvatars().size() == 0);

        // Test the avatars add all operation.
        Map<String, URL> entries = new HashMap<>();
        entries.put("a1", getNewURL("http://server1/path"));
        entries.put("a2", getNewURL("http://server2/path"));
        test = test.toBuilder().avatars(entries).build();
        assertTrue("The test avatar collection size is not 2.", test.getAvatars().size() == 2);
    }

    /** Test the toString method. */
    @Test public void testToString() {
        String value = a.toString();
        assertTrue("The toString() method generates an empty value!", value.length() != 0);
        value = a.toBuilder().toString();
        assertTrue("The builder toString*() method generates an empty value!", value.length() != 0);
    }

    // Private instance methods.

    /** Check the basic equals asserts on a given field. */
    private void runEqualsAsserts(final String field) {
        assertFalse(String.format("An empty and non-empty %s generates equal!", field), d.equals(a));
        assertFalse(String.format("A non-empty and an empty %s generates equal!", field), a.equals(d));
        String text = field.endsWith("s") ? field : field + "s";
        assertFalse(String.format("Two different non-empty %s are equal.", text), a.equals(b));
    }

    /** Encapsulate the creation of a URL for a given String. */
    private URL getNewURL(final String url) {
        URL result = null;
        try {
            result = new URL(url);
        } catch (MalformedURLException exc) {
            fail(String.format("Invalid URL!", url));
        }

        return result;
    }
}
