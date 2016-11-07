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

package com.pajato.android.gamechat.chat;

/**
 * Defines the fragments that can be shown in the chat pane.
 *
 * @author Paul Michael Reilly
 */
public enum ChatFragmentType {
    signedOut (ShowSignedOutFragment.class),
    offline (ShowOfflineFragment.class),
    noMessages (ShowNoMessagesFragment.class),
    groupList (ShowGroupListFragment.class),
    roomList (ShowRoomListFragment.class),
    messageList (ShowMessageListFragment.class),
    showNoJoinedRooms (ShowNoJoinedRoomsFragment.class),
    showNoAccount (ShowSignedOutFragment.class);

    // Private instance variables.

    /** The fragment base class for the type. */
    public  Class<? extends BaseChatFragment> fragmentClass;

    // Public constructor.

    /** Build an instance with only a given fragment class. */
    ChatFragmentType(final Class<? extends BaseChatFragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }
}
