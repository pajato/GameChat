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

package com.pajato.android.gamechat.exp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.exp.model.ExpProfile;
import com.pajato.android.gamechat.main.NetworkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.exp.ExpFragmentType.expList;
import static com.pajato.android.gamechat.exp.ExpFragmentType.groupList;
import static com.pajato.android.gamechat.exp.ExpFragmentType.noExp;
import static com.pajato.android.gamechat.exp.ExpFragmentType.offline;
import static com.pajato.android.gamechat.exp.ExpFragmentType.roomList;
import static com.pajato.android.gamechat.exp.ExpFragmentType.signedOut;

/**
 * Manages the game related aspects of the GameChat application. These include the creation of new
 * game instances, notifications, and game settings.
 *
 * @author Bryan Scott
 */
public enum GameManager {
    instance;

    // Public instance variables.

    /** The current match history. */
    public ArrayList<String> instructions = new ArrayList<>();

    // Private instance variables.

    /** The list of all fragments. */
    private BaseExperienceFragment[] mFragmentList = new BaseExperienceFragment[ExpFragmentType.values().length];

    /** The current fragment. */
    private int mCurrentFragment;

    /** The current group key as determined the last selected group. */
    //public String currentGroupKey;

    /** The current room key as determined by the last selected room. */
    //public String currentRoomKey;

    // Public instance methods.

    /** Return the current fragment's index value. */
    public int getCurrent() {
        return mCurrentFragment;
    }

    /** Return the current fragment being shown in the experience panel. */
    public BaseExperienceFragment getFragment(final int index) {
        return mFragmentList[index];
    }

    /** Return player 1 or player 2 based on the current turn value. */
    public String getTurn() {
        final int index = getCurrent();
        final ExpFragmentType type = ExpFragmentType.values()[index];
        final Fragment context = getFragment(index);
        switch (type) {
            default:
                // These two cases should never be called in an impactful way.
            case noExp:
                return null;
            case tictactoe:
                // For Tic-Tac-Toe, we need X or O.
                return getTurn(index, context, R.string.xValue, R.string.oValue);
            case checkers:
            case chess:
                // For chess and checkers, we need either primary or secondary player strings.
                return getTurn(index, context, R.string.player1, R.string.player2);
        }
    }

    /** Initialize the game manager fragment. */
    public void init() {
        // Initialize this fragment by using a negative index value; clear the current set of
        // instructions, establish the fragment tracking array and set the default display fragment
        // to show that there are no games to list.
        mCurrentFragment = -1;
        instructions.clear();
    }

    /** Return true iff a fragment for the given experience is started. */
    public boolean startNextFragment(final FragmentActivity context) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        Dispatcher<ExpFragmentType, ExpProfile> dispatcher = getDispatcher();
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    /** Return true iff a fragment for the given experience is started. */
    public boolean startNextFragment(final FragmentActivity context, final ExpFragmentType type) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        Dispatcher<ExpFragmentType, ExpProfile> dispatcher = getDispatcher(type);
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    // Private instance methods.

    /** Return a dispatcher object based on the current experience state. */
    private Dispatcher<ExpFragmentType, ExpProfile> getDispatcher() {
        // Deal with an off line user, a signed out user, or no experiences at all, in that order.
        // In each case, return an empty dispatcher but for the fragment type of the next screen to
        // show.
        Map<String, Map<String, Map<String, ExpProfile>>> expProfileMap =
                ExperienceManager.instance.expProfileMap;
        if (!NetworkManager.instance.isConnected()) return new Dispatcher<>(offline);
        if (AccountManager.instance.hasAccount()) return new Dispatcher<>(signedOut);
        if (expProfileMap.size() == 0) return new Dispatcher<>(noExp);

        // Deal with a signed in User with multiple experiences across more than one group.  Return
        // a dispatcher with a map of experience keys.
        if (expProfileMap.size() > 1) return new Dispatcher<>(groupList, expProfileMap);

        // A signed in user with experiences in more than one room but only one group. Return a
        // dispatcher identifying the group and room map.
        String groupKey = expProfileMap.keySet().iterator().next();
        Map<String, Map<String, ExpProfile>> roomMap = expProfileMap.get(groupKey);
        if (roomMap.size() > 1) return new Dispatcher<>(roomList, groupKey, roomMap);

        // A signed in user with multiple experiences in a single room.  Return a dispatcher that
        // identifies the group, room and the list of experience profiles.
        String roomKey = roomMap.keySet().iterator().next();
        List<ExpProfile> expProfileList = new ArrayList<>(roomMap.get(roomKey).values());
        if (expProfileList.size() > 1)
            return new Dispatcher<>(expList, groupKey, roomKey, expProfileList);

        // A signed in User with one experience. Return a dispatcher to show the single experience.
        ExpFragmentType fragmentType = getFragmentType(expProfileList.get(0));
        return new Dispatcher<>(fragmentType, expProfileList.get(0));
    }

    /** Return a dispatcher object for a given fragment type. */
    private Dispatcher<ExpFragmentType, ExpProfile> getDispatcher(final ExpFragmentType type) {
        // Case: there are no experiences of the given type. Return an empty dispatcher but for the
        // fragment type.
        List<ExpProfile> list = getExpProfileList(type);
        if (list == null || list.size() == 0) return new Dispatcher<>(type);

        // Case: a signed in user with a single experience. Return a dispatcher with the experience
        // key.
        if (list.size() == 1) return new Dispatcher<>(type, list.get(0));

        // A signed in user with more than one experience of the given type. Return a dispatcher
        // with the list of relevant experience keys.
        return new Dispatcher<>(type.showType, list);
    }

    /** Return cached experience profiles of a given type. */
    private List<ExpProfile> getExpProfileList(final ExpFragmentType fragmentType) {
        Map<String, Map<String, Map<String, ExpProfile>>> groupMap =
                ExperienceManager.instance.expProfileMap;
        List<ExpProfile> result = new ArrayList<>();
        for (String groupKey : groupMap.keySet()) {
            Map<String, Map<String, ExpProfile>> roomMap = groupMap.get(groupKey);
            for (String roomKey : roomMap.keySet()) {
                Map<String, ExpProfile> expProfileMap = roomMap.get(roomKey);
                for (String expProfileKey : expProfileMap.keySet()) {
                    ExpProfile expProfile = expProfileMap.get(expProfileKey);
                    ExpFragmentType type = getFragmentType(expProfile);
                    if (type == fragmentType) result.add(expProfile);
                }
            }
        }

        return result;
    }

    /** Return the fragment type associated with the experience given by the experience key. */
    private ExpFragmentType getFragmentType(final ExpProfile expProfile) {
        ExpType expType = ExpType.values()[expProfile.type];
        for (ExpFragmentType fragmentType : ExpFragmentType.values()) {
            if (expType == fragmentType.expType) return fragmentType;
        }

        return null;
    }

    /** Return the string value associated with the two players based on the current turn. */
    private String getTurn(int index, Fragment context, int first, int second) {
        return getFragment(index).getTurn() ? context.getString(first) : context.getString(second);
    }

    /** Return true iff a fragment for the given experience is started. */
    private boolean startNextFragment(final FragmentActivity context, final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
        // Ensure that the fragment exists, creating it as necessary.  Abort if the fragment cannot
        // be created.
        if (!fragmentExists(dispatcher)) return false;

        // Make the next fragment current and initialize it using the context of the fragment
        // currently in the foreground.
        int index = dispatcher.type.ordinal();
        mCurrentFragment = index;
        mFragmentList[index].setupExperience(context, dispatcher);
        context.getSupportFragmentManager().beginTransaction()
            .replace(R.id.gameFragmentContainer, mFragmentList[index])
            .commit();
        return true;
    }

    /** Return TRUE iff the fragment denoted by the given dispactcher exists. */
    private boolean fragmentExists(final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
        // Determine if the fragment needs to be created, returning true if not.
        int index = dispatcher.type.ordinal();
        if (index >= 0 && mFragmentList[index] != null) return true;

        // The fragment needs to be created. Make the attempt, leaving debug information if the
        // fragment cannot be created.
        try {

            mFragmentList[index] = dispatcher.type.fragmentClass.newInstance();
            return true;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
}
