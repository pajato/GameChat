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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import static com.pajato.android.gamechat.R.array.groups;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public class RoomsFragment extends BaseFragment {

    // Public instance variables.

    /** The Firebase account value event listener. */
    private ValueEventListener mAccountChangeHandler;
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
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        // Enable the options menu, layout the fragment, set up the ad view and the listeners for
        // backend data changes.
        setHasOptionsMenu(true);
        View result = inflater.inflate(R.layout.fragment_rooms, container, false);
        mAdView = (AdView) result.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
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
        setValueEventListener(null);
        EventBus.getDefault().unregister(this);
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onResume() {
        // When resuming, use the base class to log it, manage the ad view and the main view, set a
        // grooup id list value event listener and register the fragment to be an event handler.
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        setValueEventListener(new GroupChangeHandler());
        EventBus.getDefault().register(this);
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    // Private instance methods.

    /** Set or clear the account change value event listener. */
    private void setValueEventListener(final GroupChangeHandler handler) {
        // Determine whether to add or remove the account value event listener.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;
        String path = String.format("/accounts/%s/groupIdList", uid);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(path);
        if (uid != null && handler != null) {
            // Install a handler.
            mAccountChangeHandler = handler;
            database.addValueEventListener(mAccountChangeHandler);
        } else {
            // Remove a previously installed handler.
            database.removeEventListener(mAccountChangeHandler);
        }
    }

    /** Manage the Rooms pane UI for a given set of groups. */
    private void manageRoomsUI(final List<String> groups) {
        // Set the content screen in the FAB menu and render the content screen as well after
        // determining that the content panel view exists.
        View layout = getView();
        if (layout != null) {
            FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.rooms_fab);
            View menu = layout.findViewById(R.id.rooms_fab_menu);
            View content = layout.findViewById(groups == null ? R.id.rooms_none : R.id.rooms_main);
            FabManager.instance.setContentView(fab, content);

            // Set up the main content screen showing either a "no rooms" view or a list of groups.
            View roomsNone = layout.findViewById(R.id.rooms_none);
            View roomsMain = layout.findViewById(R.id.rooms_main);
            if (roomsNone != null)
                roomsNone.setVisibility(groups == null ? View.VISIBLE : View.GONE);
            if (roomsMain != null)
                roomsMain.setVisibility(groups == null ? View.GONE : View.VISIBLE);
        }
    }

    // Private classes.

    /** Provide a class to handle structural changes to a User's groups. */
    private class GroupChangeHandler implements ValueEventListener {

        // Private instance constants.

        /** The logcat TAG. */
        private final String TAG = this.getClass().getSimpleName();

        /** Get the current set of groups using a list of group identifiers. */
        @Override public void onDataChange(final DataSnapshot dataSnapshot) {
            // Determine how many groups are available to extract rooms from.
            if (dataSnapshot.exists()) {
                Log.d(TAG, "Value is: " + dataSnapshot.getValue());
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> groupIdList = dataSnapshot.getValue(t);
                manageRoomsUI(groupIdList != null && groupIdList.size() > 0 ? groupIdList : null);
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
