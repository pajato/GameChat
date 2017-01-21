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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;

/** Provide a base class for fragments that create something. */
public abstract class BaseCreateFragment extends BaseChatFragment {

    // Public enum.

    /** The create type. */
    public enum CreateType {group, room}

    // Protected instance variables.

    /** The current create type. */
    protected CreateType mCreateType;

    /** Set the room or group type. */
    protected abstract void setType(final int type);

    // Public instance methods.

    /** Establish the layout file to show that the app is offline due to network loss. */
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setLayoutId(R.layout.fragment_chat_create);
    }

    /** Provide a click event handler. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Log the event and determine if the event looks right.  Abort if it doesn't.
        logEvent(String.format(Locale.US, "onClick (create %s) event: {%s}.", mCreateType, event));
        if (event == null || event.view == null) return;

        // The event appears to be expected.  Confirm by finding the selector check view.
        switch (event.view.getId()) {
            case R.id.SaveButton: // Validate and persist the group, and be done with the activity.
                Account account = AccountManager.instance.getCurrentAccount();
                if (account != null)
                    save(account);
                else
                    abort("The User account does not exist.  Aborting.");
                DispatchManager.instance.startNextFragment(getActivity(), chat);
                break;

            case R.id.ClearNameButton: // Clear the group name edit text field.
                EditText editText = (EditText) mLayout.findViewById(R.id.NameText);
                if (editText != null) editText.setText("");
                break;

            case R.id.AddMembers:
                showFutureFeatureMessage(R.string.InviteMembersFeature);
                break;

            case R.id.SettableIconButton:
                showFutureFeatureMessage(R.string.SetCreateIconFeature);
                break;

            case R.id.PublicButton:
                setType(Room.PUBLIC);
                break;

            case R.id.PrivateButton:
                setType(Room.PRIVATE);
                break;

            default:
                // Ignore everything else.
                break;
        }
    }

    /** Initialize the create type. */
    @Override public void onStart() {
        super.onStart();
        EditText text = (EditText) mLayout.findViewById(R.id.NameText);
        TextView button = (TextView) mLayout.findViewById(R.id.SaveButton);
        text.addTextChangedListener(new TextChangeHandler(getContext(), text, button));
        text.setText(getDefaultName());
    }

    /** Reset the FAM to use the game home menu. */
    @Override public void onResume() {
        super.onResume();
        FabManager.chat.setVisibility(this, View.GONE);
    }

    // Protected instance methods.

    /** Log and prsent a toast message to the User. */
    protected void abort(final String message) {
        logEvent(String.format(Locale.US, "Error: (create %s): %s.", mCreateType, message));
        // TODO: throw up a toast or snackbar.
    }

    /** Return a default name. */
    protected String getDefaultName() {
        return "";
    }

    /** The save operation for a given account. */
    abstract protected void save(final Account account);

    /** Set the name of the managed object. */
    abstract protected void setName(final String value);

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

        // Public constructor.

        /** Build a text change handler to manage a given edit text field, save button and group. */
        TextChangeHandler(Context context, final EditText text, final TextView button) {
            mEditText = text;
            mSaveButton = button;
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
                setName(mEditText.getText().toString());
            }
        }

        @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

        @Override public void onTextChanged(final CharSequence s, final int start, final int before,
                                            final int count) {}

    }
}
