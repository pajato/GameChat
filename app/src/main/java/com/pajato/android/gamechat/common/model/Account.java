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

package com.pajato.android.gamechat.common.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Models a Firebase User (com.google.firebase.auth.FirebaseUser) for two contexts: as a GameChat
 * account and as a GameChat group member.  The Firebase UserInfo interface (implemented by
 * FirebaseUser) is the source for the base level information for an Account: identifier, email
 * address, full name and photo URL.
 *
 * In the GameChat account context, the class provides a list (named 'joinList') of group
 * identifiers for which the account holder is a member.
 *
 * In the GameChat group member context, the class provides a list ('joinList') of room identifers
 * which the member holder has joined.  In this context, the email address and the FirebaseUser
 * provided identifier are and will remain identical to the the values provided by the FirebaseUser
 * object they come from.  The full name (display name) and the photo url may be changed at will.
 *
 * In the member field declarations a 'final' qualifier is provided in commented form to illustrate
 * the intent since Firebase uses reflection to update values and a real 'final' qualifier would
 * have caused an exception when Firebase tried to do an update.
 *
 * @author Paul Michael Reilly
 */
@IgnoreExtraProperties public class Account {

    // Public constants.

    // The account types.

    // ? public final static int ADMIN = 1;

    /** A STANDARD account has a real email address and uses encryption on messaging. */
    public final static int STANDARD = 2;

    /** A PROTECTED account has a fake email address and does not use encrypted messaging. */
    public final static int PROTECTED = 3;

    // Public instance variables

    /** The account creation timestamp. */
    public /*final*/ long createTime;

    /** The account display name, usually something like "Fred C. Jones". */
    public /*final*/ String displayName;

    /** The account email. */
    public /*final*/ String email;

    /**
     * In an account context this is the Firebase push key for the unexposed Me Group, that private
     * group that each User has for notes and enjoying experiences (games) solo.
     *
     * In a member context this is the group push key in which the member is found.
     */
    public String groupKey;

    /** The account identifier, the backend push key. */
    public /*final*/ String id;

    /** A list of group or room push keys (depends on context) the account can join. */
    public final List<String> joinList = new ArrayList<>();

    /** The modification timestamp. */
    public long modTime;

    /** The account nickname.  Defaults to the first name. */
    public String nickname;

    /** The account provider id, a string like "google.com". */
    public /*final*/ String providerId;

    /** The account type. */
    public /*final*/ int type;

    /** The account photo URL (icon). */
    public String url;

    // Public constructors.

    /** Build a default no-arg instance. */
    public Account() {}

    /** Build a copy of an account. */
    public Account(final Account account) {
        createTime = new Date().getTime();
        displayName = account.displayName;
        nickname = account.nickname;
        email = account.email;
        id = account.id;
        modTime = 0;
        providerId = account.providerId;
        type = account.type;
        url = account.url;
    }

    // Public instance methods.

    /** Return a display name: either the nickname, the display name, the the email name. */
    @Exclude public String getDisplayName() {
        // Return the first non-null value of which the email address must not be null.
        if (displayName != null) return displayName;
        if (nickname != null) return nickname;
        return getPrefix(email, "@");
    }

    /** Return the nickname, the first name, the base email name, or a default, in that order. */
    @Exclude public String getNickName() {
        // Return the first non-null value of which the email address must not be null.
        if (nickname != null) return nickname;
        if (displayName != null) return getPrefix(displayName, " ");
        return getPrefix(email, "@");
    }

    /** Generate the map of data to persist into Firebase. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("createTime", createTime);
        result.put("displayName", displayName);
        result.put("email", email);
        result.put("groupKey", groupKey);
        result.put("joinList", joinList);
        result.put("id", id);
        result.put("modTime", modTime);
        result.put("nickname", nickname);
        result.put("providerId", providerId);
        result.put("type", type);
        result.put("url", url);

        return result;
    }

    /** Return the name part of the email address. */
    private String getPrefix(@NonNull final String value, final String splitterText) {
        // Split the value and return the first part.
        String[] parts = value.split(splitterText);
        if (parts.length > 0) return parts[0];
        return value;
    }
}