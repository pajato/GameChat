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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.PlayLocationManager;
import com.pajato.android.gamechat.common.PlayModeManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ExpListChangeEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.PlayLocationChangeEvent;
import com.pajato.android.gamechat.event.PlayModeChangeEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.main.NetworkManager;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.common.FragmentType.checkers;
import static com.pajato.android.gamechat.common.FragmentType.chess;
import static com.pajato.android.gamechat.common.FragmentType.expRoomList;
import static com.pajato.android.gamechat.common.FragmentType.experienceList;
import static com.pajato.android.gamechat.common.FragmentType.selectGroupsRooms;
import static com.pajato.android.gamechat.common.FragmentType.tictactoe;
import static com.pajato.android.gamechat.database.AccountManager.SIGNED_OUT_EXPERIENCE_KEY;
import static com.pajato.android.gamechat.database.AccountManager.SIGNED_OUT_OWNER_ID;
import static com.pajato.android.gamechat.event.BaseChangeEvent.REMOVED;
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
    protected Checkerboard mBoard;

    /** The experience being enjoyed. */
    protected Experience mExperience;

    /** A click handler for the board tiles. */
    protected TileClickHandler mTileClickHandler;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseExperienceFragment() {}

    // Public instance methods.

    /** Create a name for a 2-player experience. */
    public String createTwoPlayerName(List<Player> playerList, long timeStamp) {
        List<Player> players = playerList == null ? mExperience.getPlayers() : playerList;
        long tStamp = timeStamp > 0 ? new Date().getTime() : timeStamp;
        if (players.size() != 2)
            return String.format(Locale.US, "%s game on %s",
                    getString(mExperience.getExperienceType().displayNameResId), tStamp);
        return String.format(Locale.US, "%s vs %s on %s", players.get(0).name, players.get(1).name,
                SimpleDateFormat.getDateTimeInstance().format(tStamp));
    }

    /** Get the experience */
    public Experience getExperience() {
        return mExperience;
    }

    /** Handle a play mode selection change. */
    @Subscribe public void handlePlayModeChange(PlayModeChangeEvent event) {
        if (!mActive || mExperience == null || !(event.view instanceof TextView))
            return;
        String menuItemText = ((TextView) event.view).getText().toString();
        if (menuItemText.equals(getActivity().getString(R.string.PlayModeComputerMenuTitle))) {
            showFutureFeatureMessage(R.string.PlayModeComputerMenuTitle);
            PlayModeManager.instance.closePlayModeMenu();
            return;
        }
        if (menuItemText.equals(getActivity().getString(R.string.PlayModeLocalMenuTitle))) {
            PlayModeManager.instance.closePlayModeMenu();
            return;
        }
        PlayModeManager.instance.handlePlayModeUserSelection(event.view, this);
    }

    /** Handle a play location selection change. */
    @Subscribe public void handlePlayLocationChange(PlayLocationChangeEvent event) {
        if (!mActive || mExperience == null || !(event.view instanceof TextView))
            return;
        PlayLocationManager.instance.handlePlayLocationSelection(event.view, this);
    }

    @Subscribe public void onExperienceListChange(ExpListChangeEvent event) {
        if (mActive)
            updateAdapterList();
    }

    /** Be sure to dismiss the play mode menu, if one is present */
    @Override public void onPause() {
        super.onPause();
        PlayModeManager.instance.dismissPlayModeMenu();
        PlayLocationManager.instance.dismissPlayLocationMenu();
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        super.onResume();
        // If either the type or dispatcher does not exist, abort.
        if (type == null || mDispatcher == null)
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

    /** Initialize ads when necessary and set up the tile click handler for the experience. */
    @Override public void onStart() {
        super.onStart();
        if (mAdView != null && mLayout != null && type != null && type.expType == null)
            initAdView(mLayout);
        mTileClickHandler = new TileClickHandler(getString(R.string.friend), getString(R.string.you));
    }

    // Protected instance methods.

    /** Provide a base implementation that does nothing. */
    protected void createExperience(final Context context, final List<Account> playerAccounts) {
        // Very quietly do nothing.
    }

    /** Return a list of default two-player game players. */
    protected List<Player> getDefaultPlayers(final Context context, final List<Account> players) {
        // TODO: make this part of an interface implementation.
        List<Player> result = new ArrayList<>();
        // Handle the offline case (no players available)
        String name;
        if (players == null)
            name = context.getString(R.string.you);
        else
            name = getPlayerName(getPlayer(players, 0), context.getString(R.string.player1));
        String accountId = null;
        if (players != null && players.size() >= 1)
            accountId = players.get(0).key;
        result.add(new Player(name, "", context.getString(R.string.primaryTeam), accountId));

        if (players == null)
            name = context.getString(R.string.friend);
        else
            name = getPlayerName(getPlayer(players, 1), context.getString(R.string.friend));
        if (players != null && players.size() >= 2)
            accountId = players.get(1).key;
        else
            accountId = null;
        result.add(new Player(name, "", context.getString(R.string.secondaryTeam), accountId));
        return result;
    }

    /** Return a possibly empty list of player information for a two-player game experience. */
    public List<Account> getPlayers(final String roomKey) {
        // TODO: make this an interface implementation...
        // Determine if this is an offline experience in which no accounts are provided.
        Account player1 = AccountManager.instance.getCurrentAccount();
        if (player1 == null)
            return null;

        // This is an online experience.  Use the current signed in User as the first player and
        // determine from the play mode how to get the second player.
        List<Account> players = new ArrayList<>();
        players.add(player1);
        Room room = roomKey != null ? RoomManager.instance.roomMap.get(roomKey) : null;
        if (room == null || roomKey.equals(AccountManager.instance.getMeRoomKey()))
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

    /** Process a click event on the given view for an experience fragment. */
    protected void processClickEvent(final View view, final FragmentType type) {
        // Grab the View ID and the floating action button and dimmer views.
        logEvent(String.format("onClick: (%s) with event {%s};", type.name(), view));
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
            case R.drawable.ic_checkers_black_24dp:
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
            case R.id.roomName:
                PlayLocationManager.instance.togglePlayLocationMenu(getActivity(), view);
                break;
            case R.id.player2Name:
                PlayModeManager.instance.togglePlayModeMenu(getActivity(), view);
                break;
            default:
                // Determine if the button click was generated by a group view or room view drill
                // down.  Handle it by adding the next fragment to the back stack.
                processPayload(view);
                break;
        }

        // Chain to the game experience, if one was found and if the user is currently signed in.
        if (expFragmentType != null) {
            if (AccountManager.instance.hasAccount()) {
                DispatchManager.instance.dispatchToGame(this, expFragmentType);
            } else {
                showFutureFeatureMessage(R.string.signedOutGames);
            }
        }
    }

    /**
     * Process an experience change event. If this is an inactive fragment, the event has an
     * empty experience, is a different type of experience, or the specified experience is NOT the
     * experience of this type that we are currently viewing, abort. Otherwise resume the experience
     * and update the UI to reflect the changes in the event.
     */
    protected void processExperienceChange(@NonNull final ExperienceChangeEvent event) {
        if (event.changeType == REMOVED)
            return;
        ExpType expType = event.experience != null ? event.experience.getExperienceType() : null;
        if (!mActive || expType == null || expType != type.expType ||
                !mExperience.getExperienceKey().equals(event.experience.getExperienceKey()))
            return;
        logEvent("experienceChange");
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
            case R.string.InviteFriendMessage:
                // If not on a tablet, make sure that we switch to the chat perspective
                if (!PaneManager.instance.isTablet()) {
                    ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                    if (viewPager != null) viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                }
                String groupKey = mExperience.getGroupKey();
                if (isInMeGroup())
                    DispatchManager.instance.dispatchToFragment(this, selectGroupsRooms, this.type);
                else
                    InvitationManager.instance.extendGroupInvitation(activity, groupKey);
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
                DispatchManager.instance.dispatchToFragment(this, expRoomList, null, item);
                break;
            case expList:
                Experience exp = ExperienceManager.instance.getExperience(item.experienceKey);
                type = exp != null ? exp.getExperienceType().getFragmentType() : null;
                DispatchManager.instance.dispatchToFragment(this, type, null, item);
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
                DispatchManager.instance.dispatchToFragment(this, type, null, item);
                break;
            default:
                break;
        }
    }

    /** Process a tag click event on a given view by logging the event and handling the payload. */
    protected void processTagClickEvent(final TagClickEvent event, final FragmentType type) {
        // Determine if this event is for this fragment.  Abort if not, otherwise process a FAM
        // entry click.
        logEvent(String.format("onClick: (%s) with event {%s};", type, event.view));
        if (!mActive)
            return;
        Object tag = event.view.getTag();
        if (tag instanceof MenuEntry)
            processFamItem((MenuEntry) tag);
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
    private void processFamItem(final MenuEntry entry) {
        // Dismiss the FAB and dispatch
        FabManager.game.dismissMenu(this);
        DispatchManager.instance.dispatchToGame(this, entry.fragmentType);
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
        final Experience exp = ExperienceManager.instance.getExperience(item.experienceKey);
        if (exp == null)
            return;
        String message = String.format(getString(R.string.DeleteConfirmMessage), exp.getName());
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int id) {
                ExperienceManager.instance.deleteExperience(item);
            }
        };
        showAlertDialog(getString(R.string.DeleteExperienceTitle), message, null, okListener);
    }

    /** Return the fragment type corresponding to the sole experience in the map. */
    private FragmentType getType(@NonNull final Map<String, Experience> map, final ListItem item) {
        // Extract the experience from the map and add the key to the item.
        Experience experience = map.values().iterator().next();
        item.experienceKey = experience.getExperienceKey();
        return experience.getExperienceType().getFragmentType();
    }
}
