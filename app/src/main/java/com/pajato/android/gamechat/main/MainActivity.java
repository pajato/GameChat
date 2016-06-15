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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.fragment.ChatFragment;
import com.pajato.android.gamechat.fragment.GameFragment;
import com.pajato.android.gamechat.intro.IntroActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provide a main activity to display the chat and game panesl. */
public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    // Public class constants

    // Private class constants

    /** The logcat tag constant. */
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The preferences file name. */
    private static final String PREFS = "GameChatPrefs";

    // Private instance variables
    private GameChatPagerAdapter mAdapter;

    // Public instance methods

    // Protected instance methods

    /**
     * Set up the app per the characteristics of the running device.
     *
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        // Deal with the main activity creation.  Layout the main activity, set up the toolbar, and
        // the floating action button.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // Set up the navigation drawer and view.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final int OPEN_ID = R.string.navigation_drawer_action_open;
        final int CLOSE_ID = R.string.navigation_drawer_action_close;
        ActionBarDrawerToggle toggle =
            new ActionBarDrawerToggle(this, drawer, toolbar, OPEN_ID, CLOSE_ID);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Init the app state.
        mAdapter = new GameChatPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(mAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        // Determine if an account needs to be set up.
        if (!AccountManager.instance.hasAccount()) {
            // There is no account yet.  Present the intro activity to get things started.
            Intent introIntent = new Intent(this, IntroActivity.class);
            //startActivity(introIntent);
            //finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * An OnClick Listener for the new game button.
     *
     * @param view the new game button.
     */
    public void onNewGame(final View view) {
        // Pass responsibility for this onClick listener onto GameFragment
        ((GameFragment) mAdapter.getItem(1)).onNewGame(view);
    }

    /** The FAB click handler.  Show the various options. */
    public void fabClickHandler(final View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
    }

    /**
     * An OnClick Listener for the tic-tac-toe tiles.
     *
     * @param view the tile clicked
     */
    public void tileOnClick(final View view) {
        // Pass responsibility for this onClick listener onto GameFragment
        ((GameFragment) mAdapter.getItem(1)).tileOnClick(view);
    }

    // Private instance methods.

    // Private nested classes

    /**
     * Provide a class to handle the view pager setup.
     */
    private static class GameChatPagerAdapter extends FragmentPagerAdapter {
        private enum Panel {
            chat(R.string.chat, ChatFragment.class),
            game(R.string.game, GameFragment.class);

            /** The panel title resource id. */
            public int titleId;
            /** The fragment class associated with the panel. */
            public Class<? extends Fragment> fragmentClass;
            public Fragment fragment;

            /** Create the enum value instance given a title resource id and a fragment class. */
            Panel(final int titleId, final Class<? extends Fragment> fragmentClass) {
                this.titleId = titleId;
                this.fragmentClass = fragmentClass;
            }
        }

        /** A list of panels ordered left to right. */
        private List<Panel> panelList = new ArrayList<>();

        /** The fragment manager used to commit the fragment. */
        private Map<Panel, String> titles = new HashMap<>();

        /**
         * Build an adapter to handle the panels.
         *
         * @param manager The fragment manager.
         */
        public GameChatPagerAdapter(final FragmentManager manager, final Context context) {
            // Create the adapter and add the panels to the panel list.
            super(manager);
            panelList.add(Panel.chat);
            panelList.add(Panel.game);
            titles.put(Panel.chat, context.getString(Panel.chat.titleId));
            titles.put(Panel.game, context.getString(Panel.game.titleId));
        }

        /** Implement the getItem() interface by dereferencing the fragment from the Panel. */
        @Override public Fragment getItem(int position) {
            Fragment currFragment = panelList.get(position).fragment;
            try {
                if(currFragment == null) {
                    currFragment = panelList.get(position).fragmentClass.newInstance();
                    Log.d(TAG, String.format("getFragment: Created fragment {%s}.", currFragment));
                } else {
                    Log.d(TAG, String.format("getFragment: Fragment {%s} already exists.", currFragment));
                }
            } catch (InstantiationException exc) {
                Log.e(TAG, "Unexpected Instantiation Exception creating fragment!", exc);
            } catch (IllegalAccessException exc) {
                Log.e(TAG, "Unexpected Illegal Access Exception creating fragment!", exc);
            }
            return currFragment;
        }

        /** Implement the getCount() interface by using the panel list size. */
        @Override public int getCount() {
            return panelList.size();
        }

        /** Implement the getPageTitle() interface by using the title stored in the fragment. */
        @Override public CharSequence getPageTitle(int position) {
            return titles.get(panelList.get(position));
        }

    }

}
