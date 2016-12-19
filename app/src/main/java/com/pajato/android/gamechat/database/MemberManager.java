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

package com.pajato.android.gamechat.database;

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.chat.model.Account;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.MemberChangeHandler;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.MemberChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum MemberManager {
    instance;

    // Public class constants.

    // Database paths, often used as format strings.
    public static final String MEMBERS_PATH = GroupManager.GROUPS_PATH + "%s/members/%s";

    // Private class constants.

    /** The member change handler base name. */
    private static final String MEMBER_CHANGE_HANDLER = "memberChangeHandler";

    // Public instance variables.

    /** The map associating a group with the members in that group. */
    public Map<String, Map<String, Account>> memberMap = new HashMap<>();

    // Public instance methods.

    /** Persist the given member to the database. */
    public void createMember(final Account member) {
        member.createTime = new Date().getTime();
        String path = String.format(Locale.US, MEMBERS_PATH, member.groupKey, member.id);
        DBUtils.instance.updateChildren(path, member.toMap());
    }

    /** Return null or a group member using the current account holder's id and given group key. */
    public Account getMember(@NonNull final String groupKey) {
        // Determine if there is no curent account (should be impossible.)  Abort if so.
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null) return null;

        // Determine if there is an expected member account in the given group.  Abort if not.
        // Return the account if so.
        Map<String, Account> map = memberMap.get(groupKey);
        if (memberMap == null) return null;
        return map.get(account.id);
    }

    /** Return null or a group member using the current account holder's id and given group key. */
    public Account getMember(@NonNull final String groupKey, @NonNull final String memberKey) {
        // Determine if there is an expected member account in the given group.  Abort if not.
        // Return the account if so.
        Map<String, Account> map = memberMap.get(groupKey);
        if (memberMap == null) return null;
        return map.get(memberKey);
    }

    /** Retrun the path to the group members for the given group and member keys. */
    public String getMembersPath(final String groupKey, final String memberKey) {
        return String.format(Locale.US, MEMBERS_PATH, groupKey, memberKey);
    }

    /** Handle a account change event by setting up or clearing variables. */
    @Subscribe public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.  If so, do nothing, otherwise clear the
        // message list for the logged out User.
        if (event.account != null) return;
        memberMap.clear();
    }

    /** Handle changes to the list of joined rooms by capturing all group and room profiles. */
    @Subscribe public void onMemberChange(@NonNull final MemberChangeEvent event) {
        // Update the member in the local caches, creating the caches as necessary.
        Map<String, Account> map = memberMap.get(event.member.groupKey);
        if (map == null) map = new HashMap<>();
        map.put(event.member.id, event.member);
        memberMap.put(event.member.groupKey, map);

        // Determine if the payload is for the current account holder.  If so, set a message watcher
        // on the joined rooms.
        if (event.member.id.equals(AccountManager.instance.getCurrentAccountId()))
            for (String roomKey : event.member.joinList)
                MessageManager.instance.setMessageWatcher(event.member.groupKey, roomKey);
    }

    /** Setup a listener for member changes in the given account. */
    public void setMemberWatcher(final String groupKey, final String memberKey) {
        // Obtain a room and set watchers on all the experience profiles in that room.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String tag = String.format(Locale.US, "%s,%s", groupKey, memberKey);
        String name = DBUtils.instance.getHandlerName(MEMBER_CHANGE_HANDLER, tag);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        String path = getMembersPath(groupKey, memberKey);
        DatabaseEventHandler handler;
        handler = new MemberChangeHandler(name, path, memberKey, groupKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Update the given group profile on the database. */
    public void updateMember(final Account member) {
        String path = String.format(Locale.US, MEMBERS_PATH, member.groupKey, member.id);
        member.modTime = new Date().getTime();
        DBUtils.instance.updateChildren(path, member.toMap());
    }
}
