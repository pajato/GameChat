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
import android.support.v4.view.ViewPager;
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
import com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment;
import com.pajato.android.gamechat.help.HelpManager;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentKind.chat;
import static com.pajato.android.gamechat.common.FragmentType.chatEnvelope;
import static com.pajato.android.gamechat.common.FragmentType.chatGroupList;
import static com.pajato.android.gamechat.common.FragmentType.chatRoomList;
import static com.pajato.android.gamechat.common.FragmentType.protectedUsers;
import static com.pajato.android.gamechat.common.FragmentType.selectGroupsRooms;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.chatGroup;

/**
 * Provide a fragment class that decides which alternative chat fragment to show to the User.
 *
 * @author Paul Michael Reilly (based on ExpEnvelopeFragment written by Bryan Scott)
 */
public class ChatEnvelopeFragment extends BaseChatFragment {

    // Private static variables

    /** The fragment which is currently active within the envelope */
    private static FragmentType mCurrentFragmentType;

    // Default constructor.

    /** Build an instance setting the fragment type. */
    public ChatEnvelopeFragment() {
        type = chatEnvelope;
    }

    // Public instance methods.

    /** Satisfy base class */
    public List<ListItem> getList() {
        return null;
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        return null;
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        return null;
    }

    /** Handle a authentication change event by dealing with the fragment to display. */
    @Subscribe public void onAuthenticationChange(final AuthenticationChangeEvent event) {
        // Simply start the next logical fragment.
        logEvent(String.format("onAuthenticationChange: with event {%s};", event));
        DispatchManager.instance.dispatchToFragment(this, chat);
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
                DispatchManager.instance.dispatchToFragment(this, chatRoomList, null, listItem);
                break;
            case R.id.nav_groups:
                DispatchManager.instance.dispatchToFragment(this, chatGroupList, null, null);
                break;
            case R.id.manageProtectedUsers:
                // Ensure that the current user is not a protected user. Then, start the process of
                // adding a protected user.
                if (AccountManager.instance.isRestricted()) {
                    String protectedWarning = getString(R.string.CannotManageProtectedUser);
                    Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
                    break;
                }
                // If not on a tablet, make sure that we switch to the chat perspective and remember
                // the type that we came from.
                FragmentType activeFragmentType = this.type;
                if (!PaneManager.instance.isTablet()) {
                    ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                    if (viewPager != null && viewPager.getCurrentItem() != PaneManager.CHAT_INDEX) {
                        viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                    }
                    activeFragmentType = getCurrentFragmentType();
                }
                DispatchManager.instance.dispatchToFragment(this, protectedUsers,
                        activeFragmentType, null);
                break;
            case R.id.inviteFriends:
                DispatchManager.instance.dispatchToFragment(this, selectGroupsRooms, this.type, null);
                break;
            case R.id.settings:
                showFutureFeatureMessage(R.string.MenuItemSettings);
                break;
            case R.id.helpAndFeedback:
                HelpManager.instance.launchHelp(getActivity());
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

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        mDispatcher = dispatcher;
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
        DispatchManager.instance.dispatchToFragment(this, chat);
    }

    /** Get the type of the most recent (current) fragment */
    public static FragmentType getCurrentFragmentType() {
        return mCurrentFragmentType;
    }

    /** Set the type of the most recent (current) fragment */
    public static void setCurrentFragment(FragmentType type) {
        mCurrentFragmentType = type;
    }

}
