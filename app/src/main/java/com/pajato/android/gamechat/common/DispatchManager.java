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

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.main.NetworkManager;
import com.pajato.android.gamechat.main.PaneManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.common.FragmentKind.chat;
import static com.pajato.android.gamechat.common.FragmentType.chatGroupList;
import static com.pajato.android.gamechat.common.FragmentType.chatOffline;
import static com.pajato.android.gamechat.common.FragmentType.chatSignedOut;
import static com.pajato.android.gamechat.common.FragmentType.expGroupList;
import static com.pajato.android.gamechat.common.FragmentType.expOffline;
import static com.pajato.android.gamechat.common.FragmentType.expRoomList;
import static com.pajato.android.gamechat.common.FragmentType.expSignedOut;
import static com.pajato.android.gamechat.common.FragmentType.experienceList;
import static com.pajato.android.gamechat.common.FragmentType.noExperiences;

/**
 * Manages the game related aspects of the GameChat application. These include the creation of new
 * game instances, notifications, and game settings.
 *
 * @author Bryan Scott
 * @author Paul Reilly
 */
public enum DispatchManager {
    instance;

    // Public instance variables.

    /** The current chat fragment type */
    public FragmentType currentChatFragmentType;

    /** The current experience fragment type */
    public FragmentType currentExpFragmentType;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = DispatchManager.class.getSimpleName();

    // Private instance variables.

    /** The repository for fragments created on demand. */
    private Map<FragmentType, BaseFragment> mFragmentMap = new HashMap<>();

    // Public instance methods.

    /**
     * Dispatch from the given fragment to the game indicated by the destination type. Used when
     * chaining to games.
     * @param fragment the fragment which is initiating this transition
     * @param destinationType the type of the desired target fragment
     */
    public void dispatchToGame(final BaseFragment fragment, final FragmentType destinationType) {
        if (destinationType == null || destinationType.expType == null)
            return;
        FragmentType toType;
        if (fragment.type == noExperiences)
            toType = expGroupList;
        else if (fragment.type == expGroupList)
            toType = expRoomList;
        else if (fragment.type == expRoomList)
            toType = experienceList;
        else
            toType = destinationType;
        Dispatcher dispatcher = new Dispatcher(fragment.type, toType, destinationType.expType);
        BaseFragment targetFragment = getFragment(toType);
        targetFragment.onSetup(fragment.getActivity(), dispatcher); // sets mDispatcher
        initiateTransition(fragment, dispatcher, targetFragment);
    }

    /**
     * Dispatch to a new fragment
     * @param fragment the fragment which is initiating this transition (may be one in a chain)
     * @param kind the target fragment kind
     */
    public void dispatchToFragment(final BaseFragment fragment, final FragmentKind kind) {
        Dispatcher dispatcher = getDispatcher(kind);
        // When offline, switch automatically to experience (game) pane on a smartphone
        if (dispatcher.type == chatOffline || dispatcher.type == expOffline &&
                !PaneManager.instance.isTablet()) {
            ViewPager viewPager = fragment.getActivity().findViewById(R.id.viewpager);
            if (viewPager != null)
                viewPager.setCurrentItem(PaneManager.GAME_INDEX);
        }
        BaseFragment targetFragment = getFragment(dispatcher.type);
        if (targetFragment == null)
            return;
        targetFragment.onSetup(fragment.getActivity(), dispatcher); // sets mDispatcher
        initiateTransition(fragment, dispatcher, targetFragment);
    }

    /**
     * Dispatch to a new fragment
     * @param fragment the fragment which is initiating this transition (may be one in a chain)
     * @param type the type of the desired target fragment to which to transition
     */
    public void dispatchToFragment(final BaseFragment fragment, final FragmentType type) {
        dispatchToFragment(fragment, type, null, null);
    }

    /**
     * Dispatch to a new fragment
     * @param fragment the fragment which is initiating this transition (may be one in a chain)
     * @param type the type of the desired target fragment to which to transition
     * @param launchType the type of the fragment which initiated this chain (or null)
     */
    public void dispatchToFragment(final BaseFragment fragment, final FragmentType type,
                                   final FragmentType launchType) {
        dispatchToFragment(fragment, type, launchType, null);
    }

    /**
     * Dispatch to a new fragment
     * @param fragment the fragment which is initiating this transition (may be one in a chain)
     * @param type the type of the desired target fragment to which to transition
     * @param launchType the type of the fragment which initiated this chain (or null)
     * @param item a ListItem object containing relevant push keys; may be null
     */
    public void dispatchToFragment(final BaseFragment fragment, final FragmentType type,
                                      final FragmentType launchType, final ListItem item) {
        Dispatcher dispatcher = new Dispatcher(fragment, type, launchType, item);
        BaseFragment targetFragment = getFragment(dispatcher.type);
        if (targetFragment == null)
            return;
        targetFragment.onSetup(fragment.getActivity(), dispatcher); // sets mDispatcher
        initiateTransition(fragment, dispatcher, targetFragment);
    }

    /**
     * Return from a fragment to the appropriate previous place based on the dispatcher object in
     * the current fragment.
     * @param fragment the current fragment which is initiating this return transition
     */
    public void dispatchReturn(final BaseFragment fragment) {
        Dispatcher currentDispatcher = fragment.mDispatcher;
        // If the launcher type is set, that's where we want to go. If not, then go back to the
        // start type. If it is null, determine the fragment kind and go to it's default group list.
        FragmentType toType;
        if (currentDispatcher.launchType == null && currentDispatcher.startType == null)
            toType = fragment.type.kind == chat ? chatGroupList : expGroupList;
        else
            toType = currentDispatcher.launchType == null ? currentDispatcher.startType :
                    currentDispatcher.launchType;
        BaseFragment toFragment = getFragment(toType);
        if (toFragment == null)
            return;
        // For back nav, clear the experience type from the 'to' fragment dispatcher
        toFragment.mDispatcher.expType = null;
        initiateTransition(fragment, toFragment.mDispatcher, toFragment);
    }

    /** Return null or the fragment associated with the given type, creating it as needed. */
    public BaseFragment getFragment(final FragmentType type) {
        // If a fragment is created, return it.  Otherwise create, cache and return it.
        BaseFragment result = mFragmentMap.get(type);
        return result == null ? getFragmentInstance(type) : result;
    }

    /** Prepare to handle a dispatch for back navigation */
    public void handleBackDispatch(FragmentType type) {
        BaseFragment fragment = DispatchManager.instance.getFragment(type);
        if (fragment == null)
            return;
        FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            fragment.getActivity().onBackPressed();
            return;
        }
        DispatchManager.instance.dispatchReturn(fragment);
    }

    // Notify the dispatch manager so that back navigation is reoriented to this new experience.
    public void moveExperience(@NonNull final Experience experience) {
        FragmentType type = experience.getExperienceType().getFragmentType();
        BaseFragment fragment = getFragment(type);
        do {
            type = fragment.mDispatcher.startType;
            if (!mFragmentMap.containsKey(type))
                break;
            fragment = mFragmentMap.get(type);
            fragment.mDispatcher.groupKey = experience.getGroupKey();
            fragment.mDispatcher.roomKey = experience.getRoomKey();
        } while (type != expGroupList);
    }

    // Private instance methods.

    /** Return a dispatcher object based on fragment kind specified. */
    private Dispatcher getDispatcher(final FragmentKind kind) {

        // Deal with an off line user, a signed out user, or no messages or experiences at all, in
        // that order.  In each case, return an empty dispatcher but for the fragment type of the
        // next screen to show.
        FragmentType type = kind == chat ? chatOffline : expOffline;
        if (!NetworkManager.instance.isConnected())
            return new Dispatcher(type);
        Account account = AccountManager.instance.getCurrentAccount();
        int joinSize = account != null ? account.joinMap.size() : -1;

        // Case on the number of joined groups: -1 implies the User is signed out, 0 implies the me
        // room, 1 will use the rooms in that group, otherwise set up to show a list of groups.
        switch (joinSize) {
            case -1:            // Return an offline fragment type of the right kind.
                type = kind == chat ? chatSignedOut : expSignedOut;
                return new Dispatcher(type);
            default:            // Depends on kind and number of experiences, if any.
                if (kind == chat)
                    return new Dispatcher(chatGroupList);
                if (ExperienceManager.instance.experienceMap.size() > 0)
                    return new Dispatcher(expGroupList);
                return new Dispatcher(noExperiences);
        }
    }

    /** Return an instance for a given fragment type, null if no such instance can be created. */
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

    /** Initiate a transition from a specified fragment to a target fragment using the dispatcher */
    private void initiateTransition(final BaseFragment fragment, final Dispatcher dispatcher,
                                    final BaseFragment toFragment) {
        FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
        FragmentManager.enableDebugLogging(true);
        if (toFragment.type.kind == chat) {
            currentChatFragmentType = toFragment.type;
        } else {
            currentExpFragmentType = toFragment.type;
        }
        manager.beginTransaction()
                .replace(dispatcher.type.getEnvelopeId(), toFragment)
                .commit();
    }
}
