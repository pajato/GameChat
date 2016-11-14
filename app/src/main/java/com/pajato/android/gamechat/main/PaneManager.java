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

import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.ChatFragment;
import com.pajato.android.gamechat.exp.GameFragment;

import java.util.ArrayList;
import java.util.List;

/** Provide a singleton to manage the main fragments used to display the app panels. */
public enum PaneManager {
    instance;

    // Private class constants

    /** The fragment list index for the game fragment. */
    public static final int CHAT_INDEX = 0;

    /** The fragment list index for the game fragment. */
    public static final int GAME_INDEX = 1;

    // Private instance variables

    /** The repository for the pane fragments. */
    private List<Fragment> fragmentList = new ArrayList<>();

    /** The repository for the fragment titles. */
    private List<String> titleList = new ArrayList<>();

    // Public instance methods

    /** Initialize the two central panels in the app: chat and game/activity. */
    public void init(final AppCompatActivity context) {
        // Clear then add in the two main panels.
        fragmentList.clear();
        titleList.clear();
        titleList.add(context.getString(R.string.ChatTitle));
        titleList.add(context.getString(R.string.game));
        fragmentList.add(new ChatFragment());
        fragmentList.add(new GameFragment());

        // Determine if a paging layout is active.
        ViewPager viewPager = (ViewPager) context.findViewById(R.id.viewpager);
        if (viewPager != null) {
            // The app is running on a smart phone.  Set up the adapter for the pager and force
            // orientation to portrait.
            viewPager.setAdapter(new GameChatPagerAdapter(context.getSupportFragmentManager(),
                    (ViewGroup) context.findViewById(R.id.page_monitor)));
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            // The app is running on a tablet. Add the fragments to their containers and force
            // orientation to landscape.
            context.getSupportFragmentManager().beginTransaction()
                    .add(R.id.chat_container, fragmentList.get(CHAT_INDEX))
                    .add(R.id.game_container, fragmentList.get(GAME_INDEX))
                    .commit();
            context.invalidateOptionsMenu();
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    // Nested classes

    /** Provide a class to handle the view pager setup. */
    private class GameChatPagerAdapter extends FragmentPagerAdapter {
        private ViewGroup mPageMonitor;

        /** Build an adapter to handle the panels for a given fragment manager. */
        GameChatPagerAdapter(final FragmentManager manager, final ViewGroup pageMonitor) {
            // Create the adapter and add the panels to the panel list.
            super(manager);
            mPageMonitor = pageMonitor;
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

        /** Update the Page Monitor for a given selected position. */
        @Override public void setPrimaryItem(final ViewGroup container, final int position,
                                             final Object object) {
            super.setPrimaryItem(container, position, object);
            // Walk the list of child nodes to set the size of the selected page circle icon to
            // be twice the size of an unselected page circle icon.
            final float LARGE = 30.0f;
            final float SMALL = 15.0f;
            int count = mPageMonitor.getChildCount();
            for (int index = 0; index < count; index++) {
                TextView child = (TextView) mPageMonitor.getChildAt(index);
                child.setText(R.string.intro_page_circle);
                if (index == position) {
                    child.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE);
                } else {
                    child.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL);
                }
            }
        }

    }

}
