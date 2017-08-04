/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.common.model.JoinState;
import com.pajato.android.gamechat.database.handler.AccountChangeHandler;
import com.pajato.android.gamechat.event.AccountChangeEvent;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthStateChangedEvent;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.AuthenticationChangeHandled;
import com.pajato.android.gamechat.event.ProfileGroupChangeEvent;
import com.pajato.android.gamechat.event.ProfileGroupDeleteEvent;
import com.pajato.android.gamechat.event.ProfileRoomChangeEvent;
import com.pajato.android.gamechat.event.ProfileRoomDeleteEvent;
import com.pajato.android.gamechat.event.ProtectedUserAuthFailureEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.model.Message.STANDARD;
import static com.pajato.android.gamechat.chat.model.Message.SYSTEM;
import static com.pajato.android.gamechat.chat.model.Room.RoomType.COMMON;
import static com.pajato.android.gamechat.chat.model.Room.RoomType.ME;
import static com.pajato.android.gamechat.chat.model.Room.RoomType.PRIVATE;

/**
 * Manages the account database related aspects of the GameChat application.  These include
 * creating and persisting all accounts, standard and protected.
 *
 * @author Paul Michael Reilly on 5/26/16
 * @author Sandy Scott on 1/20/17
 */
public enum AccountManager {
    instance;

    /**
     * The account types.  Internally a User can be one of standard or restricted.  Externally,
     * a User is either standard or protected.  The Java language reserves the protected keyword
     * thus making this distinction necessary.
     *
     * standard: An unrestricted User.  Can create restricted users and has full access to all app
     * features.
     *
     * restricted: A restricted User cannot create another restricted User and has limited access
     * to app features. For example, a restricted User cannot create a group or a room. The notion
     * of a restricted/protected User is for use with young children.
     * Every restricted/protected User has a "chaperone", a standard User.  The restricted User can
     * only be a member of a group that the chaperone is a member of.
     */
    public enum AccountType {
        // admin, placeholder in the event that we actually need an admin account type.
        standard,
        restricted
    }

    // Public class constants

    /** A key used to access account available data. */
    public static final String ACCOUNT_AVAILABLE_KEY = "accountAvailable";

    /** The account change handler base name */
    public static final String ACCOUNT_CHANGE_HANDLER = "accountChangeHandler";

    /** The database path to an account profile. */
    public static final String ACCOUNT_PATH = "/accounts/%s/";

    /** The sentinel value to use for indicating an offline cached database object owner. */
    public static final String SIGNED_OUT_OWNER_ID = "signedOutOwnerId";

    /** The sentinel value to use for indicating a signed out experience key. */
    public static final String SIGNED_OUT_EXPERIENCE_KEY = "signedOutExperienceKey";

    /** The database path to protected user data */
    public static final String PROTECTED_PATH = "/protectedUsers/%s";

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = AccountManager.class.getSimpleName();

    // Public instance variables.

    /** The standard User (id) associated with this restricted User, if the User is restricted. */
    public String currentChaperone;

    /** This may not get applied to the Firebase account prior to creating the "me" room */
    public String protectedUserName;

    /** A list of group push keys to be used by a newly created protected user account */
    public List<String> protectedUserGroupKeys = new ArrayList<>();

    // Private instance variables

    /** The current account, null if there is no current account. */
    private Account mCurrentAccount;

    /** The current account key, null if there is no current account. */
    private String mCurrentAccountKey;

    /** The me group profile. */
    private Group mGroup;

    /** The repository for any messages needed. */
    private SparseArray<String> mMessageMap = new SparseArray<>();

    // Public instance methods

    /** Create a new protected user account and automatically join it to the specified groups */
    public void createProtectedAccount(@NonNull final String email, @NonNull final String name,
                                       @NonNull final String password, final boolean accountIsKnown,
                                       @NonNull List<String> groupKeys) {
        // Remember the desired group keys and the chaperone account key
        protectedUserGroupKeys.addAll(groupKeys);
        currentChaperone = AccountManager.instance.getCurrentAccountId();
        protectedUserName = name;
        FirebaseAuth.getInstance().signOut(); // Sign out from chaperone account

        // If the account is already known to exist in the database (based on previous check) then
        // just sign in. Otherwise, create a new Firebase user.
        if (accountIsKnown)
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new AuthSuccessListener())
                    .addOnFailureListener(new AuthFailureListener());
        else
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new CreateSuccessListener(name))
                    .addOnFailureListener(new AuthFailureListener());
    }

    /** Create and persist an account to the database. */
    public void createAccount(@NonNull Account account) {
        // Set up the push keys for the default "me" group and room.
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String groupKey = database.child(GroupManager.GROUPS_PATH).push().getKey();
        String path = String.format(Locale.US, RoomManager.ROOMS_PATH, groupKey);
        String roomKey = database.child(path).push().getKey();

        // Check for a chaperone account. If one exists, update this account.
        if (currentChaperone != null && !currentChaperone.equals(account.key)) {
            account.chaperone = currentChaperone;
            if (account.name == null || account.name.equals("")) {
                account.name = protectedUserName;
                protectedUserName = null;
            }
            account.type = AccountType.restricted.name();
            for (String key : protectedUserGroupKeys)
                account.joinMap.put(key, new JoinState());
        } else
            // This User has a standard account.
            account.type = AccountType.standard.name();

        // Set up and persist the account for the given user.
        long tStamp = account.createTime;
        account.groupKey = groupKey;
        path = String.format(Locale.US, ACCOUNT_PATH, account.key);
        DBUtils.updateChildren(path, account.toMap());

        // Leave the breadcrumbs for the chaperone account in the database. This must be done after
        // the new account has been added, because the database authorization rules don't allow
        // non-authorized users to have access.
        if (currentChaperone != null && !currentChaperone.equals(account.key)) {
            Map<String, Object> protectedUsers = new HashMap<>();
            protectedUsers.put(currentChaperone, account.key);
            String protectedUserPath = String.format(PROTECTED_PATH, currentChaperone);
            DBUtils.updateChildren(protectedUserPath, protectedUsers);
            currentChaperone = null;
        }

        // Update and persist the group profile.
        List<String> rooms = new ArrayList<>();
        rooms.add(roomKey);
        List<String> members = new ArrayList<>();
        members.add(account.key);
        Group group = new Group(groupKey, account.key, null, tStamp, members, rooms);
        path = String.format(Locale.US, GroupManager.GROUP_PROFILE_PATH, groupKey);
        DBUtils.updateChildren(path, group.toMap());

        // For pre-populated joined groups, add watchers (this can be the case for a protected user)
        for (String joinGroupKey : account.joinMap.keySet())
            GroupManager.instance.setWatcher(joinGroupKey);

        // Update the member entry in the default group.
        Account member = new Account(account);
        member.joinMap.put(roomKey, new JoinState());
        member.groupKey = groupKey;
        path = String.format(Locale.US, MemberManager.MEMBERS_PATH, groupKey, account.key);
        DBUtils.updateChildren(path, member.toMap());

        // Update the "me" room profile on the database.
        String name = account.getDisplayName();
        Room room = new Room(roomKey, account.key, name, tStamp, groupKey, ME);
        path = String.format(Locale.US, RoomManager.ROOM_PROFILE_PATH, groupKey, roomKey);
        DBUtils.updateChildren(path, room.toMap());

        // Update the "me" room default message on the database.
        String text = DBUtils.instance.getResource(DBUtils.WELCOME_MESSAGE_KEY);
        MessageManager.instance.createMessage(text, SYSTEM, account, room);
    }

    /** Determine if the current account has a group with the specified name */
    public boolean hasGroupWithName(String name) {
        if (mCurrentAccount == null)
            return false;
        for (String groupKey : mCurrentAccount.joinMap.keySet()) {
            Group group = GroupManager.instance.getGroupProfile(groupKey);
            if (group == null)
                continue;
            if (group.name.equals(name))
                return true;
        }
        return false;
    }

    /** Return the database path to an experience for a given experience profile. */
    public String getAccountPath(final String accountKey) {
        return String.format(Locale.US, ACCOUNT_PATH, accountKey);
    }

    /** Return the account for the current User, null if there is no signed in User. */
    public Account getCurrentAccount() {
        return mCurrentAccount;
    }

    /** Return the account for the current User and post an error message if one does not exist. */
    public Account getCurrentAccount(final Context context) {
        // Determine if there is a logged in account.  If so, return it.
        if (mCurrentAccount != null) return mCurrentAccount;

        // The User is not signed in.  Prompt them to do so now.
        String text = context.getString(R.string.NotLoggedInMessage);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        return null;
    }

    /** Return the current account id, null if there is no current signed in User. */
    public String getCurrentAccountId() {
        return mCurrentAccountKey;
    }

    /** Return null or the push key for the "me group" associated with the current account. */
    public String getMeGroupKey() {
        return hasAccount() ? mCurrentAccount.groupKey : null;
    }

    /** Test whether the specified group key is the 'me' group for the current account */
    public boolean isMeGroup(String groupKey) {
        return hasAccount() &&
                (mCurrentAccount.groupKey != null && (mCurrentAccount.groupKey.equals(groupKey)));
    }

    /** Return null or the push key for the "me room" associated with the current account. */
    public String getMeRoomKey() {
        // Ensure that there is an account and a me group profile has been registered.  If not
        // return null, otherwise return the me room push key which will be the only room in the Me
        // group's room list.
        if (!hasAccount() || mGroup == null)
            return null;
        return mGroup.roomList.get(0);
    }

    /** Return TRUE iff there is a signed in User. */
    public boolean hasAccount() {
        return mCurrentAccount != null;
    }

    /** Initialize the account manager with the given set of localized messages and class names. */
    public void init(@NonNull final SparseArray<String> map) {
        // Initialize the account manager by setting the list localized messages that may be
        // used subsequently.
        mMessageMap = map;
    }

    /** Return true iff the current user is a restricted/protected user. */
    public boolean isRestricted() {
        return hasAccount() && mCurrentAccount.chaperone != null;
    }

    /** Remove the current account from the specified group */
    public void leaveGroup(Group group) {
        // Start by making sure the account is in the group
        if (!mCurrentAccount.joinMap.keySet().contains(group.key))
            return;

        // Put a message in each room in the group, indicating that the current account has left.
        // Then delete the account from the room's member list.
        for (String roomKey : group.roomList) {
            Room room = RoomManager.instance.getRoomProfile(roomKey);
            List<String> roomMembers = room.getMemberIdList();
            if (roomMembers.contains(mCurrentAccountKey)) {
                String format = mMessageMap.get(R.string.HasDepartedMessage);
                String text = String.format(Locale.getDefault(), format, mCurrentAccount.name);
                MessageManager.instance.createMessage(text, STANDARD, mCurrentAccount, room);
                RoomManager.instance.leaveRoom(room);
            }
        }

        // Delete corresponding entry from Group's 'members' list in database
        MemberManager.instance.removeMember(group.key, mCurrentAccountKey);

        // Delete account from group profile member list
        GroupManager.instance.leaveGroup(group);

        // Delete group from the account join map - do this last as the database access rules for
        // groups (and their children) depend on there being an entry in the account join map
        mCurrentAccount.joinMap.remove(group.key);
        updateAccount(mCurrentAccount);

        AppEventManager.instance.post(new ProfileGroupDeleteEvent(group.key));
    }

    /** Remove the current group from the specified room */
    public void leaveRoom(Room room) {
        if (!room.getMemberIdList().contains(mCurrentAccountKey))
            return;
        // The account has joined the room, so now un-join it. Start by putting a message in the
        // room indicating the account has departed.
        String format = mMessageMap.get(R.string.HasDepartedMessage);
        String text = String.format(Locale.getDefault(), format, mCurrentAccount.name);
        MessageManager.instance.createMessage(text, STANDARD, mCurrentAccount, room);
        RoomManager.instance.leaveRoom(room);

        // Remove the room from the group member joinMap
        Account member = MemberManager.instance.getMember(room.groupKey);
        if (member != null) {
            member.joinMap.remove(room.key);
            MemberManager.instance.updateMember(member);
        }

        AppEventManager.instance.post(new ProfileRoomDeleteEvent(room.key));
    }

    /** Handle an account change by providing an authentication change on a sign in or sign out. */
    @Subscribe public void onAccountChange(@NonNull final AccountChangeEvent event) {
        // Detect an account sign in or sign out event.  If either is found, generate an
        // authentication change event.  It is also possible to get a new account event without
        // first getting a sign out event (this has been observed when we are switching to a
        // protected user).
        String id = event.account != null ? event.account.key : null;
        String cid = mCurrentAccountKey;
        if (cid != null && id != null && !cid.equals(id)) {
            cid = null;
            // we have swapped to a new account without first getting the sign out so simulate
            // the logout by sending events with a null account
            AppEventManager.instance.post(new AuthenticationChangeEvent(null));
            AppEventManager.instance.post(new AuthenticationChangeHandled(null));
        }

        if ((cid == null && id != null) || (cid != null && id == null)) {
            // An authentication change has taken place.  Let the app know.
            mCurrentAccountKey = id;
            mCurrentAccount = event.account;
            AppEventManager.instance.post(new AuthenticationChangeEvent(event.account));
            AppEventManager.instance.post(new AuthenticationChangeHandled(event.account));
        } else {
            // Detect a change to the account.  If found, set watchers on the me group and any
            // joined groups.
            if (event.account != null)
                for (String key : event.account.joinMap.keySet())
                    GroupManager.instance.setWatcher(key);
        }

        // Set watchers on any existing protected user accounts
        if (mCurrentAccount != null)
            for (String pUserId : mCurrentAccount.protectedUsers)
                ProtectedUserManager.instance.setWatcher(pUserId);

        // Check for protected user data and update the account if there are any.
        if (event.account != null) {
            DatabaseReference invite = FirebaseDatabase.getInstance().getReference()
                    .child(String.format(AccountManager.PROTECTED_PATH, mCurrentAccount.key));

            invite.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot dataSnapshot) {
                    // If the snapshot has nothing to offer, move on. Otherwise update the account.
                    if (!dataSnapshot.hasChildren())
                        return;
                    // Update the account.
                    Account account = AccountManager.instance.getCurrentAccount();
                    if (account == null) // not logged in
                        return;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        account.protectedUsers.add((String) data.getValue());
                        ProtectedUserManager.instance.setWatcher((String) data.getValue());
                        data.getRef().removeValue();
                    }
                    AccountManager.instance.updateAccount(account);
                }

                @Override public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Fetch protected user invitations failed.");
                    Log.e(TAG, "Database Error Details: " + databaseError.getDetails());
                }
            });
        }
    }

    /** Deal with authentication sign-out changes. */
    @Subscribe public void onAuthStateChanged(@NonNull final AuthStateChangedEvent event) {
        // Deal with a User sign-out or a new user sign-in by un-registering the current account
        // handler.
        String name = mCurrentAccount != null ? mCurrentAccount.email : null;
        if (name != null) {
            String handler = DBUtils.getHandlerName(ACCOUNT_CHANGE_HANDLER, mCurrentAccount.key);
            DatabaseRegistrar.instance.unregisterHandler(handler);
            Log.i(TAG, "Authentication sign-out from: " + name);
            AppEventManager.instance.post(new AccountChangeEvent(null));
        }

        // Deal with a sign-in by setting up an account change listener.
        if (event.user != null) {
            // A user has signed in. Ensure an account change listener is registered and persist the
            // credentials.
            name = DBUtils.getHandlerName(ACCOUNT_CHANGE_HANDLER, event.user.getUid());
            String path = getAccountPath(event.user.getUid());
            DatabaseRegistrar.instance.registerHandler(new AccountChangeHandler(name, path));
        }
    }

    /** Handle the me group profile change by obtaining the me room push key. */
    @Subscribe public void onGroupProfileChange(@NonNull final ProfileGroupChangeEvent event) {
        // Ensure that the event group profile key exists
        if (event.key == null)
            return;
        // Cache the me group and set a watcher on its room
        if (event.key.equals(getMeGroupKey())) {
            mGroup = event.group;
            RoomManager.instance.setWatcher(mGroup.key, mGroup.roomList.get(0));
        }
        // In the case of a protected user, there may be pre-populated items in the join map, so
        // create members and add room watchers.
        if (isRestricted() && mCurrentAccount.joinMap.containsKey(event.key) &&
                !event.group.memberList.contains(mCurrentAccountKey)) {
            Account member = new Account(mCurrentAccount);
            if (event.group.roomList == null)
                event.group.roomList = new ArrayList<>();
            for (String roomKey : event.group.roomList) {
                Room room = RoomManager.instance.getRoomProfile(roomKey);
                member.joinMap.put(roomKey, new JoinState());
                if (room != null) {
                    List<String> roomMembers = room.getMemberIdList();
                    roomMembers.add(getCurrentAccountId());
                    // Add a message stating the user has joined
                    String format = mMessageMap.get(R.string.HasJoinedMessage);
                    String text = String.format(Locale.getDefault(), format, mCurrentAccount.name);
                    MessageManager.instance.createMessage(text, STANDARD, mCurrentAccount, room);
                    RoomManager.instance.updateRoomProfile(room);
                }
            }
            member.groupKey = event.key;
            MemberManager.instance.createMember(member);
            event.group.memberList.add(mCurrentAccountKey);
            GroupManager.instance.updateGroupProfile(event.group);
        }
    }

    /** ... */
    @Subscribe public void onRoomProfileChange(@NonNull final ProfileRoomChangeEvent event) {
        // If the current account is protected and has been joined to a group, but is not yet in
        // the common room member list, make sure to add it.
        if (!isRestricted() || event.key == null || event.room == null)
            return;
        Account account = getCurrentAccount();
        if (account.joinMap.containsKey(event.room.groupKey) && event.room.type == COMMON
                && !event.room.getMemberIdList().contains(getCurrentAccountId())) {
            List<String> roomMembers = event.room.getMemberIdList();
            roomMembers.add(getCurrentAccountId());
            // Add a message stating the user has joined
            String format = mMessageMap.get(R.string.HasJoinedMessage);
            String text = String.format(Locale.getDefault(), format, account.getDisplayName());
            MessageManager.instance.createMessage(text, STANDARD, getCurrentAccount(), event.room);
            RoomManager.instance.updateRoomProfile(event.room);
        }
        // If the room is a private room and the current account has not joined it, remove the
        // watcher on it.
        if (!(event.room.getMemberIdList().contains(getCurrentAccountId()))
                && event.room.type == PRIVATE) {
            RoomManager.instance.roomMap.remove(event.room.key);
            RoomManager.instance.removeWatcher(event.room.key);
        }
    }

    /** Update the given account on the database. */
    public void updateAccount(final Account account) {
        String path = String.format(Locale.US, ACCOUNT_PATH, account.key);
        account.modTime = new Date().getTime();
        DBUtils.updateChildren(path, account.toMap());
    }

    // Private classes.

    /** A listener for account create success. */
    private class CreateSuccessListener implements OnSuccessListener<AuthResult> {
        String displayName;
        CreateSuccessListener(String name) {
            displayName = name;
        }
        @Override public void onSuccess(AuthResult authResult) {
            // Set display name
            // TODO: how to get the photo URL to use in the request '.setPhotoUri' method?
            UserProfileChangeRequest changeNameRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
            final FirebaseUser user = authResult.getUser();
            user.updateProfile(changeNameRequest)
                    .addOnFailureListener(new AuthFailureListener())
                    .addOnCompleteListener(new AuthCompleteListener(displayName));
        }
    }

    /** A listener for authorization failure. */
    private class AuthFailureListener implements OnFailureListener {
        @Override public void onFailure(@NonNull Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage());
            String message;
            if (e instanceof FirebaseAuthInvalidCredentialsException)
                message = mMessageMap.get(R.string.AuthFailureInvalidCredentials);
            else if (e instanceof FirebaseAuthInvalidUserException)
                message = mMessageMap.get(R.string.AuthFailureInvalidUser);
            else
                message = String.format(mMessageMap.get(R.string.AuthSignInFailure),
                        e.getLocalizedMessage());
            AppEventManager.instance.post(new ProtectedUserAuthFailureEvent(message));
        }
    }

    /** A listener for authorization success. */
    public class AuthSuccessListener implements OnSuccessListener<AuthResult> {
        @Override public void onSuccess(@NonNull AuthResult result) {
            // Clean up the stashed credentials after authorization is successful
            ProtectedUserManager.instance.removeEMailCredentials();
        }
    }

    /** A listener for authorization complete. */
    private class AuthCompleteListener implements OnCompleteListener<Void> {
        String displayName;
        AuthCompleteListener(String name) {
            displayName = name;
        }
        @Override public void onComplete(@NonNull Task<Void> task) {
            // TODO: This executes even if the name change fails, since the account creation
            // succeeded and we want to save the credential to SmartLock (if enabled).
            AccountManager.instance.mCurrentAccount.name = displayName;
            AccountManager.instance.updateAccount(mCurrentAccount);
            ProtectedUserManager.instance.removeEMailCredentials();
        }
    }
}
