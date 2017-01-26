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

package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.main.NetworkManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.database.AccountManager.SIGNED_OUT_EXPERIENCE_KEY;
import static com.pajato.android.gamechat.database.AccountManager.SIGNED_OUT_OWNER_ID;
import static com.pajato.android.gamechat.main.NetworkManager.OFFLINE_EXPERIENCE_KEY;
import static com.pajato.android.gamechat.main.NetworkManager.OFFLINE_OWNER_ID;

/**
 * Provide a base class to support fragment lifecycle debugging.  All fragment lifecycle events are
 * handled by providing logcat tracing information.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseExperienceFragment extends BaseFragment {

    // Public class constants.

    /** The mode floating action menu key. */
    public static final String EXP_MODE_FAM_KEY = "expModeFamKey";

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseExperienceFragment.class.getSimpleName();

    /** The lifecycle event format string with no bundle. */
    private static final String FORMAT_NO_BUNDLE =
            "Event: %s; Fragment: %s; Fragment Manager: %s.";

    /** The lifecycle event format string with a bundle provided. */
    private static final String FORMAT_WITH_BUNDLE =
            "Event: %s; Fragment: %s; Fragment Manager: %s; Bundle: %s.";

    // Private instance variables.

    /** The experience being enjoyed. */
    protected Experience mExperience;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseExperienceFragment() {}

    // Public instance methods.

    /** Handle the player 2 control click. */
    @Subscribe public void onClick(final ClickEvent event) {
        switch (event.view.getId()) {
            case R.id.player2Name:
                logEvent("Got a player 2 control click event.");
                // Simulate a click on the exp FAB.
                FabManager.game.toggle(this, EXP_MODE_FAM_KEY);
                break;
            default:
                break;
        }
    }

    /**
     * Provide a default implementation for setting up an experience.  There are two scenarios
     * where an experience fragment needs to be set up.  First, when a User asks to start a game,
     * like tictactoe or checkers, and a game of that type has been cached or needs to be created.
     * Second, when at startup, it is discoovered that there is a single experience to be shown.
     */
    @Override public void onSetup(final Context context, final Dispatcher dispatcher) {
        // Ensure that the dispatcher is valid.  Abort if not.
        // TODO: might be better to show a toast or snackbar on error.
        if (dispatcher == null || dispatcher.type == null || dispatcher.type.expType == null)
            return;

        // At this point there are three choices: 1) the dispatcher contains an experience, 2) the
        // dispatcher contains an experience key, or 3) the dispatcher contains the type of
        // experience which needs to be created using the given context.  experience to use with the
        // fragment being created.
        mExperience = dispatcher.experiencePayload != null
                ? dispatcher.experiencePayload
                : ExperienceManager.instance.experienceMap.get(dispatcher.key);
        if (mExperience == null)
            createExperience(context, getPlayers(dispatcher));
    }

    /** Handle the setup for the mode control. */
    @Override public void onStart() {
        // Provide a loading indicator, enable the options menu, layout the fragment, set up the ad
        // view and the listeners for backend data changes.
        super.onStart();
        FabManager.game.addMenu(EXP_MODE_FAM_KEY, getExpModeFam());
    }

    // Protected instance methods.

    /** Provide a base implementation that will result in no players, i.e. an error. */
    protected List<Account> getPlayers(final Dispatcher dispatcher) {
        return null;
    }

    /** Provide a base implementation that does nothing. */
    protected void createExperience(final Context context, final List<Account> playerAccounts) {
        // Very quietly do nothing.
    }

    /** Return either a null placeholder key value or a sentinel value as the experience key. */
    protected String getExperienceKey() {
        // Determine if there is a signed in account.  If so use the null placeholder.
        if (!AccountManager.instance.hasAccount()) return null;

        // There is no signed in User.  Return one of the two sentinel values associated with being
        // either signed out or without access to a network.
        final boolean ONLINE = NetworkManager.instance.isConnected();
        return ONLINE ? SIGNED_OUT_EXPERIENCE_KEY : OFFLINE_EXPERIENCE_KEY;
    }

        // Return either a signed in User id or a sentinel value as the owner id. */
    protected String getOwnerId() {
        // Determine if there is a signed in account.  If so return it.
        String accountId = AccountManager.instance.getCurrentAccountId();
        if (accountId != null) return accountId;

        // There is no signed in User.  Return one of the two sentinel values associated with being
        // either signed out or without access to a network.
        return NetworkManager.instance.isConnected() ? SIGNED_OUT_OWNER_ID : OFFLINE_OWNER_ID;
    }

    /** Return a name for the player by using the given account or a default. */
    protected String getPlayerName(final Account player, final String defaultName) {
        // Determine if there is an account to use.  Return the default name if not.
        if (player == null) return defaultName;

        // There is an account.  Use the first name for the game.
        return player.getNickName();
    }

    /** Return the account associated with the given index, null if there is no such account. */
    protected Account getPlayer(final List<Account> players, final int index) {
        // Determine if there is such an account, returning null if not.
        if (players == null || index < 0 || index >= players.size()) return null;

        // There is an account so return it.
        return players.get(index);
    }

    /** Return TRUE iff the User has requested to play again. */
    protected boolean isPlayAgain(final Object tag, final String className) {
        // Determine if the given tag is the class name, i.e. a snackbar action request to play
        // again.
        return ((tag instanceof String && className.equals(tag)) ||
                (tag instanceof MenuEntry && ((MenuEntry) tag).titleResId == R.string.PlayAgain));
    }

    /** Log a lifecycle event that has no bundle. */
    @Override protected void logEvent(final String event) {
        String manager = getFragmentManager().toString();
        Log.v(TAG, String.format(Locale.US, FORMAT_NO_BUNDLE, event, this, manager));
    }

    /** Log a lifecycle event that has a bundle. */
    @Override protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        Log.v(TAG, String.format(Locale.US, FORMAT_WITH_BUNDLE, event, this, manager, bundle));
    }

    /** Process the dispatcher to set up the experience fragment. */
    @Override protected boolean onDispatch(@NonNull final Context context,
                                           @NonNull final Dispatcher dispatcher) {
        return true;
    }

    /** Set the name for a given player index. */
    protected void setRoomName(final Experience model) {
        // Ensure that the name text view exists. Abort if not.  Set the value from the model if it
        // does.
        TextView name = (TextView) mLayout.findViewById(R.id.roomName);
        if (name == null) return;
        name.setText(RoomManager.instance.getRoomName(model.getRoomKey()));
    }

    // Private instance methods.

    /** Return the experience mode control FAM. */
    private List<MenuEntry> getExpModeFam() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.PlayModeLocalMenuTitle, R.drawable.ic_local_play_black_24px));
        menu.add(getTintEntry(R.string.PlayModeComputerMenuTitle, R.drawable.ic_smartphone_black_24px));
        menu.add(getTintEntry(R.string.PlayModeUserMenuTitle, R.drawable.ic_person_black_24px));
        return menu;
    }

}
