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

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.chat.model.Group;
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

import static com.pajato.android.gamechat.chat.model.Message.STANDARD;

/**
 * Handle invitations to groups.
 *
 * @author Paul Michael Reilly
 */
public enum InvitationManager implements ResultCallback<AppInviteInvitationResult> {
    instance;

    /** Simple POJO to hold the group name and common room key while we process the join */
    class GroupInviteData {
        String groupName;
        String commonRoomKey;
        boolean addedToAccountJoinList;
        boolean addedToGroupMemberList;
        boolean addedToCommRoomMemberList;

        GroupInviteData(String groupName, String commonRoomKey) {
            this.groupName = groupName;
            this.commonRoomKey = commonRoomKey;
            addedToAccountJoinList = false;
            addedToGroupMemberList = false;
            addedToCommRoomMemberList = false;
        }

        boolean isDone() {
            return addedToAccountJoinList && addedToGroupMemberList && addedToCommRoomMemberList;
        }
    }

    // Private constants.

    private static final String APP_CODE = "aq5ca";
    private static final String PLAY_STORE_LINK = "https://play.google.com/apps/testing/com.pajato.android.gamechat";
    private static final String APP_PACKAGE_NAME = "com.pajato.android.gamechat";
    private static final String WEB_LINK = "https://github.com/pajato/GameChat";

    /** The logcat TAG. */
    private static final String TAG = InvitationManager.class.getSimpleName();

    // Private instance variables

    /** The repository for any messages needed. */
    private SparseArray<String> messageMap = new SparseArray<>();

    /** Keep track of any outstanding invites to groups */
    private Map<String, GroupInviteData> mInvitedGroups = new HashMap<>();

    // Public instance variables.

    /** The map managing group invitations. */
    private Map<String, List<String>> mGroupInviteMap = new HashMap<>();

    // Public instance methods.

    /** Initialize the invitation manager */
    public void init(final AppCompatActivity context) {
        messageMap.clear();
        messageMap.put(R.string.HasJoinedMessage, context.getString(R.string.HasJoinedMessage));
    }
        /** Handle an account state change by updating the navigation drawer header. */
    @Subscribe
    public void onAuthenticationChange(final AuthenticationChangeEvent event) {
        Account account = event != null ? event.account : null;
        if (account == null) return;

        // Update Firebase
        boolean accountChanged = false;
        for (String key : mInvitedGroups.keySet()) {
            // If the account has already joined, don't add it again!
            if (account.joinList.contains(key)) {
                mInvitedGroups.remove(key);
            } else {
                account.joinList.add(key);
                accountChanged = true;
                mInvitedGroups.get(key).addedToAccountJoinList = true;
                // Set up the watcher for the common room (the group watcher is set up by
                // adding the group to the account joinList).
                RoomManager.instance.setWatcher(key, mInvitedGroups.get(key).commonRoomKey);
            }
        }
        if (accountChanged) {
            AccountManager.instance.updateAccount(account);
        }
    }

    /** Handle the room profile change */
    @Subscribe public void onRoomProfileChange(@NonNull final ProfileRoomChangeEvent event) {
        // If this room is one we care about, add the current account to the room memberIdList.
        Account currAccount = AccountManager.instance.getCurrentAccount();
        for (Map.Entry<String, GroupInviteData> entry : mInvitedGroups.entrySet()) {
            GroupInviteData data = entry.getValue();
            if (data.commonRoomKey.equals(event.key)) {
                event.room.memberIdList.add(currAccount.id);
                RoomManager.instance.updateRoomProfile(event.room);
                data.addedToCommRoomMemberList = true;
                mInvitedGroups.put(entry.getKey(), data);

                // Post a message to the common room announcing the user has joined
                String format = messageMap.get(R.string.HasJoinedMessage);
                String text = String.format(Locale.getDefault(), format, currAccount.displayName);
                MessageManager.instance.createMessage(text, STANDARD, currAccount, event.room);

                if (data.isDone()) {
                    AppEventManager.instance.post(new GroupJoinedEvent(entry.getValue().groupName));
                    // We're finally done processing this group invitation so remove it from the list
                    mInvitedGroups.remove(entry.getKey());
                }
            }
        }
    }

    /** Handle the group profile changes */
    @Subscribe public void onGroupProfileChange(@NonNull final ProfileGroupChangeEvent event) {
        // Check for joined groups to see if there are any to report and if the current event
        // is for an event on our list. If the list is empty, just return.
        if (!mInvitedGroups.keySet().contains(event.key)) return;

        Account currAccount = AccountManager.instance.getCurrentAccount();
        Group changedGroup = event.group;
        GroupInviteData data = mInvitedGroups.get(event.key);

        // Add this account to the group's member list and update the database
        changedGroup.memberList.add(currAccount.id);
        GroupManager.instance.updateGroupProfile(changedGroup);

        // Create and persist a member object to the database joined to the common (default) room
        Account member = new Account(AccountManager.instance.getCurrentAccount());
        member.joinList.add(data.commonRoomKey);
        member.groupKey = changedGroup.key;
        MemberManager.instance.createMember(member);

        mInvitedGroups.get(event.key).addedToGroupMemberList = true;

        if (mInvitedGroups.get(event.key).isDone()) {
            AppEventManager.instance.post(new GroupJoinedEvent(mInvitedGroups.get(event.key).groupName));
            // We're finally done processing this group invitation so remove it from the list
            mInvitedGroups.remove(event.key);
        }
    }

    /** Accept a group invite for a given account by updating both the group and the account. */
    public void acceptGroupInvite(@NonNull final Account account, @NonNull final String groupKey) {
        // Determine if the current account is already a member of the given group, has not been
        // invited or if there is no group to accept the invitation to. Abort if any are true.
        boolean isMember = account.joinList.contains(groupKey);
        boolean isInvited = hasGroupInvite(account, groupKey);
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        if (isMember || !isInvited || group == null) return;

        // The account holder has been invited to join the given group.  Do so by adding the group
        // key the account join list and create a copy of the account as a member of the group.
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

    /** Extend a group invite to a given account by registering both. */
    public void extendGroupInvite(@NonNull final Account account, @NonNull final String groupKey) {
        // Insert the account id into the list associated with the group key.
        List<String> memberList = mGroupInviteMap.get(groupKey);
        if (memberList == null) memberList = new ArrayList<>();
        memberList.add(account.id);
        mGroupInviteMap.put(groupKey, memberList);
    }

    // Private instance methods.

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

    /** Extend an invitation to join GameChat using AppInviteInvitation Intent */
    public void extendAppInvitation(final FragmentActivity fragmentActivity) {
        String firebaseUrl = FirebaseDatabase.getInstance().getReference().toString();
        String dynamicLink = new Uri.Builder()
                .scheme("https")
                .authority(APP_CODE + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", firebaseUrl)
                .appendQueryParameter("apn", APP_PACKAGE_NAME)
                .appendQueryParameter("afl", PLAY_STORE_LINK)
                .appendQueryParameter("ifl", WEB_LINK)
                .toString();

        Log.i(TAG, "dynamicLink=" + dynamicLink);
        Intent intent = new AppInviteInvitation.IntentBuilder(fragmentActivity.getString(R.string.InviteTitle))
                .setMessage(fragmentActivity.getString(R.string.InviteMessage))
                .setDeepLink(Uri.parse(dynamicLink))
                .build();
        fragmentActivity.startActivityForResult(intent, MainActivity.RC_INVITE);
    }

    /** Extend an invitation to join GameChat using AppInviteInvitation and specify a group to join. */
    public void extendAppInvitation(final FragmentActivity fragmentActivity, final String groupKey) {
        String firebaseUrl = FirebaseDatabase.getInstance().getReference().toString();

        Log.i(TAG, "extendAppInvitation with groupKey=" + groupKey);
        Group grp = GroupManager.instance.getGroupProfile(groupKey);
        if (grp == null) {
            Log.e(TAG, "Received invitation with groupKey: " + groupKey + " but GroupManager " +
                    "can't find this group");
            return;
        }
        firebaseUrl += "/groups/";
        firebaseUrl += groupKey;

        String dynamicLink = new Uri.Builder()
                .scheme("https")
                .authority(APP_CODE + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", firebaseUrl)
                .appendQueryParameter("apn", APP_PACKAGE_NAME)
                .appendQueryParameter("afl", PLAY_STORE_LINK)
                .appendQueryParameter("ifl", WEB_LINK)
                .appendQueryParameter("commonRoomKey", grp.commonRoomKey)
                .appendQueryParameter("groupName", grp.name)
                .toString();

        Log.i(TAG, "dynamicLink=" + dynamicLink);
        Intent intent = new AppInviteInvitation.IntentBuilder(fragmentActivity.getString(R.string.InviteTitle))
                .setMessage(fragmentActivity.getString(R.string.InviteMessage))
                .setDeepLink(Uri.parse(dynamicLink))
                .build();
        fragmentActivity.startActivityForResult(intent, MainActivity.RC_INVITE);
    }

    public void onResult(@NonNull AppInviteInvitationResult result) {
        Log.i(TAG, "getInvitation intent=" + result.getInvitationIntent());
        if (result.getStatus().isSuccess()) {
            // Extract deep link from Intent
            Intent intent = result.getInvitationIntent();
            String deepLink = AppInviteReferral.getDeepLink(intent);
            Log.i(TAG, "getInvitation with deepLink: " + deepLink);
            // If we have a deep link, try to find the groupKey. Also, extract the commonRoomKey
            // and groupName values.
            if (deepLink != null && !deepLink.equals("")) {
                Uri dlUri = Uri.parse(deepLink);
                String firebaseLink = dlUri.getQueryParameter("link");
                String commonRoomKey = dlUri.getQueryParameter("commonRoomKey");
                String groupName = dlUri.getQueryParameter("groupName");

                if (firebaseLink != null && !firebaseLink.equals("")) {
                    Uri fbUri = Uri.parse(firebaseLink);
                    // Get the group key if one was specified
                    List<String> parts = fbUri.getPathSegments();
                    if (parts.contains("groups")) {
                        String groupKey = parts.get(parts.lastIndexOf("groups") + 1); // the group key is after 'groups'
                        Log.i(TAG, "getInvitation: groupKey=" + groupKey);
                        mInvitedGroups.put(groupKey, new GroupInviteData(groupName, commonRoomKey));
                    } else {
                        Log.i(TAG, "getInvitation: no groupKey specified");
                    }
                } else {
                    Log.e(TAG, "getInvitation: can't get group key - firebaseLink is not set");
                }
            } else {
                Log.e(TAG, "getInvitation: can't get group key - deepLink is not set");
            }

        } else {
            Log.i(TAG, "getInvitation: no deep link found.");
        }
    }

}
