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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.PlayModeManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.PlayModeChangeEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.main.NetworkManager;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.common.FragmentType.checkers;
import static com.pajato.android.gamechat.common.FragmentType.chess;
import static com.pajato.android.gamechat.common.FragmentType.expGroupList;
import static com.pajato.android.gamechat.common.FragmentType.expRoomList;
import static com.pajato.android.gamechat.common.FragmentType.experienceList;
import static com.pajato.android.gamechat.common.FragmentType.selectExpGroupsRooms;
import static com.pajato.android.gamechat.common.FragmentType.selectUser;
import static com.pajato.android.gamechat.common.FragmentType.tictactoe;
import static com.pajato.android.gamechat.common.PlayModeManager.PlayModeType.user;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.expList;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.expRoom;
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

    /** Visual layout of chess board objects. */
    protected Checkerboard mBoard = new Checkerboard();

    /** The experience being enjoyed. */
    protected Experience mExperience;

    /** A click handler for the board tiles. */
    protected TileClickHandler mTileClickHandler = new TileClickHandler();

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseExperienceFragment() {}

    // Public instance methods.

    /** Be sure to dismiss the play mode menu, if one is present */
    @Override public void onPause() {
        super.onPause();
        PlayModeManager.instance.dismissPlayModeMenu();
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        // Ensure that the type exists and that this is not a pass-through fragment.  In either
        // case abort.
        super.onResume();
        if (type == null || mDispatcher == null || mDispatcher.expType != null)
            return;

        // Initialize the FAB manager, set up ads and the list adapter.
        FabManager.game.init(this);
        if (mAdView != null)
            mAdView.resume();
        switch (type) {
            case expGroupList:
            case expRoomList:
            case experienceList: // Update the state of the list adapter.
                updateAdapterList();
                break;
            default:        // Ignore all other fragments.
                break;
        }
    }

    /** Handle an experience fragment setup by establishing the experience to run. */
    @Override public void onSetup(final Context context, final Dispatcher dispatcher) {
        // Ensure that the dispatcher and the dispatcher type exist, and ensure that this setup
        // is for a true experience fragment.  If not, then abort, otherwise use the dispatcher
        // to establish the experience to run.
        super.onSetup(context, dispatcher);
        if (dispatcher == null || dispatcher.type == null || dispatcher.type.expType == null)
            return;
        String groupKey = dispatcher.groupKey;
        String roomKey = dispatcher.roomKey;
        ExpType expType = dispatcher.expType != null ? dispatcher.expType : type.expType;
        mExperience = ExperienceManager.instance.getExperience(groupKey, roomKey, expType);
        if (mExperience == null)
            createExperience(context, getPlayers(dispatcher));
    }

    /** Handle ad setup for experience fragments. */
    @Override public void onStart() {
        // Ensure that this is not a pass-through fragment.  If not, then initialize ads.
        super.onStart();
        if (mAdView != null && mLayout != null && type != null && type.expType == null)
            initAdView(mLayout);
    }

    // Protected instance methods.

    /** Return a list of default two-player game players. */
    protected List<Player> getDefaultPlayers(final Context context, final List<Account> players) {
        // TODO: make this part of an interface implementation.
        List<Player> result = new ArrayList<>();
        String name = getPlayerName(getPlayer(players, 0), context.getString(R.string.player1));
        String team = context.getString(R.string.primaryTeam);
        result.add(new Player(name, "", team));
        name = getPlayerName(getPlayer(players, 1), context.getString(R.string.friend));
        team = context.getString(R.string.secondaryTeam);
        result.add(new Player(name, "", team));
        return result;
    }

    /** Return a possibly empty list of player information for a two-player game experience. */
    protected List<Account> getPlayers(final Dispatcher dispatcher) {
        // TODO: make this an interface implementation...
        // Determine if this is an offline experience in which no accounts are provided.
        Account player1 = AccountManager.instance.getCurrentAccount();
        if (player1 == null)
            return null;

        // This is an online experience.  Use the current signed in User as the first player and
        // determine from the play mode how to get the second player.
        List<Account> players = new ArrayList<>();
        players.add(player1);
        String key = dispatcher.roomKey;
        Room room = key != null ? RoomManager.instance.roomMap.get(key) : null;
        if (room == null || key.equals(AccountManager.instance.getMeRoomKey()))
            return players;

        // Obtain the second player from the other room...
        switch (type) {
            // TODO: flesh this out...
            //case MEMBER:
            // Handle another User by providing their account.
            //    break;
            default:
                // Only one online player.  Just return.
                break;
        }
        return players;
    }

    /** Provide a base implementation that does nothing. */
    protected void createExperience(final Context context, final List<Account> playerAccounts) {
        // Very quietly do nothing.
    }

    /** Return either a null placeholder key value or a sentinel value as the experience key. */
    protected String getExperienceKey() {
        // Determine if there is a signed in account.  If so use the null placeholder, otherwise
        // return one of the two sentinel values associated with being either signed out or having
        // no network.
        if (AccountManager.instance.hasAccount())
            return null;
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

    /** Handle a play mode selection change. */
    @Subscribe void handlePlayModeChange(PlayModeChangeEvent event) {
        if (mExperience == null)
            return;
        switch (event.type) {
            case computer:
            case local:
                showFutureFeatureMessage(R.string.FutureSelectModes);
                break;
            case user:
                // Handle selecting another User by chaining to the fragment that will select the
                // User, copy the experience to a new room, and continue the game in that room with
                // the current state.
                ListItem listItem = new ListItem(mExperience, user);
                DispatchManager.instance.chainFragment(this.getActivity(), selectUser, listItem);
                break;
            default:
                break;
        }
    }

    /**
     * Return TRUE if this experience is in the "me" group. If either the 'me' group key or the
     * current experience group key is null, return true (assume we're in the 'me' situation).
     */
    protected boolean isInMeGroup() {
        String meGroupKey = AccountManager.instance.getMeGroupKey();
        if (meGroupKey == null) {
            Log.e(TAG, "The 'me' groupKey is null for experience " + mExperience.getName());
            return true;
        }
        if (mExperience == null || mExperience.getGroupKey() == null) {
            Log.e(TAG, "This experience is null or the groupKey is null ");
            return true;
        }
        return meGroupKey.equals(mExperience.getGroupKey());
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
    @Override protected void onDispatch(@NonNull final Context context) {
        // Ensure that the type is valid and that the fragment is not being used as a
        // pass-through.  Abort if either is not the case, otherwise handle each possible case.
        if (mDispatcher.type == null || mDispatcher.expType != null)
            return;
        switch (type) {
            case expRoomList:   // A room list needs an item.
                mItem = new ListItem(expRoom, mDispatcher.groupKey);
                break;
            case experienceList:
                // The experiences in a room require both the group and room keys.  Determine if the
                // group is the me group and give it special handling.
                String groupKey = mDispatcher.groupKey;
                String meGroupKey = AccountManager.instance.getMeGroupKey();
                String roomKey = meGroupKey != null && meGroupKey.equals(groupKey)
                        ? AccountManager.instance.getMeRoomKey() : mDispatcher.roomKey;
                mItem = new ListItem(expList, groupKey, roomKey, null, 0, null);
                break;
            default:
                break;
        }
    }

    /** Process a click event on the given view for an experience fragment. */
    protected void processClickEvent(final View view, final String tag) {
        // Grab the View ID and the floating action button and dimmer views.
        logEvent(String.format("onClick: (%s) with event {%s};", tag, view));
        FragmentType expFragmentType = null;
        switch (view.getId()) {
            case R.id.IconTicTacToe:
                expFragmentType = tictactoe;
                break;
            case R.id.IconCheckers:
                expFragmentType = checkers;
                break;
            case R.id.IconChess:
                expFragmentType = chess;
                break;
            case R.drawable.ic_casino_black_24dp:
                // And do it for the rooms option buttons.
                showFutureFeatureMessage(R.string.FutureSelectRooms);
                FabManager.game.dismissMenu(this);
                break;
            case R.id.endIcon:
                // Click on the end item icon
                processEndIconClick(view);
                break;
            case R.id.gameFab:
                // Click on the FAB: for games, start a new game. Otherwise, handle open or close.
                switch (this.type) {
                    case chess:
                    case checkers:
                    case tictactoe:
                        ExpHelper.handleNewGame(this.type.name(), mExperience);
                        return; // Return here to avoid the dispatch below...
                    default:
                        FabManager.game.toggle(this);
                        break;
                }
                break;
            case R.id.player2Name:
                logEvent("Got a player 2 control click event.");
                PlayModeManager.instance.showPlayModeMenu(view);
                break;
            default:
                // Determine if the button click was generated by a group view or room view drill
                // down.  Handle it by adding the next fragment to the back stack.
                processPayload(view);
                break;
        }

        if (expFragmentType != null)
            DispatchManager.instance.chainFragment(getActivity(), expFragmentType);
    }

    /** Process an experience change event by ... */
    protected void processExperienceChange(@NonNull final ExperienceChangeEvent event) {
        // Determine of this is an inactive fragment, the event has an empty experience or a
        // different type of experience . If so, abort, otherwise continue processing the event
        // experience.
        ExpType expType = event.experience != null ? event.experience.getExperienceType() : null;
        if (!mActive || expType == null || expType != type.expType)
            return;

        // Determine if this experience is waiting to be initialized.  If so, do it using the
        // experience from the event.  If not, just continue to initialize the engine and start
        // updating the UI from the data model.
        logEvent("experienceChange");
        if (mExperience == null)
            mExperience = event.experience;
        resumeExperience();
    }

    /** Handle a menu item click from the toolbar or overflow menu. */
    protected void processMenuItemEvent(final MenuItemEvent event) {
        if (!this.mActive)
            return;
        // Case on the item resource id if there is one to be had.
        FragmentActivity activity = getActivity();
        switch (event.item != null ? event.item.getItemId() : -1) {
            case R.string.InviteFriendsOverflow:
                String groupKey = mExperience.getGroupKey();
                if (isInMeGroup())
                    DispatchManager.instance.chainFragment(activity, selectExpGroupsRooms, null);
                else
                    InvitationManager.instance.extendGroupInvitation(activity, groupKey);
                break;
            case R.string.SwitchToChat:
                // If the toolbar chat icon is clicked, on smart phone devices we can change panes.
                ViewPager viewPager = (ViewPager) activity.findViewById(R.id.viewpager);
                if (viewPager != null)
                    viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                break;
            default:
                break;
        }
    }

    /** Process a button click that may be a experience list item click. */
    protected void processPayload(final View view) {
        // Ensure that the payload is valid.  Abort if not, otherwise chain to the next appropriate
        // fragment based on the type associated with the payload.
        Object payload = view.getTag();
        if (!mActive || !(payload instanceof ListItem))
            return;
        ListItem item = (ListItem) payload;
        FragmentType type;
        switch (item.type) {
            case expGroup: // Drill into the rooms in group.
                DispatchManager.instance.chainFragment(getActivity(), expRoomList, item);
                break;
            case expList:
                Experience exp = ExperienceManager.instance.experienceMap.get(item.key);
                type = exp != null ? exp.getExperienceType().getFragmentType() : null;
                DispatchManager.instance.chainFragment(getActivity(), type, item);
                break;
            case expRoom: // Show the list of experiences in a room or the one experience.
                Map<String, Map<String, Experience>> groupMap;
                Map<String, Experience> roomMap;
                String key = item.groupKey;
                groupMap = key != null ? ExperienceManager.instance.expGroupMap.get(key) : null;
                key = groupMap != null && item.roomKey != null ? item.roomKey : null;
                roomMap = key != null ? groupMap.get(key) : null;
                if (roomMap == null || roomMap.size() == 0)
                    return;
                type = roomMap.size() > 1 ? experienceList : getType(roomMap, item);
                DispatchManager.instance.chainFragment(getActivity(), type, item);
                break;
            default:
                break;
        }
    }

    /** Process a tag click event on a given view by logging the event and handling the payload. */
    protected void processTagClickEvent(final TagClickEvent event, final String name) {
        // Determine if this event is for this fragment.  Abort if not, otherwise process a FAM
        // entry click.
        logEvent(String.format("onClick: (%s) with event {%s};", name, event.view));
        if (!mActive)
            return;
        Object tag = event.view.getTag();
        if (tag instanceof MenuEntry)
            processFamItem((MenuEntry) tag, name);
    }

    // Private instance methods.

    /** Process the end icon click */
    private void processEndIconClick(final View view) {
        if (!(view.getTag() instanceof ListItem))
            return;
        ListItem item = (ListItem) view.getTag();
        switch (item.type) {
            case expList:
                verifyDeleteExperience(item);
                break;
            default:
                break;
        }
    }

    /** Process a FAM menu entry click. */
    private void processFamItem(final MenuEntry entry, final String name) {
        // Dismiss the FAB and dispatch
        FabManager.game.dismissMenu(this);
        switch(entry.titleResId) {
            default: // Dispatch to the game fragment ensuring chaining is coherent.
                FragmentActivity activity = getActivity();
                boolean doChain;
                doChain = type == expGroupList || type == expRoomList || type == experienceList;
                FragmentType nextType = doChain ? entry.fragmentType : expGroupList;
                Dispatcher dispatcher = new Dispatcher(nextType, entry.fragmentType.expType);
                if (doChain)
                    DispatchManager.instance.chainFragment(getActivity(), dispatcher);
                else {
                    DispatchManager.instance.startNextFragment(activity, dispatcher);
                }
                break;
        }
    }

    /** Resume the current fragment experience. */
    protected void resumeExperience() {
        Engine engine = mExperience.getExperienceType().getEngine();
        if (engine == null)
            return;
        engine.init(mExperience, mBoard, mTileClickHandler);
        ExpHelper.updateUiFromExperience(mExperience, mBoard);
    }

    private void verifyDeleteExperience(final ListItem item) {
        final Experience exp = ExperienceManager.instance.experienceMap.get(item.key);
        if (exp == null)
            return;
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.DeleteExperienceTitle))
                .setMessage(String.format(getString(R.string.DeleteConfirmMessage),
                        exp.getName()))
                .setNegativeButton(android.R.string.cancel, null) // dismiss
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int id) {
                                ExperienceManager.instance.deleteExperience(item);
                            }
                        })
                .create()
                .show();
    }

    /** Return the fragment type corresponding to the sole experience in the map. */
    private FragmentType getType(@NonNull final Map<String, Experience> map, final ListItem item) {
        // Extract the experience from the map and add the key to the item.
        Experience experience = map.values().iterator().next();
        item.key = experience.getExperienceKey();
        return experience.getExperienceType().getFragmentType();
    }
}
