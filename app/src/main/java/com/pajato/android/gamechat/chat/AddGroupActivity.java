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
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MemberManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Locale;

import static com.pajato.android.gamechat.chat.model.Message.STANDARD;
import static com.pajato.android.gamechat.chat.model.Room.PUBLIC;

/**
 * Provide a main activity to manage the UI for adding a group.
 *
 * @author Paul Michael Reilly
 */
public class AddGroupActivity extends AppCompatActivity {
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
        switch (event.view.getId()) {
            case R.id.saveGroupButton:
                // Process the group (validate and persist it) and be done with the activity.
                Account account = AccountManager.instance.getCurrentAccount();
                if (account != null) processGroup(account);
                finish();
                break;
            case R.id.clearGroupName:
                clearGroupName();
                break;
            case R.id.addGroupMembers:
                showFutureFeatureMessage(R.string.InviteMembersFeature);
                break;
            case R.id.setGroupIcon:
                showFutureFeatureMessage(R.string.SetCreateIconFeature);
                break;
            default:
                // Ignore everything else.
                break;
        }
    }

    /** Handle a back button press by cancelling out of the activity. */
    @Override public void onBackPressed() {
        finish();
    }

    /** Process a button click on a given view by posting a button click event. */
    public void onClick(final View view) {
        AppEventManager.instance.post(new ClickEvent(view));
    }

    /** Post the options menu on demand. */
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overflow_main_menu, menu);
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
        AppEventManager.instance.unregister(this);
    }

    /** Respect the lifecycle and ensure that the event bus spins up. */
    @Override protected void onResume() {
        // Register the components directly used by the main activity which will register
        // sub-components in turn.
        super.onResume();
        AppEventManager.instance.register(this);
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
        if (account == null) return "";

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
        // Initialize the group Firebase model class by creating two empty maps and initialize the
        // toolbar.
        mGroup = new Group();
        mGroup.roomList = new ArrayList<>();
        mGroup.memberList = new ArrayList<>();
        initToolbar();
    }

    /** Initialize the toolbar */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
        toolbar.setNavigationOnClickListener(new NavigationExitHandler());
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
        // Ensure that the account holder has been added to the group member list and fetch group
        // and room push keys.
        mGroup.owner = account.id;
        String groupKey = GroupManager.instance.getGroupKey();
        String roomKey = RoomManager.instance.getRoomKey(groupKey);
        mGroup.key = groupKey;
        mGroup.commonRoomKey = roomKey; // We create the common room next...

        // Create the default (common) room, update the group's room list, add the group key to the
        // account's group id list, and update the member in the group.
        Room room = new Room(roomKey, mGroup.owner, "Common", groupKey, 0, 0, PUBLIC);
        mGroup.roomList.add(roomKey);
        account.joinList.add(groupKey);
        mGroup.memberList.add(account.id);
        Account member = new Account(account);
        member.joinList.add(roomKey);
        member.groupKey = groupKey;

        // Persist the group and room profiles to the database, update the account with the new
        // joined list entry and create the member.
        GroupManager.instance.createGroupProfile(mGroup);
        RoomManager.instance.createRoomProfile(room);
        AccountManager.instance.updateAccount(account);
        String format = MemberManager.instance.getMembersPath(groupKey, account.id);
        String path = String.format(Locale.US, format, groupKey, account.id);
        DBUtils.instance.updateChildren(path, member.toMap());

        // Post a welcome message to the default room from the owner.
        String text = "Welcome to my new group!";
        MessageManager.instance.createMessage(text, STANDARD, account, room);
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

    /** Provide a handler for aborting via the toolbar navigation icon. */
    private class NavigationExitHandler implements View.OnClickListener {
        @Override public void onClick(final View view) {
            String format = "Got a create group activity abort click on view {%s}.";
            Log.d(TAG, String.format(Locale.US, format, view));
            finish();
        }
    }

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
