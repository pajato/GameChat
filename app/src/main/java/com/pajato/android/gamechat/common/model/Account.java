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
import com.pajato.android.gamechat.database.model.Base;

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
 * In the GameChat account context, the class provides a map (named 'joinMap') of group
 * identifiers for which the account holder is a member.
 *
 * In the GameChat group member context, the class provides a list ('joinMap') of room identifiers
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
public class Account extends Base {

    // Public instance variables

    /** The account email. */
    public /*final*/ String email;

    /** The key to the account's chaperone, if applicable. */
    public /*final*/ String chaperone;

    /** The database keys to any accounts that this account is the chaperone of. */
    public final List<String> protectedUsers = new ArrayList<>();

    /**
     * In an account context this is the Firebase push key for the unexposed Me Group, that private
     * group that each User has for notes and enjoying experiences (games) solo.
     *
     * In a member context this is the group push key in which the member is found.
     */
    public String groupKey;

    /**
     * In an account context, this is a map of group push keys for which the User is a member.  In a
     * member context, this is a map of room push keys the User has joined. The value is one of:
     * "inactive", "chat", "experience", "active" where "inactive" means the app is not in the
     * foreground or the User is not in the room at all; the other three choices indicated that the
     * app is running in the foreground, "chat" indicates presence in the chat room, "exp" indicates
     * presence with a game, and "active" indicates presence in both the chat room and with an
     * experience.
     */
    public final Map<String, JoinState> joinMap = new HashMap<>();

    /** The account nickname.  Defaults to the first name. */
    public String nickname;

    /** The account type. */
    public String type;

    /** The account photo URL (icon). */
    public String url;

    // Public constructors.

    /** Build a default no-arg instance. */
    public Account() {
        super();
    }

    /** Build a copy of an account. */
    public Account(final Account account) {
        super(account.key, account.key, account.name, new Date().getTime());
        nickname = account.nickname;
        email = account.email;
        chaperone = account.chaperone;
        type = account.type;
        url = account.url;
    }

    // Public instance methods.

    /** Return a display name: either the nickname, the display name, the the email name. */
    @Exclude public String getDisplayName() {
        // Return the first non-null value of which the email address must not be null.
        if (name != null)
            return name;
        if (nickname != null)
            return nickname;
        return getPrefix(email);
    }

    /** Return the nickname, the first name, the base email name, or a default, in that order. */
    @Exclude public String getNickName() {
        // Return the first non-null value of which the email address must not be null.
        if (nickname != null)
            return nickname;
        if (name != null)
            return name;
        return getPrefix(email);
    }

    /** Generate the map of data to persist into Firebase. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("email", email);
        result.put("chaperone", chaperone);
        result.put("protectedUsers", protectedUsers);
        result.put("groupKey", groupKey);
        result.put("joinMap", joinMap);
        result.put("nickname", nickname);
        result.put("type", type);
        result.put("url", url);

        return result;
    }

    /** Return the name part of the email address. */
    private String getPrefix(@NonNull final String value) {
        // Split the value and return the first part.
        String[] parts = value.split("@");
        if (parts.length > 0) return parts[0];
        return value;
    }
}
