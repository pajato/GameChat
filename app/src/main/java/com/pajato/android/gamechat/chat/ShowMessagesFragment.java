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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.chat.ChatListManager.MESSAGES_FORMAT;
import static com.pajato.android.gamechat.chat.ChatListManager.STANDARD;
import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoAccount;

/**
 * Display the chat associated with the room selected by the current logged in User.
 *
 * @author Paul Michael Reilly
 */
public class ShowMessagesFragment extends BaseFragment implements View.OnClickListener {

    // Public instance methods.

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        int value = event.getView() != null ? event.getView().getId() : 0;
        switch (value) {
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

    private void showFutureFeatureMessage(final int resourceId) {
        // Post a toast message.
        Context context = getContext();
        String prefix = context.getString(resourceId);
        String suffix = context.getString(R.string.FutureFeature);
        CharSequence text = String.format(Locale.getDefault(), "%s %s", prefix, suffix);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /** Handle an account state change event by showing the no sign in message. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // Determine if this represents a no account situation due to a sign out event.
        if (event.account == null) {
            // There is no account.  Switch to the no account fragment.
            ChatManager.instance.replaceFragment(showNoAccount, this.getActivity());
        }
    }

    /** Handle a button click on the FAB button by posting a new message. */
    @Override public void onClick(final View view) {
        // Ensure that the click occurred on the send message button.
        if (view instanceof FloatingActionButton) {
            // It did.  Post the message.
            postMessage(view);
        }
    }

    /** Deal with the options menu creation by making the search and back items visible. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        setOptionsMenu(menu, inflater, new int[] {R.id.back, R.id.search}, null);
        MenuItem item = menu.findItem(R.id.back);
        if (item != null) item.setVisible(true);
    }

    /** Handle the setup of the list of messages. */
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Inflate the layout for this fragment and initialize by setting the titles, declaring the
        // use of the options menu, removing the FAB button, fetching any remote configurations,
        // setting up the list of messages, setting up the edit text field and setting up the
        // database analytics.
        View result = inflater.inflate(R.layout.fragment_chat_messages, container, false);
        setTitles(null, mItem.roomKey);
        setHasOptionsMenu(true);
        mItemListType = ChatListManager.ChatListType.message;
        FabManager.chat.setState(View.GONE);
        initList(result, ChatListManager.instance.getList(mItemListType, mItem), true);
        initEditText(result);

        return result;
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.toolbar_game_icon:
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if(viewPager != null) {
                    viewPager.setCurrentItem(PaneManager.GAME_INDEX);
                }
                break;
            case R.id.back:
                // Return to the spawning room view.
                ChatManager.instance.popBackStack(getActivity());
                break;
            case R.id.search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Deal with the fragment's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Turn off the FAB.
        FabManager.chat.setState(View.GONE);
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
        // Setup the database to persist the message.
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String path = String.format(Locale.US, MESSAGES_FORMAT, mItem.groupKey, mItem.roomKey);
        String key = database.child(path).push().getKey();

        // Ensure that the edit text field and the account exist.
        View layout = getView();
        EditText editText = layout != null
                ? (EditText) layout.findViewById(R.id.messageEditText)
                : null;
        Account account = AccountManager.instance.getCurrentAccount();
        if (account != null && editText != null) {
            // The account and the edit text field exist.  Create the message instance.
            String uid = account.accountId;
            String me = getResources().getString(R.string.me);
            String anonymous = getResources().getString(R.string.anonymous);
            String name = account.getDisplayName(account, me, anonymous);
            String url = account.accountUrl != null ? account.accountUrl : null;
            long tstamp = new Date().getTime();
            String text = editText.getText().toString();
            List<String> members = ChatListManager.instance.getMembers(mItem.roomKey);
            members.remove(uid);
            int type = STANDARD;
            Message message = new Message(uid, name, url, key, tstamp, tstamp, text, type, members);

            // Persist the message instance, clear the message from the edit text control and hide
            // the soft keyboard.
            DatabaseManager.instance.updateChildren(database, path, key, message.toMap());
            editText.setText("");
            hideSoftKeyBoard(view);
        }
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
