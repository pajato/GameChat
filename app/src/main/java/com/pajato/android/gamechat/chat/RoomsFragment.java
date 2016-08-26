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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.adapter.RoomsListAdapter;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.event.JoinedRoomListChangeEvent;
import com.pajato.android.gamechat.event.MessageListChangeEvent;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public class RoomsFragment extends BaseFragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = RoomsFragment.class.getSimpleName();

    // Public instance variables.

    /** Show an ad at the top of the view. */
    private AdView mAdView;

    /** The array of content views.  One will be selected to show in the rooms panel. */
    private SparseArray<View> mContentViewMap = new SparseArray<>();

    // Public instance methods.

    /** Process a given button click event looking for one on the rooms fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the rooms fab button.
        int value = event.getView() != null ? event.getView().getId() : 0;
        switch (value) {
        case R.id.rooms_fab:
            // It is a rooms fab button.  Toggle the state.
            FabManager.instance.toggle((FloatingActionButton) event.getView());
            break;
        case R.id.addGroupButton:
        case R.id.addGroupMenuItem:
            // Dismiss the FAB menu, and start up the add group activity.
            View view = getActivity().findViewById(R.id.rooms_fab);
            FabManager.instance.dismissMenu((FloatingActionButton) view);
            Intent intent = new Intent(this.getActivity(), AddGroupActivity.class);
            startActivity(intent);
            break;
        default:
            // Ignore everything else.
            break;
        }
    }

    /** Handle the setup for the rooms panel. */
    @Override public View onCreateView(final LayoutInflater inflater,
                                       final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Provide a loading indicator, enable the options menu, layout the fragment, set up the ad
        // view and the listeners for backend data changes.
        ProgressManager.instance.show(this.getContext());
        setHasOptionsMenu(true);
        View layout = inflater.inflate(R.layout.fragment_rooms, container, false);
        initAdView(layout);
        initRoomsList(layout);
        EventBusManager.instance.register(this);
        FabManager.instance.init(layout);
        ChatManager.instance.init();

        return layout;
    }

    /** Post the chat options menu on demand. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.chat_menu, menu);
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
        case R.id.toolbar_search_icon:
            // TODO: Handle a search in the rooms panel by fast scrolling to room.
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
        EventBusManager.instance.unregister(this);
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onResume() {
        // When resuming, use the base class to log it, manage the ad view and the main view, set a
        // grooup id list value event listener and register the fragment to be an event handler.
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        EventBusManager.instance.register(this);
        EventBusManager.instance.register(ChatManager.instance);
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /** Deal with a change in the active rooms state. */
    @Subscribe public void onJoinedRoomListChange(@NonNull final JoinedRoomListChangeEvent event) {
        // Turn off the loading progress dialog and handle a signed in account with some joined
        // rooms by rendering the list.
        ProgressManager.instance.hide();
        if (event.joinedRoomList.size() == 0) {
            // Handle the case where there are no active rooms by enabling the no rooms message.
            showContent(R.id.rooms_none);
        } else {
            // Handle a joined rooms change by setting up database watchers on the messages in each
            // room.
            for (String joinedRoom : event.joinedRoomList) {
                // Set up the database watcher on this list.
                String[] split = joinedRoom.split(" ");
                String groupKey = split[0];
                String roomKey = split[1];
                ChatManager.instance.setMessageWatcher(groupKey, roomKey);
            }
            showContent(R.id.rooms_main);
        }
    }

    // Private instance methods.

    /** Initialize the ad view by building and loading an ad request. */
    private void initAdView(@NonNull final View layout) {
        mAdView = (AdView) layout.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    /** Initialize the active rooms list by setting up the recycler view. */
    private void initRoomsList(@NonNull final View layout) {
        // Initialize the recycler view.
        Context context = layout.getContext();
        int direction = OrientationHelper.VERTICAL;
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, direction, false);
        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.rooms_list);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(new RoomsListAdapter());
    }

    /** Manage the rooms panel content by showing the view for the given resource id. */
    private void showContent(final int resourceId) {
        // Determine if the rooms panel view exists.
        View layout = getView();
        if (layout == null) {
            // It does not! Abort.
            return;
        }

        // The rooms panel layout does exist.  Ensure that the view associated with the given
        // resource is in the map.
        View showView = mContentViewMap.get(resourceId);
        if (showView == null) {
            // The view is not in the map yet.  Add it now.
            showView = layout.findViewById(resourceId);
            mContentViewMap.append(resourceId, showView);
        }

        // Make all the content views be gone and then make the view associated with the given
        // resource identifier visible.
        for (int i = 0; i < mContentViewMap.size(); i++) {
            int key = mContentViewMap.keyAt(i);
            View view = mContentViewMap.get(key);
            view.setVisibility(View.GONE);
        }

        // Make the desired content view visible, if it actually exists.
        if (showView != null) showView.setVisibility(View.VISIBLE);
    }

    /** Manage the rooms list UI every time a message change occurs. */
    @Subscribe public void onMessageListChange(final MessageListChangeEvent event) {
        // Determine if the active rooms view has been inflated.  It damned well should be.
        View layout = getView();
        if (layout != null) {
            // It has.  Publish the active rooms state using a Inbox by Google layout.
            RecyclerView roomsListView = (RecyclerView) layout.findViewById(R.id.rooms_list);
            if (roomsListView != null) {
                RecyclerView.Adapter adapter = roomsListView.getAdapter();
                if (adapter instanceof RoomsListAdapter) {
                    // TODO: parse the rooms to build a list of active room information.  For now,
                    // inject dummy data into the list view adapter.
                    RoomsListAdapter listAdapter = (RoomsListAdapter) adapter;
                    listAdapter.clearItems();
                    //listAdapter.addItems(DummyData.getData());
                    listAdapter.addItems(ChatManager.instance.getData());
                    roomsListView.setVisibility(View.VISIBLE);
                    showContent(R.id.rooms_main);
                }
            }
        } else {
            Log.e(TAG, "The rooms fragment layout does not exist yet!");
        }
    }

    // Private classes.

}
