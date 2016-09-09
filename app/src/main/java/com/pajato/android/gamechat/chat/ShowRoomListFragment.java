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
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.JoinedRoomListChangeEvent;
import com.pajato.android.gamechat.event.MessageListChangeEvent;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showMessages;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoAccount;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoJoinedRooms;

/**
 * Provide a fragment to handle the display of the groups available to the current user.  This is
 * the top level view in the chat hierarchy.  It shows all the joined groups and allows for drilling
 * into rooms and chats within those rooms.
 *
 * @author Paul Michael Reilly
 */
public class ShowRoomListFragment extends BaseFragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ShowRoomListFragment.class.getSimpleName();

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        int value = event.getView() != null ? event.getView().getId() : 0;
        switch (value) {
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
                // Determine if the event has a chat list item representing a room with messages to
                // show.
                Object payload = event.getView().getTag();
                if (payload instanceof ChatListItem) {
                    // It does.  Show the messages in the room.
                    ChatListItem item = (ChatListItem) payload;
                    ChatManager.instance.chainFragment(showMessages, getActivity(), item);
                }
                break;
        }
    }

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_chat_rooms;}

    /** Handle an account state change event by showing the no sign in message. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // Determine if this represents a no account situation due to a sign out event.
        if (event.account == null) {
            // There is no account.  Switch to the no account fragment.
            ChatManager.instance.replaceFragment(showNoAccount, this.getActivity());
        }
    }

    /** Provide a general button click handler to post the click for general consumption. */
    public void onClick(final View view) {
        String className = view.getClass().getName();
        EventBus.getDefault().post(new ClickEvent(getContext(), -1, view, null, className));
    }

    /** Deal with the options menu creation by making the search and back items visible. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Turn on both the back and search buttons.
        setOptionsMenu(menu, inflater, new int[] {R.id.back, R.id.search}, null);
    }

    /** Handle the setup for the groups panel. */
    @Override public void onInitialize() {
        // Inflate the layout for this fragment and initialize by setting the titles, declaring the
        // use of the options menu, setting up the ad view and initializing the rooms handling.
        setTitles(mItem.groupKey, null);
        setHasOptionsMenu(true);
        mItemListType = ChatListManager.ChatListType.room;
        initAdView(mLayout);
        initList(mLayout, ChatListManager.instance.getList(mItemListType, mItem), false);
    }

    /** Deal with a change in the joined rooms state. */
    @Subscribe public void onJoinedRoomListChange(@NonNull final JoinedRoomListChangeEvent event) {
        // Determine if there are no joined rooms.
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

    /** Manage the groups list UI every time a message change occurs. */
    @Subscribe public void onMessageListChange(final MessageListChangeEvent event) {
        // Determine if the groups panel has been inflated.  It damned well should be.
        View layout = getView();
        if (layout != null) {
            // It has.  Publish the joined rooms state using a Inbox by Google layout.
            RecyclerView roomsListView = (RecyclerView) layout.findViewById(R.id.chatList);
            if (roomsListView != null && mItem != null) {
                RecyclerView.Adapter adapter = roomsListView.getAdapter();
                if (adapter instanceof ChatListAdapter) {
                    // Get the data to display.
                    ChatListAdapter listAdapter = (ChatListAdapter) adapter;
                    listAdapter.clearItems();
                    listAdapter.addItems(ChatListManager.instance.getList(mItemListType, mItem));
                    roomsListView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            Log.e(TAG, "The groups fragment layout does not exist yet!");
        }
    }

    /** Handle an options menu choice. */
    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_game_icon:
                // Show the game panel.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if(viewPager != null) {
                    viewPager.setCurrentItem(PaneManager.GAME_INDEX);
                }
                break;
            case R.id.back:
                // Pop back to the groups list view.
                ChatManager.instance.popBackStack(getActivity());
                break;
            case R.id.search:
                // TODO: Handle a search in the groups view by fast scrolling to some list item
                // containting the search text.
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /** Deal with the fragment's activity's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Turn off the FAB.
        FabManager.chat.setState(this, View.VISIBLE);
        super.onResume();
    }

    /** Use the start lifecycle event to initialize the data. */
    @Override public void onStart() {
        // Display messages modified in the room using the message change handler with a null event.
        super.onStart();
        onMessageListChange(null);
    }

}
