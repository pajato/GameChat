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

import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.adapter.RoomsHeaderItem;
import com.pajato.android.gamechat.chat.adapter.SelectableMemberItem;
import com.pajato.android.gamechat.chat.adapter.SelectableRoomItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.model.Message.SYSTEM;
import static com.pajato.android.gamechat.chat.model.Room.PRIVATE;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum JoinManager {
    instance;

    // Public instance methods.

    /** Join the curernt account holder to a room specified by a given item. */
    public void joinRoom(@NonNull final ChatListItem item) {
        // Ensure that the member object exists, aborting if not.
        String roomKey = null;
        Account member = MemberManager.instance.getMember(item.groupKey);
        if (member == null) return;

        // Case on the item type to handle joing an existing public room or a freshly minted private
        // room.
        switch (item.type) {
            case ChatListItem.SELECTABLE_MEMBER_ITEM_TYPE:
                // Create and persist the private chat room and get it's push key.
                roomKey = joinMember(item.groupKey, item.key);
                break;
            case ChatListItem.SELECTABLE_ROOM_ITEM_TYPE:
                // Update and persist the room.
                roomKey = joinRoom(item.groupKey, item.key);
                break;
            default:
                break;
        }

        // Ensure that the roomKey was returned (abort if not), update and persist the member join
        // list.
        if (roomKey == null) return;
        member.joinList.add(roomKey);
        String path = String.format(Locale.US, MemberManager.MEMBERS_PATH, item.groupKey, member.id);
        DBUtils.instance.updateChildren(path, member.toMap());
    }

    /** Return a set of room the current user might choose to join. */
    public List<ChatListItem> getJoinableRoomsListData(final ChatListItem item) {
        // Determine if there are any rooms to present (excludes joined rooms).
        List<ChatListItem> result = new ArrayList<>();
        result.addAll(getAvailableRooms(item));
        result.addAll(getAvailableMembers(item));
        if (result.size() > 0) return result;

        // There are no rooms to join.  Provide a header message to that effect.
        result.add(new ChatListItem(new RoomsHeaderItem(R.string.NoJoinableRoomsHeaderText)));
        return result;
    }

    /** Return a list of joinable rooms: those that are public and not already joined. */
    public List<String> getJoinableRooms(@NonNull final List<String> groupList,
                                          @NonNull final List<String> joinedRoomList) {
        List<String> result = new ArrayList<>();
        for (String groupKey : groupList)
            // For each group, include each unjoined, public room in the result.
            for (String roomKey : getRoomList(groupKey)) {
                // Determine if this room is joined.  If so, continue.
                if (joinedRoomList.contains(roomKey)) continue;

                // Determine if this room is public.  If so, accumulate it to the result.
                Room room = RoomManager.instance.roomMap.get(roomKey);
                if (room.type == Room.PUBLIC) result.add(roomKey);
            }
        return result;
    }

    /** Return a list of joined room push keys for the current User. */
    public List<String> getJoinedRooms(final List<String> groups) {
        List<String> result = new ArrayList<>();
        for (String groupKey : groups) {
            Map<String, Account> map = MemberManager.instance.memberMap.get(groupKey);
            String id = AccountManager.instance.getCurrentAccountId();
            Account member = map != null && id != null ? map.get(id) : null;
            if (member != null) result.addAll(member.joinList);
        }
        return result;
    }

    // Private instance methods.

    /** Return a list of available room entries from the given group excluding joined ones. */
    public List<ChatListItem> getAvailableMembers(final ChatListItem item) {
        // Get a list of all members visible to the current User.
        List<ChatListItem> result = new ArrayList<>();
        List<ChatListItem> items = new ArrayList<>();
        List<String> groupList = GroupManager.instance.getGroups(item);
        for (String groupKey : groupList) {
            Map<String, Account> map = MemberManager.instance.memberMap.get(groupKey);
            for (Account member : map.values()) {
                if (member.id.equals(AccountManager.instance.getCurrentAccountId())) continue;
                items.add(new ChatListItem(new SelectableMemberItem(groupKey, member)));
            }
        }

        // Generate a header for the members section based on the absence or presence of items.
        int noAvailableMembers = R.string.MembersNotAvailableHeaderText;
        int availableMembers = R.string.MembersAvailableHeaderText;
        int resourceId = items.size() == 0 ? noAvailableMembers : availableMembers;
        result.add(new ChatListItem(new RoomsHeaderItem(resourceId)));
        result.addAll(items);
        return result;
    }

    /** Return a possibly empty list of items consisting of unjoined rooms. */
    private List<ChatListItem> getAvailableRooms(final ChatListItem item) {
        // Determine if there are groups to look at.  If not, return an empty result.
        List<ChatListItem> result = new ArrayList<>();
        List<String> groupList = GroupManager.instance.getGroups(item);

        // The group list is not empty.  Determine if there are any joinable rooms.
        List<String> joinedRoomList = JoinManager.instance.getJoinedRooms(groupList);
        List<String> joinableRoomList = JoinManager.instance.getJoinableRooms(groupList, joinedRoomList);
        if (joinableRoomList.size() > 0) {
            // There are joinable rooms.  Add a header item and the list of joinable rooms.
            result.add(new ChatListItem(new RoomsHeaderItem(R.string.RoomsAvailableHeaderText)));
            for (String roomKey : joinableRoomList) {
                Room room = RoomManager.instance.roomMap.get(roomKey);
                result.add(new ChatListItem(new SelectableRoomItem(room.groupKey, roomKey)));
            }
        }
        return result;
    }

    /** Return a list of candidate rooms in a given group. */
    private List<String> getRoomList(@NonNull final String groupKey) {
        List<String> result = new ArrayList<>();
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        if (group != null) result.addAll(group.roomList);
        return result;
    }

    /** Join the given member and the curernt User to a private room. */
    private String joinMember(@NonNull final String groupKey, @NonNull final String memberKey) {
        // Ensure that a current account, member and group profile all exist (abort if not) and
        // obtain a push key for the new room.
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        Account account = AccountManager.instance.getCurrentAccount();
        Account member = MemberManager.instance.getMember(groupKey, memberKey);
        if (account == null || group == null || member == null) return null;
        String path = String.format(Locale.US, RoomManager.ROOMS_PATH, groupKey);
        String roomKey = FirebaseDatabase.getInstance().getReference().child(path).push().getKey();

        // Build, update and persist a room object adding the two principals as members.
        long tstamp = new Date().getTime();
        Room room = new Room(roomKey, account.id, null, groupKey, tstamp, 0, PRIVATE);
        room.memberIdList.add(account.id);
        room.memberIdList.add(memberKey);
        path = String.format(Locale.US, RoomManager.ROOM_PROFILE_PATH, groupKey, roomKey);
        DBUtils.instance.updateChildren(path, room.toMap());

        // Update and persist the group profile.
        group.roomList.add(roomKey);
        path = String.format(Locale.US, GroupManager.GROUP_PROFILE_PATH, groupKey);
        DBUtils.instance.updateChildren(path, group.toMap());

        // Update the "me" room default message on the database.
        String format = "We have created a room for %s and %s to share private messages.";
        String accountName = account.getDisplayName();
        String memberName = member.getDisplayName();
        String text = String.format(Locale.getDefault(), format, accountName, memberName);
        MessageManager.instance.createMessage(text, SYSTEM, account, room);
        return roomKey;
    }

    /** Join the given room in the given group to the current User. */
    private String joinRoom(@NonNull final String groupKey, @NonNull final String roomKey) {
        // Ensuure that the signed in User is really signed in and has an id.  Abort if either is
        // not true.
        String id = AccountManager.instance.getCurrentAccountId();
        if (id == null) return null;

        // Update the member id list in the room profile.
        Room room = RoomManager.instance.getRoomProfile(roomKey);
        room.memberIdList.add(id);
        room.modTime = new Date().getTime();
        String path = String.format(Locale.US, RoomManager.ROOM_PROFILE_PATH, groupKey, roomKey);
        DBUtils.instance.updateChildren(path, room.toMap());
        return roomKey;
    }
}
