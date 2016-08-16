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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        // Enable the options menu, layout the fragment, set up the ad view and the FAB button.
        setHasOptionsMenu(true);
        View result = inflater.inflate(R.layout.fragment_rooms, container, false);
        mAdView = (AdView) result.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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
        EventBus.getDefault().unregister(this);
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onResume() {
        // When resuming, use the base class to log it, manage the ad view and the main view, and
        // register the fragment to be an event handler.
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        selectMainView();
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

    /** Select the view to be visible: either the "no groups" or the "show groups" view. */
    private void selectMainView() {
        View view = getView();
        if (view != null) {
            FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.rooms_fab);
            View menu = view.findViewById(R.id.rooms_fab_menu);
            View content = view.findViewById(R.id.rooms_none);
            FabManager.instance.init(fab, content, menu);
        }
    }

}
