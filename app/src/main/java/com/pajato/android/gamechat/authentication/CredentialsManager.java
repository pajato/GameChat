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

package com.pajato.android.gamechat.authentication;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.pajato.android.gamechat.preferences.Preference;
import com.pajato.android.gamechat.preferences.PreferencesProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.database.AccountManager.ACCOUNT_AVAILABLE_KEY;

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
    public static final String CREDENTIALS_SET_KEY = "credentialsSetKey";

    // Private class constants.

    ///** The logcat tag. */
    //private static final String TAG = CredentialsManager.class.getSimpleName();

    // Private instance variables.

    /** The map associating an email address with a provider and token/password. */
    private Map<String, Credentials> mCredentialsMap = new HashMap<>();


    /** The list tracking the most recently used email keys and their positions for the icons. */
    private List<String> mEmailList = new LinkedList<>();

    /** The current preferences provider. */
    private PreferencesProvider mPrefs;

    // Public instance methods

    /** Return the authentication credential for the given provider type and token. */
    public AuthCredential getAuthCredential(@NonNull final String email) {
        Credentials credentials = mCredentialsMap.get(email);
        if (credentials == null)
            return null;

        // Return the appropriate authentication credential.
        switch (credentials.provider) {
            case EmailAuthProvider.PROVIDER_ID:
                // Provide a Beta hack to permit email users to leverage "switch user".
                return EmailAuthProvider.getCredential(email, "gamechat");
            case GoogleAuthProvider.PROVIDER_ID:
                return GoogleAuthProvider.getCredential(credentials.token, null);
            case FacebookAuthProvider.PROVIDER_ID:
                return FacebookAuthProvider.getCredential(credentials.token);
            case TwitterAuthProvider.PROVIDER_ID:
                return TwitterAuthProvider.getCredential(credentials.token, credentials.secret);
            default: return null;
        }
    }

    /** Return the credentials map. */
    public Map<String, Credentials> getMap() {
        return mCredentialsMap;
    }

    /** Return the list of emails stored. */
    public List<String> getEmailList() {
        return mEmailList;
    }

    /** Build the object by reading in the saved credentials. */
    public void init(final PreferencesProvider prefs) {
        mPrefs = prefs;
        mCredentialsMap = new HashMap<>();
        mEmailList = new LinkedList<>();
        Set<String> stringSet = prefs.getStringSet(CREDENTIALS_SET_KEY, null);
        if (stringSet != null) {
            for (String value : stringSet)
                if (isValid(value))
                    cacheValue(value);
        }
    }

    /** Persist the given IDP credentials by updating the User persistence store. */
    public void persist(@NonNull final IdpResponse response) {
        // Persist the login information that will be necessary to display the switch user menu and
        // will enable re-authentication.
        final String email = response.getEmail();
        final String provider = response.getProviderType();
        final String token = response.getIdpToken();
        final String secret = response.getIdpSecret();
        persist(new Credentials(provider, email, token, secret));
    }

    /** Remove a credential and persist the updated credentials to our preferences provider. */
    public void removeCredentialAndPersist(@NonNull final String email) {
        mCredentialsMap.remove(email);
        mEmailList.remove(email);

        List<Preference> list = new ArrayList<>();
        list.add(new Preference(ACCOUNT_AVAILABLE_KEY, true));
        list.add(new Preference(CREDENTIALS_SET_KEY, getStringSet()));
        mPrefs.persist(list);
    }

    /** Update the User properties into the persistence store. */
    public void update(@NonNull final String email, final Uri uri) {
        // Ensure that the given User's credentials are cached.  Ignore them if not.
        Credentials credentials = mCredentialsMap.get(email);
        String url = uri != null ? uri.toString() : null;
        if (credentials == null)
            return;

        // Determine if there are any changes to the photo URL. Update and persist the credentials
        // if so.
        if ((credentials.url == null && url != null) || (credentials.url != null && url == null)
                || (credentials.url != null && !credentials.url.equals(url))) {
            credentials.url = url;
            persist(credentials);
        }
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

    /** Cache the given, tag encoded value, */
    private void cacheValue(final String value) {
        Credentials credentials = new Credentials(value);
        mCredentialsMap.put(credentials.email, credentials);
        mEmailList.add(0, credentials.email);
    }

    /** Return TRUE iff the given credentials are not complete and sensible. */
    private boolean isValid(@NonNull final String value) {
        Credentials credentials = new Credentials(value);
        return credentials.provider != null && !credentials.provider.isEmpty();
    }

    /** Save the given credentials to the persistence store (shared preferences). */
    private void persist(final Credentials credentials) {
        // Ensure that the credential provider property exists. Abort if not.
        if (credentials.provider == null)
            return;

        // Persist and cache the credentials.
        mCredentialsMap.put(credentials.email, credentials);
        while (mEmailList.contains(credentials.email))
            mEmailList.remove(credentials.email);
        mEmailList.add(0, credentials.email);

        List<Preference> list = new ArrayList<>();
        list.add(new Preference(ACCOUNT_AVAILABLE_KEY, true));
        list.add(new Preference(CREDENTIALS_SET_KEY, getStringSet()));
        mPrefs.persist(list);
    }
}
