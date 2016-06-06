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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.ChatManager;
import com.pajato.android.gamechat.fragment.TTTFragment;
import com.pajato.android.gamechat.game.GameManager;
import com.pajato.android.gamechat.intro.IntroActivity;

import java.util.ArrayList;
import java.util.List;

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

    // The Match History
    private ArrayList<String> mInstructions;
    private static final String mKEY_INSTRUCTIONS = "match_history";

    // Keeps track of the mTurn user. True = Player 1, False = Player 2.
    private boolean mTurn;
    private static final String mKEY_TURN = "current_turn";

    // Player Turn Strings
    private String mPLAYER1;
    private String mPLAYER2;

    // Individual Game Fragments
    private TTTFragment ticTacToe;

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
        GameChatPagerAdapter adapter = new GameChatPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        // Determine if an account needs to be set up.
        if (!AccountManager.instance.hasAccount()) {
            // There is no account yet.  Present the intro activity to get things started.
            Intent introIntent = new Intent(this, IntroActivity.class);
            //startActivity(introIntent);
            //finish();
        }

        // Initialize the member variables.
        mPLAYER1 = getString(R.string.player_1);
        mPLAYER2 = getString(R.string.player_2);
        mInstructions = new ArrayList<>();
        mTurn = true;

        // Setup the Tic-Tac-Toe fragment and inflate its layout.
        ticTacToe = (TTTFragment) Panel.game.getFragment(getApplicationContext());
        ticTacToe.setArguments(getIntent().getExtras());
        /*
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, ticTacToe)
                .commit();
        */
        // If we saved any Instructions, we need to recreate the board state.
        if(savedInstanceState != null) {
            mTurn = savedInstanceState.getBoolean(mKEY_TURN);
            mInstructions = savedInstanceState.getStringArrayList(mKEY_INSTRUCTIONS);
            for(int i = 0; i < mInstructions.size(); i++) {
                sendMessage(mInstructions.get(i));
            }
            // If there is an odd number of Instructions, then the turn indicator will be set to
            // the wrong value. We can handle this here.
            if(mInstructions.size() % 2 != 0) {
                mTurn = !mTurn;
            }
            //ticTacToe.checkNotFinished();
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

    @Override public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the Instructions and Turn for the save state.
        outState.putStringArrayList(mKEY_INSTRUCTIONS, mInstructions);
        outState.putBoolean(mKEY_TURN, mTurn);
    }

    /**
     * Sends a message alerting the event handling system that the new game button was clicked.
     *
     * @param view the new game button.
     */
    public void onNewGame(final View view) {
        // Create the message.
        String msg = getTurn() + "\n";
        msg += view.getTag().toString();

        // Empty the instructions list, as a new game has begun.
        mInstructions.clear();
        mInstructions.add(msg);

        // Output New Game Message
        String newTurn = (getTurn().equals(getString(R.string.player_1)) ?
                "Player 1 (" + getString(R.string.xValue) + ")" :
                "Player 2 (" + getString(R.string.oValue) + ")") + "'s Turn";
        Snackbar start = Snackbar.make(findViewById(R.id.activity_main), "New Game! " + newTurn, Snackbar.LENGTH_SHORT);
        start.show();

        //TODO: replace this with an implemented event handling system.
        sendMessage(msg);
    }

    /** The FAB click handler.  Show the various options. */
    public void fabClickHandler(final View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
    }

    /**
     * Sends a message alerting the event handling system that there was a tile clicked, and
     * swaps the mTurn to the opposite player.
     *
     * @param view the tile clicked
     */
    public void tileOnClick(final View view) {
        String msg = getTurn() + "\n";
        msg = msg + view.getTag().toString();

        // Keep track of mInstructions for recreating the board.
        mInstructions.add(msg);

        //TODO: replace this with an implemented event handling system.
        sendMessage(msg);

        mTurn = !mTurn;
    }

    // Private instance methods.

    /**
     * A placeholder method for a message handler / event coordinator to be implemented at a later time.
     *
     * @param msg the message to transmit to the message handler.
     */
    private void sendMessage(final String msg) {
        //TODO: replace this with an implemented event handling system.
        // This will be a switch for each of the individual game fragment handlers.
        ticTacToe.messageHandler(msg);
    }

    /**
     * Gets the current mTurn and returns a string reflecting the player's
     * name who is currently playing.
     *
     * @return player 1 or player 2, depending on the mTurn.
     */
    private String getTurn() {
        return mTurn ? mPLAYER1 : mPLAYER2;
    }

    // Private nested classes

    /**
     * Provide a class to handle the view pager setup.
     */
    private class GameChatPagerAdapter extends FragmentStatePagerAdapter {

        /**
         * A list of panels ordered left to right.
         */
        private List<Panel> panelList = new ArrayList<>();

        /**
         * Build an adapter to handle the panels.
         *
         * @param fm The fragment manager.
         */
        public GameChatPagerAdapter(final FragmentManager manager) {
            super(manager);
            panelList.add(Panel.chat);
            panelList.add(Panel.game);
        }

        @Override
        public Fragment getItem(int position) {
            return panelList.get(position).getFragment(MainActivity.this);
        }

        @Override
        public int getCount() {
            return panelList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MainActivity.this.getString(panelList.get(position).getTitleId());
        }

    }

}
