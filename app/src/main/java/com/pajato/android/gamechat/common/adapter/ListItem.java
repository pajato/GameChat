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

package com.pajato.android.gamechat.common.adapter;

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.PlayModeManager.PlayModeType;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;

import java.util.Locale;

import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.contact;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.experience;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.group;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.inviteGroup;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.inviteRoom;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.message;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.room;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.roomsHeader;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.selectUser;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.selectableMember;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.selectableRoom;

/**
 * Provides a POJO to encapsulate a number of recycler view list items.
 * <p><ul>
 * <li> a date label item showing periods like now, yesterday, last week, this year, etc,
 * <li> a group item encapsulating an icon, the group name and the the group's room names,
 * <li> a room item encapsulating an icon, the room name and some members of the room,
 * <li> a group selection item,
 * <li> a room selection item,
 * <li> a User (aka member) selection item,
 * <li> and likely other to be defined in the future.
 * </ul><p>
 * @author Paul Michael Reilly
 */
public class ListItem {

    // Public enums

    /** Provide an enumeration of the date header limit and resource data used in handling lists. */
    public enum DateHeaderType {
        now (60000L, R.string.now),
        recent (3600000L, R.string.recent),
        today (3600000L * 24, R.string.today),
        yesterday (3600000L * 48, R.string.yesterday),
        thisWeek (3600000L * 24 * 7, R.string.thisWeek),
        lastWeek (3600000L * 24 * 14, R.string.lastWeek),
        thisMonth (3600000L * 24 * 30, R.string.thisMonth),
        lastMonth (3600000L * 24 * 60, R.string.lastMonth),
        thisYear (3600000L * 24 * 365, R.string.thisYear),
        lastYear (3600000L * 24 * 365 * 2, R.string.lastYear),
        old (-1, R.string.old);

        public long limit;
        public int resId;

        DateHeaderType(final long limit, final int resId) {
            this.limit = limit;
            this.resId = resId;
        }
    }

    /** Identifies the types of list items supported. */
    public enum ItemType {
        contactHeader,
        contact,
        date,
        experience,
        group,
        message,
        room,
        roomsHeader,
        roomList,
        selectUser,
        selectRoom,
        selectableMember,
        selectableRoom,
        inviteGroup,
        inviteRoom,
        inviteCommonRoom
    }

    // Public instance variables.

    /** The number of new messages or experiences in a group or a room. */
    public int count;

    /** The item email address, possibly null, used for contact items. */
    public String email;

    /** The item enabled state */
    public boolean enabled;

    /** The group (push) key, possibly null, used for many list items (groups, rooms, messages) */
    public String groupKey;

    /** The item (push) key, possibly null, either a room, member or experience key. */
    public String key;

    /** The item name, possibly null, used for all items. */
    public String name;

    /** The item name resource identifier. */
    int nameResourceId;

    /** The experience play mode type for this item. */
    private PlayModeType playMode;

    /** The contact phone number. */
    private String phone;

    /** The room (push) key, possibly null, used for accessing an experience. */
    public String roomKey;

    /** The item selection state. */
    public boolean selected;

    /** The list of rooms or groups with messages to show, or the text of a message. */
    public String text;

    /** The item type, always non-null. */
    public ItemType type;

    /** The URL for the item, possibly null, used for icons with contacts and chat list items. */
    public String url;

    // Private instance variables.

    /** A description of the item. */
    private String mDesc;

    // Public constructors.

    /** Build an item instance for the given group. */
    public ListItem(final String groupKey, final String name, final int count, final String text) {
        // Set the type and populate the member fields for the given group.
        type = group;
        this.groupKey = groupKey;
        this.name = name;
        this.count = count;
        this.text = text;
    }

    /** Build a header instance for a given resource id. */
    public ListItem(final ItemType type, int resId) {
        this.type = type;
        nameResourceId = resId;
    }

    /** Build an instance for a contact item. */
    public ListItem(final String name, final String email, final String phone, final String url) {
        type = contact;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.url = url;
    }

    /** Build an instance for a given room list item. */
    public ListItem(final ExperienceItem item, PlayModeType playMode) {
        type = experience;
        this.playMode = playMode;
        groupKey = item.groupKey;
        roomKey = item.roomKey;
        key = item.key;
    }

    /** Build an instance for a given room list item. */
    public ListItem(final MessageItem item) {
        type = message;
        groupKey = item.groupKey;
        key = item.roomKey;
        name = item.name;
        count = 0;
        text = item.text;
        url = item.url;
        String format = "Message item with name {%s}, key: {%s}, count: {%s} and text {%s}.";
        mDesc = String.format(Locale.US, format, name, key, count, text);
    }

    /** Build an instance for a given room list item. */
    public ListItem(final RoomItem item) {
        type = room;
        groupKey = item.groupKey;
        key = item.roomKey;
        name = item.name;
        count = item.count;
        text = item.text;
        String format = "Room item with name {%s}, key: {%s}, count: {%s} and text {%s}.";
        mDesc = String.format(Locale.US, format, name, key, count, text);
    }

    /** Build an instance for a given available rooms header item. */
    public ListItem(final ResourceHeaderItem item) {
        type = roomsHeader;
        nameResourceId = item.getNameResourceId();
        mDesc = String.format(Locale.US, "Resource header with id: {%d}.", nameResourceId);
    }

    /** Build an instance for a given available rooms header item. */
    public ListItem(final RoomsHeaderItem item) {
        type = roomsHeader;
        nameResourceId = item.getNameResourceId();
        mDesc = String.format(Locale.US, "Rooms header with id: {%d}.", nameResourceId);
    }

    /** Build an instance for a given contact list item. */
    public ListItem(final SelectableMemberItem item) {
        type = selectableMember;
        groupKey = item.groupKey;
        key = item.memberKey;
        name = item.name;
        text = item.text;
        url = item.url;
        String format = "Member item with name {%s}, email: {%s}, and url {%s}.";
        mDesc = String.format(Locale.US, format, name, email, url);
    }

    /** Build an instance for a given selectable group. */
    public ListItem(final SelectableGroupItem item) {
        type = inviteGroup;
        groupKey = item.groupKey;
        key = item.groupKey;
        name = item.name;
        text = "";
        String format = "Selectable group item with name {%s}.";
        mDesc = String.format(Locale.US, format, name);
        enabled = true;
    }

    /** Build an instance for a given contact list item. */
    public ListItem(final SelectableRoomItem item) {
        type = selectableRoom;
        groupKey = item.groupKey;
        key = item.roomKey;
        name = item.name;
        text = item.text;
        String format = "Selectable room item with name {%s} and text: {%s}.";
        mDesc = String.format(Locale.US, format, name, text);
        enabled = true;
    }

    /** Build an instance for a given User item. */
    public ListItem(@NonNull final UserItem item) {
        type = selectUser;
        groupKey = item.groupKey;
        key = item.memberKey;
        name = item.name;
        text = item.text;
        url = item.url;
        String format = "Member item with name {%s}, email: {%s}, and url {%s}.";
        mDesc = String.format(Locale.US, format, name, email, url);
    }

    /** Build an instance for a room item used for invitations */
    public ListItem(final InviteRoomItem item) {
        type = inviteRoom;
        groupKey = item.groupKey;
        key = item.roomKey;
        name = item.name;
        text = item.text;
        String format = "Selectable room item with name {%s} and text: {%s}.";
        mDesc = String.format(Locale.US, format, name, text);
        enabled = true;
    }

    /** Build an instance for a common room item where selection is reflected but disabled */
    public ListItem(final ItemType type, final String groupKey, final String key) {
        this.type = type;
        this.groupKey = groupKey;
        this.key = key;
        Room room = RoomManager.instance.getRoomProfile(key);
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        name = room.name;
        text = group != null ? group.name : "";
        enabled = false;
    }

    // Public instance methods.

    @Override public String toString() {
        return getDescription();
    }

    // Private instance methods.

    /** Return a description for the item. */
    private String getDescription() {
        // Deal with a uninitialized type, a legacy type and a modern type in that order to provide
        // a description of the list item.
        String format;
        if (type == null)
            return "Uninitialized list item.";
        if (mDesc != null)
            return mDesc;
        switch (type) {
            case contact:
                format = "Contact item with name {%s}, email: {%s}, phone: {%s} and url {%s}.";
                return String.format(Locale.US, format, name, email, phone, url);
            case contactHeader:
                return String.format(Locale.US, "Contact header with resource id: {%d}.", nameResourceId);
            case date:
                return String.format(Locale.US, "Date header with resource id: {%d}.", nameResourceId);
            case experience:
                format = "Experience item with group/room/exp keys {%s/%s/%s} and mode {%s}.";
                return String.format(Locale.US, format, groupKey, roomKey, key, playMode);
            case group:
                format = "Group item with name {%s}, key: {%s}, count: {%s} and text {%s}.";
                return String.format(Locale.US, format, name, key, count, text);
            case inviteCommonRoom:
                format = "Common room item with name {%s} and text: {%s}.";
                return String.format(Locale.US, format, name, text);
            default:
                format = "Undescribed type: {%s}.";
                return String.format(Locale.US, format, type);
        }
    }
}
