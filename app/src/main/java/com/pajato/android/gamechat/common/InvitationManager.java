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

package com.pajato.android.gamechat.common;

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MemberManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum InvitationManager {
    instance;

    // Public instance variables.

    /** The map managing group invitations. */
    private Map<String, List<String>> mGroupInviteMap = new HashMap<>();

    // Public instance methods.

    /** Accept a group invite for a given account by updating both the group and the account. */
    public void acceptGroupInvite(@NonNull final Account account, @NonNull final String groupKey) {
        // Determine if the current account is already a member of the given group, has not been
        // invited or if there is no group to accept the invitation to. Abort if any are true.
        boolean isMember = account.joinList.contains(groupKey);
        boolean isInvited = hasGroupInvite(account, groupKey);
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        if (isMember || !isInvited || group == null) return;

        // The account holder has been invited to join the given group.  Do so by adding the group
        // key the account join list and create a copy of the account as a member of the group.
        account.joinList.add(groupKey);
        AccountManager.instance.updateAccount(account);
        Account member = new Account(account);
        member.groupKey = groupKey;
        String path = MemberManager.instance.getMembersPath(groupKey, member.id);
        DBUtils.instance.updateChildren(path, member.toMap());

        // Finally add the invited account to the accepting group's profile member list.
        group.memberList.add(member.id);
        path = GroupManager.instance.getGroupProfilePath(groupKey);
        DBUtils.instance.updateChildren(path, group.toMap());
    }

    /** Extend a group invite to a given account by registering both. */
    public void extendGroupInvite(@NonNull final Account account, @NonNull final String groupKey) {
        // Insert the account id into the list associated with the group key.
        List<String> memberList = mGroupInviteMap.get(groupKey);
        if (memberList == null) memberList = new ArrayList<>();
        memberList.add(account.id);
        mGroupInviteMap.put(groupKey, memberList);
    }

    // Private instance methods.

    /** Return TRUE iff the given group has an invitation registered for the given account owner. */
    private boolean hasGroupInvite(@NonNull final Account account, @NonNull final String groupKey) {
        // Ensure that there is a list of invites for the given group.
        List<String> invitedMembers = mGroupInviteMap.get(groupKey);
        if (invitedMembers == null) return false;

        // Ensure that the invited members list includes the account owner.
        if (!invitedMembers.contains(account.id)) return false;

        // Remove the invited account holder from the list and possibly the list from the map.
        invitedMembers.remove(account.id);
        if (invitedMembers.size() == 0) mGroupInviteMap.remove(groupKey);
        return true;
    }

}
