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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.adapter.GroupItem;
import com.pajato.android.gamechat.chat.adapter.RoomItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.MenuItemEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.LinearLayoutCompat.VERTICAL;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.GROUP_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.ROOM_ITEM_TYPE;
import static com.pajato.android.gamechat.common.FragmentType.chatRoomList;
import static com.pajato.android.gamechat.common.FragmentType.messageList;

/**
 * Provide a base class to support fragment lifecycle debugging.  All lifecycle events except for
 * onViewCreate() are handled by providing logcat tracing information.  The fragment manager is
 * displayed in order to help catch an elusive connected check failure.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseChatFragment extends BaseFragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseChatFragment.class.getSimpleName();

    /** The lifecycle event format string. */
    private static final String LOG_FORMAT = "Event: %s; Fragment: %s; Fragment Manager: %s.";

    /** Extra information format string. */
    private static final String SUFFIX_FORMAT = "Fragment Type: %s; State: %s; Bundle: %s.";

    // Protected instance variables.

    /** Show an ad at the top of the view. */
    protected AdView mAdView;

    /** The item information passed from the parent fragment. */
    protected ChatListItem mItem;

    // Public instance methods.

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) mAdView.destroy();
    }

    /** Initialize chat list fragments by dealing with ads. */
    @Override public void onStart() {
        super.onStart();
        initAdView(mLayout);
    }

    /** Handle a menu item click by processing the join developer group menu item. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        // Case on the menu id to handle the item.
        switch (event.item.getItemId()) {
            case R.id.joinDeveloperGroups:
                // Handle a request to join the developer groups by adding this User to the
                // developer groups as necessary.  Do not propagate the event any further.
                joinDeveloperGroups();
                AppEventManager.instance.cancel(event);
                break;
            default:
                // Handle all other events by logging a message for now.
                final String format = "Default handling for menu item with title: {%s}";
                Log.d(TAG, String.format(Locale.US, format, event.item.getTitle()));
                break;
        }
    }

    /** Log the lifecycle event, stop showing ads and turn off the app event bus. */
    @Override public void onPause() {
        super.onPause();
        if (mAdView != null) mAdView.pause();
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        // Log the event, put the FAB into the start state and update the list, if any.
        super.onResume();
        redisplay();
    }

    /** Set the item defining this fragment (passed from the parent (spawning) fragment. */
    public void setItem(final ChatListItem item) {
        mItem = item;
    }

    // Protected instance methods.

    /** Initialize the ad view by building and loading an ad request. */
    protected void initAdView(@NonNull final View layout) {
        mAdView = (AdView) layout.findViewById(R.id.adView);
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    /** Log a lifecycle event that has no bundle. */
    @Override protected void logEvent(final String event) {
        logEvent(event, null);
    }

    /** Log a lifecycle event that has a bundle. */
    @Override protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        String state = mActive ? "foreground" : "background";
        String bundleMessage = bundle == null ? "N/A" : bundle.toString();
        Log.v(TAG, String.format(Locale.US, LOG_FORMAT, event, this, manager));
        Log.v(TAG, String.format(Locale.US, SUFFIX_FORMAT, type, state, bundleMessage));
    }

    /** Return TRUE iff the fragment setup is handled successfully. */
    @Override protected boolean onDispatch(@NonNull final Context context,
                                           @NonNull final Dispatcher dispatcher) {
        // Ensure that the type is valid.  Signal failure if not, otherwise handle each possible
        // case signalling success.  If there are no valid cases signal failure.
        if (dispatcher.type == null) return false;
        switch (type) {
            case chatGroupList: // A group list does not need an item.
                return true;
            case messageList:   // The messages in a room require both the group and room keys.
                RoomItem roomItem = new RoomItem(dispatcher.groupKey, dispatcher.roomKey);
                mItem = new ChatListItem(roomItem);
                return true;
            case createRoom:
            case joinRoom:
            case chatRoomList:  // The rooms in a group need the group key.
                if (dispatcher.groupKey == null)
                    return false;
                GroupItem groupItem = new GroupItem(dispatcher.groupKey);
                mItem = new ChatListItem(groupItem);
                return true;
            default:
                return false;
        }
    }

    /** Proces a button click that may be a chat list item click. */
    protected void processPayload(final View view) {
        // Ensure that the payload is valid.  Abort if not, otherwise determine chain to the next
        // appropriate fragment based on the type associated with the payload.
        Object payload = view.getTag();
        if (!(payload instanceof ChatListItem))
            return;
        ChatListItem item = (ChatListItem) payload;
        switch (item.type) {
            case GROUP_ITEM_TYPE: // Drill into the rooms in group.
                DispatchManager.instance.chainFragment(getActivity(), chatRoomList, item);
                break;
            case ROOM_ITEM_TYPE: // Show the messages in a room.
                DispatchManager.instance.chainFragment(getActivity(), messageList, item);
                break;
            default:
                break;
        }
    }

    /** Do a redisplay to catch potential changes that should be shown in the current view. */
    protected void redisplay() {
        // Update the FAB for this fragment, process the ad, determine if a list adapter update
        // needs be processed and set the toolbar titles.
        FabManager.chat.init(this);
        if (mAdView != null)
            mAdView.resume();
        if (type != null)
            switch (type) {
                case joinRoom:
                case chatGroupList:
                case chatRoomList:
                case selectGroupsAndRooms:
                case messageList:   // Update the state of the list adapter.
                    updateAdapterList();
                    break;
                default:            // Ignore all other fragments.
                    break;
            }
        ToolbarManager.instance.setTitles(this, mItem);
    }

    // Private instance methods.

    /** Development hack: poor man's invite handler to join one or more developer groups. */
    private void joinDeveloperGroups() {
        // Ensure that the supported developer groups have been joined.  This a short term hack in
        // lieu of dynamic linking support.  First ensure that the User is signed in.  Note they
        // will be prompted to sign in if they have not already done so.
        Account account = AccountManager.instance.getCurrentAccount(getContext());
        if (account == null) return;

        // Walk the list of developer groups to ensure that all are joined.
        List<String> groupList = new ArrayList<>();
        groupList.add("-KYUYMjr5X4XLPT6XM2P");      // Reilly-Scott Group
        groupList.add("-KYUPSx4g74wSHKb4pGw");      // Heather Music
        groupList.add("-KYUPdVXsbWCGmAO4Umd");      // Hawthorn
        groupList.add("-KYUWj1hk1RAf30u8NDa");      // GameChat Tester Group
        groupList.add("-KYUV6N3J8UHkL5c5X2V");      // Pajato Technologies
        groupList.add("-KYUUkaT-v-KMsWXPyae");      // Pajato Support Group
        for (String groupKey : groupList) {
            // Extend an invitation to the group and verify that this group has been joined.
            InvitationManager.instance.extendGroupInvite(account, groupKey);
            if (!account.joinList.contains(groupKey)) {
                // Join the group now if it has been loaded.  It will be queued for joining later if
                // necessary.
                Group group = GroupManager.instance.getGroupProfile(groupKey);
                if (group != null)
                    // The group is available.  Accept any open invitations. There should be at
                    // least one!
                    InvitationManager.instance.acceptGroupInvite(account, groupKey);
            }
        }
    }

    /** Return TRUE iff the list can be considered up to date. */
    private boolean updateAdapterList() {
        // Determine if the fragment has a view and that it has a list type.
        View layout = getView();
        if (layout == null) return false;

        // It has both.  Ensure that the list view (recycler) exists.
        RecyclerView view = (RecyclerView) layout.findViewById(R.id.chatList);
        if (view == null) return false;

        // The recycler view exists.  Show the chat list, either groups, messages or rooms.
        RecyclerView.Adapter adapter = view.getAdapter();
        if (adapter == null) {
            // Initialize the recycler view.
            adapter = new ChatListAdapter();
            view.setAdapter(adapter);
            Context context = layout.getContext();
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
            view.setLayoutManager(layoutManager);
            view.setItemAnimator(new DefaultItemAnimator());
        }

        // Inject the list items into the recycler view making sure to scroll to the end of the
        // list when showing messages.
        ChatListAdapter listAdapter = (ChatListAdapter) adapter;
        listAdapter.clearItems();
        List<ChatListItem> items = DBUtils.instance.getList(type, mItem);
        Log.d(TAG, String.format(Locale.US, "Updating with %d items.", items.size()));
        listAdapter.addItems(items);
        if (type == messageList) view.scrollToPosition(listAdapter.getItemCount() - 1);
        return true;
    }
}
