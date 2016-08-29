/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;

import com.pajato.android.gamechat.R;

import java.util.Locale;

import static com.pajato.android.gamechat.chat.ChatManager.ChatFragmentType.showGroupList;

/**
 * Provides the interface to the database/back end, primarily Firebase.
 *
 * @author Paul Michael Reilly
 */
enum ChatManager {
    instance;

    /** Provide a set of enum constants to identify the chat related fragments. */
    public enum ChatFragmentType {
        showGroupList (ShowGroupListFragment.class);
        //showRoomList (ShowRoomListFragment.class),
        //showMessages (ShowMessagesFragment.class),
        //showNewGroup (ShowNewGroupFragment.class),
        //showNewRoom (ShowNewRoomFragment.class),
        //showJoinRoom (ShowJoinRoomFragment.class),
        //showNoJoinedRooms (ShowNoJoinedRoomsFragment.class),
        //showNoAccount (ShowNoAccountFragment.class);

        // Private instance variables.

        /** The fragment class associated with this type. */
        Class<? extends Fragment> fragmentClass;

        /** Build in instance of the type with a given fragment class. */
        ChatFragmentType(final Class<? extends Fragment> fragmentClass) {
            this.fragmentClass = fragmentClass;
        }

    }
    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ChatManager.class.getSimpleName();

    // Public instance methods.

    /** The repository for fragments created on demand. */
    private SparseArray<Fragment> mFragmentMap = new SparseArray<>();

    /** Initialize the component. */
    public void init(final FragmentActivity activity) {
        // Create the fragment where the groups and rooms with messages to view will be summarized.
        // These are derived from a User's joined rooms which are in turn derived from the groups
        // which the User is a member.
        replaceFragment(showGroupList, activity);
    }

    // Private instance methods.

    /** Attach a drill down fragment identified by a type, creating that fragment as necessary. */
    public void chainFragment(final ChatFragmentType type, final FragmentActivity context) {
        // Determine if the replacement fragment has been attached yet.
        Fragment fragment = getFragment(type);
        if (fragment == null) return;

        // Run the transaction to attach the fragment to the activity, adding a backstack.
        context.getSupportFragmentManager().beginTransaction()
            .replace(R.id.chatFragmentContainer, fragment)
            .addToBackStack(null)
            .commit();
    }

    /** Attach a fragment identified by a type, creating that fragment as necessary. */
    public void replaceFragment(final ChatFragmentType type, final FragmentActivity context) {
        // Determine if the replacement fragment has been attached yet.
        Fragment fragment = getFragment(type);
        if (fragment == null) return;

        // Run the transaction to attach the fragment to the activity.
        context.getSupportFragmentManager().beginTransaction()
            .replace(R.id.chatFragmentContainer, fragment)
            .commit();
    }

    // Private instance methods.

    /** Obtain a fragment, if possible. */
    private Fragment getFragment(final ChatFragmentType type) {
        Fragment result = mFragmentMap.get(type.ordinal());
        if (result == null) {
            // The fragment has not been created yet.  Do so now, aborting if the fragment can not
            // be created.
            result = getFragmentInstance(type);
        }

        return result;
    }

    /** Return an instance for a given class, null if no such instance can be created. */
    private Fragment getFragmentInstance(final ChatFragmentType type) {
        // Create the fragment instance. Log any exceptions.
        try {
            Fragment result = type.fragmentClass.newInstance();
            mFragmentMap.put(type.ordinal(), result);
            return result;
        } catch (InstantiationException | IllegalAccessException exc) {
            String format = "Failed to create a fragment for the class: %s";
            Log.e(TAG, String.format(Locale.US, format, type.fragmentClass.getSimpleName()), exc);
        }

        return null;
    }

}
