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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.chat.adapter.RoomsListAdapter;
import com.pajato.android.gamechat.database.DatabaseEventHandler;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public class RoomsFragment extends BaseFragment {

    // Public instance variables.

    /** Show an ad at the top of the view. */
    private AdView mAdView;

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
        // Enable the options menu, layout the fragment, set up the ad view and the listeners for
        // backend data changes.
        setHasOptionsMenu(true);
        View result = inflater.inflate(R.layout.fragment_rooms, container, false);
        mAdView = (AdView) result.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        EventBusManager.instance.register(this);
        FabManager.instance.init(result);
        return result;
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
            // TODO: Handle a seach in the rooms panel by fast scrolling to room.
            break;
        default:
            break;

        }
        return super.onOptionsItemSelected(item);
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
        EventBusManager.instance.unregister(this);
    }

    /** Set up the main recycler ... */
    @Override public void onStart() {
        // Initialize the recycler view.
        super.onStart();
        View view = getView();
        if (view != null) {
            LinearLayoutManager linearLayoutManager =
                    new LinearLayoutManager(view.getContext(), OrientationHelper.VERTICAL, false);
            RecyclerView mRecyclerView = (RecyclerView) getView().findViewById(R.id.rooms_list);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(new RoomsListAdapter());
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

        // Register a group list change handler on the account, if there is one active and finally
        // register the event bus for this class.
        Account account = AccountManager.instance.getCurrentAccount();
        String accountId = account != null ? account.accountId : null;
        if (accountId != null) {
            // There is an active account.  Register it.
            String path = String.format("/accounts/%s/groupIdList", accountId);
            DatabaseEventHandler handler = new GroupChangeHandler("roomsGroupChangeHandler", path);
            DatabaseManager.instance.registerHandler(handler);
        }
        EventBusManager.instance.register(this);
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /** Update the UI based on the rooms and accounts present in the current account. */
    @Subscribe public void updateUI(final AccountStateChangeEvent event) {
        manageRoomsUI(event.account.groupIdList);
    }

    // Private instance methods.

    /** Manage the Rooms pane UI for a given set of groups. */
    private void manageRoomsUI(final List<String> groups) {
        // Set the content screen in the FAB menu and render the content screen as well after
        // determining that the content panel view exists.
        View layout = getView();
        if (layout != null) {
            FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.rooms_fab);
            View content = layout.findViewById(groups == null ? R.id.rooms_none : R.id.rooms_main);
            FabManager.instance.setContentView(fab, content);

            // Set up the main content screen showing either a "no rooms" view or a list of groups.
            View roomsNone = layout.findViewById(R.id.rooms_none);
            View roomsMain = layout.findViewById(R.id.rooms_main);
            if (roomsNone != null) {
                roomsNone.setVisibility(groups == null ? View.VISIBLE : View.GONE);
            }
            if (roomsMain != null) {
                roomsMain.setVisibility(groups == null ? View.GONE : View.VISIBLE);
            }
        }
    }

    // Private classes.

    /** Provide a class to handle structural changes to a User's groups. */
    private class GroupChangeHandler extends DatabaseEventHandler implements ValueEventListener {

        // Private instance constants.

        /** The logcat TAG. */
        private final String TAG = this.getClass().getSimpleName();

        // Public constructors.

        /** Build a handler with the given name and path. */
        GroupChangeHandler(final String name, final String path) {
            super(name, path);
        }

        /** Get the current set of groups using a list of group identifiers. */
        @Override public void onDataChange(final DataSnapshot dataSnapshot) {
            // Determine how many groups are available to extract rooms from.

            if (dataSnapshot.exists()) {
                Log.d(TAG, "Value is: " + dataSnapshot.getValue());
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> groupIdList = dataSnapshot.getValue(t);
                manageRoomsUI(groupIdList != null && groupIdList.size() > 0 ? groupIdList : null);
            } else {
                manageRoomsUI(null);
            }
        }

        /** ... */
        @Override public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }

        // Private instance methods.

    }

}
