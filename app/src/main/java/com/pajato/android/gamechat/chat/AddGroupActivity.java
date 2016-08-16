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
import android.content.res.Resources;
import android.os.Bundle;
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

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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

    // Public instance methods

    /** Process a button click on a given view by posting a button click event. */
    public void onClick(final View view) {
        EventUtils.post(this, view);
    }

    /** Process a button click on a given view by posting a button click event. */
    public void menuClick(final MenuItem item) {
        // Post all menu button clicks.
        int value = item.getItemId();
        String className = item.getClass().getSimpleName();
        EventBus.getDefault().post(new ClickEvent(this, value, null, item, className));
    }

    /** Post the options menu on demand. */
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_group_menu, menu);
        return true;
    }

    /** Process a given button click event by logging it. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        String format = "Button click event on type: {%s} with value {%d}.";
        int value = event.getValue();
        Log.v(TAG, String.format(format, event.getClassName(), value));

        // Process the sign in and sign out button clicks.
        switch (value) {
            case R.id.saveGroupButton:
                // Process the form and write the new group to Firebase.
                finish();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /** Initialize the main activity. */
    private void init() {
        // Set up the app components: toolbar, navigation drawer and edit text.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
        toolbar.setNavigationOnClickListener(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            EditText text = (EditText) findViewById(R.id.groupNameText);
            TextView button = (TextView) toolbar.findViewById(R.id.saveGroupButton);
            text.addTextChangedListener(new TextChangeHandler(this, text, button));
        }
    }

    // Private classes.

    private class TextChangeHandler implements TextWatcher {

        // Private class constants.

        /** The logcat tag. */
        private final String TAG = this.getClass().getSimpleName();

        // Private instance variables.

        /** The button being controlled by this change handler. */
        private TextView mSaveButton;

        /** The edit text widget. */
        private EditText mEditText;

        /** The text color for a disabled save button. */
        private int mDisabledColor;

        /** The text color for an enabled save button. */
        private int mEnabledColor;

        // Public constructor.

        /** Build a text change handler to manage a given save button. */
        TextChangeHandler(Context context, final EditText text, final TextView button) {
            mEditText = text;
            mSaveButton = button;
            Resources res = context.getResources();
            mDisabledColor = button.getCurrentTextColor();
            mEnabledColor = res.getColor(R.color.colorPrimaryDark);
        }

        /** Make sure that the hint is restored when necessary and that the name is unique. */
        @Override public void afterTextChanged(Editable s) {
            if (mEditText.getText().length() == 0) {
                // Restore the hint and disable the save button.
                //restoreHint();
                mSaveButton.setTextColor(mDisabledColor);
                mSaveButton.setEnabled(false);
            } else {
                if (isUnique()) {
                    // Render the save button enabled with the primary dark color.
                    mSaveButton.setEnabled(true);
                    mSaveButton.setTextColor(mEnabledColor);
                }
            }
        }

        @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override public void onTextChanged(final CharSequence s, final int start, final int before,
                                            final int count) {
        }

        /** Determine if the current group name is unique. */
        private boolean isUnique() {
            // TODO: enhance for v2
            return true;
        }

    }

}
