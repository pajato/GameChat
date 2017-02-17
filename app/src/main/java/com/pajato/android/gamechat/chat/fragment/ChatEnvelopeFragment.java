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

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.MemberChangeEvent;
import com.pajato.android.gamechat.event.NavDrawerOpenEvent;
import com.pajato.android.gamechat.event.ProfileGroupChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentKind.chat;
import static com.pajato.android.gamechat.common.FragmentType.chatGroupList;
import static com.pajato.android.gamechat.common.FragmentType.chatRoomList;
import static com.pajato.android.gamechat.common.FragmentType.protectedUsers;
import static com.pajato.android.gamechat.common.FragmentType.selectChatGroupsRooms;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.chatGroup;

/**
 * Provide a fragment class that decides which alternative chat fragment to show to the User.
 *
 * @author Paul Michael Reilly (based on ExpEnvelopeFragment written by Bryan Scott)
 */
public class ChatEnvelopeFragment extends BaseChatFragment {

    // Default constructor.

    /** Build an instance setting the fragment type. */
    public ChatEnvelopeFragment() {
        type = FragmentType.chatEnvelope;
    }

    // Public instance methods.

    /** Handle a authentication change event by dealing with the fragment to display. */
    @Subscribe public void onAuthenticationChange(final AuthenticationChangeEvent event) {
        // Simply start the next logical fragment.
        logEvent(String.format("onAuthenticationChange: with event {%s};", event));
        DispatchManager.instance.startNextFragment(getActivity(), chat);
    }

    /** Process a given button click event looking for the navigation drawer. */
    @Subscribe public void onClick(final NavDrawerOpenEvent event) {
        // Ensure that the event is not empty.  If it is empty, abort, otherwise process nav drawer
        // button click events.
        if (event == null || event.item == null)
            return;
        switch (event.item.getItemId()) {
            case R.id.nav_me_room:
                String groupKey = AccountManager.instance.getMeGroupKey();
                ListItem listItem = new ListItem(chatGroup, groupKey, null, null, 0, null);
                DispatchManager.instance.chainFragment(getActivity(), chatRoomList, listItem);
                break;
            case R.id.nav_groups:
                DispatchManager.instance.startNextFragment(getActivity(), chatGroupList);
                break;
            case R.id.manageProtectedUsers:
                // Ensure that the current user is not a protected user. Then, start the process of
                // adding a protected user.
                if (AccountManager.instance.getCurrentAccount().chaperone != null) {
                    String protectedWarning = "Protected Users cannot make other Protected Users.";
                    Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
                    break;
                }
                DispatchManager.instance.chainFragment(getActivity(), protectedUsers, null);
                break;
            case R.id.inviteFriends:
                DispatchManager.instance.chainFragment(getActivity(), selectChatGroupsRooms, null);
                break;
            default:
                break;
        }
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
            InvitationManager.instance.acceptGroupInvite(account, groupKey);
    }

    /** Create the view to do essentially nothing. */
    @Override public void onStart() {
        // Initialize the FAB.
        super.onStart();
        FabManager.chat.setTag(this.getTag());
    }

    /** Deal with a change in the joined rooms state by logging it. */
    @Subscribe public void onMemberChange(@NonNull final MemberChangeEvent event) {
        logEvent(String.format(Locale.US, "onGroupMemberChange with event: {%s}", event));
    }

    /** Dispatch to a more suitable fragment. */
    @Override public void onResume() {
        // The experience manager will load a fragment to view into this envelope fragment.
        super.onResume();
        DispatchManager.instance.startNextFragment(getActivity(), chat);
    }

    /** Setup the fragment with what would otherwise be constructor arguments. */
    @Override public void onSetup(final Context context, final Dispatcher dispatcher) {

    }
}
