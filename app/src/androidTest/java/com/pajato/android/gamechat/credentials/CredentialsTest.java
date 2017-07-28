package com.pajato.android.gamechat.credentials;

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
public class CredentialsTest {

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

    private String getLengths(final String... properties) {
        StringBuilder result = new StringBuilder();
        for (String prop : properties)
            result.append(String.format(Locale.US, "%d:", prop.length()));
        return result.toString();
    }
}
