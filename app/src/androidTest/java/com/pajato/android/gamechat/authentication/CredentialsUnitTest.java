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

package com.pajato.android.gamechat.authentication;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static junit.framework.Assert.assertEquals;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class CredentialsUnitTest {

    /** Ensure that the string constructor handles null appropriately. */
    @Test public void testNullInput() {
        // Test that an null value in gives null values out.
        Credentials testCredentials = new Credentials(null);
        assertEquals("Email is not null", null, testCredentials.email);
        assertEquals("Token is not null", null, testCredentials.token);
        assertEquals("Secret is not null", null, testCredentials.secret);
        assertEquals("Provider is not null", null, testCredentials.provider);
        assertEquals("Photo URL is not null", null, testCredentials.url);
    }

    /** Ensure that an empty credentials object outputs the correct value. */
    @Test public void testEmptyOutput() {
        Credentials testCredentials = new Credentials();
        assertEquals("Bad output", "0:0:0:0:0:", testCredentials.toString());
    }

    /** Ensure that the string constructor handles empty input appropriately. */
    @Test public void testEmptyInput() {
        // Test that an null value in gives null values out.
        Credentials testCredentials = new Credentials("");
        assertEquals("Email is not null", null, testCredentials.email);
        assertEquals("Token is not null", null, testCredentials.token);
        assertEquals("Secret is not null", null, testCredentials.secret);
        assertEquals("Provider is not null", null, testCredentials.provider);
        assertEquals("Photo URL is not null", null, testCredentials.url);
    }

    /** Ensure that the string constructor handles empty input appropriately. */
    @Test public void testNonEmptyInput() {
        // Test that an null value in gives null values out.
        String email = "test@gc.com";
        String provider = "google.com";
        String secret = "!@@@$#!$#^^^^$";
        String token = "fjajfja;fja;";
        String url = "https://server/ph.png";
        String lengths = getLengths(email, provider, secret, token, url);
        String content = email + provider + secret + token + url;
        String input = lengths + content;
        Credentials testCredentials = new Credentials(input);
        assertEquals("Email is wrong:", email, testCredentials.email);
        assertEquals("Provider is wrong.", provider, testCredentials.provider);
        assertEquals("Secret is wrong.", secret, testCredentials.secret);
        assertEquals("Token is wrong.", token, testCredentials.token);
        assertEquals("Photo URL is wrong.", url, testCredentials.url);
        assertEquals("Bad output: ", input, testCredentials.toString());
    }

    /** Ensure that the string constructor handles no password input appropriately. */
    @Test public void testSomeEmptyInput() {
        // Test that an null value in gives null values out.
        String email = "test@gc.com";
        String provider = "google.com";
        String secret = null;
        String token = null;
        String url = null;
        String lengths = getLengths(email, provider, secret, token, url);
        String content = email + provider
            ;
        String input = lengths + content;
        Credentials testCredentials = new Credentials(input);
        assertEquals("Email is wrong:", email, testCredentials.email);
        assertEquals("Provider is wrong.", provider, testCredentials.provider);
        assertEquals("Secret is wrong.", secret, null);
        assertEquals("Token is wrong.", token, null);
        assertEquals("Photo URL is wrong.", url, null);
        assertEquals("Bad output: ", input, testCredentials.toString());
    }

    /** Ensure that the string constructor handles empty input appropriately. */
    @Test public void testBadEmailInput() {
        // Test that an null value in gives null values out.
        String email = "testgc.com";
        String provider = "google.com";
        String secret = "!";
        String token = ";";
        String url = "https://server/ph.png";
        String lengths = getLengths(email, provider, secret, token, url);
        String content = email + provider + secret + token + url;
        String input = lengths + content;
        Credentials testCredentials = new Credentials(input);
        assertEquals("Email is not null", null, testCredentials.email);
        assertEquals("Token is not null", null, testCredentials.token);
        assertEquals("Secret is not null", null, testCredentials.secret);
        assertEquals("Provider is not null", null, testCredentials.provider);
        assertEquals("Photo URL is not null", null, testCredentials.url);
    }

    /** Ensure that the ... constructor gets exercised. */
    @Test public void testTestConstructor() {
        // Test that an null value in gives null values out.
        String email = "testgc.com";
        String name = "fred";
        String url = "https://server/ph.png";
        boolean isKnown = false;
        Credentials testCredentials = new Credentials(email, name, url, isKnown);
        assertEquals("Email is wrong", email, testCredentials.email);
        assertEquals("Name is wrong", name, testCredentials.name);
        assertEquals("Photo URL is wrong", url, testCredentials.url);
        assertEquals("Is known flag is wrong", isKnown, testCredentials.accountIsKnown);
    }

    /** Ensure that the ... constructor gets exercised. */
    @Test public void testOtherConstructor() {
        // Test that an null value in gives null values out.
        String email = "testgc.com";
        String provider = "fred";
        String token = "https://server/ph.png";
        String secret = "some password";
        Credentials testCredentials = new Credentials(provider, email, token, secret);
        assertEquals("Email is wrong", email, testCredentials.email);
        assertEquals("Token is wrong", token, testCredentials.token);
        assertEquals("Secret is wrong", secret, testCredentials.secret);
        assertEquals("Provider is wrong", provider, testCredentials.provider);
    }

    // Private instance methods.

    private String getLengths(final String... properties) {
        StringBuilder result = new StringBuilder();
        for (String prop : properties)
            result.append(String.format(Locale.US, "%d:", prop != null ? prop.length() : 0));
        return result.toString();
    }
}
