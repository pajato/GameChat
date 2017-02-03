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

import com.pajato.android.gamechat.common.PlayModeManager.PlayModeType;

import java.util.Locale;

import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.contact;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.contactHeader;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.date;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.experience;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.group;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.inviteCommonRoom;
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

    // Public enums.

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
    public PlayModeType playMode;

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

    /** Build an instance for a given contact header item. */
    public ListItem(final ContactHeaderItem item) {
        type = contactHeader;
        nameResourceId = item.getNameResourceId();
        mDesc = String.format(Locale.US, "Contact header with id: {%d}.", nameResourceId);
    }

    /** Build an instance for a given contact list item. */
    public ListItem(final ContactItem item) {
        type = contact;
        name = item.name;
        email = item.email;
        // TODO: uncomment when phone numbers are relevant: phone = item.phone;
        url = item.url;
        String format = "Contact item with name {%s}, email: {%s}, phone: {%s} and url {%s}.";
        mDesc = String.format(Locale.US, format, name, email, "todo/tbd", url);
    }

    /** Build an instance for a given date header item. */
    public ListItem(final DateHeaderItem item) {
        type = date;
        nameResourceId = item.getNameResourceId();
        mDesc = String.format(Locale.US, "Contact header with id: {%d}.", nameResourceId);
    }

    /** Build an instance for a given room list item. */
    public ListItem(final ExperienceItem item, PlayModeType playMode) {
        type = experience;
        this.playMode = playMode;
        groupKey = item.groupKey;
        roomKey = item.roomKey;
        key = item.key;
        String format = "Experience item with group/room/experience keys {%s/%s/%s} and mode {%s}.";
        mDesc = String.format(Locale.US, format, groupKey, roomKey, key, playMode);
    }

    /** Build an instance for a given group list item. */
    public ListItem(final GroupItem item) {
        type = group;
        groupKey = item.groupKey;
        name = item.name;
        count = item.count;
        text = item.text;
        String format = "Group item with name {%s}, key: {%s}, count: {%s} and text {%s}.";
        mDesc = String.format(Locale.US, format, name, key, count, text);
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

    /** Build an instance for a common room item where selection is reflected but never enabled */
    public ListItem(final CommonRoomItem item) {
        type = inviteCommonRoom;
        groupKey = item.groupKey;
        key = item.roomKey;
        name = item.name;
        text = item.text;
        String format = "Common room item with name {%s} and text: {%s}.";
        mDesc = String.format(Locale.US, format, name, text);
        enabled = false;
    }

    // Public instance methods.

    @Override public String toString() {return mDesc;}
}
