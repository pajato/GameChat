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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.common.model.JoinState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.model.Message.STANDARD;
import static com.pajato.android.gamechat.chat.model.Message.SYSTEM;
import static com.pajato.android.gamechat.chat.model.Room.RoomType.COMMON;
import static com.pajato.android.gamechat.chat.model.Room.RoomType.PRIVATE;
import static com.pajato.android.gamechat.chat.model.Room.RoomType.PUBLIC;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.roomsHeader;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.selectableMember;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.selectableRoom;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public enum JoinManager {
    instance;

    /** The repository for any messages needed. */
    private SparseArray<String> mMessageMap = new SparseArray<>();

    /** The logcat TAG. */
    private static final String TAG = JoinManager.class.getSimpleName();

    // Public instance methods.

    /** Initialize the join manager */
    public void init(final AppCompatActivity context) {
        mMessageMap.clear();
        mMessageMap.put(R.string.HasJoinedMessage, context.getString(R.string.HasJoinedMessage));
        mMessageMap.put(R.string.JoinMemberRoomMessage,
                context.getString(R.string.JoinMemberRoomMessage));
    }

    /** Join the current account holder to a room specified by a given item. */
    public void joinRoom(@NonNull final ListItem item) {
        // Ensure that the member object exists, aborting if not.
        Room room = null;
        Account member = MemberManager.instance.getMember(item.groupKey);
        if (member == null)
            return;

        // Case on the item type to handle joining an existing public room or a new private room.
        switch (item.type) {
            case experience:
            case selectUser:
            case selectableMember:
                // Create and persist the private chat room and get it's push key.
                room = joinMember(item.groupKey, item.memberKey);
                item.roomKey = room != null ? room.key : null;
                break;
            case selectableRoom:
                // Update and persist the room.
                room = joinRoom(item.groupKey, item.roomKey);
                item.roomKey = room != null ? room.key : null;
                break;
            default:
                break;
        }

        // Abort if the room wasn't returned or if the room has already been joined. Otherwise
        // update and persist the member join list.
        if (room == null || member.joinMap.keySet().contains(room.key))
            return;
        member.joinMap.put(room.key, new JoinState());
        String path = String.format(Locale.US, MemberManager.MEMBERS_PATH, item.groupKey, member.key);
        DBUtils.updateChildren(path, member.toMap());

        // Post a message to the room announcing the user has joined
        String format = mMessageMap.get(R.string.HasJoinedMessage);
        String text = String.format(Locale.getDefault(), format, member.name);
        MessageManager.instance.createMessage(text, STANDARD, member, room);
    }

    /** Return a set of explicit (public) and implicit (member) rooms the current User can join. */
    public List<ListItem> getListItemData(final Dispatcher dispatcher) {
        // Determine if there are any rooms to present (excludes joined rooms).
        List<ListItem> result = new ArrayList<>();
        result.addAll(getAvailableRooms());
        result.addAll(getAvailableMembers(dispatcher));
        if (result.size() > 0) return result;

        // There are no rooms to join.  Provide a header message to that effect.
        result.add(new ListItem(roomsHeader, R.string.NoJoinableRoomsHeaderText));
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
                if (room == null)
                    Log.e(TAG, "RoomManager roomMap doesn't contain roomKey " + roomKey);
                else if (room.type == PUBLIC || room.type == COMMON)
                    result.add(roomKey);
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
            if (member != null)
                result.addAll(member.joinMap.keySet());
        }
        return result;
    }

    // Private instance methods.

    /** Return a list of member room entries excluding joined ones and disallowing duplicates */
    public List<ListItem> getAvailableMembers(final Dispatcher dispatcher) {
        // Get a list of all members visible to the current User.
        List<ListItem> items = new ArrayList<>();
        List<String> groupList = GroupManager.instance.getGroups(dispatcher.groupKey);
        String currentAccountId = AccountManager.instance.getCurrentAccountId();
        for (String groupKey : groupList) {
            Map<String, Account> map = MemberManager.instance.memberMap.get(groupKey);
            for (Account member : map.values()) {
                if (member.key.equals(currentAccountId))
                    continue;

                // Don't add the member to the list of members if there is already a room which
                // is private, contains only two members and they are the current account and
                // the member we're considering now.
                boolean canAdd = true;
                for (Map.Entry<String, Room> entry : RoomManager.instance.roomMap.entrySet()) {
                    Room room = entry.getValue();
                    if (room.isMemberPrivateRoom(member.key, currentAccountId)) {
                        canAdd = false;
                        break;
                    }
                }
                // Check for duplicate member (member in > 1 group)
                for (ListItem anItem : items) {
                    if (anItem.memberKey.equals(member.key)) {
                        // modify the already added item and remember we can't add this one
                        anItem.addGroupKey(groupKey);
                        canAdd = false;
                    }
                }
                if (canAdd)
                    items.add(new ListItem(selectableMember, groupKey, member.key,
                            member.getNickName(), GroupManager.instance.getGroupName(groupKey),
                            member.url));
            }
        }

        // Generate a header for the members section based on the absence or presence of items.
        int noAvailableMembers = R.string.MembersNotAvailableHeaderText;
        int availableMembers = R.string.MembersAvailableHeaderText;
        int resourceId = items.size() == 0 ? noAvailableMembers : availableMembers;
        List<ListItem> result = new ArrayList<>();
        result.add(new ListItem(roomsHeader, resourceId));
        result.addAll(items);
        return result;
    }

    /** Return a possibly empty list of items consisting of unjoined rooms. */
    private List<ListItem> getAvailableRooms() {
        // Determine if there are groups to look at.  If not, return an empty result.
        List<ListItem> result = new ArrayList<>();
        List<String> groupList = GroupManager.instance.getGroups(null);

        // The group list is not empty.  Determine if there are any joinable rooms.
        List<String> joinedRoomList = JoinManager.instance.getJoinedRooms(groupList);
        List<String> joinableRoomList = JoinManager.instance.getJoinableRooms(groupList, joinedRoomList);
        if (joinableRoomList.size() > 0) {
            // There are joinable rooms.  Add a header item and the list of joinable rooms.
            result.add(new ListItem(roomsHeader, R.string.RoomsAvailableHeaderText));
            for (String roomKey : joinableRoomList) {
                Room room = RoomManager.instance.roomMap.get(roomKey);
                String name = room.name;
                String text = GroupManager.instance.getGroupName(room.groupKey);
                result.add(new ListItem(selectableRoom, room.groupKey, roomKey, name, text));
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

    /** Join the given member and the current User to a private room. */
    private Room joinMember(@NonNull final String groupKey, @NonNull final String memberKey) {
        // Ensure that a current account, member and group profile all exist. Abort if not,
        // otherwise determine if the room already exists.  Return it if so, otherwise obtain a
        // push key for the new room. To get started, work using the first item in the groupKeys
        // list (it may also be the only item).
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        Account account = AccountManager.instance.getCurrentAccount();
        Account member = MemberManager.instance.getMember(groupKey, memberKey);
        Room room = RoomManager.instance.getPrivateRoom(group, account, member);
        if (account == null || group == null || member == null)
            return null;
        if (room != null)
            return room;
        String path = String.format(Locale.US, RoomManager.ROOMS_PATH, groupKey);
        String roomKey = FirebaseDatabase.getInstance().getReference().child(path).push().getKey();
        member.joinMap.put(roomKey, new JoinState());
        MemberManager.instance.updateMember(member);

        // Build, update and persist a room object adding the two principals as members.
        long tStamp = new Date().getTime();
        room = new Room(roomKey, account.key, null, tStamp, groupKey, PRIVATE);
        room.addMember(account.key);
        room.addMember(memberKey);
        path = String.format(Locale.US, RoomManager.ROOM_PROFILE_PATH, groupKey, roomKey);
        DBUtils.updateChildren(path, room.toMap());

        // Update and persist the group profile.
        group.roomList.add(roomKey);
        path = String.format(Locale.US, GroupManager.GROUP_PROFILE_PATH, groupKey);
        DBUtils.updateChildren(path, group.toMap());

        // Update the room default message on the database.
        String format = mMessageMap.get(R.string.JoinMemberRoomMessage);
        String accountName = account.getDisplayName();
        String memberName = member.getDisplayName();
        String text = String.format(Locale.getDefault(), format, accountName, memberName);
        MessageManager.instance.createMessage(text, SYSTEM, account, room);
        return room;
    }

    /** Join the given room in the given group to the current User. */
    private Room joinRoom(@NonNull final String groupKey, @NonNull final String roomKey) {
        // Ensure that the signed in User is really signed in and has an id.  Abort if either is
        // not true.
        String id = AccountManager.instance.getCurrentAccountId();
        if (id == null) return null;

        // Update the member id list in the room profile.
        Room room = RoomManager.instance.getRoomProfile(roomKey);
        room.addMember(id);
        room.modTime = new Date().getTime();
        String path = String.format(Locale.US, RoomManager.ROOM_PROFILE_PATH, groupKey, roomKey);
        DBUtils.updateChildren(path, room.toMap());
        return room;
    }
}
