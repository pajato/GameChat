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

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an account data model tailored to Firebase.
 *
 * @author Paul Michael Reilly
 */
@IgnoreExtraProperties public class Account {

    // Public constants

    /** The account types. */
    public final static int STANDARD = 1;
    public final static int PROTECTED = 2;

    // Public instance variables

    /** The account creation timestamp. */
    public long createTime;

    /** The account display name, usually something like "Fred C. Jones". */
    public String displayName;

    /** The account email. */
    public String email;

    /** A list of group ids the account can access. */
    public List<String> groupIdList = new ArrayList<>();

    /** The account id, the backend push key. */
    public String id;

    /** The list of joined rooms providing the group and room push keys. */
    public List<String> joinedRoomList = new ArrayList<>();

    /** The modification timestamp. */
    public long modTime;

    /** The account provider id, a string like "google.com". */
    public String providerId;

    /** The account token, an access key supplied by the provider. */
    public String token;

    /** The account type. */
    public int type;

    /** The account icon, a URL. */
    public String url;

    // Public instance methods.

    /** Get a non-null display name using "Anonymous" if need be. */
    public String getDisplayName(final Account current, final String me, final String anonymous) {
        // Determine if the given account is this account and return the me value if it has been
        // provided.
        if (current != null && current.id.equals(id) && me != null) return me;

        // Determine if this account has a display name or should use the given default.
        if (displayName == null && anonymous != null) return anonymous;

        // Otherwise use this display name.
        return displayName;
    }

    /** Generate the map of data to persist into Firebase. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("createTime", createTime);
        result.put("displayName", displayName);
        result.put("email", email);
        result.put("groupIdList", groupIdList);
        result.put("id", id);
        result.put("joinedRoomList", joinedRoomList);
        result.put("modTime", modTime);
        result.put("providerId", providerId);
        result.put("token", token);
        result.put("type", type);
        result.put("url", url);

        return result;
    }
}
