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
import android.util.Log;

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
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.GroupJoinedEvent;
import com.pajato.android.gamechat.main.MainActivity;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Handle invitations to groups.
 *
 * @author Paul Michael Reilly
 */
public enum InvitationManager implements ResultCallback<AppInviteInvitationResult> {
    instance;

    // Private constants.

    /** The logcat TAG. */
    private static final String TAG = InvitationManager.class.getSimpleName();

    /** Keep track of any outstanding invites to groups */
    private List<String> groupKeysToJoin = new ArrayList<>();

    // Public instance variables.

    /** The map managing group invitations. */
    private Map<String, List<String>> mGroupInviteMap = new HashMap<>();

    // Public instance methods.

    /** Get groupsToJoin after login. Smells like this belongs somewhere else... */
    public List<String> getGroupsToJoin() {
        return groupKeysToJoin;
    }

    /** Remove all entries from the 'groupsToJoin' list */
    public void clearGroupsToJoin() {
        groupKeysToJoin = new ArrayList<>();
    }

    /** Handle an account state change by updating the navigation drawer header. */
    @Subscribe
    public void onAuthenticationChange(final AuthenticationChangeEvent event) {
        Account account = event != null ? event.account : null;
        if(account != null) {
            // Update Firebase
            for(String key : groupKeysToJoin) {
                account.joinList.add(key);
            }
            AccountManager.instance.updateAccount(account);

            // Send an event with the name of all the groups we've joined
            List<String> joinedGroups = new ArrayList<>();
            for(String grpKey : groupKeysToJoin) {
                Group joinGrp = GroupManager.instance.getGroupProfile(grpKey);
                if(joinGrp != null) {
                    joinedGroups.add(joinGrp.name);
                }
            }
            if (joinedGroups.size() > 0) {
                AppEventManager.instance.post(new GroupJoinedEvent(joinedGroups));
            }

            // Finally, clear the join list
            clearGroupsToJoin();
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
    public void extendAppInvitation(FragmentActivity fragmentActivity, String groupKey) {
        Log.i(TAG, "extendAppInvitation with groupKey=" + groupKey);
        String firebaseUrl = FirebaseDatabase.getInstance().getReference().toString();
        firebaseUrl += "/groups/";
        if(groupKey == null || groupKey.equals("")) {
            firebaseUrl += AccountManager.instance.getMeGroup();
            Log.i(TAG, "extendAppInvitation: " + firebaseUrl);
        } else {
            firebaseUrl += groupKey;
        }

        String APP_CODE = "aq5ca";
        String PLAY_STORE_LINK = "https://play.google.com/apps/testing/com.pajato.android.gamechat";
        String APP_PACKAGE_NAME = "com.pajato.android.gamechat";
        String WEB_LINK = "https://github.com/pajato/GameChat";

        String dynamicLink = new Uri.Builder()
                .scheme("https")
                .authority(APP_CODE + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", firebaseUrl)
                .appendQueryParameter("apn", APP_PACKAGE_NAME)
                .appendQueryParameter("afl", PLAY_STORE_LINK)
                .appendQueryParameter("ifl", WEB_LINK).toString();

        Log.i(TAG, "dynamicLink=" + dynamicLink);
        Intent intent = new AppInviteInvitation.IntentBuilder(fragmentActivity.getString(R.string.InviteTitle))
                .setMessage(fragmentActivity.getString(R.string.InviteMessage))
                .setDeepLink(Uri.parse(dynamicLink))
                .build();
        fragmentActivity.startActivityForResult(intent, MainActivity.RC_INVITE);
    }

    public void onResult(@NonNull AppInviteInvitationResult result) {
        Log.i(TAG, "getInvitation intent=" + result.getInvitationIntent());
        Intent i = result.getInvitationIntent();
        if (i != null) {
            Log.i(TAG, "extras: " + i.getExtras().toString());
        }
        if (result.getStatus().isSuccess()) {
            // Extract deep link from Intent
            Intent intent = result.getInvitationIntent();
            String deepLink = AppInviteReferral.getDeepLink(intent);
            Log.i(TAG, "getInvitation with deepLink: " + deepLink);
            String invitationId = AppInviteReferral.getInvitationId(intent);
            Log.i(TAG, "getInvitation: invitationId=" + invitationId);
            boolean hasRef = AppInviteReferral.hasReferral(intent);
            Log.i(TAG, "getInvitation: invitation has a referral=" + hasRef);
            boolean isFromPlayStore = AppInviteReferral.isOpenedFromPlayStore(intent);
            Log.i(TAG, "getInvitation: launched after install from play store=" + isFromPlayStore);
            // If we have a deep link, try to find the groupKey.
            if (deepLink != null && !deepLink.equals("")) {
                Uri dlUri = Uri.parse(deepLink);
                String firebaseLink = dlUri.getQueryParameter("link");
                if (firebaseLink != null && !firebaseLink.equals("")) {
                    Uri fbUri = Uri.parse(firebaseLink);
                    List<String> parts = fbUri.getPathSegments();
                    // Get the last value which should be the group key
                    String groupKey = parts.get(parts.size() - 1);
                    Log.i(TAG, "getInvitation: groupKey=" + groupKey);
//                                            Account currAccount = AccountManager.instance.getCurrentAccount();
                    groupKeysToJoin.add(groupKey);
//                                            DBUtils.instance.updateChildren(
//                                                    AccountManager.instance.getAccountPath(currAccount.id),
//                                                    currAccount.toMap());
                } else {
                    Log.i(TAG, "getInvitation: can't get group key - firebaseLink is not set");
                }
            } else {
                Log.i(TAG, "getInvitation: can't get group key - deepLink is not set");
            }

        } else {
            Log.i(TAG, "getInvitation: no deep link found.");
        }
    }

}
