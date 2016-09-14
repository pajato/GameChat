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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.pajato.android.gamechat.R.id.clearGroupName;
import static com.pajato.android.gamechat.chat.model.Message.STANDARD;

/**
 * Provide a main activity to manage the UI for adding a group.
 *
 * @author Paul Michael Reilly
 */
public class AddGroupActivity extends AppCompatActivity implements View.OnClickListener {
    // Public class constants.

    // Private class constants.

    /** The logcat tag constant. */
    private static final String TAG = AddGroupActivity.class.getSimpleName();

    // Private instance variables.

    /** The group which will ostensibly be persisted on the back end. */
    private Group mGroup;

    // Public instance methods

    /** Handle group add button clicks by processing them. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        int value = event.getValue();
        switch (value) {
            case R.id.saveGroupButton:
                // Process the group (validate and persist it) and be done with the activity.
                Account account = AccountManager.instance.getCurrentAccount();
                if (account != null) processGroup(account);
                finish();
                break;
            case clearGroupName:
                clearGroupName();
                break;
            case R.id.addGroupMembers:
                showFutureFeatureMessage(R.string.InviteGroupMembersFeature);
                break;
            case R.id.setGroupIcon:
                showFutureFeatureMessage(R.string.SetAddGroupIconFeature);
                break;
            default:
                // Ignore everything else.
                break;
        }
    }

    /** Process a button click on a given view by posting a button click event. */
    public void menuClick(final MenuItem item) {
        // Post all menu button clicks.
        int value = item.getItemId();
        String className = item.getClass().getSimpleName();
        EventBus.getDefault().post(new ClickEvent(this, value, null, item, className));
    }

    /** Handle a back button press by cancelling out of the activity. */
    @Override public void onBackPressed() {
        finish();
    }

    /** Process a button click on a given view by posting a button click event. */
    public void onClick(final View view) {
        EventUtils.post(this, view);
    }

    /** Post the options menu on demand. */
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_group_menu, menu);
        return true;
    }

    /** Handle a clear button click on the toolbar by canceling the activity. */
    @Override public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Determine how to deal with the selected options menu item.
        switch (menuItem.getItemId()) {
            case R.id.addGroupLearnMore:
                showFutureFeatureMessage(R.string.LearnMoreFeature);
                return true;
            case R.id.addGroupFeedback:
                sendFeedbackEmail("Add Group Feedback");
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    // Protected instance methods

    /** Set up the UI to create a new group defined by the User. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        // Render the form and initialize for the new group.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add);
        init();
    }

    /** Respect the lifecycle and ensure that the event bus shuts down. */
    @Override protected void onPause() {
        // Unregister the components directly used by the main activity which will unregister
        // sub-components in turn.
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /** Respect the lifecycle and ensure that the event bus spins up. */
    @Override protected void onResume() {
        // Register the components directly used by the main activity which will register
        // sub-components in turn.
        super.onResume();
        EventBus.getDefault().register(this);
    }

    // Private instance methods.

    /** Clear the group name edit text field by setting it to contain the empty string. */
    private void clearGroupName() {
        EditText editText = (EditText) findViewById(R.id.groupNameText);
        if (editText != null) editText.setText("");
    }

    /** Return a default group name based on the given account. */
    private String getDefaultAccountName() {
        // Ensure that the account exists.
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null) return null;

        // Obtain a sane default group name.
        String group = getResources().getString(R.string.Group);
        String value = account.displayName == null ? getEmailName(account) : account.displayName;
        return String.format(Locale.getDefault(), "%s %s", value, group);
    }

    /** Return the base part of the email address (excludes domain). */
    private String getEmailName(final Account account) {
        String email = account.email;
        int index = email.indexOf('@');
        String value = email.substring(0, index);
        return value.substring(0, 1).toUpperCase(Locale.getDefault()) + value.substring(1);
    }

    /** Initialize the main activity. */
    private void init() {
        // Initialize the group Firebase model class and the toolbar.
        long tstamp = new Date().getTime();
        mGroup = new Group("", "", tstamp, 0, new ArrayList<String>());
        initToolbar();
    }

    /** Initialize the toolbar */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
        toolbar.setNavigationOnClickListener(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            // All is not well.  Log the issue and abort.
            Log.e(TAG, "An action bar is not available for the add group activity!  Aborting.");
            finish();
        } else {
            // All is well.  Finish setting up the toolbar and the group name edit field.
            actionBar.setDisplayShowHomeEnabled(true);
            EditText text = (EditText) findViewById(R.id.groupNameText);
            TextView button = (TextView) toolbar.findViewById(R.id.saveGroupButton);
            text.addTextChangedListener(new TextChangeHandler(this, text, button, mGroup));
            text.setText(getDefaultAccountName());
        }
    }

    /** Process a newly created group by saving it to Firebase. */
    private void processGroup(@NonNull Account account) {
        // Ensure that the owner is also a group member and persist the group to the database.
        mGroup.owner = account.id;
        if (!mGroup.memberIdList.contains(mGroup.owner)) mGroup.memberIdList.add(mGroup.owner);
        String groupKey = DatabaseManager.instance.createGroupProfile(mGroup);

        // Create the default room and persist it to the database.
        String name = getString(R.string.DefaultRoomName);
        Room room = new Room(mGroup.owner, name, groupKey, mGroup.createTime, 0, "public", null);
        String roomKey = DatabaseManager.instance.createRoomProfile(groupKey, room);

        // Update the owner's account to add the new group to the group id list and the joined room
        // list on the account profile.
        account.groupIdList.add(groupKey);
        String joinedRoom = groupKey + " " + roomKey;
        account.joinedRoomList.add(joinedRoom);
        DatabaseManager.instance.updateAccount(account);

        // Put a welcome message in the default room from the owner.
        String text = "Welcome to my new group!";
        DatabaseManager.instance.createMessage(text, STANDARD, account, groupKey, roomKey, room);
    }

    /** Send email to the support address. */
    private void sendFeedbackEmail(final String subject) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL  , new String[] {"support@pajato.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email clients are installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Indicate a future feature opportunity. */
    private void showFutureFeatureMessage(final int resourceId) {
        // Post a toast message.
        String prefix = getString(resourceId);
        String suffix = getString(R.string.FutureFeature);
        CharSequence text = String.format(Locale.getDefault(), "%s %s", prefix, suffix);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(this, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // Private classes.

    /** Provide a handler to accumulate the group name. */
    private class TextChangeHandler implements TextWatcher {

        // Private class constants.

        // Private instance variables.

        /** The button being controlled by this change handler. */
        private TextView mSaveButton;

        /** The edit text widget. */
        private EditText mEditText;

        /** The text color for a disabled save button. */
        private int mDisabledColor;

        /** The text color for an enabled save button. */
        private int mEnabledColor;

        /** The group to be added to the backend. */
        private Group mGroup;

        // Public constructor.

        /** Build a text change handler to manage a given edit text field, save button and group. */
        TextChangeHandler(Context context, final EditText text, final TextView button,
                          final Group group) {
            mEditText = text;
            mSaveButton = button;
            mGroup = group;
            mDisabledColor = button.getCurrentTextColor();
            mEnabledColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        }

        /** Make sure that the hint is restored when necessary and that the name is unique. */
        @Override public void afterTextChanged(Editable s) {
            if (mEditText.getText().length() == 0) {
                // Restore the hint and disable the save button.
                //restoreHint();
                mSaveButton.setTextColor(mDisabledColor);
                mSaveButton.setEnabled(false);
            } else {
                // TODO: determine if the group name is unique.
                // Render the save button enabled with the primary dark color and update the
                // group.
                mSaveButton.setEnabled(true);
                mSaveButton.setTextColor(mEnabledColor);
                mGroup.name = mEditText.getText().toString();
            }
        }

        @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override public void onTextChanged(final CharSequence s, final int start, final int before,
                                            final int count) {
        }

    }

}
