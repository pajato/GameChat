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

import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.database.DatabaseManager;

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
    public void acceptGroupInvite(@NonNull final Account account, @NonNull Group group,
                                  @NonNull final String groupKey) {
        // Ensure that the account is not already a member of the given group and that there is an
        // invitation to join extended.
        boolean isMember = account.groupIdList.contains(groupKey);
        boolean isInvited = hasGroupInvite(account, groupKey);
        if (isMember || !isInvited) return;

        // Update the group member list, the account group list, and the joined room list on the
        // database.
        group.memberIdList.add(account.id);
        DatabaseManager.instance.updateGroup(group, groupKey);
        account.groupIdList.add(groupKey);
        DatabaseManager.instance.appendDefaultJoinedRoomEntry(account, group);
        DatabaseManager.instance.updateAccount(account);
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
