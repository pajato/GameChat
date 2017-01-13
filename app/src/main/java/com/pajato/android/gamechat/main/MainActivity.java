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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.BuildConfig;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.credentials.CredentialsManager;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.chat.fragment.ChatFragment;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.NavDrawerOpenEvent;
import com.pajato.android.gamechat.exp.ExperienceFragment;
import com.pajato.android.gamechat.exp.ExpManager;
import com.pajato.android.gamechat.intro.IntroActivity;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.database.AccountManager.ACCOUNT_AVAILABLE_KEY;

/**
 * Provide a main activity to display the chat and game fragments.
 *
 * @author Paul Michael Reilly
 */
public class MainActivity extends BaseActivity
    implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    // Public class constants.

    /** ... */
    public static final String SKIP_INTRO_ACTIVITY_KEY = "skipIntroActivityKey";

    /** The test user name key. */
    public static final String TEST_USER_KEY = "testUserKey";

    // Private class constants.

    /** The logcat tag constant. */
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The preferences file name. */
    private static final String PREFS = "GameChatPrefs";

    /** The Intro activity request code. */
    private static final int RC_INTRO = 1;

    // Public instance methods

    /** Handle an account state change by updating the navigation drawer header. */
    @Subscribe public void onAuthenticationChange(final AuthenticationChangeEvent event) {
        // Due to a "bug" in Android, using XML to configure the navigation header current profile
        // click handler does not work.  Instead we do it here programmatically.
        Account account = event != null ? event.account : null;
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0) != null
            ? navView.getHeaderView(0)
            : navView.inflateHeaderView(R.layout.nav_header_main);
        View layout = header.findViewById(R.id.currentProfile);
        if (layout != null) layout.setOnClickListener(this);
        NavigationManager.instance.setAccount(account, header);
    }

    /** Handle a back button press event delivered by the system. */
    @Override public void onBackPressed() {
        if (NavigationManager.instance.closeDrawerIfOpen(this)) return;
        super.onBackPressed();
    }

    /** Process a given button click event handling the nav drawer closing. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Log all button clicks and rocess the sign in and sign out button clicks.
        View view = event.view;
        String format = "Button click event on view: {%s}.";
        Log.v(TAG, String.format(Locale.US, format, view.getClass().getSimpleName()));
        switch (view.getId()) {
            case R.id.currentProfile:
            case R.id.signIn:
            case R.id.signOut:
                // On a sign in or sign out event, make sure the navigation drawer gets closed.
                AppEventManager.instance.post(new NavDrawerOpenEvent(this));
                break;
            default:
                // Ignore everything else.
                break;
        }
    }

    /** Process a click on a given view by posting a button click event. */
    public void onClick(final View view) {
        // Use the Event bus to post the click event.
        AppEventManager.instance.post(new ClickEvent(view));
    }

    /** Process a navigation menu item click by posting a click event. */
    @Override public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        // Handle navigation view item clicks here by posting a click event and closing the drawer.
        switch (item.getItemId()) {
            default:
                // Todo: add menu button handling as a future feature.
                break;
        }
        AppEventManager.instance.post(new NavDrawerOpenEvent(this));
        return true;
    }

    /** Setup the standard set of activty menu items. */
    @Override public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the main options menu and enable the join developer groups item in debug builds.
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (BuildConfig.DEBUG) {
            MenuItem item = menu.findItem(R.id.joinDeveloperGroups);
            if (item != null) item.setVisible(true);
        }
        return true;
    }

    /** Handle a menu item click by providing a last ditch chance to do something. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        // Case on the menu id to handle the item.
        switch (event.item.getItemId()) {
            case R.id.helpAndFeedback:
                SupportManager.instance.sendFeedback(this, "GameChat Feedback");
                AppEventManager.instance.cancel(event);
                break;
            case R.id.fileBugReport:
                handleBugReport(event);
                break;
            default:
                // Handle all other events by logging a message for now.
                final String format = "Default handling for menu item with title: {%s}";
                Log.d(TAG, String.format(Locale.US, format, event.item.getTitle()));
                break;
        }
    }

    /** Post the menu item click to the app. */
    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        AppEventManager.instance.post(new MenuItemEvent(item));
        return true;
    }

    // Protected instance methods

    /** Process the result from the Intro activity. */
    @Override protected void onActivityResult(final int requestCode, final int resultCode,
                                              final Intent intent) {
        // Ensure that the request code and the result are valid.
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_INTRO && resultCode == RESULT_OK) {
            // The request code is valid and the result is good.  Update the account available flag
            // based on the result from the intro activity intent data.
            SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            String uid = intent.getStringExtra(Intent.EXTRA_TEXT);
            String key = ACCOUNT_AVAILABLE_KEY;
            editor.putBoolean(key, intent.getBooleanExtra(key, uid != null));
            editor.apply();
        }
    }

    /** Set up the app per the characteristics of the running device. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        // Deal with signin, set up the main layout, and initialize the app.
        super.onCreate(savedInstanceState);
        signIn();
        setContentView(R.layout.activity_main);
        init();
    }

    // Private instance methods.

    /** Return "about" information: a string describing the app and it's version information. */
    private String getAbout() {
        final String format = "GameChat %s-%d Bug Report";
        final String name = BuildConfig.VERSION_NAME;
        final int code = BuildConfig.VERSION_CODE;
        return String.format(Locale.US, format, name, code);
    }

    /** Return null if the given bitmap cannot be saved or the file path it has been saved to. */
    private String getBitmapPath(final Bitmap bitmap) {
        // Create the image file on internal storage.  Abort if the subdirectories cannot be
        // created.
        FileOutputStream outputStream;
        File dir = new File(getFilesDir(), "images");
        if (!dir.exists() && !dir.mkdirs()) return null;

        // Flush the bitmap to the image file as a stream and return the result.
        File imageFile = new File(dir, "screenshot.png");
        Log.d(TAG, String.format(Locale.US, "Image file path is {%s}", imageFile.getPath()));
        try {
            outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException exc) {
            Log.e(TAG, exc.getMessage(), exc);
            return null;
        }

        return imageFile.getPath();
    }

    /** Handle a bug report by performing a screen capture, grabbing logcat and sending email. */
    private void handleBugReport(final MenuItemEvent event) {
        // Capture the screen (with any luck, sans menu.), send the message and cancel event
        // propagation.
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        List<String> attachments = new ArrayList<>();
        String path = getBitmapPath(rootView.getDrawingCache());
        if (path != null) attachments.add(path);
        path = getLogcatPath();
        if (path != null) attachments.add(path);
        SupportManager.instance.sendFeedback(this, getAbout(), "Extra information: ", attachments);
        AppEventManager.instance.cancel(event);
    }

    /** Initialize the main activity and all of it's subsystems. */
    private void init() {
        // Set up the account manager with a list of class names.  These classes must be registered
        // with the app event manager before Firebase can be enabled.  And when any of them are
        // unregistered, Firebase will be turned off.
        List<String> list = new ArrayList<>();
        list.add(this.getClass().getName());
        list.add(ChatFragment.class.getName());
        list.add(ExperienceFragment.class.getName());
        AccountManager.instance.init(list);
        CredentialsManager.instance.init(getSharedPreferences(PREFS, Context.MODE_PRIVATE));

        // Finish initializing the important manager modules.
        DBUtils.instance.init(this);
        NetworkManager.instance.init(this);
        PaneManager.instance.init(this);
        ExpManager.instance.init();
    }

    /** Return the file where logcat data has been placed, null if no data is available. */
    private String getLogcatPath() {
        // Capture the current state of the logcat file.
        File dir = new File(getFilesDir(), "logcat");
        if (!dir.exists() && !dir.mkdirs()) return null;

        File outputFile = new File(dir, "logcat.txt");
        try {
            Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException exc) {
            Log.e(TAG, exc.getMessage(), exc);
            return null;
        }

        Log.d(TAG, String.format("File size is %d.", outputFile.length()));
        Log.d(TAG, String.format("File path is {%s}.", outputFile.getPath()));

        return outputFile.getPath();
    }

    /** Handle sign in by processing the invoking intent and checking for an existing account. */
    private void signIn() {
        // Determine if the calling intent prefers not to run the intro activity (for example
        // during a connected test run) or if there is an available account.  Abort in either case.
        Intent intent = getIntent();
        boolean skipIntro = intent.hasExtra(SKIP_INTRO_ACTIVITY_KEY);
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean hasAccount = prefs.getBoolean(ACCOUNT_AVAILABLE_KEY, false);
        if (skipIntro || hasAccount) return;

        // This is a fresh installation of the app.  Present the intro activity to get
        // things started, which will introduce the user to the app and provide a chance to
        // sign in.
        Intent introIntent = new Intent(this, IntroActivity.class);
        startActivityForResult(introIntent, RC_INTRO);
    }
}
