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

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.ProtectedUserManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ChatListChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.chatGroupList;
import static com.pajato.android.gamechat.common.FragmentType.chatRoomList;
import static com.pajato.android.gamechat.common.FragmentType.createChatGroup;
import static com.pajato.android.gamechat.common.FragmentType.createProtectedUser;
import static com.pajato.android.gamechat.common.FragmentType.createRoom;
import static com.pajato.android.gamechat.common.FragmentType.messageList;
import static com.pajato.android.gamechat.common.FragmentType.protectedUsers;

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

    /** Handle a group profile change by trying again to start a better fragment. */
    @Subscribe
    public void onChatListChange(final ChatListChangeEvent event) {
        updateAdapterList();
    }

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        super.onDestroy();
        if (mAdView != null)
            mAdView.destroy();
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
        if (mAdView != null)
            mAdView.resume();
        if (type != null)
            switch (type) {
                case chatGroupList:
                case chatRoomList:
                case createProtectedUser:
                case groupMembersList:
                case groupsForProtectedUser:
                case joinRoom:
                case messageList:
                case protectedUsers:
                case roomMembersList:
                case selectGroupsRooms:
                    updateAdapterList();   // Update the state of the list adapter.
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

    /** Process a click event by either toggling the FAB or handling a list item click. */
    protected void processClickEvent(final View view, final FragmentType type) {
        // Determine if this event is for the chat fab button.
        logEvent(String.format("onClick: (%s) with event {%s};", type.name(), view));
        switch (view.getId()) {
            case R.id.chatFab:
                // It is a chat fab button.  Toggle the state.
                FabManager.chat.toggle(this);
                break;
            case R.id.endIcon:
            case R.id.veryEndIcon:
                // Click on the end (or very end) item icon
                processEndIconClick(view);
                break;
            default:
                // Determine if the button click was generated by a group view or room view drill
                // down.  Handle it by adding the next fragment to the back stack.
                processPayload(view, type);
                break;
        }
    }

    /** Process the end icon click */
    private void processEndIconClick(final View view) {
        if (!(view.getTag() instanceof ListItem))
            return;
        ListItem item = (ListItem) view.getTag();
        switch (item.type) {
            case protectedUserList:
                if (view.getId() == (R.id.veryEndIcon)) {
                    processDeleteProtectedUserClick(item);
                } else {
                    processPromoteProtectedUserClick(item);
                }
                break;
            case expGroup:
            case chatGroup:
                handleLeaveGroupClick(item);
                break;
            case expRoom:
            case chatRoom:
                handleLeaveRoomClick(item);
                break;
            default:
                break;
        }
    }

    /** Handle click on item to promote a protected user */
    private void processPromoteProtectedUserClick(final ListItem item) {
        String message = String.format(getString(R.string.PromoteUserConfirm), item.name);
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int id) {
                ProtectedUserManager.instance.promoteUser(item.memberKey);
            }
        };
        showAlertDialog(getString(R.string.PromoteUserTitle), message, null, okListener);
    }

    /** Handle click on item to delete a protected user */
    private void processDeleteProtectedUserClick(final ListItem item) {
        String message = String.format(getString(R.string.DeleteUserConfirm), item.name);
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int id) {
                ProtectedUserManager.instance.deleteProtectedUser(item.memberKey);
            }
        };
        showAlertDialog(getString(R.string.DeleteUserTitle), message, null, okListener);
    }

    /** Handle click on item to leave (or delete) group */
    private void handleLeaveGroupClick(final ListItem item) {
        final Group group = GroupManager.instance.getGroupProfile(item.groupKey);
        if (group == null)
            return;
        if (AccountManager.instance.getCurrentAccountId().equals(group.owner))
            showFutureFeatureMessage(R.string.DeleteGroupMessage);
        else {
            String message = String.format(getString(R.string.LeaveConfirmMessage), group.name);
            DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int id) {
                    AccountManager.instance.leaveGroup(group);
                }
            };
            showAlertDialog(getString(R.string.LeaveGroupTitle), message, null, okListener);
        }
    }

    /** Handle click on item to leave (or delete) room */
    private void handleLeaveRoomClick(final ListItem item) {
        final Room room = RoomManager.instance.getRoomProfile(item.roomKey);
        if (room == null)
            return;
        if (AccountManager.instance.getCurrentAccountId().equals(room.owner))
            showFutureFeatureMessage(R.string.DeleteRoomMessage);
        else {
            String message = String.format(getString(R.string.LeaveConfirmMessage), room.getName());
            DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int id) {
                    AccountManager.instance.leaveRoom(room);
                }
            };
            showAlertDialog(getString(R.string.LeaveRoomTitle), message, null, okListener);
        }
    }

    /** Process a button click that may be a chat list item click. */
    protected void processPayload(final View view, final FragmentType type) {
        // Ensure that the payload is valid.  Abort if not, otherwise chain to the next appropriate
        // fragment based on the type associated with the payload.
        Object payload = view.getTag();
        if (!mActive || !(payload instanceof ListItem))
            return;
        ListItem item = (ListItem) payload;
        switch (item.type) {
            case chatGroup: // Drill into the rooms in group.
                DispatchManager.instance.dispatchToFragment(this, chatRoomList, null, item);
                break;
            case chatRoom: // Show the messages in a room.
                DispatchManager.instance.dispatchToFragment(this, messageList, null, item);
                break;
            case newItem:
                handleNewItem(type);
                break;
            default:
                break;
        }
    }

    /** Determine the type of the desired new item and dispatch accordingly */
    protected void handleNewItem(final FragmentType type) {
        if (type == protectedUsers) {
            if (AccountManager.instance.isRestricted()) {
                String protectedWarning = getString(R.string.CannotMakeProtectedUser);
                Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
            }
            DispatchManager.instance.dispatchToFragment(this, createProtectedUser);
        } else if (type == chatGroupList) {
            DispatchManager.instance.dispatchToFragment(this, createChatGroup);
        } else if (type == chatRoomList) {
            DispatchManager.instance.dispatchToFragment(this, createRoom);
        }
    }
}
