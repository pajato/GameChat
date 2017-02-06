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

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Provide a singleton to manage User credentials.  Each time a User signs into the app, the
 * information about the account is persisted to the app preferences store.
 *
 * @author Paul Michael Reilly
 */
public enum CredentialsManager {
    instance;

    // Public class constants.

    /** The preferences key for the saved credentials. */
    public static final String CREDENTIALS_KEY = "credentialsKey";

    // Private class constants.

    /** The email provider type. */
    private static final String EMAIL_PROVIDER = "emailProvider";

    /** The format used to persist a credential email address. */
    private static final String FORMAT_EMAIL = "%s:email:%s";

    /** The format used to persist a credential provider. */
    private static final String FORMAT_PROVIDER = "%s:provider:%s";

    /** The format used to persist a credential secret (password or token). */
    private static final String FORMAT_SECRET = "%s:secret:%s";

    // Private instance variables.

    /** The map associating an email address with a provider and token/password. */
    private Map<String, Credentials> mCredentialsMap = new HashMap<>();

    /** The application preferences store. */
    private SharedPreferences mPrefs;

    // Public instance methods

    /** Return credentials for a given User account by email address, null on no account. */
    public AuthCredential getAuthCredential(final String emailAddress) {
        // Determine if there is no such account.
        Credentials credentials = mCredentialsMap.get(emailAddress);
        if (credentials == null) return null;

        // Case on the provider type.
        switch (credentials.provider) {
            case GoogleAuthProvider.PROVIDER_ID:
                return GoogleAuthProvider.getCredential(credentials.secret, null);
            case FacebookAuthProvider.PROVIDER_ID:
                return FacebookAuthProvider.getCredential(credentials.secret);
            case TwitterAuthProvider.PROVIDER_ID:
                return TwitterAuthProvider.getCredential(credentials.secret, credentials.secret);
            default: return null;
        }
    }

    /** Build the object by reading in the saved credentials. */
    public void init(final SharedPreferences prefs) {
        mPrefs = prefs;
        Set<String> stringSet = prefs.getStringSet(CREDENTIALS_KEY, null);
        if (stringSet != null) {
            for (String value : stringSet) cacheValue(value);
            cacheValidate();
        }
    }

    /** Save a set of credentials to the preferences store. */
    public void saveCredentials(final String provider, final String email, final String secret) {
        // Cache the credentials.
        Credentials credentials = new Credentials(provider, email, secret);
        mCredentialsMap.put(email, credentials);

        // Persist the credentials.
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putStringSet(CREDENTIALS_KEY, getStringSet());
        editor.apply();
    }

    // Private instance methods.

    /** Return the current cached credentials as a set of strings. */
    private Set<String> getStringSet() {
        // Extract the keys from each credentials object in the cache, tag and accumulate them.
        final Set<String> result = new HashSet<>();
        for (Credentials credentials : mCredentialsMap.values())
            result.addAll(getStrings(credentials.email, credentials));
        return result;
    }

    /** Return a list of strings representing a credential set. */
    private List<String> getStrings(final String key, final Credentials credentials) {
        // Tag each string with an index and an identifier in order to extract them as a unit when
        // reading from the persistence store.
        List<String> result = new ArrayList<>();
        result.add(String.format(Locale.US, FORMAT_EMAIL, key, credentials.email));
        result.add(String.format(Locale.US, FORMAT_PROVIDER, key, credentials.provider));
        result.add(String.format(Locale.US, FORMAT_SECRET, key, credentials.secret));
        return result;
    }

    /** Validate the entries in the cache, removing any invalid entries. */
    private void cacheValidate() {
        // Build a list of invalid entry key values, then strip those entries from the cache.
        List<String> invalidEntryList = new ArrayList<>();
        for (Credentials credentials : mCredentialsMap.values())
            if (isInvalid(credentials)) invalidEntryList.add(credentials.email);
        for (String key : invalidEntryList) mCredentialsMap.remove(key);
    }

    /** Cache the given, tag encoded value, */
    private void cacheValue(final String value) {
        // Ensure that the value is valid.  Abort if not.
        final String SEP = ":";
        int keyIndex = value.indexOf(SEP);
        int typeIndex = keyIndex != -1 ? value.indexOf(SEP, keyIndex + 1) : -1;
        int contentIndex = typeIndex != -1 ? value.indexOf(SEP, typeIndex + 1) : -1;
        String key = keyIndex > 0 ? value.substring(0, keyIndex) : null;
        if (keyIndex == -1 || typeIndex == -1 || contentIndex == -1 || !isValidEmail(key)) return;

        // The key is valid.  Ensure that an entry exists for this key, creating one if necessary.
        Credentials credentials = mCredentialsMap.get(key);
        if (credentials == null) {
            credentials = new Credentials();
            credentials.email = key;
            mCredentialsMap.put(key, credentials);
        }

        // Append the tagged content according to the type tag value.
        String contentType = value.substring(keyIndex + 1, typeIndex);
        String content = value.substring(typeIndex + 1);
        if (contentType.equals("provider")) credentials.provider = content;
        else if (contentType.equals("secret")) credentials.secret = content;
    }

    /** Return TRUE iff the given credentials are not complete and sensible. */
    private boolean isInvalid(@NonNull final Credentials credentials) {
        return !isValidEmail(credentials.email) || !isValidProvider(credentials.provider)
                || credentials.secret == null || credentials.secret.length() == 0;
    }

    /** Return TRUE iff the given email address is valid. */
    private boolean isValidEmail(final String email) {
        // Detect a null or empty email address.
        if (email == null || email.length() == 0) return false;

        // Detect a badly formed email address.
        int index = email.indexOf("@");
        return !(index <= 0 || index == email.length());
    }

    /** Return TRUE iff the given provider is valid. */
    private boolean isValidProvider(final String provider) {
        // Detect a null or empty provider.
        if (provider == null || provider.length() == 0) return false;

        // Detect an unsupported provider.
        switch (provider) {
            case GoogleAuthProvider.PROVIDER_ID:
            case FacebookAuthProvider.PROVIDER_ID:
            case TwitterAuthProvider.PROVIDER_ID:
            case EMAIL_PROVIDER: return true;
            default: return false;
        }
    }
}
