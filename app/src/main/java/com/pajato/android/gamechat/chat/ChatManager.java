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
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.main.NetworkManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.ChatFragmentType.groupList;
import static com.pajato.android.gamechat.chat.ChatFragmentType.noMessages;
import static com.pajato.android.gamechat.chat.ChatFragmentType.offline;
import static com.pajato.android.gamechat.chat.ChatFragmentType.roomList;
import static com.pajato.android.gamechat.chat.ChatFragmentType.signedOut;

/**
 * Provides the interface to the database/back end, primarily Firebase.
 *
 * @author Paul Michael Reilly
 */
public enum ChatManager {
    instance;


    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ChatManager.class.getSimpleName();

    /** ... */
    public ChatFragmentType lastTypeShown;

    /** The repository for fragments created on demand. */
    private Map<ChatFragmentType, BaseFragment> mFragmentMap = new HashMap<>();

    // Public instance methods.

    /** Return true iff an appropriate fragment is started. */
    public boolean startNextFragment(final FragmentActivity context) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        Dispatcher<ChatFragmentType, Message> dispatcher = getDispatcher();
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    /** Return true iff a fragment of the fiven type is started. */
    public boolean startNextFragment(FragmentActivity context, ChatFragmentType type) {
        Dispatcher<ChatFragmentType, Message> dispatcher = new Dispatcher<>(type);
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    // Private instance methods.

    /** Return a dispatcher object based on the current message list state. */
    private Dispatcher<ChatFragmentType, Message> getDispatcher() {
        // Deal with an off line user, a signed out user, or no messages at all, in that order.
        // In each case, return an empty dispatcher but for the fragment type of the next screen to
        // show.
        Map<String, Map<String, Map<String, Message>>> messageMap;
        messageMap = MessageManager.instance.messageMap;
        if (!NetworkManager.instance.isConnected()) return new Dispatcher<>(offline);
        if (!AccountManager.instance.hasAccount()) return new Dispatcher<>(signedOut);
        if (messageMap.size() == 0) return new Dispatcher<>(noMessages);

        // Deal with a signed in User with multiple messages across more than one group.  Return
        // a dispatcher with a map of experience keys.
        if (messageMap.size() > 1) return new Dispatcher<>(groupList, messageMap);

        // A signed in user with messages in more than one room but only one group. Return a
        // dispatcher identifying the group and room map.
        String groupKey = messageMap.keySet().iterator().next();
        Map<String, Map<String, Message>> roomMap = messageMap.get(groupKey);
        return new Dispatcher<>(roomList, groupKey, roomMap);
    }

    /** Attach a drill down fragment identified by a type, creating that fragment as necessary. */
    public void chainFragment(final ChatFragmentType type, final FragmentActivity context,
                              final ChatListItem item) {
        // Determine if the replacement fragment has been attached yet.
        Fragment fragment = getFragment(type);
        if (fragment == null) return;

        // Set the item on the fragment and run the transaction to attach the fragment to the
        // activity, adding a backstack.
        lastTypeShown = type;
        setItem(fragment, item);
        FragmentManager manager = context.getSupportFragmentManager();
        FragmentManager.enableDebugLogging(true);
        manager.beginTransaction()
            .replace(R.id.chatFragmentContainer, fragment)
            .addToBackStack(type.toString())
            .commit();
    }

    // Private instance methods.

    /** Obtain a fragment, if possible. */
    private Fragment getFragment(final ChatFragmentType type) {
        Fragment result = mFragmentMap.get(type);
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
            BaseFragment result = type.fragmentClass.newInstance();
            mFragmentMap.put(type, result);
            return result;
        } catch (InstantiationException | IllegalAccessException exc) {
            String format = "Failed to create a fragment for the class: %s";
            Log.e(TAG, String.format(Locale.US, format, type.fragmentClass.getSimpleName()), exc);
        }

        return null;
    }

    /** Set the item to be relevant for a list of groups, rooms or messages. */
    private void setItem(Fragment fragment, ChatListItem item) {
        if (fragment instanceof BaseChatFragment) {
            ((BaseChatFragment) fragment).setItem(item);
        }
    }

    /** Return true iff a fragment for the given experience is started. */
    private boolean startNextFragment(final FragmentActivity context,
                                      final Dispatcher<ChatFragmentType, Message> dispatcher) {
        // Ensure that the fragment exists, creating it as necessary.  Abort if the fragment cannot
        // be created.
        if (!fragmentExists(dispatcher)) return false;

        // Make the next fragment current and initialize it using the context of the fragment
        // currently in the foreground.
        mFragmentMap.get(dispatcher.type).onSetup(context, dispatcher);
        context.getSupportFragmentManager().beginTransaction()
            .replace(R.id.chatFragmentContainer, mFragmentMap.get(dispatcher.type))
            .commit();
        return true;
    }

    /** Return TRUE iff the fragment denoted by the given dispactcher exists. */
    private boolean fragmentExists(final Dispatcher<ChatFragmentType, Message> dispatcher) {
        // Determine if the fragment needs to be created, returning true if not.
        if (mFragmentMap.containsKey(dispatcher.type)) return true;

        // The fragment needs to be created. Make the attempt, leaving debug information if the
        // fragment cannot be created.
        try {
            mFragmentMap.put(dispatcher.type, dispatcher.type.fragmentClass.newInstance());
            return true;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
}
