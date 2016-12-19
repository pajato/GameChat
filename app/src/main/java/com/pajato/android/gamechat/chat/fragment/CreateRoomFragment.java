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

package com.pajato.android.gamechat.chat.fragment;

import android.support.annotation.NonNull;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Account;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MemberManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static com.pajato.android.gamechat.chat.model.Message.STANDARD;
import static com.pajato.android.gamechat.chat.model.Room.PRIVATE;
import static com.pajato.android.gamechat.chat.model.Room.PUBLIC;

/**
 * Create a room ...
 *
 * Validate that the associated group and member both exist even as early as possible.
 */
public class CreateRoomFragment extends BaseCreateFragment {

    // Private instance variables.

    /** The group containing the new room. */
    private Group mGroup;

    /** The member associated with teh account creating the new room. */
    private Account mMember;

    /** The room being created. */
    private Room mRoom;

    // Public instance methods.

    /** Provide a click event handler. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Log the event and determine if the event looks right.  Abort if it doesn't.
        logEvent(String.format(Locale.US, "onClick (create room) event: {%s}.", event));
        if (event == null || event.view == null) return;

        // The event appears to be ok.  Update the room type.
        switch (event.view.getId()) {
            case R.id.PublicButton:
                mRoom.type = PUBLIC;
                break;
            case R.id.PrivateButton:
                mRoom.type = PRIVATE;
                break;
            default:
                // Ignore everything else.
                break;
        }
    }

    /** Establish the create time state. */
    @Override public void onInitialize() {
        // Ensure that there is a group to use in creating the new room.
        super.onInitialize();
        mGroup = GroupManager.instance.getGroupProfile(mItem.groupKey);
        Account account = AccountManager.instance.getCurrentAccount();
        mMember = MemberManager.instance.getMember(mGroup.key, account.id);
        if (mGroup == null || mMember == null) {
            // Return to the previous fragment using a back press.
            getActivity().onBackPressed();
            // Probably good to put a toast or snackbar here.
            return;
        }

        // Establish the list type and setup the toolbar.
        mCreateType = CreateType.room;
        mItemListType = DBUtils.ChatListType.addRoom;
        initToolbar();

        // Set up the room profile.
        mRoom = new Room();
        mRoom.owner = account.id;
        mRoom.name = getDefaultName();
        mRoom.groupKey = mGroup.key;
        mRoom.owner = mMember.id;
    }

    // Protected instance methods.

    /** Save the room being created to the Firebase realtime database. */
    @Override protected void save(@NonNull Account account) {
        // Persist the configured room.
        mRoom.key = RoomManager.instance.getRoomKey(mGroup.key);
        RoomManager.instance.createRoomProfile(mRoom);

        // Update and persist the group adding the new room to it's room list.
        mGroup.roomList.add(mRoom.key);
        GroupManager.instance.updateGroupProfile(mGroup);

        // Update and persist the member adding the new room to it's join list.
        mMember.joinList.add(mRoom.key);
        MemberManager.instance.updateMember(mMember);

        // Post a welcome message to the new room from the owner.
        String text = "Welcome to my new room!";
        MessageManager.instance.createMessage(text, STANDARD, account, mRoom);
    }

    /** Set the room name conditionally to the given value. */
    @Override protected void setName(final String value) {if (mRoom != null) mRoom.name = value;}
}
