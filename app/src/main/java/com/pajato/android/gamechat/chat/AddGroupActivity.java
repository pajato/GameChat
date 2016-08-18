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
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

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
                processGroup();
                finish();
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
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    // Protected instance methods

    /** Set up the UI to create a new group defined by the User. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        // Render the form and initialize for the new group.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add);
        mGroup = new Group();
        mGroup.ownerId = AccountManager.instance.getCurrentAccount().accountId;
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

    /** Initialize the main activity. */
    private void init() {
        // Set up the toolbar to support a cancel button in the "home" position and ensure that all
        // is well.
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
        }

        // TODO: Set up the member invitation list for this group.
    }

    /** Process a newly created group by saving it to Firebase. */
    private void processGroup() {
        // Persist the Group to the backend.
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Account account = AccountManager.instance.getCurrentAccount();
        String key = database.child("groups").push().getKey();
        account.groupIdList.add(key);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/groups/" + key, mGroup.toMap());
        childUpdates.put("/accounts/" + mGroup.ownerId, account.toMap());
        database.updateChildren(childUpdates);
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
        TextChangeHandler(Context context, final EditText text, final TextView button, final Group group) {
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
