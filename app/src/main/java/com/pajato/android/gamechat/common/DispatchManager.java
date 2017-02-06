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

package com.pajato.android.gamechat.common;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.main.NetworkManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.common.FragmentKind.chat;
import static com.pajato.android.gamechat.common.FragmentType.chatGroupList;
import static com.pajato.android.gamechat.common.FragmentType.chatOffline;
import static com.pajato.android.gamechat.common.FragmentType.chatSignedOut;
import static com.pajato.android.gamechat.common.FragmentType.expGroupList;
import static com.pajato.android.gamechat.common.FragmentType.expOffline;
import static com.pajato.android.gamechat.common.FragmentType.expSignedOut;

/**
 * Manages the game related aspects of the GameChat application. These include the creation of new
 * game instances, notifications, and game settings.
 *
 * @author Bryan Scott
 * @author Paul Reilly
 */
public enum DispatchManager {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = DispatchManager.class.getSimpleName();

    // Private instance variables.

    /** The repository for fragments created on demand. */
    private Map<FragmentType, BaseFragment> mFragmentMap = new HashMap<>();

    // Public instance methods.

    /**
     * Attach a drill down fragment identified by a type, creating that fragment as necessary.
     *
     * @param context The activity attached to the fragment that spawned this call.
     * @param type The type of the fragment to drill into.  One will be created if necessary.
     */
    public void chainFragment(final FragmentActivity context, final FragmentType type) {
        chainFragment(context, type, null);
    }

    /**
     * Attach a drill down fragment identified by a type, creating that fragment as necessary.
     *
     * @param context The activity attached to the fragment that spawned this call.
     * @param type The type of the fragment to drill into.  One will be created if necessary.
     * @param item Encapsulates the push key information appropriate to the fragment.
     */
    public void chainFragment(final FragmentActivity context, final FragmentType type,
                              final ListItem item) {
        // Ensure that type is valid.  Abort if not, otherwise validate the fragment, aborting if
        // invalid.
        if (type == null)
            return;
        Dispatcher dispatcher = getDispatcher(type, item);
        BaseFragment fragment = getFragment(type);
        if (fragment == null)
            return;

        // Setup the fragment using the dispatcher and chain to the fragment such that the back
        // arrow and a back press return to the originating fragment.
        fragment.onSetup(context, dispatcher);
        FragmentManager manager = context.getSupportFragmentManager();
        FragmentManager.enableDebugLogging(true);
        manager.beginTransaction()
                .replace(type.getEnvelopeId(), fragment)
                .addToBackStack(type.toString())
                .commit();
    }

    /** Return null or the fragment associated with the given type, creating it as needed. */
    public BaseFragment getFragment(final FragmentType type) {
        // Ensure that a fragment is created and cached.  If so, return it, otherwise attempt to do
        // so.
        BaseFragment result = mFragmentMap.get(type);
        return result == null ? getFragmentInstance(type) : result;
    }

    /**
     * Start the next fragment as indicated by the current app state.  The fragment will be of the
     * given kind.
     *
     * @param context The activity that will attach to the next fragment.
     * @param kind The fragment kind, either chat or experience.
     *
     * @return TRUE iff the next fragment is started.
     */
    public boolean startNextFragment(final FragmentActivity context, final FragmentKind kind) {
        // Ensure that the dispatcher has a valid kind.  If not then abort, otherwise create a
        // dispatcher of the given kind and determine if an associated fragment can be started.
        // Return false if not, otherwise start the fragment and return true iff the fragment is
        // successfully started.
        if (kind == null)
            return false;
        Dispatcher dispatcher = getDispatcher(kind);
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    /**
     * Start the next fragment of a given type as indicated by the current app state.  The fragment
     * type will determine the dispatch kind.
     *
     * @param context The activity that will attach to the next fragment.
     * @param type The fragment type, which determines the dispatch kind.
     *
     * @return TRUE iff the next fragment is started.
     */
    public boolean startNextFragment(final FragmentActivity context, final FragmentType type) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        if (type == null)
            return false;
        Dispatcher dispatcher = getDispatcher(type, null);
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    /**
     * Start the next fragment of a given type as indicated by the current app state.  The fragment
     * type will determine the dispatch kind.
     *
     * @param context The activity that will attach to the next fragment.
     * @param type The fragment type, which determines the dispatch kind.
     * @param item A template containing payload data to pass along with the UI views.
     *
     * @return TRUE iff the next fragment is started.
     */
    public boolean startNextFragment(final FragmentActivity context, final FragmentType type,
                                     final ListItem item) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        if (type == null)
            return false;
        Dispatcher dispatcher = getDispatcher(type, item);
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    // Private instance methods.

    /**
     *  Return a dispatcher object for a predisposed experience type.
     *
     * @param type An predisposed experience type or null to indicate the type should be computed.
     * @param item The chat list item carrying the group key.
     */
    private Dispatcher getDispatcher(final FragmentType type, final ListItem item) {
        // Determine if the dispatcher should be generate based on the kind, in which case a
        // suitable dispatcher will be returned, otherwise set up an experience dispatcher based
        // on the given type.
        switch (type) {
            case checkers:
            case chess:
            case tictactoe:     // Handle an experience dispatch providing a type.
                return new Dispatcher(type);

            case chatRoomList:
            case createRoom:
            case joinRoom:
            case messageList:
            case selectChatGroupsRooms:
            case selectExpGroupsRooms:
            case selectRoom:
            case selectUser:    // Handle a chat dispatch providing both a type and an item.
                return new Dispatcher(type, item);

            default:            // Handle all the other types in the normal fashion.
                return getDispatcher(type.getKind());
        }
    }

    /** Return a dispatcher object based on the current message list state. */
    private Dispatcher getDispatcher(final FragmentKind kind) {
        // Deal with an off line user, a signed out user, or no messages or experiences at all, in
        // that order.  In each case, return an empty dispatcher but for the fragment type of the
        // next screen to show.
        FragmentType type = kind == chat ? chatOffline : expOffline;
        if (!NetworkManager.instance.isConnected()) return new Dispatcher(type);
        Account account = AccountManager.instance.getCurrentAccount();
        int groupsJoined = account != null ? account.joinList.size() : -1;

        // Case on the number of joined groups: -1 implies the User is signed out, 0 implies the me
        // room, 1 will use the rooms in that group, otherwise set up to show a list of groups.
        switch (groupsJoined) {
            case -1: // Return an offline fragment type of the right kind.
                type = kind == chat ? chatSignedOut : expSignedOut;
                return new Dispatcher(type);

            default:
                return new Dispatcher(kind == chat ? chatGroupList : expGroupList);
        }
    }

    /** Return an instance for a given class, null if no such instance can be created. */
    private BaseFragment getFragmentInstance(final FragmentType type) {
        // Create the fragment instance. Log any exceptions.
        try {
            BaseFragment result = type.fragmentClass.newInstance();
            result.type = type;
            mFragmentMap.put(type, result);
            return result;
        } catch (InstantiationException | IllegalAccessException exc) {
            String format = "Failed to create a fragment for the class: %s";
            Log.e(TAG, String.format(Locale.US, format, type.fragmentClass.getSimpleName()), exc);
        }

        return null;
    }

    /** Return true iff a fragment for the given experience is started. */
    private boolean startNextFragment(final FragmentActivity context, final Dispatcher dispatcher) {
        // Ensure that the fragment exists, creating it as necessary, based on the dispatcher type.
        // If not, abort, signalling false.  Otherwise, setup the fragment using the dispatcher and
        // use the fragment support manager to initiate the lifecycle on the fragment.
        BaseFragment fragment = getFragment(dispatcher.type);
        if (fragment == null)
            return false;
        fragment.onSetup(context, dispatcher);
        context.getSupportFragmentManager().beginTransaction()
            .replace(dispatcher.type.getEnvelopeId(), fragment)
            .commit();
        return true;
    }
}
