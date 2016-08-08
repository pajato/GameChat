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

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.pajato.android.gamechat.game.GameManager;
import com.pajato.android.gamechat.main.PaneManager;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

/**
 * Provide a fragment to handle the display of the rooms available to the current user.
 *
 * @author Paul Michael Reilly
 */
public class RoomsFragment extends Fragment {

    // Public instance variables.

    /** Show an ad at the top of the view. */
    private AdView mAdView;

    /** Handle the setup for the rooms panel. */
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        // Enable the options menu, layout the fragment and set up the ad view.
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
        initFabListener();
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
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Deal with the fragment's activity's lifecycle by managing the ad. */
    @Override public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /** Setup the chat functions provided by the FAB button. */
    private void initFabListener() {
        // Set up the FAB speed dial menu.
        FabSpeedDial fab = (FabSpeedDial) getActivity().findViewById(R.id.fab_speed_dial);
        fab.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                switch(menuItem.getItemId()) {
                    // Start a new Tic-Tac-Toe game
                    case R.id.fab_ttt:
                        if(viewPager != null) { viewPager.setCurrentItem(PaneManager.GAME_INDEX); }
                        GameManager.instance.sendNewGame(GameManager.TTT_LOCAL_INDEX, getActivity());
                        break;
                    // Navigate to the Game Settings panel
                    case R.id.fab_new_game:
                        if(viewPager != null) { viewPager.setCurrentItem(PaneManager.GAME_INDEX); }
                        GameManager.instance.sendNewGame(GameManager.INIT_INDEX, getActivity());
                    default:
                        break;
                }
                return false;
            }
        });
    }

}
