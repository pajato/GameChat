/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.common;

import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The fragment dispatcher provides mediation between the experience or chat managers and the main
 * (envelope, experience or chat) fragment. It captures all information a delegated fragment will
 * need to instantiate and take the foreground.
 *
 * @author Paul Michael Reilly
 */
public class Dispatcher {

    // Public instance variables.

    /** The experience or message key. */
    public String key;

    /** The experience payload. */
    public Experience experiencePayload;

    /** The message payload. */
    public Message messagePayload;

    /** The group key. */
    public String groupKey;

    /** The room key. */
    public String roomKey;

    /** The map associating a room key with experiences in the room. */
    public Map<String, Map<String, Experience>> experienceRoomMap;

    /** The map associating a room key with messagess in the room. */
    public Map<String, Map<String, Message>> messageRoomMap;

    /** The fragment type denoting the fragment index and the experience type. */
    public FragmentType type;

    // Public Constructors.

    /** Build an instance given a type. */
    Dispatcher(final FragmentType type) {
        // Capture the type and handle any of the experience types.
        this.type = type;
        if (type != null) processType();
    }

    /** Build an instance given a group key. */
    Dispatcher(final FragmentType type, final String groupKey) {
        this.type = type;
        this.groupKey = groupKey;
        switch (type) {
            case chatGroupList:
                messageRoomMap = MessageManager.instance.messageMap.get(groupKey);
                break;
            case expGroupList:
                experienceRoomMap = ExperienceManager.instance.expGroupMap.get(groupKey);
                break;
            default: break;
        }
    }

    /** Build an instance given a group key, room key, and an experience or message list. */
    Dispatcher(final FragmentType type, final String groupKey, final String roomKey) {
        this.type = type;
        this.groupKey = groupKey;
        this.roomKey = roomKey;
        switch (type) {
            case messageList:
                List<Message> list = MessageManager.instance.getMessageList(groupKey, roomKey);
                messagePayload = list.size() > 0 ? list.get(0) : null;
                key = messagePayload != null ? messagePayload.key : null;
                break;
            case experienceList: // Handle a list of experiences in a room.
                processExperienceList(groupKey, roomKey);
                break;
            default: break;
        }
    }

    /** Build an instance given a chat list item. */
    public Dispatcher(FragmentType type, ChatListItem item) {
        this.type = type;
        if (item != null) {
            groupKey = item.groupKey;
            roomKey = item.key;
        }
    }

    // Private instance methods.

    /** Return the fragment type associated with the given experience type. */
    private FragmentType getFragmentType(ExpType experienceType) {
        for (FragmentType type : FragmentType.values())
            if (type.expType == experienceType)
                return type;
        return null;
    }

    /** Handle a list of experiences in a given room. */
    private void processExperienceList(final String gKey, final String rKey) {
        Map<String, Map<String, Experience>> map = ExperienceManager.instance.expGroupMap.get(gKey);
        Collection<Experience> experiences = map.get(rKey).values();
        switch (experiences.size()) {
            case 0: // impossible.
                break;
            case 1: // A single experience in the room.
                experiencePayload = experiences.iterator().next();
                groupKey = experiencePayload.getGroupKey();
                roomKey = experiencePayload.getRoomKey();
                key = experiencePayload.getExperienceKey();
                type = getFragmentType(experiencePayload.getExperienceType());
                break;
            default: // Show a room list.
                type = FragmentType.expRoomList;
                break;
        }
    }

    /** Handle one of the main experience types. */
    private void processExperienceType() {
        // There are three cases to be handled: 1) there are no experiences of the given type;
        // 2) there is exactly one experience of the given type; or 3) there are multiple
        // experiences of the given type.
        List<Experience> experienceList = ExperienceManager.instance.getExperienceList(type);
        switch (experienceList.size()) {
            case 0: // There is no experiences of this type.  One will be created shortly.
                break;
            case 1: // There is exactly one experience of this type.  Use it.
                experiencePayload = experienceList.get(0);
                groupKey = experiencePayload.getGroupKey();
                roomKey = experiencePayload.getRoomKey();
                key = experiencePayload.getExperienceKey();
                break;
            default: // There are multiple experiences of this type.  Present a list of
                // them by changing the type to the corresponding list type.
                type = type.listType;
                break;
        }
    }

    /** Handle the non-null type to refine the choices. */
    private void processType() {
        switch (type) {
            case checkers:
            case chess:
            case tictactoe:  // Process one of the game types.
                processExperienceType();
                break;
            default: break;
        }
    }
}
