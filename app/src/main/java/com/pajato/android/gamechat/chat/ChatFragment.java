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

package com.pajato.android.gamechat.chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.JoinedRoomListChangeEvent;
import com.pajato.android.gamechat.event.ProfileGroupChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showGroupList;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoAccount;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoJoinedRooms;

/**
 * Provide a fragment class that decides which alternative chat fragment to show to the User.
 * Indecision will result in a default "flummoxed" message being displayed.
 *
 * @author Paul Michael Reilly (based on GameFragment written by Bryan Scott)
 */
public class ChatFragment extends BaseChatFragment {

    // Public instance methods.

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        View view = event.view;
        logEvent(String.format("onClick: with event {%s};", view));
        switch (view.getId()) {
            case R.id.chatFab:
                // It is a chat fab button.  Toggle the state.
                FabManager.chat.toggle(this);
                break;
            case R.id.addGroupButton:
            case R.id.addGroupMenuItem:
                // Dismiss the FAB menu, and start up the add group activity.
                FabManager.chat.dismissMenu(this);
                Intent intent = new Intent(this.getActivity(), AddGroupActivity.class);
                startActivity(intent);
                break;
            default:
                // Determine if the button click was generaged by a group view or room view drill
                // down.  Handle it by adding the next fragment to the back stack.
                processPayload(event.view);
                break;
        }
    }

    /** Set the layout file, which specifies the chat FAB and the basic options menu. */
    @Override public int getLayout() {return R.layout.fragment_chat;}

    /** Handle a authentication change event by dealing with the fragment to display. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // Log the event and determine if there is an active account.
        logEvent("onAccountStateChange");
        if (event.account == null) {
            // There is no active account.  Show the no account fragment.
            ChatManager.instance.replaceFragment(showNoAccount, this.getActivity());
        } else {
            // There is an active account.  Determine if a default fragment needs to be set up and,
            // if so, show the group list, otherwise just let whatever fragment is running stay
            // running.
            ChatManager.ChatFragmentType type = ChatManager.instance.lastTypeShown;
            if (type == null) ChatManager.instance.replaceFragment(showGroupList, getActivity());
        }
    }

    /** Post the chat options menu on demand. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.chat_menu_base, menu);
    }

    /** A new group loaded event has been detected.  Join it if not already joined. */
    @Subscribe public void onGroupProfileChange(final ProfileGroupChangeEvent event) {
        // Ensure that the User is logged in (a prompting will be generated if not.)
        Account account = AccountManager.instance.getCurrentAccount(getContext());
        if (account == null) return;

        // Ensure that a group key and group are packaged in the event.
        String groupKey = event.key;
        Group group = event.group;
        if (groupKey != null && group != null)
            // The group and key are available.  Accept any open invitations.
            DatabaseManager.instance.acceptGroupInvite(account, group, groupKey);
    }

    /** Create the view to do essentially nothing. Things will happen in the onStart() method. */
    @Override public void onInitialize() {
        // Declare the use of the options menu and intialize the FAB and it's menu.
        super.onInitialize();
        FabManager.chat.init(mLayout, this.getTag());
    }

    /** Deal with a change in the joined rooms state. */
    @Subscribe public void onJoinedRoomListChange(@NonNull final JoinedRoomListChangeEvent event) {
        // Turn off the loading progress dialog and handle a signed in account with some joined
        // rooms by rendering the list.
        if (event.joinedRoomList.size() == 0) {
            // Handle the case where there are no joined rooms by enabling the no rooms message.
            ChatManager.instance.replaceFragment(showNoJoinedRooms, this.getActivity());
        } else {
            // Handle a joined rooms change by setting up database watchers on the messages in each
            // room.
            for (String joinedRoom : event.joinedRoomList) {
                // Set up the database watcher on this list.
                String[] split = joinedRoom.split(" ");
                String groupKey = split[0];
                String roomKey = split[1];
                ChatListManager.instance.setMessageWatcher(groupKey, roomKey);
            }
        }
    }

}
