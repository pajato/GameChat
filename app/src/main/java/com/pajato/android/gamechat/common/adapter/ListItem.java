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
import com.pajato.android.gamechat.exp.Experience;

import java.util.Locale;

import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.contact;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.experience;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.inviteRoom;

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
        chatGroup ("Chat group item with name {%s}, key: {%s}, count: {%s} and text {%s}."),
        chatRoom ("Chat room item with name {%s}, group, room: {%s, %s}, count: {%s}, text {%s}."),
        contact ("Contact item with name {%s}, email: {%s}, phone: {%s} and url {%s}."),
        contactHeader ("Contact header with resource id: {%d}."),
        date ("Date header with resource id: {%d}."),
        expGroup ("Exp group item with name {%s}, key: {%s}, count: {%s} and text {%s}."),
        expList ("Exp list item with name {%s}, group, room: {%s, %s}, count: {%s}, text {%s}."),
        expRoom ("Exp room item with name {%s}, group, room: {%s, %s}, count: {%s}, text {%s}."),
        experience ("Experience item with group/room/exp keys {%s/%s/%s} and mode {%s}."),
        message ("Message item with name {%s}, key: {%s}, count: {%s} and text {%s}."),
        resourceHeader ("Resource header with id: {%d}."),
        roomsHeader ("Rooms header with id: {%d}."),
        selectUser ("Member item with name {%s}, email: {%s}, and iconUrl {%s}."),
        selectableMember ("Member item with name {%s}, email: {%s}, and iconUrl {%s}."),
        selectableRoom ("Selectable room item with name {%s} and text: {%s}."),
        inviteGroup ("Selectable group item with name {%s}."),
        inviteRoom ("Selectable room item with name {%s} and text: {%s}."),
        inviteCommonRoom ("Common room item with name {%s} and text: {%s}.");

        public String format;

        /** Build an instance with a given format string. */
        ItemType(final String format) {
            this.format = format;
        }

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

    /** The item image resource id for dynamic images, as used with experiences. */
    public int iconResId;

    /** The URL for the item, possibly null, used for icons with contacts and chat list items. */
    String iconUrl;

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

    // Public constructors.

    /** Build an item instance for a group or room item. */
    public ListItem(final ItemType type, final String groupKey, final String roomKey,
                    final String name, final int count, final String text) {
        // Set the type and populate the member fields for a group or room item type.
        this.type = type;
        this.groupKey = groupKey;
        this.roomKey = roomKey;
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
        iconUrl = url;
    }

    /** Build an instance for a given room list item. */
    public ListItem(@NonNull final Experience exp, final PlayModeType playMode) {
        type = experience;
        this.playMode = playMode;
        groupKey = exp.getGroupKey();
        roomKey = exp.getRoomKey();
        key = exp.getExperienceKey();
    }

    /** Build an instance for a room and selectable room or member list items. */
    public ListItem(final ItemType type, final String groupKey, final String key,
                    final String name, final String text, final String iconUrl) {
        this.type = type;
        this.groupKey = groupKey;
        this.key = key;
        this.name = name;
        this.text = text;
        this.iconUrl = iconUrl;
    }

    /** Build an instance for a given selectable group. */
    public ListItem(final ItemType type, final String groupKey, final String name) {
        this(type, groupKey, groupKey, name, null, null);
        enabled = true;
    }

    /** Build an instance for a selectable room list item. */
    public ListItem(final ItemType type, final String groupKey, final String roomKey,
                    final String name, final String text) {
        this(type, groupKey, roomKey, name, text, null);
        enabled = true;
    }

    /** Build an instance for a room or common room invitation. */
    public ListItem(final ItemType type, final Room room) {
        this.type = type;
        groupKey = room.groupKey;
        key = room.key;
        name = room.name;
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        text = group != null ? group.name : "";
        enabled = type == inviteRoom;
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
        if (type == null)
            return "Uninitialized list item.";
        switch (type) {
            case chatGroup:
                return String.format(Locale.US, type.format, name, key, count, text);
            case chatRoom:
                return String.format(Locale.US, type.format, name, groupKey, roomKey, count, text);
            case contact:
                return String.format(Locale.US, type.format, name, email, phone, iconUrl);
            case contactHeader:
                return String.format(Locale.US, type.format, nameResourceId);
            case date:
                return String.format(Locale.US, type.format, nameResourceId);
            case expGroup:
                return String.format(Locale.US, type.format, name, key, count, text);
            case expList:
                return String.format(Locale.US, type.format, name, groupKey, roomKey, count, text);
            case expRoom:
                return String.format(Locale.US, type.format, name, groupKey, roomKey, count, text);
            case experience:
                return String.format(Locale.US, type.format, groupKey, roomKey, key, playMode);
            case message:
                return String.format(Locale.US, type.format, name, key, count, text);
            case inviteCommonRoom:
                return String.format(Locale.US, type.format, name, text);
            case inviteGroup:
                return String.format(Locale.US, type.format, name);
            case inviteRoom:
                return String.format(Locale.US, type.format, name, text);
            case resourceHeader:
                return String.format(Locale.US, type.format, nameResourceId);
            case roomsHeader:
                return String.format(Locale.US, type.format, nameResourceId);
            case selectableMember:
                return String.format(Locale.US, type.format, name, email, iconUrl);
            case selectableRoom:
                return String.format(Locale.US, type.format, name, text);
            case selectUser:
                return String.format(Locale.US, type.format, name, email, iconUrl);
            default:
                return String.format(Locale.US, "Un-described type: {%s}.", type);
        }
    }
}
