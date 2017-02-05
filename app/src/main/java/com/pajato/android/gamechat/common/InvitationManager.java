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

package com.pajato.android.gamechat.common;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.adapter.InviteRoomItem;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.ResourceHeaderItem;
import com.pajato.android.gamechat.common.adapter.SelectableGroupItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.common.model.GroupInviteData;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.DBUtils;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MemberManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.GroupJoinedEvent;
import com.pajato.android.gamechat.event.ProfileGroupChangeEvent;
import com.pajato.android.gamechat.event.ProfileRoomChangeEvent;
import com.pajato.android.gamechat.main.MainActivity;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.pajato.android.gamechat.chat.model.Message.STANDARD;
import static com.pajato.android.gamechat.chat.model.Room.RoomType.COMMON;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.inviteCommonRoom;

/**
 * Handle invitations to groups.
 *
 * @author Paul Michael Reilly
 */
public enum InvitationManager implements ResultCallback<AppInviteInvitationResult>,
        GoogleApiClient.OnConnectionFailedListener {
    instance;

    // Private constants.

    private static final String APP_CODE = "aq5ca";
    private static final String PLAY_STORE_LINK = "https://play.google.com/apps/testing/com.pajato.android.gamechat";
    private static final String APP_PACKAGE_NAME = "com.pajato.android.gamechat";
    private static final String WEB_LINK = "https://github.com/pajato/GameChat";
//    public static final String APP_INVITE_PATH = "/invites/app/";
    public static final String APP_INVITE_ID_PATH = "/invites/app/%s/";

    /** The logcat TAG. */
    private static final String TAG = InvitationManager.class.getSimpleName();

    // Private instance variables.

    /** The map managing group invitations. */
    private Map<String, List<String>> mGroupInviteMap = new HashMap<>();

    /** Outstanding invitations found during app creation but prior to account authentication */
    private List<String> mInvitationIds = new ArrayList<>();

    /** Map to hold group/rooms while the external invite intent is running */
    private Map<String, GroupInviteData> mInviteMap = new HashMap<>();

    /** The repository for any messages needed. */
    private SparseArray<String> mMessageMap = new SparseArray<>();

    // Public instance variables.

    // Public instance methods.

    /** Accept a group invite for a given account by updating both the group and the account. */
    public void acceptGroupInvite(@NonNull final Account account, @NonNull final String groupKey) {
        // Determine if the current account is already a member of the given group, has not been
        // invited or if there is no group to accept the invitation to. Abort if any are true.
        boolean isMember = account.joinList.contains(groupKey);
        boolean isInvited = hasGroupInvite(account, groupKey);
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        if (isMember || !isInvited || group == null) return;

        // The account holder has been invited to join the given group.  Do so by adding the group
        // key to the account join list and create a copy of the account as a member of the group.
        account.joinList.add(groupKey);
        AccountManager.instance.updateAccount(account);
        Account member = new Account(account);
        member.groupKey = groupKey;
        String path = MemberManager.instance.getMembersPath(groupKey, member.id);
        DBUtils.instance.updateChildren(path, member.toMap());

        // Finally add the invited account to the accepting group's profile member list.
        group.memberList.add(member.id);
        path = GroupManager.instance.getGroupProfilePath(groupKey);
        DBUtils.instance.updateChildren(path, group.toMap());
    }

    /** Clear the external app invitations map */
    public void clearInvitationMap() {
        mInviteMap.clear();
    }

    /**
     * Extend an invitation to join GameChat using AppInviteInvitation and specify a map of
     * groups and their rooms to join (always has at least the Common room).
     */
    public void extendInvitation(final FragmentActivity activity,
                                 final Map<String, GroupInviteData> keys) {
        Log.i(TAG, "extendInvitation with list of keys");
        // Save the groups/rooms map to write to Firebase after the invite id(s) are returned */
        mInviteMap = keys;
        startInvitationIntent(activity, null);
    }

    /** Extend an invitation to join GameChat using AppInviteInvitation and specify a room to join. */
    public void extendRoomInvitation(final FragmentActivity activity, final String roomKey) {
        Room room = RoomManager.instance.getRoomProfile(roomKey);
        if (room == null) {
            Log.e(TAG, "Room manager cannot find room " + roomKey + "; invitation aborted!");
            return;
        }
        Group group = GroupManager.instance.getGroupProfile(room.groupKey);
        if (group == null) {
            Log.e(TAG, "Group manager cannot find group " + room.groupKey + "; invitation aborted!");
            return;
        }
        Map<String, GroupInviteData> dataMap = new HashMap<>();
        List<String> roomList = new ArrayList<>();
        roomList.add(room.key);
        GroupInviteData map = new GroupInviteData(group.key, group.name, group.commonRoomKey,
                roomList);
        dataMap.put(group.key, map);
        extendInvitation(activity, dataMap);
    }

    /** Extend an invitation to join GameChat using AppInviteInvitation and specify a group to join. */
    public void extendGroupInvitation(final FragmentActivity activity, final String groupKey) {

        Log.i(TAG, "extendGroupInvitation with groupKey=" + groupKey);
        Group grp = GroupManager.instance.getGroupProfile(groupKey);
        if (grp == null) {
            Log.e(TAG, "Received invitation with groupKey: " + groupKey + " but GroupManager " +
                    "can't find this group");
            return;
        }
        // Create an entry for the invite map for the specified group.
        GroupInviteData data = new GroupInviteData(grp.key, grp.name, grp.commonRoomKey);
        mInviteMap.put(data.groupKey, data);
        startInvitationIntent(activity, grp.name);
    }

    /** Return a set of groups and rooms for invitations */
    public List<ListItem> getListItemData() {
        List<ListItem> result = new ArrayList<>();
        result.addAll(getInviteItems());
        if (result.size() > 0) return result;

        // There is nothing available for invitations.  Provide a header message to that effect.
        result.add(new ListItem(new ResourceHeaderItem(R.string.NoSelectableItemsHeaderText)));
        return result;
    }

    /** Initialize the invitation manager */
    public void init(final AppCompatActivity context) {
        mMessageMap.clear();
        mMessageMap.put(R.string.HasJoinedMessage, context.getString(R.string.HasJoinedMessage));

        // Build GoogleApiClient with AppInvite API for receiving deep links
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(context, InvitationManager.instance)
                .addApi(AppInvite.API)
                .build();

        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        final boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, context, autoLaunchDeepLink)
                .setResultCallback(InvitationManager.instance);
    }

    /** Handle an account state change by updating the navigation drawer header. */
    @Subscribe
    public void onAuthenticationChange(final AuthenticationChangeEvent event) {
        Account account = event != null ? event.account : null;
        if (account == null) return;

        // Since Firebase invitations aren't working, add a watcher for app invites so we can
        // bypass the Firebase mechanism for now.
//        DatabaseReference invites = FirebaseDatabase.getInstance().getReference()
//                .child(APP_INVITE_PATH);
//        invites.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.i(TAG, "onChildAdded");
//                mInvitationIds.add(dataSnapshot.getKey());
//                handleOutstandingInvite(dataSnapshot.getKey());
//                // For now, don't delete the invite from the database. This is hack-in-progress!
////                dataSnapshot.getRef().removeValue();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                Log.i(TAG, "onChildChanged");
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.i(TAG, "onChildRemoved");
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                Log.i(TAG, "onChildMoved");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.i(TAG, "onCancelled: " + databaseError.getMessage());
//            }
//        });

        // Get any outstanding invitation(s) and set up to process
        for (String id : mInvitationIds) {
            handleOutstandingInvite(id);
        }
    }

    /**
     * Handle an error connecting the client to the  GoogleApiClient service (required for
     * AppInvite API
     */
    public void onConnectionFailed (@NonNull ConnectionResult result) {
        Log.i(TAG, "connection failed: " + result.toString());
    }

    /** Handle the group profile changes */
    @Subscribe public void onGroupProfileChange(@NonNull final ProfileGroupChangeEvent event) {
        // If the current event is for a group not represented on the invite list, just return.
        if (!mInviteMap.keySet().contains(event.key))
            return;

        Account currAccount = AccountManager.instance.getCurrentAccount();
        if (currAccount == null) return;
        Group changedGroup = event.group;
        GroupInviteData data = mInviteMap.get(event.key);

        // Add this account to the group profile member list; add a new member object to the group
        if (!changedGroup.memberList.contains(currAccount.id)) {
            changedGroup.memberList.add(currAccount.id);
            GroupManager.instance.updateGroupProfile(changedGroup);

            // Create and persist a member object to the database and join to the specified rooms
            Account member = new Account(currAccount);
            member.joinList.add(data.commonRoomKey);
            for (String roomKey : data.rooms) {
                member.joinList.add(roomKey);
            }
            member.groupKey = changedGroup.key;
            MemberManager.instance.createMember(member);
        }
        mInviteMap.get(event.key).addedToGroupMemberList = true;

        handleInvitationComplete(mInviteMap.get(event.key));
    }

    /** Handle app invitation result. Called only from MainActivity onResult method. */
    public void onInvitationResult(final int resultCode, final Intent intent) {
        if (resultCode != RESULT_OK)
            clearInvitationMap();
        else {
            // Get the invitation IDs of all sent messages
            String[] ids = AppInviteInvitation.getInvitationIds(resultCode, intent);
            for (String id : ids) {
                Log.d(TAG, "onInvitationResult: sent invitation " + id);
                saveInvitation(id);
            }
        }
    }

    /** Handle result of invitation intent (after receiving invitation) */
    public void onResult(@NonNull AppInviteInvitationResult result) {
        Log.i(TAG, "getInvitation intent=" + result.getInvitationIntent());
        if (!result.getStatus().isSuccess()) {
            Log.i(TAG, "getInvitation: no deep link found.");
            return;
        }

        // Extract deep link from Intent. If no deep link exists, just log and return
        Intent intent = result.getInvitationIntent();

        // Get our data out of firebase for this invitation and save for use after authentication
        String invitationId = AppInviteReferral.getInvitationId(intent);
        mInvitationIds.add(invitationId);
    }

    /** Handle the room profile change */
    @Subscribe public void onRoomProfileChange(@NonNull final ProfileRoomChangeEvent event) {
        // If this room is in the invite list, add the current account to the room member list.
        Account currAccount = AccountManager.instance.getCurrentAccount();
        if (currAccount == null) return;
        for (Map.Entry<String, GroupInviteData> entry : mInviteMap.entrySet()) {
            GroupInviteData data = entry.getValue();
            if (data.commonRoomKey.equals(event.key)) {
                if (!event.room.hasMember(currAccount.id)) {
                    // Add account as member, update the profile and the invitation map data
                    event.room.addMember(currAccount.id);
                    RoomManager.instance.updateRoomProfile(event.room);
                    data.addedToCommRoomMemberList = true;
                    // Post a message to the common room announcing the user has joined
                    String format = mMessageMap.get(R.string.HasJoinedMessage);
                    String text = String.format(Locale.getDefault(), format, currAccount.displayName);
                    MessageManager.instance.createMessage(text, STANDARD, currAccount, event.room);
                }
            } else if (data.rooms.contains(event.key)) {
                if (!event.room.getMemberIdList().contains(currAccount.id)) {
                    // Add account as member, update the profile and the invitation map data
                    event.room.addMember(currAccount.id);
                    RoomManager.instance.updateRoomProfile(event.room);

                    // Update the member within the group to include this room
                    Account member = MemberManager.instance.getMember(data.groupKey);
                    if (member != null) {
                        member.joinList.add(event.key);
                        String path = String.format(Locale.US, MemberManager.MEMBERS_PATH, data.groupKey, member.id);
                        DBUtils.instance.updateChildren(path, member.toMap());
                    }

                    // Post a message to the room announcing the user has joined
                    String format = mMessageMap.get(R.string.HasJoinedMessage);
                    String text = String.format(Locale.getDefault(), format, currAccount.displayName);
                    MessageManager.instance.createMessage(text, STANDARD, currAccount, event.room);
                    // Increment the room complete count
                    data.roomsCompleted++;
                }
            }
            handleInvitationComplete(data);
        }
    }

    /** Persist invitation information in Firebase */
    public void saveInvitation(String id) {
        Map<String, Object> objMap = new HashMap<>();
        for (Map.Entry<String, GroupInviteData> entry : mInviteMap.entrySet()) {
            objMap.put(entry.getKey(), entry.getValue());
        }
        // For the time being, put app invites in their own location in firebase to differentiate
        // them from invites for protected users.
        String invitePath = String.format(APP_INVITE_ID_PATH, id);
        DBUtils.instance.updateChildren(invitePath, objMap);
        clearInvitationMap();
    }


    // Private instance methods.

    /** Build the dynamic link to be used for Firebase invitations */
    private String buildDynamicLink() {
        String firebaseUrl = FirebaseDatabase.getInstance().getReference().toString();
        String dynamicLink =  new Uri.Builder()
                .scheme("https")
                .authority(APP_CODE + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", firebaseUrl)
                .appendQueryParameter("apn", APP_PACKAGE_NAME)
                .appendQueryParameter("afl", PLAY_STORE_LINK)
                .appendQueryParameter("ifl", WEB_LINK).toString();
        Log.i(TAG, "dynamicLink=" + dynamicLink);
        return dynamicLink;
    }

    /**
     * Return a list of items which represent the available groups and their rooms.
     */
    private List<ListItem> getInviteItems() {
        // Determine if there are groups to look at.  If not, return an empty result.
        List<ListItem> result = new ArrayList<>();
        if (GroupManager.instance.groupMap.size() == 0) {
            result.add(new ListItem(new ResourceHeaderItem(R.string.NoSelectableItemsHeaderText)));
            return result;
        }

        for (Map.Entry<String, Group> entry : GroupManager.instance.groupMap.entrySet()) {
            result.add(new ListItem(new SelectableGroupItem(entry.getKey())));
            List<Room> rooms = RoomManager.instance.getRooms(entry.getKey(), true);
            for (Room aRoom : rooms) {
                if (aRoom.type == COMMON)
                    result.add(new ListItem(inviteCommonRoom, aRoom.groupKey, aRoom.key));
                else
                    result.add(new ListItem(new InviteRoomItem(aRoom.groupKey, aRoom.key)));
            }
        }
        return result;
    }

    /** Check for group invitation completed. If so, post event and clean up. */
    private void handleInvitationComplete(GroupInviteData groupData) {
        if (groupData.isDone()) {
            // If the invitation added additional room(s) to an existing group, make the message
            // informative.
            List<String> roomList = new ArrayList<>();
            if (groupData.roomsOnly) {
                for (String roomKey : groupData.rooms)
                    roomList.add(RoomManager.instance.getRoomProfile(roomKey).name);
            }
            AppEventManager.instance.post(new GroupJoinedEvent(groupData.groupName, roomList));
            // We're finally done processing this group invitation so remove it from the list
            mInviteMap.remove(groupData.groupKey);
        }
    }

    /** Set up value listener on invitation id and handler to start processing invitation. */
    private void handleOutstandingInvite(String inviteId) {
        DatabaseReference invite = FirebaseDatabase.getInstance().getReference()
                .child(String.format(APP_INVITE_ID_PATH, inviteId));
        invite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If the snapshot has nothing to offer, move on...
                if (!dataSnapshot.hasChildren())
                    return;

                // The key in the snapshot is the invitation id. The value is a map of groupKey
                // to group item data objects. Save the found data in the invite map.
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String key = child.getKey();
                    GroupInviteData value = child.getValue(GroupInviteData.class);
                    if (value.rooms == null) // avoid null pointer exceptions
                        value.rooms = new ArrayList<>();
                    mInviteMap.put(key, value);
                }

                startInvitationProcessing();

                // Finally, remove invitation from Firebase
                dataSnapshot.getRef().removeValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called with error: " + databaseError.getMessage());
            }
        });
    }

    /** Return TRUE iff the given group has an invitation registered for the given account owner. */
    private boolean hasGroupInvite(@NonNull final Account account, @NonNull final String groupKey) {
        // Ensure that there is a list of invites for the given group.
        List<String> invitedMembers = mGroupInviteMap.get(groupKey);
        if (invitedMembers == null) return false;

        // Ensure that the invited members list includes the account owner.
        if (!invitedMembers.contains(account.id)) return false;

        // Remove the invited account holder from the list and possibly the list from the map.
        invitedMembers.remove(account.id);
        if (invitedMembers.size() == 0) mGroupInviteMap.remove(groupKey);
        return true;
    }

    /**
     * Build the dynamic link to be used for Firebase invitations. Optionally specify a group
     * name if there is only one group in the invitation.
     */
    private void startInvitationIntent(final FragmentActivity activity, final String groupName) {
        String msgText;
        if (groupName != null) {
            msgText = String.format(activity.getString(R.string.InviteToGroupFormat), groupName);
        } else {
            msgText = activity.getString(R.string.InviteMessage);
        }
        Intent intent = new AppInviteInvitation.IntentBuilder(activity.getString(R.string.InviteTitle))
                .setMessage(msgText)
                .setDeepLink(Uri.parse(buildDynamicLink()))
                .build();
        activity.startActivityForResult(intent, MainActivity.RC_INVITE);
    }

    /** Start to process an invitation: a series of steps to update various Firebase objects */
    private void startInvitationProcessing() {
        Account account = AccountManager.instance.getCurrentAccount();
        boolean accountChanged = false;
        for (String key : mInviteMap.keySet()) {
            // If the account has already joined, don't add it again and don't wait to re-join
            // common room or group, since that won't happen.
            if (account.joinList.contains(key)) {
                mInviteMap.get(key).roomsOnly = true;
                mInviteMap.get(key).addedToAccountJoinList = true;
                mInviteMap.get(key).addedToCommRoomMemberList = true;
                mInviteMap.get(key).addedToGroupMemberList = true;
            } else {
                account.joinList.add(key);
                accountChanged = true;
                mInviteMap.get(key).addedToAccountJoinList = true;
            }
            // Set a watcher for the common room unless this account is already a member of the
            // group. The group watcher is set up by adding the group to the account joinList.
            // Watch all other rooms in the invite.
            if (!mInviteMap.get(key).addedToCommRoomMemberList)
                RoomManager.instance.setWatcher(key, mInviteMap.get(key).commonRoomKey);
            if (mInviteMap.get(key).rooms != null) {
                for (String roomKey : mInviteMap.get(key).rooms)
                    RoomManager.instance.setWatcher(key, roomKey);
            }
        }
        if (accountChanged) {
            AccountManager.instance.updateAccount(account);
        }
    }
}
