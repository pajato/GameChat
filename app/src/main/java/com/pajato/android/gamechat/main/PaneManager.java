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

package com.pajato.android.gamechat.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.ChatFragment;
import com.pajato.android.gamechat.fragment.GameFragment;

import java.util.ArrayList;
import java.util.List;

/** Provide a singleton to manage the main fragments used to display the chat and game panels. */
public enum PaneManager {
    instance;

    // Private class constants

    /** The logcat tag constant. */
    private static final String TAG = PaneManager.class.getSimpleName();

    /** The fragment list index for the game fragment. */
    private static final int GAME_INDEX = 1;

    // Private instance variables

    /** The view pager adapter. */
    private GameChatPagerAdapter mAdapter;

    /** The repository for the pane fragments. */
    private List<Fragment> fragmentList = new ArrayList<>();

    /** The repository for the fragment titles. */
    private List<String> titleList = new ArrayList<>();

    // Public instance methods

    /**
     * Set up the app per the characteristics of the running device.
     *
     * @see android.app.Activity#onCreate(Bundle)
     */
    public void init(final AppCompatActivity context) {
        // Clear the current panes and determine if a paging layout is active.
        fragmentList.clear();
        titleList.clear();
        titleList.add(context.getString(R.string.chat));
        titleList.add(context.getString(R.string.game));
        ViewPager viewPager = (ViewPager) context.findViewById(R.id.viewpager);
        if (viewPager != null) {
            // The app is running on a smart phone.  Create the pane fragments and set up the
            // adapter for the pager.
            fragmentList.add(new ChatFragment());
            fragmentList.add(new GameFragment());
            viewPager.setAdapter(new GameChatPagerAdapter(context.getSupportFragmentManager()));
            TabLayout tabLayout = (TabLayout) context.findViewById(R.id.tablayout);
            tabLayout.setupWithViewPager(viewPager);
        } else {
            // The app is running on a tablet and the fragments have been created.  Add them to the
            // fragment map.
            //TODO: uncomment these for tablets:
            //fragmentList.add((Fragment) context.findViewById(R.id.chatFragment));
            //fragmentList.add((Fragment) context.findViewById(R.id.gameFragment));
        }
    }

    /** Handle the delegated tile click by delegating it to the game fragment. */
    public void tileOnClick(final View view) {
        // Delegate this to the game fragment.
        ((GameFragment) fragmentList.get(GAME_INDEX)).tileOnClick(view);
    }

    /** Handle the delegated new game click by delegating it to the game fragment. */
    public void onNewGame(final View view) {
        // Delegate this to the game fragment.
        ((GameFragment) fragmentList.get(GAME_INDEX)).onNewGame(view);
    }

    // Nested classes

    /** Provide a class to handle the view pager setup. */
    private class GameChatPagerAdapter extends FragmentPagerAdapter {

        /** Build an adapter to handle the panels for a given fragment manager. */
        public GameChatPagerAdapter(final FragmentManager manager) {
            // Create the adapter and add the panels to the panel list.
            super(manager);
        }

        /** Implement getItem() by using the fragment list. */
        @Override public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        /** Implement getCount() by using the fragment list size. */
        @Override public int getCount() {
            return fragmentList.size();
        }

        /** Implement getPageTitle() by using the title list. */
        @Override public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

    }

}
