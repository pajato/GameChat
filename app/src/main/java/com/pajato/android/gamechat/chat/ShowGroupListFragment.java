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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.event.JoinedRoomListChangeEvent;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoAccount;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoJoinedRooms;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showRoomList;

/**
 * Provide a fragment to handle the display of the groups available to the current user.  This is
 * the top level view in the chat hierarchy.  It shows all the joined groups and allows for drilling
 * into rooms and chats within those rooms.
 *
 * @author Paul Michael Reilly
 */
public class ShowGroupListFragment extends BaseFragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ShowGroupListFragment.class.getSimpleName();

    // Public instance methods.

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        int value = event.getView() != null ? event.getView().getId() : 0;
        switch (value) {
            case R.id.chatFab:
                // It is a chat fab button.  Toggle the state.
                FabManager.chat.toggle(getView());
                break;
            case R.id.addGroupButton:
            case R.id.addGroupMenuItem:
                // Dismiss the FAB menu, and start up the add group activity.
                FabManager.chat.dismissMenu();
                Intent intent = new Intent(this.getActivity(), AddGroupActivity.class);
                startActivity(intent);
                break;

        default:
            // Drill down into the the group.
            showRooms(event.getView());
            break;
        }
    }

    /** Handle an account state change event by showing the no account fragment if necessary. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // Determine if this represents a no account situation due to a sign out event.
        if (event.account == null) {
            // There is no account.  Switch to the no account fragment.
            ChatManager.instance.replaceFragment(showNoAccount, this.getActivity());
        }
    }

    /** Deal with the options menu by hiding the back button. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Turn off the back option and turn on the search option.
        setOptionsMenu(menu, inflater, new int[] {R.id.search}, new int[] {R.id.back});
    }

    /** Handle the setup for the groups panel. */
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Provide a loading indicator, enable the options menu, layout the fragment, set up the ad
        // view and the listeners for backend data changes.
        setTitles(null, null);
        setHasOptionsMenu(true);
        mItemListType = ChatListManager.ChatListType.group;
        View layout = inflater.inflate(R.layout.fragment_chat_groups, container, false);
        initAdView(layout);
        initList(layout);
        EventBusManager.instance.register(this);

        return layout;
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
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
            case R.id.search:
                // TODO: Handle a search in the groups panel by fast scrolling to room.
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /** Deal with the fragment's activity's lifecycle pause event. */
    @Override public void onPause() {
        // Deal with the ad and turn off app event listeners.
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onResume() {
        // When resuming, use the base class to log it, manage the ad view and the main view, set a
        // grooup id list value event listener and register the fragment to be an event handler.
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        FabManager.chat.setState(View.VISIBLE);
        EventBusManager.instance.register(this);
    }

    // Private instance methods.

    /** Initialize the joined rooms list by setting up the recycler view. */
    private void initList(@NonNull final View layout) {
        // Initialize the recycler view.
        Context context = layout.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.chatList);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ChatListAdapter adapter = new ChatListAdapter();
        adapter.addItems(ChatListManager.instance.getList(mItemListType, mItem));
        mRecyclerView.setAdapter(adapter);
    }

    /** Handle a button click on a group by showing the rooms in the group. */
    private void showRooms(final View view) {
        // Ensure that the list item contains a list item payload.
        Object payload = view.getTag();
        if (payload instanceof ChatListItem) {
            // It does.  Show the rooms in the group.
            ChatListItem item = (ChatListItem) payload;
            ChatManager.instance.chainFragment(showRoomList, this.getActivity(), item);
        }
    }

}
