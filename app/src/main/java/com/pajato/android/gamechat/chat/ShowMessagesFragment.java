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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.account.AccountStateChangeEvent;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.fragment.BaseFragment;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showNoAccount;

/**
 * Display the chat associated with the room selected by the current logged in User.
 *
 * @author Paul Michael Reilly
 */
public class ShowMessagesFragment extends BaseFragment {

    /** The logcat tag constant. */
    private static final String TAG = ShowMessagesFragment.class.getSimpleName();

    /** The format used to generate the database path for messages. */
    private static final String MESSAGES_FORMAT = "/groups/%s/rooms/%s/messages/";

    // Public class constants.

    public static final String FRIENDLY_MSG_LENGTH = "friendly_msg_length";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 60;

    // Private class constants.


    // Private instance variables

    // Public instance methods

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe
    public void buttonClickHandler(final ClickEvent event) {
        // Determine if this event is for the chat fab button.
        int value = event.getView() != null ? event.getView().getId() : 0;
        switch (value) {
            default:
                // Provide an empty placeholder for would be handlers.
                break;
        }
    }

    /** Obtain the remote configuration from Firebase. */
    public void fetchConfig() {
        // Set the cache expiration time to one hour (or not at all if developer mode is turned on).
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        boolean developerMode = config.getInfo().getConfigSettings().isDeveloperModeEnabled();
        final long cacheExpiration = developerMode ? 0 : 3600;
        config.fetch(cacheExpiration)
            .addOnSuccessListener(new RemoteFetchSuccessHandler())
            .addOnFailureListener(new RemoteFetchFailureHandler());
    }

    /** Handle an account state change event by showing the no sign in message. */
    @Subscribe public void onAccountStateChange(final AccountStateChangeEvent event) {
        // Determine if this represents a no account situation due to a sign out event.
        if (event.account == null) {
            // There is no account.  Switch to the no account fragment.
            ChatManager.instance.replaceFragment(showNoAccount, this.getActivity());
        }
    }

    /** Deal with the options menu creation by making the search and back items visible. */
    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        setOptionsMenu(menu, inflater, new int[] {R.id.back, R.id.search}, null);
        MenuItem item = menu.findItem(R.id.back);
        item.setVisible(true);
        item.setVisible(true);
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
        FabManager.chat.setState(View.GONE);
        initRemoteConfig();
        initList(result, ChatListManager.instance.getMessageListData(mItem), true);
        initEditText(result);
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

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

    // Private instance methods.

    /**
     * Apply retrieved length limit to edit text field.
     * This result may be fresh from the server or it may be from cached
     * values.
     */
    private void applyRetrievedLengthLimit() {
        // Determine if there is a size constraint available to apply and a message text view to
        // apply it to.
        View layout = getView();
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        Long friendly_msg_length = config.getLong("friendly_msg_length");
        int id = R.id.messageEditText;
        EditText editText = layout != null ? (EditText) layout.findViewById(id) : null;
        if (editText != null) {
            // The message text view has been created.  Establish the app size constraint.
            InputFilter filter = new InputFilter.LengthFilter(friendly_msg_length.intValue());
            InputFilter[] filterArray = new InputFilter[] {filter};
            editText.setFilters(filterArray);
            return;
        }

        Log.e(TAG, "Could not apply the retrieved message length: no edit text field found!");
    }

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
        // ...
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int lengthPref = prefs.getInt(FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT);
        InputFilter lengthFilter = new InputFilter.LengthFilter(lengthPref);
        InputFilter[] filters = new InputFilter[] {lengthFilter};
        EditText editText = (EditText) layout.findViewById(R.id.messageEditText);
        editText.setFilters(filters);
        editText.addTextChangedListener(new EditTextWatcher(layout));
    }

    /** Initialize the remote configuration. */
    private void initRemoteConfig() {
        // TODO: This should probably reside in the main activity or in the chat envelope fragment,
        // likely a RemoteConfigManager.
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(true)
            .build();
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("friendly_msg_length", 10L);
        config.setConfigSettings(settings);
        config.setDefaults(defaultConfigMap);
    }

    /** Post a message using the given view to clear the software keyboard. */
    private void postMessage(final View view) {
        // Setup the database to persist the message.
        String path = String.format(Locale.US, MESSAGES_FORMAT, mItem.groupKey, mItem.roomKey);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(path);
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
            String name = account.getDisplayName();
            long tstamp = new Date().getTime();
            String text = editText.getText().toString();
            List<String> members = ChatListManager.instance.getMembers(mItem.roomKey);
            String type = "standard";
            Message message = new Message(uid, name, key, tstamp, tstamp, text, type, members);

            // Persist the message instance, clear the message from the edit text control and hide
            // the soft keyboard.
            DatabaseManager.instance.updateChildren(database, path, key, message.toMap());
            editText.setText("");
            hideSoftKeyBoard(view);
        }
    }

    // Private inner classes.

    /** Handle successful remote fetch operations. */
    private class RemoteFetchSuccessHandler implements OnSuccessListener<Void> {
        @Override public void onSuccess(Void aVoid) {
            // Make the fetched config available via
            // FirebaseRemoteConfig get<type> calls.
            FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
            config.activateFetched();
            applyRetrievedLengthLimit();
        }
    }

    /** Handle failed remote fetch operations. */
    private class RemoteFetchFailureHandler implements OnFailureListener {
        @Override public void onFailure(@NonNull Exception exc) {
            // Log the remote fetch error and retrieve a default value.
            Log.w(TAG, "Error fetching config: " + exc.getMessage());
            applyRetrievedLengthLimit();
        }
    }

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
