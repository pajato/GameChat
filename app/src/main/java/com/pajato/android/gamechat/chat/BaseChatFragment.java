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
import com.pajato.android.gamechat.event.MenuItemEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.LinearLayoutCompat.VERTICAL;
import static com.pajato.android.gamechat.chat.ChatFragmentType.messageList;
import static com.pajato.android.gamechat.chat.ChatFragmentType.roomList;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.GROUP_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.ROOM_ITEM_TYPE;
import static com.pajato.android.gamechat.database.DatabaseListManager.ChatListType.message;

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

    /** The lifecycle event format string with no bundle. */
    private static final String FORMAT_NO_BUNDLE =
        "Event: %s; Fragment: %s; Fragment Manager: %s; Fragment List Type: %s.";

    /** The lifecycle event format string with a bundle provided. */
    private static final String FORMAT_WITH_BUNDLE =
        "Event: %s; Fragment: %s; Fragment Manager: %s; Fragment List Type: %s; Bundle: %s.";

    // Protected instance variables.

    /** Show an ad at the top of the view. */
    protected AdView mAdView;

    /** The item information passed from the parent fragment. */
    protected ChatListItem mItem;

    /** The list type for this fragment. */
    protected DatabaseListManager.ChatListType mItemListType;

    /** A flag used to queue adapter list updates during the onResume lifecycle event. */
    protected boolean mUpdateOnResume;

    // Public instance methods.

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) mAdView.destroy();
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
        // Log the event, handle ads and apply any queued adapter updates.  Only one try is
        // attempted.
        super.onResume();
        FabManager.chat.init(this);
        if (mAdView != null) mAdView.resume();
        if (!mUpdateOnResume) return;
        updateAdapterList();
        mUpdateOnResume = false;
    }

    /** Set the item defining this fragment (passed from the parent (spawning) fragment. */
    public void setItem(final ChatListItem item) {
        mItem = item;
    }

    /** Return TRUE iff the list can be considered up to date. */
    public boolean updateAdapterList() {
        // Determine if the fragment has a view and that it has a list type.
        View layout = getView();
        if (layout == null || mItemListType == null) return false;

        // It has both.  Ensure that the list view (recycler) exists.
        RecyclerView view = (RecyclerView) layout.findViewById(R.id.chatList);
        if (view == null) return false;

        // The recycler view exists.  Show the chat list, either groups, messages or rooms.
        RecyclerView.Adapter adapter = view.getAdapter();
        if (!(adapter instanceof ChatListAdapter)) return true;

        // Inject the list items into the recycler view making sure to scroll to the end of the
        // list when showing messages.
        ChatListAdapter listAdapter = (ChatListAdapter) adapter;
        listAdapter.clearItems();
        listAdapter.addItems(DatabaseListManager.instance.getList(mItemListType, mItem));
        if (mItemListType == message) view.scrollToPosition(listAdapter.getItemCount() - 1);
        return true;
    }

    // Protected instance methods.

    /** Initialize the fragment's chat list. */
    protected void initList(@NonNull final View layout, final List<ChatListItem> items,
                            final boolean stackFromEnd) {
        // Initialize the recycler view.
        Context context = layout.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.chatList);
        if (stackFromEnd) layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up the adapter on the recycler view with a set of default messages.
        ChatListAdapter adapter = new ChatListAdapter();
        adapter.addItems(items);
        recyclerView.setAdapter(adapter);
    }

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
        String manager = getFragmentManager().toString();
        String format = FORMAT_NO_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, event, this, manager, mItemListType));
    }

    /** Log a lifecycle event that has a bundle. */
    @Override protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        String format = FORMAT_WITH_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, event, this, manager, mItemListType, bundle));
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
        groupList.add("-KUpGKrumJPeBi8-dTqH");      // Paul Reilly Group
        groupList.add("-KUpHCHRnXW0IvIDe4hd");      // GameChat Group
        groupList.add("-KUpHLkxb9c1nM2O8Ocs");      // Pajato Technologies LLC
        groupList.add("-KUpHWgtdLSnFwMiMp0k");      // Pajato Support Group
        for (String groupKey : groupList) {
            // Extend an invitation to the group and verify that this group has been joined.
            InvitationManager.instance.extendGroupInvite(account, groupKey);
            if (!account.groupIdList.contains(groupKey)) {
                // Join the group now if it has been loaded.  It will be queued for joining later if
                // necessary.
                Group group = DatabaseListManager.instance.getGroupProfile(groupKey);
                if (group != null)
                    // The group is available.  Accept any open invitations. There should be at
                    // least one!
                    InvitationManager.instance.acceptGroupInvite(account, group, groupKey);
            }
        }
    }

}
