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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.intro.IntroActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Provide a main activity to display the chat and game panels.
 *
 * @author Paul Michael Reilly
 */
public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    // Private class constants

    /** The logcat tag constant. */
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The preferences file name. */
    private static final String PREFS = "GameChatPrefs";

    /** The preferences key for tracking a fresh install. */
    private static final String FRESH_INSTALL = "FreshInstall";

    // Public instance methods

    /** Process a button click on a given view by posting a button click event. */
    public void buttonClick(final View view) {
        int value = view.getTag() != null ? getIntegerTag(view) : view.getId();
        EventBus.getDefault().post(new ClickEvent(this, value, view.getClass().getSimpleName()));
    }

    /** Process a button click on a given view by posting a button click event. */
    public void menuClick(final MenuItem item) {
        // Post all menu button clicks.
        int value = item.getItemId();
        EventBus.getDefault().post(new ClickEvent(this, value, item.getClass().getSimpleName()));
    }

    /** Process a given button click event by logging it. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        String format = "Button click event on type: {%s} with value {%d}.";
        int value = event.getValue();
        Log.v(TAG, String.format(format, event.getClassName(), value));

        // Process the sign in and sign out button clicks.
        switch (value) {
            case R.id.currentProfile:
            case R.id.signIn:
            case R.id.signOut:
                // On a sign in or sign out event, make sure the navigation drawer gets closed.
                NavigationManager.instance.closeDrawerIfOpen(this);
                break;
            default:
                // Ignore everything else.
                break;
        }    }

    /** Handle a back button press. */
    @Override public void onBackPressed() {
        // If the navigation drawer is open, close it, otherwise let the system deal with it.
        if (!NavigationManager.instance.closeDrawerIfOpen(this)) super.onBackPressed();
    }

    /** Process a navigation menu item click by posting a click event. */
    @Override public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here by posting a click event and closing the drawer.
        int value = item.getItemId();
        EventBus.getDefault().post(new ClickEvent(this, value, item.getClass().getSimpleName()));
        NavigationManager.instance.closeDrawerIfOpen(this);
        return true;
    }

    /**
     * TODO: Remove this method and use the event bus to propagate all button clicks.
     * An OnClick Listener for the tic-tac-toe tiles.
     *
     * @param view the tile clicked
     */
    public void tileOnClick(final View view) {
        // Pass responsibility for this onClick listener onto GameFragment
        PaneManager.instance.tileOnClick(view);
    }

    // Protected instance methods

    /**
     * Set up the app per the characteristics of the running device.
     *
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        // Allow superclasses to initialize using the saved state and determine if there has been a
        // fresh install on this device and proceed accordingly.
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(FRESH_INSTALL, true)) {
            // This is a fresh installation of the app.  Present the intro activity to get things
            // started, which will introduce the user to the app and provide a chnance to sign in or
            // register an account.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FRESH_INSTALL, false);
            editor.apply();
            Intent introIntent = new Intent(this, IntroActivity.class);
            startActivity(introIntent);
        }

        // Deal with the main activity creation.
        setContentView(R.layout.activity_main);
        PaneManager.instance.init(this);
        AccountManager.instance.init(this);
        init(savedInstanceState);
    }

    /** Respect the lifecycle and ensure that the event bus shuts down. */
    @Override protected void onPause() {
        // Unregister the components directly used by the main activity which will unregister
        // sub-components in turn.
        super.onPause();
        PaneManager.instance.unregister();
        AccountManager.instance.unregister();
        EventBus.getDefault().unregister(this);
    }

    /** Respect the lifecycle and ensure that the event bus spins up. */
    @Override protected void onResume() {
        // Register the components directly used by the main activity which will register
        // sub-components in turn.
        super.onResume();
        EventBus.getDefault().register(this);
        PaneManager.instance.register();
        AccountManager.instance.register();
    }

    // Private instance methods.

    /** Return the integer value of the tag in the given view, -1 if the value is not an integer. */
    private int getIntegerTag(final View view) {
        Object o = view.getTag();
        if (o instanceof String) {
            String s = (String) o;
            return Integer.valueOf(s).intValue();
        } else if (o instanceof Integer) {
            Integer i = (Integer) o;
            return i.intValue();
        } else {
            return -1;
        }
    }

    /** Initialize the main activity. */
    private void init(final Bundle savedInstanceState) {
        // Set up the app components: toolbar and navigation drawer.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationManager.instance.init(this, toolbar);

        // Set up the handle navigation menu item clicks.
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /** Handle an account state change by updating the navigation drawer header. */
    @Subscribe public void accountStateChanged(final AccountStateChangeEvent event) {
        // If there is an account, set up the navigation drawer header accordingly.
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        if (header == null) header =  navigationView.inflateHeaderView(R.layout.nav_header_main);
        Account account = event.getAccount();
        if (account != null) {
            // There is an account.  Set it up in the header.
            NavigationManager.instance.setAccount(account, header);
        } else {
            // There is no current user.  Provide the sign in button in the header.
            NavigationManager.instance.setNoAccount(header);
        }
    }

}
