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
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.adapter.MessageItem;
import com.pajato.android.gamechat.chat.adapter.RoomItem;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.main.MainActivity;
import com.pajato.android.gamechat.main.NavigationManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.LinearLayoutCompat.VERTICAL;
import static com.pajato.android.gamechat.chat.ChatFragmentType.messageList;
import static com.pajato.android.gamechat.chat.ChatFragmentType.roomList;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.GROUP_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.ROOM_ITEM_TYPE;
import static com.pajato.android.gamechat.database.DatabaseListManager.ChatListType.group;
import static com.pajato.android.gamechat.database.DatabaseListManager.ChatListType.message;
import static com.pajato.android.gamechat.database.DatabaseListManager.ChatListType.room;

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
    private static final String SUFFIX_FORMAT = "Fragment List Type: %s; State: %s; Bundle: %s.";

    // Protected instance variables.

    /** Show an ad at the top of the view. */
    protected AdView mAdView;

    /** The item information passed from the parent fragment. */
    protected ChatListItem mItem;

    /** The list type for this fragment. */
    protected DatabaseListManager.ChatListType mItemListType;

    // Public instance methods.

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onChatListChange(final ChatListChangeEvent event) {
        // Determine if this fragment cares about chat list changes:
        String format = "onChatListChange with event {%s}";
        logEvent(String.format(Locale.US, format, "no list", event));
        if (mActive && (mItemListType == group || mItemListType == room)) redisplay();
        ChatManager.instance.startNextFragment(getActivity());
    }

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) mAdView.destroy();
    }

    /** Initialize chat list fragments by dealing with ads. */
    @Override public void onInitialize() {
        super.onInitialize();
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

    /** Initialize the toolbar by connecting it up to the navigation drawer. */
    protected void initToolbar() {
        // Determine if the fragment supports a toolbar.  Abort if not.
        Toolbar toolbar = (Toolbar) mLayout.findViewById(R.id.toolbar);
        if (toolbar == null) return;

        // There is a toolbar.  Setup the overflow menu to present standard items.
        int id = R.drawable.ic_more_vert_white_24dp;
        toolbar.inflateMenu(R.menu.add_group_menu);
        toolbar.setOverflowIcon(VectorDrawableCompat.create(getResources(), id, null));

        // Case on the list type to set up the navigation icon.
        switch (mItemListType) {
            case message:
            case room:
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
                toolbar.setNavigationOnClickListener(new UpHandler());
                break;
            case group:
                NavigationManager.instance.init((MainActivity) getActivity(), toolbar);
                break;
            case joinRoom:
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                toolbar.setNavigationOnClickListener(new ExitHandler());
                break;
            default:
                break;
        }
    }

    /** Log a lifecycle event that has no bundle. */
    @Override protected void logEvent(final String event) {
        logEvent(event, null);
    }

    /** Log a lifecycle event that has a bundle. */
    @Override protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        String list = mItemListType == null ? "N/A" : mItemListType.toString();
        String state = mActive ? "foreground" : "background";
        String bundleMessage = bundle == null ? "N/A" : bundle.toString();
        Log.v(TAG, String.format(Locale.US, LOG_FORMAT, event, this, manager));
        Log.v(TAG, String.format(Locale.US, SUFFIX_FORMAT, list, state, bundleMessage));
    }

    /** Return TRUE iff the fragment setup is handled successfully. */
    @Override protected boolean onDispatch(@NonNull final Context context,
                                           @NonNull final Dispatcher dispatcher) {
        // Ensure that the type and payload both are consistent with a chat dispatch.
        if (!(dispatcher.type instanceof ChatFragmentType)) return false;

        // Case on the fragment type to set up the fragment item.
        ChatFragmentType type = (ChatFragmentType) dispatcher.type;
        switch (type) {
            case groupList:
                // A group list does not need an item.
                return true;
            case messageList:
                Message payload = (Message) dispatcher.payload;
                MessageItem messageItem = new MessageItem(payload);
                mItem = new ChatListItem(messageItem);
                return true;
            case roomList:
                String roomKey = dispatcher.roomMap.keySet().iterator().next().toString();
                RoomItem roomItem = new RoomItem(dispatcher.groupKey, roomKey);
                mItem = new ChatListItem(roomItem);
                return true;
            default:
                return false;
        }
    }

    /** Proces a button click that may be a chat list item click. */
    protected void processPayload(final View view) {
        // Determine if some action needs to be taken, i.e if the button click is coming
        // from a group or room item view.
        Object payload = view.getTag();
        if (!(payload instanceof ChatListItem)) return;

        // Action needs be taken.  Case on the item to determine what action.
        ChatListItem item = (ChatListItem) payload;
        switch (item.type) {
            case GROUP_ITEM_TYPE:
                // Drill into the rooms in group.
                ChatManager.instance.chainFragment(roomList, getActivity(), item);
                break;
            case ROOM_ITEM_TYPE:
                // Show the messages in a room.
                ChatManager.instance.chainFragment(messageList, getActivity(), item);
                break;
            default:
                break;
        }
    }

    /** Set the title in the toolbar based on the list type. */
    @Override protected void setTitles() {
        // Ensure that there is an accessible toolbar at this point.  Abort if not, otherwise case
        // on the list type to apply the titles.
        Toolbar bar = mLayout != null ? (Toolbar) mLayout.findViewById(R.id.toolbar) : null;
        if (bar == null || mItemListType == null) return;
        switch (mItemListType) {
            default:
            case group:
                setTitle(bar, getResources().getString(R.string.app_name));
                break;
            case room:
                // Determine if there is an item. If so, use the group name as the title and clearn
                // the subtitle, otherwise use the app name.
                setTitle(bar, mItem);
                break;
            case message:
                // Determine if there is an item. If so, use the group name as the title and clearn
                // the subtitle, otherwise use the app name.
                setTitleAndSubtitle(bar, mItem);
                break;
            case joinRoom:
                String title = getResources().getString(R.string.JoinRoomsMenuTitle);
                String key = mItem != null ? mItem.groupKey : null;
                String subtitle = key != null
                        ? DatabaseListManager.instance.getGroupName(key) : null;
                setTitles(bar, title, subtitle);
                break;
        }
    }

    /** Implement the setTitles() contract. */
    @Override protected void setTitles(final String groupKey, final String roomKey) {
        setTitles();
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
                Group group = DatabaseListManager.instance.getGroupProfile(groupKey);
                if (group != null)
                    // The group is available.  Accept any open invitations. There should be at
                    // least one!
                    InvitationManager.instance.acceptGroupInvite(account, groupKey);
            }
        }
    }

    /** Do a redisplay to catch potential changes that should be shown in the current view. */
    private void redisplay() {
        FabManager.chat.init(this);
        if (mAdView != null) mAdView.resume();
        if (mItemListType != null) updateAdapterList();
        setTitles();
    }

    /** Set the titles in the given toolbar using the given (possibly null) titles. */
    private void setTitles(@NonNull final Toolbar bar, final String title, final String subtitle) {
        // Apply the given titles to the toolbar; nulls will clear the fields.
        bar.setTitle(title);
        bar.setSubtitle(subtitle);
    }

    /** Set the titles in the given toolbar using the given title; reset the subtitle. */
    private void setTitle(@NonNull final Toolbar bar, final String title) {
        setTitles(bar, title, null);
    }

    /** Set the titles in the given toolbar using the given item. */
    private void setTitle(@NonNull final Toolbar bar, final ChatListItem item) {
        // Determine if the item is available.  Use the app name if not.
        String title = item != null && item.groupKey != null
                ? DatabaseListManager.instance.getGroupName(item.groupKey)
                : getResources().getString(R.string.app_name);
        setTitles(bar, title, null);
    }

    /** Set the titles in the given toolbar using the given item. */
    private void setTitleAndSubtitle(@NonNull final Toolbar bar, final ChatListItem item) {
        // Determine if the item is available.  Use the app name if not.
        String title = item != null && item.key != null
                ? DatabaseListManager.instance.getRoomName(item.key)
                : getResources().getString(R.string.app_name);
        String subtitle = item != null && item.groupKey != null
                ? DatabaseListManager.instance.getGroupName(item.groupKey) : null;
        setTitles(bar, title, subtitle);
    }

    /** Return TRUE iff the list can be considered up to date. */
    private boolean updateAdapterList() {
        // Determine if the fragment has a view and that it has a list type.
        View layout = getView();
        if (layout == null || mItemListType == null) return false;

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
        List<ChatListItem> items = DatabaseListManager.instance.getList(mItemListType, mItem);
        Log.d(TAG, String.format(Locale.US, "Updating with %d items.", items.size()));
        listAdapter.addItems(items);
        if (mItemListType == message) view.scrollToPosition(listAdapter.getItemCount() - 1);
        return true;
    }

    // Protected inner classes.

    /** Provide an exit handler to abort the fragment. */
    protected class ExitHandler implements View.OnClickListener {
        @Override public void onClick(final View view) {
            ChatManager.instance.startNextFragment(getActivity());
        }
    }

    /** Provide a handler that will generate a backpress event. */
    protected class UpHandler implements View.OnClickListener {
        /** Handle a click on the back arrow button by generating a back press. */
        public void onClick(final View view) {
            getActivity().onBackPressed();
        }
    }

}
