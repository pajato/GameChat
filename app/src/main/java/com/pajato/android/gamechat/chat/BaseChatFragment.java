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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.database.ProtectedUserManager;
import com.pajato.android.gamechat.database.RoomManager;

import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.chatGroupList;
import static com.pajato.android.gamechat.common.FragmentType.chatRoomList;
import static com.pajato.android.gamechat.common.FragmentType.createChatGroup;
import static com.pajato.android.gamechat.common.FragmentType.createProtectedUser;
import static com.pajato.android.gamechat.common.FragmentType.createRoom;
import static com.pajato.android.gamechat.common.FragmentType.messageList;
import static com.pajato.android.gamechat.common.FragmentType.protectedUsers;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.chatGroup;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.chatRoom;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.groupList;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.roomList;

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
    @Override protected void onDispatch(@NonNull final Context context) {
        // Ensure that the type is valid.  Signal failure if not, otherwise handle each possible
        // case signalling success.  If there are no valid cases signal failure.
        if (mDispatcher.type == null)
            return;
        switch (type) {
            case messageList:   // The messages in a room require both the group and room keys.
                String groupKey = mDispatcher.groupKey;
                String roomKey = mDispatcher.roomKey;
                String name = RoomManager.instance.getRoomName(roomKey);
                markMessagesSeen(groupKey, roomKey);
                mItem = new ListItem(chatRoom, groupKey, roomKey, name, 0, null);
                break;
            case roomMembersList:
                if (mDispatcher.groupKey == null || mDispatcher.roomKey == null)
                    return;
                String roomName = RoomManager.instance.getRoomName(mDispatcher.roomKey);
                mItem = new ListItem(roomList, mDispatcher.groupKey, mDispatcher.roomKey, roomName);
                break;
            case groupMembersList:
                if (mDispatcher.groupKey == null)
                    return;
                String groupName = GroupManager.instance.getGroupName(mDispatcher.groupKey);
                mItem = new ListItem(groupList, mDispatcher.groupKey, groupName);
                break;
            case createRoom:
            case joinRoom:
            case protectedUsers:
            case chatRoomList:  // The rooms in a group need the group key.
                if (mDispatcher.groupKey == null)
                    return;
                mItem = new ListItem(chatGroup, mDispatcher.groupKey, null, null, 0, null);
                break;
            default:
                break;
        }
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

    /** Process a click event by either toggling the FAB or handling a list item click. */
    protected void processClickEvent(final View view, final FragmentType type) {
        // Determine if this event is for the chat fab button.
        logEvent(String.format("onClick: (%s) with event {%s};", type.name(), view));
        switch (view.getId()) {
            case R.id.chatFab:
                // Handle the no-menu case for protected users
                switch (this.type) {
                    case protectedUsers:
                        if (AccountManager.instance.isRestricted()) {
                            String protectedWarning = getString(R.string.CannotMakeProtectedUser);
                            Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        DispatchManager.instance.chainFragment(getActivity(), createProtectedUser);
                        break;
                    default:
                        // It is a chat fab button.  Toggle the state.
                        FabManager.chat.toggle(this);
                        break;
                }
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
                ProtectedUserManager.instance.promoteUser(item.key);
            }
        };
        showAlertDialog(getString(R.string.PromoteUserTitle), message, null, okListener);
    }

    /** Handle click on item to delete a protected user */
    private void processDeleteProtectedUserClick(final ListItem item) {
        String message = String.format(getString(R.string.DeleteUserConfirm), item.name);
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int id) {
                ProtectedUserManager.instance.deleteProtectedUser(item.key);
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
                DispatchManager.instance.chainFragment(getActivity(), chatRoomList, item);
                break;
            case chatRoom: // Show the messages in a room.
                DispatchManager.instance.chainFragment(getActivity(), messageList, item);
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
        if (type.name().equals(protectedUsers.name())) {
            if (AccountManager.instance.isRestricted()) {
                String protectedWarning = getString(R.string.CannotMakeProtectedUser);
                Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
            }
            DispatchManager.instance.chainFragment(getActivity(), createProtectedUser);
        } else if (type.name().equals(chatGroupList.name())) {
            DispatchManager.instance.chainFragment(getActivity(), createChatGroup);
        } else if (type.name().equals(chatRoomList.name())) {
            DispatchManager.instance.chainFragment(getActivity(), createRoom, mItem);
        }
    }
}
