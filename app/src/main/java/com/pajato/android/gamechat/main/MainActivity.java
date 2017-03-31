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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.ContactManager;
import com.pajato.android.gamechat.chat.fragment.ChatEnvelopeFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FragmentKind;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.credentials.CredentialsManager;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.database.DatabaseRegistrar;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.JoinManager;
import com.pajato.android.gamechat.database.MemberManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.database.ProtectedUserManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.InviteEvent;
import com.pajato.android.gamechat.event.GroupJoinedEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.NavDrawerOpenEvent;
import com.pajato.android.gamechat.event.ProtectedUserAuthFailureEvent;
import com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment;
import com.pajato.android.gamechat.help.HelpManager;
import com.pajato.android.gamechat.intro.IntroActivity;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.chat.ContactManager.REQUEST_CONTACTS;
import static com.pajato.android.gamechat.common.FragmentKind.chat;
import static com.pajato.android.gamechat.credentials.CredentialsManager.EMAIL_KEY;
import static com.pajato.android.gamechat.database.AccountManager.ACCOUNT_AVAILABLE_KEY;
import static com.pajato.android.gamechat.event.InviteEvent.ItemType.group;

/**
 * Provide a main activity to display the chat and game fragments.
 *
 * @author Paul Michael Reilly
 */
public class MainActivity extends BaseActivity
    implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    // Public class constants.

    /** The preferences file name. */
    public static final String PREFS = "GameChatPrefs";

    /** The invite activity request code. */
    public static final int RC_INVITE = 2;

    /** ... */
    public static final String SKIP_INTRO_ACTIVITY_KEY = "skipIntroActivityKey";

    /** The test user name key. */
    public static final String TEST_USER_KEY = "testUserKey";

    // Private class constants.

    /** The logcat tag constant. */
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The Intro activity request code. */
    private static final int RC_INTRO = 1;

    // Private instance variables.

    /** The current, possibly null, fragment back press handler for chat. */
    private View.OnClickListener mChatUpHandler;

    /** The current, possibly null, fragment back press handler for experiences. */
    private View.OnClickListener mExpUpHandler;

    // Public instance methods.

    /** Lazily create a navigation back press handler for chat fragment toolbars. */
    private View.OnClickListener getChatUpHandler() {
        if (mChatUpHandler == null)
            mChatUpHandler = new UpHandler();
        return mChatUpHandler;
    }

    /** Lazily create a navigation back press handler for chat fragment toolbars. */
    private View.OnClickListener getExpUpHandler() {
        if (mExpUpHandler == null)
            mExpUpHandler = new UpHandler();
        return mExpUpHandler;
    }

    /** Get the UpHandler based on the fragment type */
    public View.OnClickListener getUpHandler(FragmentKind kind) {
        return (kind == chat) ? getChatUpHandler() : getExpUpHandler();
    }

    /** Handle an account state change by updating the navigation drawer header. */
    @Subscribe
    public void onAuthenticationChange(final AuthenticationChangeEvent event) {
        // Due to a "bug" in Android, using XML to configure the navigation header current profile
        // click handler does not work.  Instead we do it here programmatically.  But first, turn
        // off the sign in spinner.
        ProgressManager.instance.hide();
        Account account = event != null ? event.account : null;
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0) != null
            ? navView.getHeaderView(0)
            : navView.inflateHeaderView(R.layout.nav_header_main);
        View layout = header.findViewById(R.id.currentProfile);
        if (layout != null) layout.setOnClickListener(this);
        NavigationManager.instance.setAccount(account, header);

        // Turn off all database handlers if the account has been signed out.
        if (account == null)
            DatabaseRegistrar.instance.unregisterAll();
    }

    /** Handle group joined event */
    @Subscribe public void onGroupJoined(final GroupJoinedEvent event) {
        if (event.groupName != null && !event.groupName.equals("")) {
            String message;
            if (event.rooms.size() == 1) {
                message = String.format(Locale.US, getString(R.string.JoinedOneRoom),
                        event.rooms.get(0), event.groupName);
            } else if (event.rooms.size() > 1) {
                String roomList = TextUtils.join(", ", event.rooms);
                message = String.format(Locale.US, getString(R.string.JoinedMultiRooms),
                        roomList, event.groupName);
            } else {
                String format = getString(R.string.JoinedGroupsMessage);
                message = String.format(Locale.US, format, event.groupName);
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /** Handle a back button press event delivered by the system. */
    @Override public void onBackPressed() {
        if (NavigationManager.instance.closeDrawerIfOpen(this))
            return;
        FragmentType type =
                this.equals(mChatUpHandler) ? ChatEnvelopeFragment.getCurrentFragmentType() :
                        ExpEnvelopeFragment.getCurrentFragmentType();
        DispatchManager.instance.handleBackDispatch(type);
    }

    /** Process a given button click event handling the nav drawer closing. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Log all button clicks and process the sign in and sign out button clicks.
        View view = event.view;
        String format = "Button click event on view: {%s}.";
        Log.v(TAG, String.format(Locale.US, format, view.getClass().getSimpleName()));
        switch (view.getId()) {
            case R.id.currentProfile:
            case R.id.signIn:
                // On a sign in or sign out event, make sure the navigation drawer gets closed.
                AppEventManager.instance.post(new NavDrawerOpenEvent(this, null));
                break;
            case R.id.signOut:
                // On a sign in or sign out event, make sure the navigation drawer gets closed.
                AppEventManager.instance.post(new NavDrawerOpenEvent(this, null));
                AccountManager.instance.signOut(this);
                break;
            case R.id.switchAccount:
                // Post a toast message indicating this is a future feature
                String prefix = getString(R.string.MenuItemSwitchAccount);
                String suffix = getString(R.string.FutureFeature);
                CharSequence text = String.format(Locale.getDefault(), "%s %s", prefix, suffix);
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            default:
                // Ignore everything else.
                break;
        }
    }

    /** Handle Snackbar click event. */
    @Subscribe
    public void onClick(final InviteEvent event) {
        // Send the invitation
        if (event.type == group) {
            InvitationManager.instance.extendGroupInvitation(this, event.key);
        } else {
            InvitationManager.instance.extendRoomInvitation(this, event.key);
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
        String format =  "Navigation Item Selected on view: {%s}";
        Log.v(TAG, String.format(Locale.US, format, item.getClass().getSimpleName()));
        AppEventManager.instance.post(new NavDrawerOpenEvent(this, item));
        return true;
    }

    /** Handle a menu item click by providing a last ditch chance to do something. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        // Case on the menu id to handle the item.
        switch (event.item.getItemId()) {
            case R.string.MenuItemHelpAndFeedback:
                HelpManager.instance.launchHelp(this);
                break;
            case R.string.SwitchToChat:
                // If the toolbar chat icon is clicked, on smart phone devices we can change panes.
                ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
                if (viewPager != null) viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                break;
            case R.string.SwitchToExp:
                // If the toolbar game icon is clicked, on smart phone devices we can change panes.
                viewPager = (ViewPager) findViewById(R.id.viewpager);
                if (viewPager != null) viewPager.setCurrentItem(PaneManager.GAME_INDEX);
                break;
            default:
                // Handle all other events by logging a message for now.
                final String format = "Default handling for menu item with title: {%s}";
                Log.d(TAG, String.format(Locale.US, format, event.item.getTitle()));
                break;
        }
    }

    @Subscribe public void onProtectedUserAuthFailureEvent(ProtectedUserAuthFailureEvent event) {
        // Pop up an alert indicating the problem
        String title = getString(R.string.AuthProtectedUserFailureTitle);
        String userEmail = ProtectedUserManager.instance.getEMailCredentials().email;
        String format = getString(R.string.AuthProtectedUserFailureMessage);
        String message = String.format(Locale.US, format, userEmail, event.message);
        showAlertDialog(title, message, false, null, true, null);
    }

    /** Show an alert dialog with "ok" and "cancel". */
    public void showOkCancelDialog(final String title, final String message,
                                   DialogInterface.OnClickListener cancelListener,
                                   DialogInterface.OnClickListener okListener) {
        showAlertDialog(title, message, true, cancelListener, true, okListener);
    }

    /** Show an alert dialog with cancel and/or ok button(s). */
    public void showAlertDialog(final String title, final String message, boolean showCancel,
                                DialogInterface.OnClickListener cancelListener, boolean showOk,
                                DialogInterface.OnClickListener okListener) {
        if (!showCancel && !showOk)
            Log.e(TAG, "showAlertDialog called but no buttons are specified.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle(title).
                setMessage(message);
        if (showCancel)
            builder.setNegativeButton(android.R.string.cancel, cancelListener);
        if (showOk)
            builder.setPositiveButton(android.R.string.ok, okListener);
        builder.create().show();
    }

    /** Process the result from a request for a permission */
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                                     @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACTS)
            ContactManager.instance.onRequestContactsResult(this, permissions, grantResults);
    }

    // Protected instance methods

    /** Process the result from the intro or invite activities. */
    @Override
    protected void onActivityResult(final int request, final int result, final Intent intent) {
        // Handle a result from either the intro activity or the invite activity.
        super.onActivityResult(request, result, intent);
        if (result != RESULT_OK)
            logFailedResult(request, intent, result == RESULT_CANCELED);
        else if (request == RC_INVITE) {
            // Invite activity result; process in the invitation manager.
            Log.d(TAG, "onActivityResult: requestCode=RC_INVITE, resultCode=" + result);
            InvitationManager.instance.onInvitationResult(result, intent);
        } else if (request == RC_INTRO)
            // Intro activity result: Update the account data based on the result from the intro
            // activity intent data.
            saveAccountData(intent);
    }

    /** Set up the app per the characteristics of the running device. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        // Deal with sign-in, set up the main layout, and initialize the app.
        super.onCreate(savedInstanceState);

        // Enable the database managers to listen for database activity immediately upon starting
        // the sign in page.
        AppEventManager.instance.register(GroupManager.instance);
        AppEventManager.instance.register(RoomManager.instance);
        AppEventManager.instance.register(MessageManager.instance);
        AppEventManager.instance.register(ExperienceManager.instance);
        AppEventManager.instance.register(MemberManager.instance);
        AppEventManager.instance.register(NavigationManager.instance);
        AppEventManager.instance.register(InvitationManager.instance);
        AppEventManager.instance.register(ProtectedUserManager.instance);

        // Deal with initial sign in via the intro activity and normal processing.
        processIntroPage();
        setContentView(R.layout.activity_main);
        init();
    }

    // Private instance methods.

    /** Initialize the main activity and all of it's subsystems. */
    private void init() {
        // Set up the account manager with a list of class names.  These classes must be registered
        // with the app event manager before Firebase can be enabled.  And when any of them are
        // unregistered, Firebase will be turned off.
        List<String> list = new ArrayList<>();
        list.add(this.getClass().getName());
        list.add(ChatEnvelopeFragment.class.getName());
        list.add(ExpEnvelopeFragment.class.getName());
        AccountManager.instance.init(this, list);
        CredentialsManager.instance.init(getSharedPreferences(PREFS, Context.MODE_PRIVATE));

        // TODO: figure out where this needs to be after it is working.
        Intent serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);

        // Finish initializing the important manager modules.
        DBUtils.instance.init(this);
        NetworkManager.instance.init(this);
        PaneManager.instance.init(this);
        InvitationManager.instance.init(this);
        JoinManager.instance.init(this);
        NavigationManager.instance.init(this, (Toolbar) findViewById(R.id.toolbar));
        RoomManager.instance.init(this);

        // Register the first of many app event listeners.
        AppEventManager.instance.register(AccountManager.instance);
        AppEventManager.instance.register(this);
    }

    /** Provide a logcat message about a failed result. */
    private void logFailedResult(int request, Intent intent, boolean isCancelled) {
        String requester;
        switch (request) {
            case RC_INTRO:
                requester = "IntroActivity";
                break;
            case RC_INVITE:
                requester = "InvitationActivity";
                break;
            default:
                requester = "Unknown";
                break;
        }
        // Intent may be null so prevent null-pointer-exception crash
        if (isCancelled) {
            String message = String.format(Locale.US, "%s CANCELED", request);
            Log.d(TAG, message);
        } else {
            String format = "onActivityResult: %s, FAILED, \nIntent: %s";
            Log.d(TAG, String.format(Locale.US, format, requester, intent.toString()));
        }
    }

    /** Determine if the intro screen needs to be presented and do so. */
    private void processIntroPage() {
        // Determine if the calling intent prefers not to run the intro activity (for example
        // during a connected test run) or if there is an available account.  Abort in either case.
        Intent intent = getIntent();
        boolean skipIntro = intent.hasExtra(SKIP_INTRO_ACTIVITY_KEY);
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean hasAccount = prefs.getBoolean(ACCOUNT_AVAILABLE_KEY, false);
        if (skipIntro || hasAccount)
            return;

        // This is a fresh installation of the app.  Present the intro activity to get
        // things started, which will introduce the user to the app and provide a chance to
        // sign in.
        Intent introIntent = new Intent(this, IntroActivity.class);
        startActivityForResult(introIntent, RC_INTRO);
    }

    /** Handle the intro activity result by saving the account availability information. */
    private void saveAccountData(@NonNull final Intent intent) {
        Log.d(TAG, "onActivityResult: IntroActivity, SUCCESS");
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        boolean hasAccount = intent.getStringExtra(EMAIL_KEY) != null;
        editor.putBoolean(ACCOUNT_AVAILABLE_KEY, hasAccount);
        editor.apply();
        if (hasAccount)
            CredentialsManager.instance.saveCredentials(intent, prefs);
    }

    // Protected inner classes.

    /** Provide a handler that will generate a back-press event. */
    private class UpHandler implements View.OnClickListener {
        /** Handle a click on the back arrow button by generating a back press. */
        public void onClick(final View view) {
            FragmentType type =
                    this.equals(mChatUpHandler) ? ChatEnvelopeFragment.getCurrentFragmentType() :
                            ExpEnvelopeFragment.getCurrentFragmentType();
            DispatchManager.instance.handleBackDispatch(type);
        }
    }
}
