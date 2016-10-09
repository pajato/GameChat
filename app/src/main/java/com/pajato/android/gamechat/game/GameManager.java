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

package com.pajato.android.gamechat.game;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.main.NetworkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pajato.android.gamechat.game.FragmentType.noExp;
import static com.pajato.android.gamechat.game.FragmentType.offline;
import static com.pajato.android.gamechat.game.FragmentType.signedOut;

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
    private BaseGameFragment[] mFragmentList = new BaseGameFragment[FragmentType.values().length];

    /** The current fragment. */
    private int mCurrentFragment;

    /** The map associating groups, rooms, and experiences. */
    private Map<String, Map<String, List<String>>> mExpMap = new HashMap<>();

    /** A map associating an experience key with a the database model class. */
    private Map<String, Experience> mExperienceMap = new HashMap<>();

    /** The current group key as determined the last selected group. */
    public String currentGroupKey;

    /** The current room key as determined by the last selected room. */
    public String currentRoomKey;

    // Public instance methods.

    /** Return the current fragment's index value. */
    public int getCurrent() {
        return mCurrentFragment;
    }

    /** Return the current fragment being shown in the experience panel. */
    public BaseGameFragment getFragment(final int index) {
        return mFragmentList[index];
    }

    /** Return player 1 or player 2 based on the current turn value. */
    public String getTurn() {
        final int index = getCurrent();
        final FragmentType type = FragmentType.values()[index];
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

    /** Create and show a Snackbar notification based on the given parameters. */
    public void notify(final View view, final String output, final int color, final boolean done) {
        Snackbar notification;
        if (done) {
            // The game is ended so generate a notification that could start a new game.
            notification = Snackbar.make(view, output, Snackbar.LENGTH_LONG);
            final String playAgain = getFragment(getCurrent()).getString(R.string.PlayAgain);
            notification.setAction(playAgain, new NotificationActionHandler());
        } else {
            // The game hasn't ended so generate a notification without an action.
            notification = Snackbar.make(view, output, Snackbar.LENGTH_SHORT);
        }

        // Determine if a color has been specified. If so, set it, otherwise display the
        // notification to the User.
        if (color != -1) notification.getView().setBackgroundColor(color);
        notification.show();
    }

    /** Return true iff a fragment for the given experience is started. */
    public boolean startNextFragment(final FragmentActivity context) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        Dispatcher dispatcher = getDispatcher();
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    /** Return true iff a fragment for the given experience is started. */
    public boolean startNextFragment(final FragmentActivity context, final FragmentType type) {
        // Ensure that the dispatcher has a valid type.  Abort if not. Set up the fragment using the
        // dispatcher if so.
        Dispatcher dispatcher = getDispatcher(type);
        return dispatcher.type != null && startNextFragment(context, dispatcher);
    }

    // Private instance methods.

    /** Return a dispatcher object based on the current experience state. */
    private Dispatcher getDispatcher() {
        // Deal with an off line user, a signed out user, or no experiences at all, in that order.
        // In each case, return an empty dispatcher but for the fragment type of the next screen to
        // show.
        if (!NetworkManager.instance.isConnected()) return new Dispatcher(offline);
        if (!AccountManager.instance.hasAccount()) return new Dispatcher(signedOut);
        if (mExpMap.size() == 0) return new Dispatcher(noExp);

        // Deal with a signed in User with multiple experiences across more than one group.  Return
        // a dispatcher with a map of experience keys.
        if (mExpMap.size() > 1) return new Dispatcher(mExpMap);

        // A signed in user with experiences in more than one room but only one group. Return a
        // dispatcher identifying the group and room.
        String groupKey = mExpMap.keySet().iterator().next();
        Map<String, List<String>> roomMap = mExpMap.get(groupKey);
        if (roomMap.size() > 1) return new Dispatcher(groupKey, roomMap);

        // A signed in user with multiple experiences in a single room.  Return a dispatcher that
        // identifies the group, room and the list of experiences, possibly filtered.
        String roomKey = roomMap.keySet().iterator().next();
        List<String> expList = roomMap.get(roomKey);
        if (expList.size() > 1) return new Dispatcher(groupKey, roomKey, expList);

        // A signed in User with one experience. Return a dispatcher to show the single experience.
        FragmentType fragmentType = getFragmentType(expList.get(0));
        return new Dispatcher(fragmentType, groupKey, roomKey, expList.get(0));
    }

    /** Return a dispatcher object for a given fragment type. */
    private Dispatcher getDispatcher(final FragmentType type) {
        // Case: there are no experiences of the given type. Return an empty dispatcher but for the
        // fragment type.
        List<String> list = getExperiences(type);
        if (list == null || list.size() == 0) return new Dispatcher(type);

        // Case: a signed in user with a single experience. Return a dispatcher with the experience
        // key.
        if (list.size() == 1) return new Dispatcher(type, list.get(0));

        // A signed in user with more than one experience of the given type. Return a dispatcher
        // with the list of relevant experience keys.
        return new Dispatcher(type.showType, list);
    }

    /** Return cached experiences of a given type. */
    private List<String> getExperiences(final FragmentType type) {
        List<String> result = new ArrayList<>();
        for (String expKey : DatabaseListManager.instance.experienceMap.keySet()) {
            Experience experience = DatabaseListManager.instance.experienceMap.get(expKey);
            if (experience.getFragmentType() == type) result.add(expKey);
        }

        return result;
    }

    /** Return the fragment type associated with the experience given by the experience key. */
    private FragmentType getFragmentType(final String expKey) {
        return mExperienceMap.get(expKey).getFragmentType();
    }

    /** Return the string value associated with the two players based on the current turn. */
    private String getTurn(int index, Fragment context, int first, int second) {
        return getFragment(index).getTurn() ? context.getString(first) : context.getString(second);
    }

    /** Return true iff a fragment for the given experience is started. */
    private boolean startNextFragment(final FragmentActivity context, final Dispatcher dispatcher) {
        // Ensure that the fragment exists.  Create it if not.
        int index = dispatcher.type.ordinal();
        if (mFragmentList[index] == null) {
            try {
                mFragmentList[index] = dispatcher.type.fragmentClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Make the next fragment current and initialize it using the context of the fragment
        // currently in the foreground.
        mCurrentFragment = index;
        mFragmentList[index].setupExperience(context, dispatcher);
        context.getSupportFragmentManager().beginTransaction()
            .replace(R.id.game_pane_fragment_container, mFragmentList[index])
            .commit();
        return true;
    }

    // Inner classes.

    /** Provide an inner class to handle a notification action click. */
    private class NotificationActionHandler implements View.OnClickListener {
        @Override public void onClick(final View v) {
            // TODO: figure out what we really want done here.
        }
    }

}
