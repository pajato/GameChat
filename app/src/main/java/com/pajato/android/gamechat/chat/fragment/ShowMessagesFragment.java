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

package com.pajato.android.gamechat.chat.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.main.MainService;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.chat.model.Message.STANDARD;
import static com.pajato.android.gamechat.common.FragmentType.roomMembersList;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.game;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.members;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.search;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.common.model.JoinState.JoinType.chat;
import static com.pajato.android.gamechat.main.MainService.ROOM_KEY;

/**
 * Display the chat associated with the room selected by the current logged in User.
 *
 * @author Paul Michael Reilly
 */
public class ShowMessagesFragment extends BaseChatFragment implements View.OnClickListener,
        TextView.OnEditorActionListener {

    // Public instance methods.

    /** Return null or a list to be displayed by the list adapter */
    public List<ListItem> getList() {
        return MessageManager.instance.getListItemData(mDispatcher);
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        return GroupManager.instance.getGroupName(mDispatcher.groupKey);
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        if (AccountManager.instance.isMeGroup(mDispatcher.groupKey))
            return getString(R.string.GroupMeToolbarTitle);
        return RoomManager.instance.getRoomName(mDispatcher.roomKey);
    }

    /** Ensure that all the messages in a given room are marked as seen by the account holder. */
    private void markMessagesSeen(@NonNull final String groupKey, @NonNull final String roomKey) {
        List<Message> list = MessageManager.instance.getMessageList(groupKey, roomKey);
        String accountId = AccountManager.instance.getCurrentAccountId();
        if (list == null || list.size() == 0 || accountId == null)
            return;
        for (Message message : list)
            if (message.unseenList.contains(accountId)) {
                message.unseenList.remove(accountId);
                MessageManager.instance.updateMessage(message);
            }
    }

    /** Process a given button click event looking for one on the chat fab button. */
    @Subscribe public void onClick(final ClickEvent event) {
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

    /** Handle a button click on the FAB button by posting a new message. */
    @Override public void onClick(final View view) {
        // Ensure that the click occurred on the send message button.
        if (view instanceof FloatingActionButton) {
            // It did.  Post the message.
            postMessage(view);
        }
    }

    /** Manage the list UI every time a message change occurs. */
    @Subscribe public void onChatListChange(final ChatListChangeEvent event) {
        // Determine if this fragment cares about chat list changes.  If so, do a redisplay.
        String format = "onChatListChange with event {%s}";
        logEvent(String.format(Locale.US, format, "no list", event));
        if (mActive)
            updateAdapterList();
    }

    /** Handle an enter key press to allow the user to press enter to send their text. */
    @Override public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_NULL) {
            postMessage(textView);
        }
        return false;
    }

    /** Handle a menu item selection. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        if (!this.mActive)
            return;
        // Case on the item resource id if there is one to be had.
        switch (event.item != null ? event.item.getItemId() : -1) {
            case R.string.MenuItemSearch:
                showFutureFeatureMessage(R.string.MenuItemSearch);
                break;
            case R.string.MembersMenuItem:
                DispatchManager.instance.dispatchToFragment(this, roomMembersList);
                break;
            default:
                break;
        }
    }

    /** Deal with the fragment's lifecycle by marking the join inactive. */
    @Override public void onPause() {
        super.onPause();
        clearJoinState(mDispatcher.groupKey, mDispatcher.roomKey, chat);
    }

    /** Deal with the fragment's lifecycle by managing the FAB. */
    @Override public void onResume() {
        super.onResume();        // Turn off the FAB and force a recycler view update.
        initEditText(mLayout);
        FabManager.chat.setVisibility(this, View.GONE);
        setJoinState(mDispatcher.groupKey, mDispatcher.roomKey, chat);
    }

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        mDispatcher = dispatcher;
        markMessagesSeen(mDispatcher.groupKey, mDispatcher.roomKey);
    }

    /** Setup the toolbar. */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, helpAndFeedback, members, game, search, invite, settings);
    }

    // Private instance methods.

    /** Initialize the edit text field. */
    private void initEditText(@NonNull final View layout) {
        // Set up the edit text field and the send button.
        EditText editText = (EditText) layout.findViewById(R.id.messageEditText);
        editText.addTextChangedListener(new EditTextWatcher(layout));
        editText.setOnEditorActionListener(this);
        View sendButton = layout.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }

    /** Post a message using the given view to clear the software keyboard. */
    private void postMessage(final View view) {
        // Ensure that the edit text field and the account exist.
        View layout = getView();
        int id = R.id.messageEditText;
        EditText editText = layout != null ? (EditText) layout.findViewById(id) : null;
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null || editText == null) {
            // Something is wrong.  Log it and tell the User.
            Snackbar.make(view, "Software error: could not send message!", Snackbar.LENGTH_LONG);
            dismissKeyboard();
            return;
        }

        // The account and the edit text field exist.  Persist the message to the database, inform
        // the User that the message has been sent.
        String text = editText.getText().toString();
        String roomKey = mDispatcher.roomKey;
        Room room = RoomManager.instance.getRoomProfile(roomKey);
        MessageManager.instance.createMessage(text, STANDARD, account, room);
        String snackbarMessage = getResources().getString(R.string.MessageSentText);
        Snackbar.make(layout, snackbarMessage, Snackbar.LENGTH_SHORT);

        // Wrap-up by notifying room members, clearing the edit text field and dismissing the
        // keyboard.
        Intent intent = new Intent(this.getActivity(), MainService.class);
        intent.putExtra(ROOM_KEY, room.key);
        getContext().startService(intent);
        editText.setText("");
        dismissKeyboard();
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
