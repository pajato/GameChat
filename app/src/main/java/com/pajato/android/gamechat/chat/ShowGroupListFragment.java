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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.chat.adapter.GroupsListAdapter;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.event.JoinedRoomListChangeEvent;
import com.pajato.android.gamechat.event.MessageListChangeEvent;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

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

    // Public instance variables.

    /** Show an ad at the top of the view. */
    private AdView mAdView;

    /** The array of content views.  One will be selected to show in the chat panel. */
    private SparseArray<View> mContentViewMap = new SparseArray<>();

    // Public instance methods.

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        int value = event.getView() != null ? event.getView().getId() : 0;
        switch (value) {
        case R.id.groupsFab:
            // It is a chat fab button.  Toggle the state.
            FabManager.room.toggle((FloatingActionButton) event.getView());
            break;
        case R.id.addGroupButton:
        case R.id.addGroupMenuItem:
            // Dismiss the FAB menu, and start up the add group activity.
            View view = getActivity().findViewById(R.id.groupsFab);
            FabManager.room.dismissMenu((FloatingActionButton) view);
            Intent intent = new Intent(this.getActivity(), AddGroupActivity.class);
            startActivity(intent);
            break;
        default:
            // Ignore everything else.
            break;
        }
    }

    /** Handle an account state change event by showing the no sign in message. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // Ensure that there is a layout to use.
        View layout = getView();
        if (layout == null) {
            Log.e(TAG, "There is no groups fragment layout! Aborting.");
            return;
        }

        // Dismiss the progress loading dialog if one is active and determine if there is no account
        // (sign out).
        ProgressManager.instance.hide();
        if (event.account == null) {
            // There is no account.  Set the empty list message text appropriately and show it as
            // the main content.
            TextView textView = (TextView) layout.findViewById(R.id.emptyListMessage);
            textView.setText(layout.getContext().getString(R.string.noSignInMessage));
            showContent(layout, R.id.emptyListContent);
        } else {
            // There is an account.  Show the main list content.
            showContent(layout, R.id.groupsListContent);
        }
    }

    /** Post the chat options menu on demand. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.chat_menu, menu);
    }

    /** Handle the setup for the groups panel. */
    @Override public View onCreateView(final LayoutInflater inflater,
                                       final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Provide a loading indicator, enable the options menu, layout the fragment, set up the ad
        // view and the listeners for backend data changes.
        ProgressManager.instance.show(this.getContext());
        setHasOptionsMenu(true);
        View layout = inflater.inflate(R.layout.fragment_chat_groups, container, false);
        init(layout);

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
        ProgressManager.instance.hide();
        View layout = getView();
        if (event.joinedRoomList.size() == 0) {
            // Handle the case where there are no joined rooms by enabling the no rooms message.
            showContent(layout, R.id.emptyListContent);
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
            showContent(layout, R.id.groupsListContent);
        }
    }

    /** Manage the groups list UI every time a message change occurs. */
    @Subscribe public void onMessageListChange(final MessageListChangeEvent event) {
        // Determine if the groups panel has been inflated.  It damned well should be.
        View layout = getView();
        if (layout != null) {
            // It has.  Publish the joined rooms state using a Inbox by Google layout.
            RecyclerView groupsListView = (RecyclerView) layout.findViewById(R.id.groupsList);
            if (groupsListView != null) {
                RecyclerView.Adapter adapter = groupsListView.getAdapter();
                if (adapter instanceof GroupsListAdapter) {
                    // TODO: parse the rooms to build a list of active room information.  For now,
                    // inject dummy data into the list view adapter.
                    GroupsListAdapter listAdapter = (GroupsListAdapter) adapter;
                    listAdapter.clearItems();
                    //listAdapter.addItems(DummyData.getData());
                    listAdapter.addItems(ChatManager.instance.getData());
                    groupsListView.setVisibility(View.VISIBLE);
                    showContent(layout, R.id.groupsListContent);
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
            case R.id.toolbar_search_icon:
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
        AccountManager.instance.register();
    }

    // Private instance methods.

    /** Initialize for the given layout. */
    private void init(final View layout) {
        // Set up the ad view, the groups list and the listeners.
        initAdView(layout);
        initGroupsList(layout);
        EventBusManager.instance.register(this);
        FabManager.room.init(layout);
        ChatManager.instance.init();
        AccountManager.instance.init();

        // Dismiss the progress manager if one is showing and show the empty list content by
        // default.
        ProgressManager.instance.hide();
        if (mContentViewMap.size() == 0) {
            showContent(layout, R.id.emptyListContent);
        }
    }

    /** Initialize the ad view by building and loading an ad request. */
    private void initAdView(@NonNull final View layout) {
        mAdView = (AdView) layout.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    /** Initialize the joined rooms list by setting up the recycler view. */
    private void initGroupsList(@NonNull final View layout) {
        // Initialize the recycler view.
        Context context = layout.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.groupsList);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(new GroupsListAdapter());
    }

    /** Manage the groups panel content by showing the view for the given resource id. */
    private void showContent(final View layout, final int resourceId) {
        // Determine if the groups panel view exists.
        if (layout == null) {
            // It does not! Abort.
            return;
        }

        // The groups panel layout does exist.  Ensure that the view associated with the given
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

    // Private classes.

}
