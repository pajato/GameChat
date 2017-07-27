/*
 * Copyright (C) 2016 Pajato Technologies LLC.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see http://www.gnu.org/licenses
 */

package com.pajato.android.gamechat.credentials;

/**
 * Provide a POJO to model an a User.
 *
 * @author Paul Michael Reilly
 */
public class Credentials {

    // Private instance variables.

    /** The User email address. */
    public String email;

    /** The email has been tested and is a known account (so we cannot set a username) */
    public boolean accountIsKnown;

    /** The identity provider. */
    public String provider;

    /** The identity password for email and twitter providers. */
    public String secret;

    /** The id token supplied by Firebase that is part of the re-authentication credentials. */
    public String token;

    /** The identity icon url. */
    public String url;

    /** The user name */
    public String name;

    // Public constructors.

    /** Build an empty constructor. */
    Credentials() {}


    /** Build an instance for e-mail login (used when creating a protected user) */
    public Credentials(final String email, final String name, final String url,
                       final boolean isKnown) {
        this.email = email;
        this.name = name;
        this.url = url;
        this.accountIsKnown = isKnown;
    }

    /** Build an instance accepting a URI for the photo URL. */
    Credentials(final String provider, final String email, final String token,
                final String secret) {
        this.provider = provider;
        this.email = email;
        this.token = token;
        this.secret = secret;
    }

    /** Build an instance accepting an encoded persistence string. */
    Credentials(final String value) {
        // Sanity check the value by detecting an empty or non-existent value. Abort silently when
        // an error is detected.
        if (value == null || value.isEmpty())
            return;

        // Use a brute force approach to parsing the value, which is encoded as follows:
        // <email-length>:<provider-length>:<secret-length>:<token-length>:<url-length>:<rest>
        //
        // In other words, the lengths of the properties separated by colons followed by the
        // concatenated property values.

        // Step 1) separate the lengths part from the concatenated property values part by indexing
        // on the '@' character in the email address.
        int atIndex = value.indexOf("@");
        int splitIndex = getSplitIndex(atIndex, value);
        if (splitIndex == -1)
            return;
        String values = value.substring(splitIndex + 1);

        // Step 2) build an array of lengths by splitting the lengths part on the colon characters.
        int[] lengthArray = getLengths(value.substring(0, splitIndex));

        // Step 3) extract the properties using the array of lengths and a running index into the
        // values part.
        int runningIndex = 0;
        email = values.substring(runningIndex, runningIndex + lengthArray[0]);
        runningIndex += lengthArray[0];
        provider = values.substring(runningIndex, runningIndex + lengthArray[1]);
        runningIndex += lengthArray[1];
        if (lengthArray[2] > 0)
            secret = values.substring(runningIndex, runningIndex + lengthArray[2]);
        runningIndex += lengthArray[2];
        if (lengthArray[3] > 0)
            token = values.substring(runningIndex, runningIndex + lengthArray[3]);
        runningIndex += lengthArray[3];
        if (lengthArray[4] > 0)
            url = values.substring(runningIndex);
    }

    // Public instance methods.

    /** Return an encoded representation of the credential. */
    public String toString() {
        StringBuilder indices = new StringBuilder();
        StringBuilder values = new StringBuilder();
        String[] props = {email, provider, secret, token, url};
        for (String prop : props) {
            int length = prop != null ? prop.length() : 0;
            indices.append(String.valueOf(length)).append(":");
            values.append(prop != null ? prop : "");
        }
        return indices.toString() + values.toString();
    }

    // Private instance methods.

    /** Return a five element string array containing the credential property lengths. */
    private int[] getLengths(final String indices) {
        String[] strings = indices.split(":");
        int[] result = new int[strings.length];
        for (int i = 0; i < strings.length; i++)
            result[i] = Integer.parseInt(strings[i]);
        return result;
    }

    /** Return the index where the lengths part ends and the values part begins. */
    private int getSplitIndex(final int atIndex, final String value) {
        int index = atIndex - 1;
        while (index > 1) {
            if (value.charAt(index--) != ':')
                continue;
            return index + 1;
        }
        return -1;
    }
}
