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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.pajato.android.gamechat.BuildConfig;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ChatListChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static com.pajato.android.gamechat.chat.model.Message.STANDARD;

/**
 * Display the chat associated with the room selected by the current logged in User.
 *
 * @author Paul Michael Reilly
 */
public class ShowMessageListFragment extends BaseChatFragment implements View.OnClickListener {

    // Public instance methods.

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        View view = event.view;
        switch (view.getId()) {
            case R.id.insertPhoto:
                showFutureFeatureMessage(R.string.InsertPhoto);
                break;
            case R.id.takePicture:
                showFutureFeatureMessage(R.string.TakePicture);
                break;
            case R.id.takeVideo:
                showFutureFeatureMessage(R.string.TakeVideo);
                break;
            case R.id.insertEmoticon:
                showFutureFeatureMessage(R.string.InsertEmoticon);
                break;
            case R.id.insertMap:
                showFutureFeatureMessage(R.string.InsertMap);
                break;
            default:
                // Provide an empty placeholder for would be handlers.
                break;
        }
    }

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_chat_messages;}

    /** Handle a button click on the FAB button by posting a new message. */
    @Override public void onClick(final View view) {
        // Ensure that the click occurred on the send message button.
        if (view instanceof FloatingActionButton) {
            // It did.  Post the message.
            postMessage(view);
        }
    }

    /** Deal with the options menu creation by making the search item visible. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        setItemState(menu, R.id.search, true);
    }

    /** Handle the setup of the list of messages. */
    @Override public void onInitialize() {
        // Inflate the layout for this fragment and initialize by setting the app bar title text,
        // declaring the use of the options menu, removing the FAB button, fetching any remote
        // configurations, setting up the list of messages, and by setting up the edit text field.
        super.onInitialize();
        if (BuildConfig.DEBUG && mItem == null) throw new AssertionError("mitem is null!");
        setTitles(mItem.groupKey, mItem.roomKey);
        mItemListType = DatabaseListManager.ChatListType.message;
        FabManager.chat.setState(this, View.GONE);
        initList(mLayout, DatabaseListManager.instance.getList(mItemListType, mItem), true);
        initEditText(mLayout);
    }

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onChatListChange(final ChatListChangeEvent event) {
        // Log the event and update the list saving the result for a retry later.
        logEvent(String.format(Locale.US, "onMessageListChange with event {%s}", event));
        mUpdateOnResume = !updateAdapterList();
    }

    /** Deal with the fragment's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Turn off the FAB and force a recycler view update.
        setTitles(mItem.groupKey, mItem.roomKey);
        FabManager.chat.setState(this, View.GONE);
        mUpdateOnResume = true;
        super.onResume();
    }

    // Private instance methods.

    /** Dismiss the virtual keyboard in response to a click on the given view. */
    private void hideSoftKeyBoard(final View view) {
        // Determine if the keyboard is active before dismissing it.
        final String SERVICE = Context.INPUT_METHOD_SERVICE;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(SERVICE);
        if(imm.isAcceptingText()) {
            // The virtual keyboard is active.  Dismiss it.
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /** Initialize the edit text field. */
    private void initEditText(@NonNull final View layout) {
        // Set up the edit text field and the send button.
        EditText editText = (EditText) layout.findViewById(R.id.messageEditText);
        editText.addTextChangedListener(new EditTextWatcher(layout));
        View sendButton = layout.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }

    /** Post a message using the given view to clear the software keyboard. */
    private void postMessage(final View view) {
        // Ensure that the edit text field and the account exist.
        View layout = getView();
        EditText editText = layout != null
                ? (EditText) layout.findViewById(R.id.messageEditText) : null;
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null || editText == null) {
            // Something is wrong.  Log it and tell the User.
            Snackbar.make(view, "Software error: could not send message!", Snackbar.LENGTH_LONG);
            hideSoftKeyBoard(view);
            return;
        }

        // The account and the edit text field exist.  Persist the message to the database and
        // inform the User that the message has been sent.
        String text = editText.getText().toString();
        int type = STANDARD;
        String roomKey = mItem.roomKey;
        Room room = DatabaseListManager.instance.getRoomProfile(roomKey);
        DatabaseManager.instance.createMessage(text, type, account, room);
        editText.setText("");
        Snackbar.make(layout, "Message sent.", Snackbar.LENGTH_SHORT);
        hideSoftKeyBoard(view);
    }

    // Private inner classes.

    /** Provide a text handler for messages to be posted. */
    private class EditTextWatcher implements TextWatcher {

        // Private instance variables.

        /** The send button. */
        View mSendButton;

        // Public constructor.

        /** Build a handler with a given layout. */
        EditTextWatcher(final View layout) {
            mSendButton = layout.findViewById(R.id.sendButton);
        }

        // Public instance methods.

        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        /** Ensure that the send button gets enabled when there is text to send. */
        @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Don't accept any text if there is no send button.
            if (mSendButton != null) {
                boolean value = false;
                if (charSequence.toString().trim().length() > 0) value = true;
                mSendButton.setEnabled(value);
            }
        }

        /** ... */
        @Override public void afterTextChanged(Editable editable) {}

    }
}
