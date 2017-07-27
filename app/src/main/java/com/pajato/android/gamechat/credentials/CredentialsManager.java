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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    /** The persistence key for the saved set of credentials. */
    public static final String CREDENTIALS_SET_KEY = "credentialsKey";

    /** The preferences key for the last saved email address. */
    public static final String LAST_USED_EMAIL_KEY = "lastUsedEmailKey";

    // Private class constants.

    /** The email provider type. */
    private static final String EMAIL_PROVIDER = "emailProvider";

    ///** The logcat tag. */
    //private static final String TAG = CredentialsManager.class.getSimpleName();

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
        mCredentialsMap = new HashMap<>();
        Set<String> stringSet = prefs.getStringSet(CREDENTIALS_SET_KEY, null);
        if (stringSet != null) {
            for (String value : stringSet)
                cacheValue(value);
            cacheValidate();
        }
    }

    /** Persist the given IDP credentials by updating the User persistence store. */
    public void persist(@NonNull final Activity activity, @NonNull final IdpResponse response) {
        // Persist the login information that will be necessary to display the switch user menu and
        // will enable re-authentication.
        final String email = response.getEmail();
        final String provider = response.getProviderType();
        final String token = response.getIdpToken();
        final String secret = response.getIdpSecret();
        persist(activity, new Credentials(provider, email, token, secret));
    }

    // Private instance methods.

    /** Return the current cached credentials as a set of strings. */
    private Set<String> getStringSet() {
        // Extract the keys from each credentials object in the cache, tag and accumulate them.
        final Set<String> result = new HashSet<>();
        for (Credentials credentials : mCredentialsMap.values())
            result.add(credentials.toString());
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
        Credentials credentials = new Credentials(value);
        mCredentialsMap.put(credentials.email, credentials);
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
        // Ensure that the credential provider property exists. Abort if not.
        if (credentials.provider == null)
            return;

        // Persist and cache the credentials.
        mCredentialsMap.put(credentials.email, credentials);
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ACCOUNT_AVAILABLE_KEY, true);
        editor.putStringSet(CREDENTIALS_SET_KEY, getStringSet());
        editor.putString(LAST_USED_EMAIL_KEY, credentials.email);
        editor.apply();
    }

    /** Update the User properties into the persistence store. */
    public void update(Activity activity, FirebaseUser user) {
        // Ensure that the given User's credentials are cached.  Ignore them if not.
        Credentials credentials = mCredentialsMap.get(user.getEmail());
        if (credentials == null || credentials.email == null)
            return;

        // Determine if there are any changes to the credentials. Update and persist them if so.
        String url = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
        boolean doUpdate = credentials.url != null && !credentials.url.equals(url);
        if (doUpdate) {
            credentials.url = url;
            persist(activity, credentials);
        }
    }
}
