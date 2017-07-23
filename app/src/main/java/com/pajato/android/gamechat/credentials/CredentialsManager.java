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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.database.AccountManager.ACCOUNT_AVAILABLE_KEY;
import static com.pajato.android.gamechat.main.MainActivity.PREFS;

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

    /** The preferences key for the credentials provider. */
    public static final String PROVIDER_KEY = "providerKey";

    /** The preferences key for the credentials email address. */
    public static final String EMAIL_KEY = "emailKey";

    /** The preferences key for the last saved email address. */
    public static final String LAST_USED_EMAIL_KEY = "lastUsedEmailKey";

    /** The preferences key for the credentials icon URL. */
    public static final String URL_KEY = "urlKey";

    // Private class constants.

    /** The email provider type. */
    private static final String EMAIL_PROVIDER = "emailProvider";

    /** The format used to persist a credential email address. */
    private static final String FORMAT_EMAIL = "%s:email:%s";

    /** The format used to persist a credential provider. */
    private static final String FORMAT_PROVIDER = "%s:provider:%s";

    /** The format used to persist a credential icon URL. */
    private static final String FORMAT_URL = "%s:url:%s";

    /** The logcat tag. */
    private static final String TAG = CredentialsManager.class.getSimpleName();

    // Private instance variables.

    /** The map associating an email address with a provider and token/password. */
    private Map<String, Credentials> mCredentialsMap = new HashMap<>();

    // Public instance methods

    /** Return the credentials map. */
    public Map<String, Credentials> getMap() {
        return mCredentialsMap;
    }

    /** Build the object by reading in the saved credentials. */
    public void init(final SharedPreferences prefs) {
        Set<String> stringSet = prefs.getStringSet(CREDENTIALS_KEY, null);
        if (stringSet != null) {
            for (String value : stringSet)
                cacheValue(value);
            cacheValidate();
        }
    }

    /** Persist the given account credentials by updating the User persistence store. */
    public void persist(final Activity activity, final FirebaseUser user) {
        String email = user != null ? user.getEmail() : null;
        List<String> list = user != null ? user.getProviders() : null;
        String provider = list != null && list.size() > 0 ? list.get(0) : null;
        Uri uri = user != null ? user.getPhotoUrl() : null;
        persist(activity, new Credentials(provider, email, uri));
    }

    /** Persist the given intent credentials by updating the User persistence store. */
    public void persist(final Activity activity, final Intent intent) {
        // Ensure the intent exists and has complete credentials. Abort if not.
        String email = intent != null ? intent.getStringExtra(EMAIL_KEY) : null;
        String provider = email != null ? intent.getStringExtra(PROVIDER_KEY) : null;
        String url = intent != null ? intent.getStringExtra(URL_KEY) : null;
        persist(activity, new Credentials(provider, email, url));
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
        result.add(String.format(Locale.US, FORMAT_URL, key, credentials.url));
        return result;
    }

    /** Validate the entries in the cache, removing any invalid entries. */
    private void cacheValidate() {
        // Build a list of invalid entry key values, then strip those entries from the cache.
        List<String> invalidEntryList = new ArrayList<>();
        for (Credentials credentials : mCredentialsMap.values())
            if (!isValid(credentials))
                invalidEntryList.add(credentials.email);
        for (String key : invalidEntryList)
            mCredentialsMap.remove(key);
    }

    /** Cache the given, tag encoded value, */
    private void cacheValue(final String value) {
        // Ensure that the value has three fields separated by two colons.  Abort, ignoring the
        // value if not.
        final String SEP = ":";
        String[] triple = value.split(SEP);
        if (triple.length != 3) {
            Log.e(TAG, String.format(Locale.US, "Invalid value: {%s}.", value));
            return;
        }

        // Unpack the string into the credentials map.
        String key = triple[0];
        String type = triple[1];
        String content = triple[2];
        if (key == null || key.isEmpty() || type == null || type.isEmpty())
            return;

        // The key and type are valid.  Ensure that an entry exists for this key, creating one if
        // necessary.
        Credentials credentials = mCredentialsMap.get(key);
        if (credentials == null) {
            credentials = new Credentials();
            credentials.email = key;
            mCredentialsMap.put(key, credentials);
        }

        // Set non-empty content according to the type tag value.
        if (content == null || content.isEmpty() || content.equals("null"))
            return;
        switch (type) {
            case "provider":
                credentials.provider = content;
                break;
            case "url":
                credentials.url = content;
                break;
        }
    }

    /** Return TRUE iff the given credentials are not complete and sensible. */
    private boolean isValid(@NonNull final Credentials credentials) {
        return isValidEmail(credentials.email) && isValidProvider(credentials.provider);
    }

    /** Return TRUE iff the given email address is valid. */
    private boolean isValidEmail(final String email) {
        // Detect an invalid email address by looking for a null or empty string, or no "@" in
        // the string.
        return email != null && !email.isEmpty() && email.contains("@");
    }

    /** Return TRUE iff the given provider is valid. */
    private boolean isValidProvider(final String provider) {
        // Detect a null or empty provider.
        return provider != null && !provider.isEmpty() && isSupported(provider);
    }

    /** Return TRUE iff the given provider is not supported. */
    private boolean isSupported(final String provider) {
        // Detect an unsupported provider.
        switch (provider) {
            case GoogleAuthProvider.PROVIDER_ID:
            case FacebookAuthProvider.PROVIDER_ID:
            case TwitterAuthProvider.PROVIDER_ID:
            case EMAIL_PROVIDER:
                return true;
            default:
                return false;
        }
    }

    /** Save the given credentials to the persistence store (shared preferences). */
    private void persist(final Activity activity, final Credentials credentials) {
        // Ensure that the credential email and provider properties exist. Abort if not.
        if (credentials.provider == null)
            return;

        // Persist and cache the credentials.
        mCredentialsMap.put(credentials.email, credentials);
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ACCOUNT_AVAILABLE_KEY, true);
        editor.putStringSet(CREDENTIALS_KEY, getStringSet());
        editor.putString(LAST_USED_EMAIL_KEY, credentials.email);
        editor.apply();
    }
}
