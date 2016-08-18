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
@IgnoreExtraProperties
public class Account {

    // Public instance variables

    /** The account id, the backend push key. */
    public String accountId;

    /** The account email. */
    public String accountEmail;

    /** The account icon, a URL. */
    public String accountUrl;

    /** The account display name, usually something like "Fred C. Jones". */
    public String displayName;

    /** The account token, an access key supplied by the provider. */
    public String token;

    /** The account provider name, a string like "Facebook". */
    public String providerName;

    /** The account provider id, a string like "google.com". */
    public String providerId;

    /** A list of group ids the account can access. */
    public List<String> groupIdList = new ArrayList<>();

    // Public instance methods.

    /** Generate the map of data to persist into Firebase. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("accountId", accountId);
        result.put("accountEmail", accountEmail);
        result.put("accountUrl", accountUrl);
        result.put("displayName", displayName);
        result.put("providerName", providerName);
        result.put("providerId", providerId);
        result.put("groupIdList", groupIdList);

        return result;
    }
}
