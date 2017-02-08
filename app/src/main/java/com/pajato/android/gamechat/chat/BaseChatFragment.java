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
import android.util.Log;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.RoomItem;

import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.chatRoomList;
import static com.pajato.android.gamechat.common.FragmentType.messageList;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.chatGroup;

/**
 * Provide a base class to support fragment lifecycle debugging.  All lifecycle events except for
 * onViewCreate() are handled by providing logcat tracing information.  The fragment manager is
 * displayed in order to help catch an elusive connected check failure.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseChatFragment extends BaseFragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseChatFragment.class.getSimpleName();

    /** The lifecycle event format string. */
    private static final String LOG_FORMAT = "Event: %s; Fragment: %s; Fragment Manager: %s.";

    /** Extra information format string. */
    private static final String SUFFIX_FORMAT = "Fragment Type: %s; State: %s; Bundle: %s.";

    // Public instance methods.

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) mAdView.destroy();
    }

    /** Log the lifecycle event, stop showing ads and turn off the app event bus. */
    @Override public void onPause() {
        super.onPause();
        if (mAdView != null)
            mAdView.pause();
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        // Log the event, update the FAB for this fragment, process the ad, determine if a list
        // adapter update needs be processed and set the toolbar titles.
        super.onResume();
        FabManager.chat.init(this);
        if (mAdView != null)
            mAdView.resume();
        if (type != null)
            switch (type) {
                case joinRoom:
                case chatGroupList:
                case chatRoomList:
                case selectChatGroupsRooms:
                case selectExpGroupsRooms:
                case messageList:   // Update the state of the list adapter.
                    updateAdapterList();
                    break;
                default:            // Ignore all other fragments.
                    break;
            }
    }

    /** Initialize chat list fragments by dealing with ads. */
    @Override public void onStart() {
        super.onStart();
        initAdView(mLayout);
    }

    /** Set the item defining this fragment (passed from the parent (spawning) fragment. */
    public void setItem(final ListItem item) {
        mItem = item;
    }

    // Protected instance methods.

    /** Log a lifecycle event that has no bundle. */
    @Override protected void logEvent(final String event) {
        logEvent(event, null);
    }

    /** Log a lifecycle event that has a bundle. */
    @Override protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        String state = mActive ? "foreground" : "background";
        String bundleMessage = bundle == null ? "N/A" : bundle.toString();
        Log.v(TAG, String.format(Locale.US, LOG_FORMAT, event, this, manager));
        Log.v(TAG, String.format(Locale.US, SUFFIX_FORMAT, type, state, bundleMessage));
    }

    /** Return TRUE iff the fragment setup is handled successfully. */
    @Override protected boolean onDispatch(@NonNull final Context context,
                                           @NonNull final Dispatcher dispatcher) {
        // Ensure that the type is valid.  Signal failure if not, otherwise handle each possible
        // case signalling success.  If there are no valid cases signal failure.
        if (dispatcher.type == null) return false;
        switch (type) {
            case chatGroupList: // A group list does not need an item.
                return true;
            case messageList:   // The messages in a room require both the group and room keys.
                RoomItem roomItem = new RoomItem(dispatcher.groupKey, dispatcher.roomKey);
                mItem = new ListItem(roomItem);
                return true;
            case createRoom:
            case joinRoom:
            case chatRoomList:  // The rooms in a group need the group key.
                if (dispatcher.groupKey == null)
                    return false;
                mItem = new ListItem(chatGroup, dispatcher.groupKey, null, null, 0, null);
                return true;
            default:
                return false;
        }
    }

    /** Process a click event by either toggling the FAB or handling a list item click. */
    protected void processClickEvent(final View view, final String tag) {
        // Determine if this event is for the chat fab button.
        logEvent(String.format("onClick: (%s) with event {%s};", tag, view));
        switch (view.getId()) {
            case R.id.chatFab:
                // It is a chat fab button.  Toggle the state.
                FabManager.chat.toggle(this);
                break;
            default:
                // Determine if the button click was generated by a group view or room view drill
                // down.  Handle it by adding the next fragment to the back stack.
                processPayload(view);
                break;
        }
    }

    /** Process a button click that may be a chat list item click. */
    protected void processPayload(final View view) {
        // Ensure that the payload is valid.  Abort if not, otherwise chain to the next appropriate
        // fragment based on the type associated with the payload.
        Object payload = view.getTag();
        if (!mActive || !(payload instanceof ListItem))
            return;
        ListItem item = (ListItem) payload;
        switch (item.type) {
            case chatGroup: // Drill into the rooms in group.
                DispatchManager.instance.chainFragment(getActivity(), chatRoomList, item);
                break;
            case chatRoom: // Show the messages in a room.
                DispatchManager.instance.chainFragment(getActivity(), messageList, item);
                break;
            default:
                break;
        }
    }
}
