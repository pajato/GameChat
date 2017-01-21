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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseCreateFragment;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MemberManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.database.RoomManager;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.pajato.android.gamechat.chat.model.Message.STANDARD;
import static com.pajato.android.gamechat.chat.model.Room.PUBLIC;

public class CreateGroupFragment extends BaseCreateFragment {

    // Private instance variables.

    /** The group being created. */
    private Group mGroup;

    // Public instance methods.

    /** Establish the create time state. */
    @Override public void onStart() {
        // Establish the create type, the list type, setup the toolbar and turn off the access
        // control.
        super.onStart();
        mCreateType = CreateType.group;
        mItemListType = DBUtils.ChatListType.addGroup;
        initToolbar();
        RadioGroup accessControl = (RadioGroup) mLayout.findViewById(R.id.AccessControl);
        accessControl.setVisibility(View.GONE);

        EditText hint = (EditText) mLayout.findViewById(R.id.NameText);
        hint.setHint(R.string.CreateGroupNameHint);

        // Create the group to be configure and, optionally, persisted.
        mGroup = new Group();
        mGroup.name = getDefaultName();
        mGroup.roomList = new ArrayList<>();
        mGroup.memberList = new ArrayList<>();
    }

    // Protected instance methods.

    /** Return a default group name based on the given account. */
    @Override protected String getDefaultName() {
        // Ensure that the account exists.
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null) return "";

        // Obtain a sane default group name.
        String group = getResources().getString(R.string.Group);
        String value = account.getDisplayName();
        return String.format(Locale.getDefault(), "%s %s", value, group);
    }

    /** Save the group being created to the Firebase realtime database. */
    @Override protected void save(@NonNull Account account) {
        // Generate push keys for new group and it's default room; set the self reference key and
        // the owner field values on the group.
        String groupKey = GroupManager.instance.getGroupKey();
        String roomKey = RoomManager.instance.getRoomKey(groupKey);

        // Update and persist the group adding the default room to it's room list.
        mGroup.key = groupKey;
        mGroup.owner = account.id;
        mGroup.roomList.add(roomKey);
        mGroup.memberList.add(account.id);
        GroupManager.instance.createGroupProfile(mGroup);

        // Create and persist the default (common) room.
        Room room = new Room(roomKey, mGroup.owner, "Common", groupKey, 0, 0, PUBLIC);
        room.memberIdList.add(account.id);
        RoomManager.instance.createRoomProfile(room);

        // Create and persist a member object to the database joined to the default room.
        Account member = new Account(account);
        member.joinList.add(roomKey);
        member.groupKey = groupKey;
        MemberManager.instance.createMember(member);

        // Update and persist the User account with the new joined list entry.
        account.joinList.add(groupKey);
        AccountManager.instance.updateAccount(account);

        // Post a welcome message to the default room from the owner.
        String text = "Welcome to my new group!";
        MessageManager.instance.createMessage(text, STANDARD, account, room);

        // Dismiss the Keyboard and return to the previous fragment.
        Activity activity = getActivity();
        InputMethodManager manager;
        manager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        if (manager.isAcceptingText() && activity.getCurrentFocus() != null)
            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        activity.onBackPressed();
    }

    /** Set the name of the managed object conditionally to the given value. */
    @Override protected void setName(final String value) {if (mGroup != null) mGroup.name = value;}

    /** Implement the set type as a nop. */
    @Override protected void setType(final int type) {}
}
