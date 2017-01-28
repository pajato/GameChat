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
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.adapter.CommonRoomItem;
import com.pajato.android.gamechat.common.adapter.InviteRoomItem;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.ResourceHeaderItem;
import com.pajato.android.gamechat.common.adapter.SelectableGroupItem;
import com.pajato.android.gamechat.common.model.Account;
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
import static com.pajato.android.gamechat.chat.model.Room.RoomType.COMMON;

/**
 * Handle invitations to groups.
 *
 * @author Paul Michael Reilly
 */
public enum InvitationManager implements ResultCallback<AppInviteInvitationResult> {
    instance;

    /** Simple POJO to hold the group name and common room key while the join is processed */
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
    private SparseArray<String> mMessageMap = new SparseArray<>();

    /** Keep track of any outstanding invites to groups */
    private Map<String, GroupInviteData> mInvitedGroups = new HashMap<>();

    // Public instance variables.

    /** The map managing group invitations. */
    private Map<String, List<String>> mGroupInviteMap = new HashMap<>();

    // Public instance methods.

    /** Initialize the invitation manager */
    public void init(final AppCompatActivity context) {
        mMessageMap.clear();
        mMessageMap.put(R.string.HasJoinedMessage, context.getString(R.string.HasJoinedMessage));
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
        // If this room is in the invite list, add the current account to the room member list.
        Account currAccount = AccountManager.instance.getCurrentAccount();
        if (currAccount == null) return;
        for (Map.Entry<String, GroupInviteData> entry : mInvitedGroups.entrySet()) {
            GroupInviteData data = entry.getValue();
            if (data.commonRoomKey.equals(event.key)) {
                event.room.addMember(currAccount.id);
                RoomManager.instance.updateRoomProfile(event.room);
                data.addedToCommRoomMemberList = true;
                mInvitedGroups.put(entry.getKey(), data);

                // Post a message to the common room announcing the user has joined
                String format = mMessageMap.get(R.string.HasJoinedMessage);
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
        if (currAccount == null) return;
        Group changedGroup = event.group;
        GroupInviteData data = mInvitedGroups.get(event.key);

        // Add this account to the group's member list and update the database
        changedGroup.memberList.add(currAccount.id);
        GroupManager.instance.updateGroupProfile(changedGroup);

        // Create and persist a member object to the database joined to the common (default) room
        Account member = new Account(currAccount);
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

    /** Extend an invitation to join GameChat using AppInviteInvitation on the given group. */
    public void extendInvitation(final FragmentActivity activity, final String groupKey) {

        Log.i(TAG, "extendInvitation with groupKey=" + groupKey);

        Uri dynLinkUri = buildDefaultDynamicLink();

        String encodedInfo = encodeGroupInfo(groupKey, null);
        dynLinkUri.buildUpon().appendQueryParameter("group", encodedInfo);
        String dynamicLink = dynLinkUri.toString();
        Log.i(TAG, "dynamicLink=" + dynamicLink);

        String title = activity.getString(R.string.InviteTitle);
        Intent intent = new AppInviteInvitation.IntentBuilder(title)
                .setMessage(activity.getString(R.string.InviteMessage))
                .setDeepLink(Uri.parse(dynamicLink))
                .build();
        activity.startActivityForResult(intent, MainActivity.RC_INVITE);
    }

    /** Extend an invitation to join GameChat using AppInviteInvitation and specify a map of
     *  groups and their rooms to join (always has at least the Common room). */
    public void extendInvitation(final FragmentActivity activity,
                                 final Map<String, List<String>> keys) {
        Log.i(TAG, "extendInvitation with list of keys");

        Uri dynLinkUri = buildDefaultDynamicLink();

        // add a "group" item to the dynamic link for each group specified
        for(String groupKey : keys.keySet()) {
            String encodedInfo = encodeGroupInfo(groupKey, keys.get(groupKey));
            if (!encodedInfo.equals(""))
                dynLinkUri.buildUpon().appendQueryParameter("group", encodedInfo);
        }
        String dynamicLink = dynLinkUri.toString();
        Log.i(TAG, "dynamicLink=" + dynamicLink);

        Intent intent = new AppInviteInvitation.IntentBuilder(activity.getString(R.string.InviteTitle))
                .setMessage(activity.getString(R.string.InviteMessage))
                .setDeepLink(Uri.parse(dynamicLink))
                .build();
        activity.startActivityForResult(intent, MainActivity.RC_INVITE);
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

    private Uri buildDefaultDynamicLink() {
        String firebaseUrl = FirebaseDatabase.getInstance().getReference().toString();
        return new Uri.Builder()
                .scheme("https")
                .authority(APP_CODE + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", firebaseUrl)
                .appendQueryParameter("apn", APP_PACKAGE_NAME)
                .appendQueryParameter("afl", PLAY_STORE_LINK)
                .appendQueryParameter("ifl", WEB_LINK).build();
    }

    private String encodeGroupInfo(final String groupKey, List<String> roomKeys) {
        Group group = GroupManager.instance.getGroupProfile(groupKey);
        if (group == null) {
            Log.e(TAG, "Received invitation with groupKey: " + groupKey + " but GroupManager " +
                    "can't find this group");
            return "";
        }

        // If no roomkeys were specified, get the common room key - we must always have it
        if (roomKeys == null) {
            roomKeys = new ArrayList<>();
            roomKeys.add(group.commonRoomKey);
        }

        // make a list of items we need to encode into our URI for this group
        List<String> items = new ArrayList<>();
        items.add(groupKey);
        items.add(group.name);
        // add a comma-separated list of room keys for the specified group to the list
        items.add(TextUtils.join(",", roomKeys));

        // Create a string like this: groupKey/groupName/commonRoomKey[,roomkey...]
        return TextUtils.join("/", items);
    }

    public void onResult(@NonNull AppInviteInvitationResult result) {
        Log.i(TAG, "getInvitation intent=" + result.getInvitationIntent());
        if (!result.getStatus().isSuccess()) {
            Log.i(TAG, "getInvitation: no deep link found.");
            return;
        }

        // Extract deep link from Intent. If no deep link exists, just log and return
        Intent intent = result.getInvitationIntent();
        String deepLink = AppInviteReferral.getDeepLink(intent);
        Log.i(TAG, "getInvitation with deepLink: " + deepLink);
        if (deepLink == null || deepLink.equals("")) {
            Log.e(TAG, "getInvitation: can't get group key - deepLink is not set");
            return;
        }

        // try to find the groupKey, commonRoomKey and groupName values
        Uri dlUri = Uri.parse(deepLink);
        String firebaseLink = dlUri.getQueryParameter("link");
        String commonRoomKey = dlUri.getQueryParameter("commonRoomKey");
        String groupName = dlUri.getQueryParameter("groupName");

        // if the link isn't set, just log an error
        if (firebaseLink == null || firebaseLink.equals("")) {
            Log.e(TAG, "getInvitation: can't get group key - firebaseLink is not set");
            return;
        }

        // if there is no group set, log an error
        Uri fbUri = Uri.parse(firebaseLink);
        List<String> parts = fbUri.getPathSegments();
        if (!parts.contains("groups")) {
            Log.i(TAG, "getInvitation: no groupKey specified");
            return;
        }

        // Get the group key
        String groupKey = parts.get(parts.lastIndexOf("groups") + 1);
        Log.i(TAG, "getInvitation: groupKey=" + groupKey);
        mInvitedGroups.put(groupKey, new GroupInviteData(groupName, commonRoomKey));
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
                    result.add(new ListItem(new CommonRoomItem(aRoom.groupKey, aRoom.key)));
                else
                    result.add(new ListItem(new InviteRoomItem(aRoom.groupKey, aRoom.key)));
            }
        }

        return result;
    }
}
