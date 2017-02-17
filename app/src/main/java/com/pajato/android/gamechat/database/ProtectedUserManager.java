/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
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
package com.pajato.android.gamechat.database;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.handler.ProtectedUserChangeHandler;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ProtectedUserChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.protectedUserList;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.resourceHeader;

/**
 * Manages the protected user accounts associated with the current user account.
 */

public enum ProtectedUserManager {
    instance;

    /** The logcat tag. */
    private static final String TAG = ProtectedUserManager.class.getSimpleName();

    /** The protected users associated with the current account */
    private List<Account> mProtectedUserAccounts = new ArrayList<>();

    /** The account change handler base name */
    private static final String PROTECTED_USER_CHANGE_HANDLER = "protectedUserChangeHandler";

    /** The repository for any messages needed. */
    private SparseArray<String> mMessageMap = new SparseArray<>();

    /** Delete a protected user */
    public void deleteProtectedUser(final String accountId) {
        Log.i(TAG, "deleteProtectedUser with account id: " + accountId);
        Account parentAccount = AccountManager.instance.getCurrentAccount();
        if (!parentAccount.protectedUsers.contains(accountId))
            return;

        Account protectedAccount = getProtectedUserAccount(accountId);
        if (protectedAccount == null)
            return;

        // Remove protected user account from all of its groups and rooms. If the protected user
        // owns a room, the room must be deleted. Otherwise, just remove the account from the room
        // member list.
        for (String groupKey : protectedAccount.joinMap.keySet()) {
            Account member = MemberManager.instance.getMember(groupKey, accountId);
            if (member != null) {
                for (String roomKey : member.joinMap.keySet()) {
                    Room room = RoomManager.instance.getRoomProfile(roomKey);
                    if (room.owner.equals(accountId))
                        RoomManager.instance.deleteRoom(room);
                    else {
                        List<String> roomMembers = room.getMemberIdList();
                        roomMembers.remove(accountId);
                        room.setMemberIdList(roomMembers);
                        RoomManager.instance.updateRoomProfile(room);
                    }
                }
            }
            // Delete member from the group.
            MemberManager.instance.removeMember(groupKey, accountId);
            Group group = GroupManager.instance.getGroupProfile(groupKey);
            if (group != null)
                group.memberList.remove(protectedAccount.id);
        }

        parentAccount.protectedUsers.remove(accountId);
        removeWatcher(accountId);
        AccountManager.instance.updateAccount(parentAccount);
    }

    /** Get the account for a specified protected user key */
    public Account getProtectedUserAccount(String accountId) {
        for (Account account : mProtectedUserAccounts) {
            if (account.id.equals(accountId)) {
                return account;
            }
        }
        return null;
    }

    /** Handle initialization by obtaining potentially required messages. */
    public void init(final AppCompatActivity context) {
        // Save any messages necessary later
        mMessageMap.clear();
        mMessageMap.put(R.string.HasDepartedMessage, context.getString(R.string.HasDepartedMessage));
    }

    /** Promote a protected user so that is is no longer protected (restricted) */
    public void promoteUser(final String accountId) {
        Log.i(TAG, "promoteUser with account id: " + accountId);
        Account parentAccount = AccountManager.instance.getCurrentAccount();
        if (parentAccount.protectedUsers.contains(accountId)) {
            Account protectedUser = ProtectedUserManager.instance.getProtectedUserAccount(accountId);
            if (protectedUser != null) {
                protectedUser.chaperone = "";
                AccountManager.instance.updateAccount(protectedUser);
                ProtectedUserManager.instance.mProtectedUserAccounts.remove(protectedUser);
                AppEventManager.instance.post(new ProtectedUserChangeEvent(protectedUser));
            }
            parentAccount.protectedUsers.remove(accountId);
            AccountManager.instance.updateAccount(parentAccount);
        }
    }

    /** Return a list of protected user account items */
    public List<ListItem> getProtectedUsersItemData() {
        List<ListItem> result = new ArrayList<>();
        for (Account pUser : mProtectedUserAccounts) {
            String displayName = pUser.getDisplayName();
            result.add(new ListItem(protectedUserList, "", pUser.id, displayName, pUser.email,
                    pUser.url));
        }
        if (result.size() > 0)
            return result;
        // There are no protected users, so add a header to that effect. */
        result.add(new ListItem(resourceHeader, R.string.NoProtectedUsersHeaderText));
        return result;
    }

    /** Keep track of protected users that belong to the current account */
    @Subscribe public void onProtectedUserChange(ProtectedUserChangeEvent event) {
        if (!AccountManager.instance.getCurrentAccount().protectedUsers.contains(event.account.id))
            return;
        if (!event.account.chaperone.equals(AccountManager.instance.getCurrentAccountId()))
            return;
        mProtectedUserAccounts.add(event.account);
    }

    /**
     * Setup a database listener for the specified account. This is expected to be used for watching
     * protected users so this should NOT be called for the current user account.
     */
    public void setWatcher(final String accountKey) {
        String path = AccountManager.instance.getAccountPath(accountKey);
        String name = DBUtils.getHandlerName(PROTECTED_USER_CHANGE_HANDLER, accountKey);
        if (DatabaseRegistrar.instance.isRegistered(name))
            return;
        DatabaseRegistrar.instance.registerHandler(new ProtectedUserChangeHandler(name, path));
    }

    /** Remove the watcher on the specified account */
    public void removeWatcher(final String accountKey) {
        String name = DBUtils.getHandlerName(PROTECTED_USER_CHANGE_HANDLER, accountKey);
        if (DatabaseRegistrar.instance.isRegistered(name))
            DatabaseRegistrar.instance.unregisterHandler(name);
    }

}
