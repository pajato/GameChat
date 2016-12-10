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

package com.pajato.android.gamechat.chat.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.database.DatabaseListManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provide a Firebase model class for representing a chat room. */
@IgnoreExtraProperties public class Room {

    // The room types.

    /** A room no one can join. */
    public final static int ME = 0;

    /** A room for two or more members by implicit or explicit invitation. */
    public final static int PRIVATE = 1;

    /** A room in which any User can join. */
    public final static int PUBLIC = 2;

    /** The creation timestamp. */
    public long createTime;

    /** The list of experience profiles.  One exists for each experience. */
    //@Exclude public List<ExpProfile> expProfileList;

    /** The parent group push key. */
    public String groupKey;

    /** The room push key. */
    public String key;

    /** The room member identifiers. These are the Users joined to the room. */
    public List<String> memberIdList = new ArrayList<>();

    /** The last modification timestamp. */
    public long modTime;

    /** The room name. */
    public String name;

    /** The room owner/creator, an account identifier. */
    public String owner;

    /** The room type, one of "public", "private" or "me". */
    public int type;

    /** Build an empty args constructor for the database. */
    public Room() {}

    /** Build a default room. */
    public Room(final String key, final String owner, final String name, final String groupKey,
                final long createTime, final long modTime, final int type) {
        this.createTime = createTime;
        this.groupKey = groupKey;
        this.key = key;
        this.modTime = modTime;
        this.name = name;
        this.owner = owner;
        this.type = type;
    }

    /** Provide a default map for a Firebase create/update. */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("createTime", createTime);
        result.put("groupKey", groupKey);
        result.put("key", key);
        result.put("name", name);
        result.put("owner", owner);
        result.put("memberIdList", memberIdList);
        result.put("modTime", modTime);
        result.put("type", type);
        return result;
    }

    /** Return a stylized version of the name. */
    @Exclude public String getName() {
        // Case on the room type.
        Account account = AccountManager.instance.getCurrentAccount();
        switch (type) {
            case ME:
                return account.getDisplayName();
            case PRIVATE:
                return memberNames(account);
            default:
                return name;
        }
    }

    // Private instance methods.

    /** Return a list of comma separated member names excluding the account holder's name. */
    private String memberNames(@NonNull final Account account) {
        StringBuilder result = new StringBuilder();
        for (String key : memberIdList) {
            // Determine if this member is the current User, in which case just continue.
            Account member = DatabaseListManager.instance.getGroupMember(groupKey, key);
            if (key.equals(account.id) || member == null) continue;

            // Add the member's, display name to the list.
            if (result.length() > 0) result.append(", ");
            result.append(member.getDisplayName());
        }
        return result.toString();
    }
}
