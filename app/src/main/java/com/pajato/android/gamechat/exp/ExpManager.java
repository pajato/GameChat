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

import android.support.v4.app.FragmentActivity;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
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
public enum ExpManager {
    instance;

    // Public instance variables.

    /** The current match history. */
    public ArrayList<String> instructions = new ArrayList<>();

    // Private instance variables.

    /** The list of all fragments. */
    private BaseExperienceFragment[] mFragmentList = new BaseExperienceFragment[ExpFragmentType.values().length];

    /** The current fragment. */
    private int mCurrentFragment;

    // Public instance methods.

    /** Return the current fragment's index value. */
    public int getCurrent() {
        return mCurrentFragment;
    }

    /** Initialize the game manager's current fragment. */
    public void init() {
        // Initialize this fragment by using a negative index; clear the current set of instructions
        mCurrentFragment = -1;
        instructions.clear();
    }

    /** Return true iff a fragment for the given experience is started. */
    public boolean startNextFragment(final FragmentActivity context) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        Dispatcher<ExpFragmentType, Experience> dispatcher = getDispatcher();
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    /** Return true iff a fragment for the given experience is started. */
    public boolean startNextFragment(final FragmentActivity context, final ExpFragmentType type) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        Dispatcher<ExpFragmentType, Experience> dispatcher = getDispatcher(type);
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    // Private instance methods.

    /** Return a dispatcher object based on the current experience state. */
    private Dispatcher<ExpFragmentType, Experience> getDispatcher() {
        // Deal with an off line user, a signed out user, or no experiences at all, in that order.
        // In each case, return an empty dispatcher but for the fragment type of the next screen to
        // show.
        if (!NetworkManager.instance.isConnected()) return new Dispatcher<>(offline);
        if (!AccountManager.instance.hasAccount()) return new Dispatcher<>(signedOut);
        if (ExperienceManager.instance.experienceMap.size() == 0) return new Dispatcher<>(noExp);

        // Deal with a signed in User with multiple experiences across more than one group.  Return
        // a dispatcher with a map of experience keys.
        if (ExperienceManager.instance.expGroupMap.size() > 1) return new Dispatcher<>(groupList);

        // A signed in user with experiences in more than one room but only one group. Return a
        // dispatcher identifying the group and room map.
        Experience exp = ExperienceManager.instance.experienceMap.values().iterator().next();
        Map<String, Map<String, Experience>> roomMap;
        roomMap = ExperienceManager.instance.expGroupMap.get(exp.getGroupKey());
        if (roomMap.size() > 1) return new Dispatcher<>(roomList, exp.getGroupKey(), roomMap);

        // A signed in user with multiple experiences in a single room.  Return a dispatcher that
        // identifies the group, room and the list of experience profiles.
        String roomKey = roomMap.keySet().iterator().next();
        List<Experience> experienceList = new ArrayList<>(roomMap.get(roomKey).values());

        // If we have a signed in user with multiple experience in a single room and the current
        // experience is one of the experiences in said room, just stick with it
        if(experienceList.contains(exp)) {
            ExpFragmentType type = ExpFragmentType.values()[mCurrentFragment];
            return new Dispatcher<>(type, exp);
        }

        if (experienceList.size() > 1)
            return new Dispatcher<>(expList, exp.getGroupKey(), roomKey, experienceList);

        // A signed in User with one experience. Return a dispatcher to show the single experience.
        ExpFragmentType fragmentType = getFragmentType(experienceList.get(0));
        return new Dispatcher<>(fragmentType, experienceList.get(0));
    }

    /** Return a dispatcher object for a given fragment type. */
    private Dispatcher<ExpFragmentType, Experience> getDispatcher(final ExpFragmentType type) {
        // Case: there are no experiences of the given type. Return an empty dispatcher but for the
        // fragment type.
        List<Experience> list = getExperienceList(type);
        if (list == null || list.size() == 0) return new Dispatcher<>(type);

        // Case: a signed in user with a single experience. Return a dispatcher with the experience
        // key.
        if (list.size() == 1) return new Dispatcher<>(type, list.get(0));

        // A signed in user with more than one experience of the given type. Return a dispatcher
        // with the list of relevant experience keys.
        return new Dispatcher<>(type.showType, list);
    }

    /** Return cached experience profiles of a given type. */
    private List<Experience> getExperienceList(final ExpFragmentType fragmentType) {
        Map<String, Map<String, Map<String, Experience>>> expGroupMap =
                ExperienceManager.instance.expGroupMap;
        List<Experience> result = new ArrayList<>();
        for (String groupKey : expGroupMap.keySet()) {
            Map<String, Map<String, Experience>> roomMap = expGroupMap.get(groupKey);
            for (String roomKey : roomMap.keySet()) {
                Map<String, Experience> experienceMap = roomMap.get(roomKey);
                for (String key : experienceMap.keySet()) {
                    Experience experience = experienceMap.get(key);
                    ExpFragmentType type = getFragmentType(experience);
                    if (type == fragmentType) result.add(experience);
                }
            }
        }

        return result;
    }

    /** Return the fragment type associated with the experience given by the experience key. */
    private ExpFragmentType getFragmentType(final Experience experience) {
        ExpType expType = ExpType.values()[experience.getExperienceType().ordinal()];
        for (ExpFragmentType fragmentType : ExpFragmentType.values())
            if (expType == fragmentType.expType) return fragmentType;

        return null;
    }

    /** Return true iff a fragment for the given experience is started. */
    private boolean startNextFragment(final FragmentActivity context,
                                      final Dispatcher<ExpFragmentType, Experience> dispatcher) {
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
    private boolean fragmentExists(final Dispatcher<ExpFragmentType, Experience> dispatcher) {
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
