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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.adapter.GroupItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.MemberChangeEvent;
import com.pajato.android.gamechat.event.NavDrawerOpenEvent;
import com.pajato.android.gamechat.event.ProfileGroupChangeEvent;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;
import static com.pajato.android.gamechat.common.FragmentType.chatGroupList;
import static com.pajato.android.gamechat.common.FragmentType.chatRoomList;

/**
 * Provide a fragment class that decides which alternative chat fragment to show to the User.
 *
 * @author Paul Michael Reilly (based on ExpEnvelopeFragment written by Bryan Scott)
 */
public class ChatEnvelopeFragment extends BaseChatFragment {

    // Public constants.

    // Public instance methods.

    /** Handle a authentication change event by dealing with the fragment to display. */
    @Subscribe public void onAuthenticationChange(final AuthenticationChangeEvent event) {
        // Simply start the next logical fragment.
        logEvent(String.format("onAuthenticationChange: with event {%s};", event));
        DispatchManager.instance.startNextFragment(getActivity(), chat);
    }

    /** Set the layout file, which specifies the chat FAB and the basic options menu. */
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setLayoutId(R.layout.fragment_chat);
    }

    /** Process a given button click event looking for the navigation drawer. */
    @Subscribe public void onClick(final NavDrawerOpenEvent event) {
        // Ensure that the event is not empty.  If it is empty, abort, otherwise process nav drawer
        // button click events.
        if (event == null || event.item == null)
            return;
        switch (event.item.getItemId()) {
            case R.id.nav_me_room:
                GroupItem groupItem = new GroupItem(AccountManager.instance.getMeGroupKey());
                ChatListItem listItem = new ChatListItem(groupItem);
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
                AccountManager.instance.mChaperone = AccountManager.instance.getCurrentAccountId();
                FirebaseAuth.getInstance().signOut();
                AccountManager.instance.signIn(getContext());
                break;
            case R.id.inviteFriends:
                InvitationManager.instance.extendAppInvitation(getActivity());
                break;
            default:
                // Todo: add more menu button handling as a future feature.
                break;
        }
    }

    /** Process a given button click event looking for the chat FAB. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        View view = event.view;
        logEvent(String.format("onClick: with event {%s};", view));
        switch (view.getId()) {
            case R.id.chatFab:
                // It is a chat fab button.  Toggle the state.
                FabManager.chat.toggle(this);
                break;
            default:
                // Determine if the button click was generated by a group view or room view drill
                // down.  Handle it by adding the next fragment to the back stack.
                processPayload(event.view);
                break;
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
            InvitationManager.instance.acceptGroupInvite(account, groupKey);
    }

    /** Create the view to do essentially nothing. Things will happen in the onStart() method. */
    @Override public void onStart() {
        // Declare the use of the options menu and intialize the FAB and it's menu.
        super.onStart();
        String title = getString(R.string.SignInDialogTitleText);
        String message = getString(R.string.SignInDialogMessageText);
        ProgressManager.instance.show(this.getContext(), title, message);
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
