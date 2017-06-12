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

package com.pajato.android.gamechat.exp.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.exp.ExpHelper;
import com.pajato.android.gamechat.exp.NotificationManager;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.exp.model.TTTBoard;
import com.pajato.android.gamechat.exp.model.TicTacToe;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.selectGroupsRooms;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.common.model.JoinState.JoinType.exp;
import static com.pajato.android.gamechat.event.BaseChangeEvent.REMOVED;
import static com.pajato.android.gamechat.exp.ExpHelper.getBaseFragment;
import static com.pajato.android.gamechat.exp.ExpType.tttET;
import static com.pajato.android.gamechat.exp.model.TTTBoard.BEG_COL;
import static com.pajato.android.gamechat.exp.model.TTTBoard.BOT_ROW;
import static com.pajato.android.gamechat.exp.model.TTTBoard.END_COL;
import static com.pajato.android.gamechat.exp.model.TTTBoard.LEFT_DIAG;
import static com.pajato.android.gamechat.exp.model.TTTBoard.MID_COL;
import static com.pajato.android.gamechat.exp.model.TTTBoard.MID_ROW;
import static com.pajato.android.gamechat.exp.model.TTTBoard.RIGHT_DIAG;
import static com.pajato.android.gamechat.exp.model.TTTBoard.TOP_ROW;
import static com.pajato.android.gamechat.exp.model.TicTacToe.ACTIVE;
import static com.pajato.android.gamechat.main.NetworkManager.OFFLINE_EXPERIENCE_KEY;

/**
 * A Tic-Tac-Toe game that stores its current state on Firebase, allowing for cross-device play.
 *
 * TODO: Abstract out a player: name, win count, association (X/O/black/white/etc.), type (creator,
 * online, offline, gamechat, other?)
 *
 * @author Bryan Scott (original code)
 * @author Paul Michael Reilly (extensive revisions)
 */
public class TTTFragment extends BaseExperienceFragment implements View.OnClickListener {

    // Public constants.

    /** The lookup key for the FAB tictactoe menu. */
    public static final String TIC_TAC_TOE_FAM_KEY = "TicTacToeFamKey";

    // Private constants.

    /** The logcat TAG. */
    private static final String TAG = TTTFragment.class.getSimpleName();

    // Public instance methods.

    /** Satisfy base class */
    public List<ListItem> getList() {
        return null;
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        return GroupManager.instance.getGroupName(mDispatcher.groupKey);
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        if (mExperience == null)
            mExperience = ExperienceManager.instance.getExperience(mDispatcher.key);
        return mExperience.getName();
    }

    /** Handle a button click event by delegating the event to the base class. */
    @Subscribe public void onClick(final ClickEvent event) {
        processClickEvent(event.view, this.type);
    }

    /** Handle a FAM or Snackbar TicTacToe click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Determine if this fragment is active, i.e. is running in the foreground.  Abort if not,
        // otherwise process the event, which has been initiated by a FAM menu item.  It is either
        // a snackbar action (start a new game) or a menu (FAM or Player2) entry.  Detect and
        // handle a snackbar action first.
        if (!mActive)
            return;
        Object tag = event.view.getTag();
        FabManager.game.dismissMenu(this);

        // The event is either a snackbar action (start a new game) or a menu (FAM or Player2)
        // entry.  Detect and handle start a new game first.
        if (ExpHelper.isPlayAgain(tag, TAG))
            handleNewGame();
    }

    /** Handle a TTT board tile click. */
    @Override public void onClick(final View view) {
        Object tag = view.getTag();
        if (tag instanceof String && ((String) tag).startsWith("button"))
            handleClick((String) tag);
    }

    /** Handle a menu item selection. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        if (!this.mActive)
            return;
        // Case on the item resource id if there is one to be had.
        switch (event.item != null ? event.item.getItemId() : -1) {
            case R.string.InviteFriendMessage:
                // If not on a tablet, make sure that we switch to the chat perspective
                if (!PaneManager.instance.isTablet()) {
                    ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                    if (viewPager != null)
                        viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                }
                if (isInMeGroup())
                    DispatchManager.instance.dispatchToFragment(this, selectGroupsRooms, this.type);
                else
                    InvitationManager.instance.extendGroupInvitation(getActivity(),
                            mExperience.getGroupKey());
                break;
            default:
                break;
        }
    }

    /**
     * Handle an experience change event. If this is an inactive fragment, the event has an empty
     * experience, is not a tic-tac-toe experience, or the specified experience is NOT the
     * tic-tac-toe experience that we are currently viewing, abort. Otherwise resume the experience
     * and update the UI to reflect the changes in the event.
     */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Check the payload to see if this is not tictactoe.  Abort if not.
        if (event.changeType == REMOVED)
            return;
        if (!mActive || event.experience == null || event.experience.getExperienceType() != tttET)
            return;
        if (!mExperience.getExperienceKey().equals(event.experience.getExperienceKey()))
            return;
        logEvent("tictactoe experienceChange");
        mExperience = event.experience;
        resume();
    }

    /** Deal with the fragment's lifecycle by marking the join inactive. */
    @Override public void onPause() {
        super.onPause();
        if (mExperience != null)
            clearJoinState(mExperience.getGroupKey(), mExperience.getRoomKey(), exp);
    }

    /** Handle taking the foreground by updating the UI based on the current experience. */
    @Override public void onResume() {
        // Determine if there is an experience ready to be enjoyed.  If not, hide the layout and
        // present a spinner.  When an experience is posted by the app event manager, the game can
        // be shown
        super.onResume();
        setJoinState(mExperience.getGroupKey(), mExperience.getRoomKey(), exp);
        resume();
    }

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        // The experiences in a room require both the group and room keys.  Determine if the
        // group is the me group and give it special handling.
        String meGroupKey = AccountManager.instance.getMeGroupKey();
        dispatcher.roomKey = meGroupKey != null && meGroupKey.equals(dispatcher.groupKey)
                ? AccountManager.instance.getMeRoomKey() : dispatcher.roomKey;
        if (dispatcher.roomKey == null) {
            Log.e(TAG, "Got to onSetup without a room key - we may be offline");
        }
        mExperience = ExperienceManager.instance.getExperience(dispatcher.groupKey, dispatcher.roomKey, tttET);
        if (mExperience == null)
            createExperience(context, getPlayers(dispatcher.roomKey));
        mDispatcher = dispatcher;
    }

    /** Initialize by setting up tile click handlers on the board. */
    @Override public void onStart() {
        // Initialize the FAB/FAM and the toolbar.
        super.onStart();
        FabManager.game.setMenu(TIC_TAC_TOE_FAM_KEY, getTTTMenu());
        FabManager.game.init(this);
        ToolbarManager.instance.init(this, helpAndFeedback, settings, chat, invite);

        // Place a click listener on each button in the grid.
        final String format = "Invalid tag found on button with tag {%s}";
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                String tag = String.format(Locale.US, "button%s%s", i, j);
                View view = mLayout.findViewWithTag(tag);
                if (view != null)
                    view.setOnClickListener(this);
                else
                    Log.e(TAG, String.format(Locale.US, format, tag));
            }
    }

    // Protected instance methods.

    /** Create a default, partially populated, TicTacToe experience. */
    protected void createExperience(final Context context, final List<Account> playerAccounts) {
        // Setup the default key, players, and name.
        String key = getExperienceKey();
        List<Player> players = getDefaultPlayers(context, playerAccounts);
        long tStamp = new Date().getTime();
        String name = createTwoPlayerName(players, tStamp);

        // Set up the default group (Me Group) and room (Me Room) keys, the owner id and create the
        // object on the database.
        String groupKey = AccountManager.instance.getMeGroupKey();
        String roomKey = AccountManager.instance.getMeRoomKey();
        String id = getOwnerId();
        TicTacToe model = new TicTacToe(key, id, name, tStamp, groupKey, roomKey, players);
        mExperience = model;
        if (groupKey != null && roomKey != null)
            ExperienceManager.instance.createExperience(model);
    }

    /** Return a list of default TicTacToe players. */
    protected List<Player> getDefaultPlayers(final Context context, final List<Account> players) {
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
        result.add(new Player(name, context.getString(R.string.xValue), "", accountId));

        if (players == null)
            name = context.getString(R.string.friend);
        else
            name = getPlayerName(getPlayer(players, 1), context.getString(R.string.friend));
        if (players != null && players.size() >= 2)
            accountId = players.get(1).key;
        else
            accountId = null;
        result.add(new Player(name, context.getString(R.string.oValue), "", accountId));
        return result;
    }

    /** Return a possibly null list of player information for a two participant experience. */
    @Override public List<Account> getPlayers(final String roomKey) {
        // Determine if this is an offline experience in which no accounts are provided.
        Account player1 = AccountManager.instance.getCurrentAccount();
        if (player1 == null) return null;

        // This is an online experience.  Use the current signed in User as the first player.
        List<Account> players = new ArrayList<>();
        players.add(player1);

        // Determine the second account, if any, based on the room.
        Room room = roomKey != null ? RoomManager.instance.roomMap.get(roomKey) : null;
        if (room == null) return players;

        switch (type) {
            //case MEMBER:
            // Handle another User by providing their account.
            //    break;
            default:
                // Only one online player.  Just return.
                break;
        }

        return players;
    }

    // Private instance methods.

    /** Return a done message text to show in a snackbar.  The given model provides the state. */
    private String getDoneMessage(final TicTacToe model) {
        // Determine if there is a winner.  If not, return the "tie" message.
        String name = model.getWinningPlayerName();
        if (name == null) return getString(R.string.TieMessageNotification);

        // There was a winner.  Return a congratulatory message.
        String format = getString(R.string.WinMessageNotificationFormat);
        return String.format(Locale.getDefault(), format, name);
    }

    /** Return the TicTacToe model class, null if it does not exist. */
    private TicTacToe getModel() {
        if (mExperience == null || !(mExperience instanceof TicTacToe)) return null;
        return (TicTacToe) mExperience;
    }

    /** Return the game state after applying the given button move to the data model. */
    private int getState(@NonNull final TicTacToe model, String buttonTag) {
        // Check to see if the game is a tie, with all moves exhausted.
        if (model.state == ACTIVE && model.board.grid.size() == 9) return TicTacToe.TIE;

        // Not a tie.  Determine the winner state based on the current play given by the button tag.
        int value = model.getSymbolValue();
        if (model.state == ACTIVE) return getState(model, value, buttonTag);

        return model.state;
    }

    /** Return the game state after applying the given button move to the data model. */
    private int getState(@NonNull final TicTacToe model, final int value, final String buttonTag) {
        // Case on the button tag to update the appropriate tallies.
        switch (buttonTag) {
            case "button00": return getState(model, value, TOP_ROW, BEG_COL, LEFT_DIAG);
            case "button01": return getState(model, value, TOP_ROW, MID_COL);
            case "button02": return getState(model, value, TOP_ROW, END_COL, RIGHT_DIAG);
            case "button10": return getState(model, value, MID_ROW, BEG_COL);
            case "button11": return getState(model, value, MID_ROW, MID_COL, LEFT_DIAG, RIGHT_DIAG);
            case "button12": return getState(model, value, MID_ROW, END_COL);
            case "button20": return getState(model, value, BOT_ROW, BEG_COL, RIGHT_DIAG);
            case "button21": return getState(model, value, BOT_ROW, MID_COL);
            case "button22": return getState(model, value, BOT_ROW, END_COL, LEFT_DIAG);
            default: break;
        }

        return model.state;
    }

    /** Return the game state after updating the given tallies. */
    private int getState(@NonNull final TicTacToe model, final int value, final String... tallyKeys) {
        // Ensure that the tallies map exists, creating it if it doesn't and apply the value to each
        // of the keys and return the state.
        final int X_WIN_VALUE = 3;
        final int O_WIN_VALUE = 12;
        if (model.board.tallies == null) model.board.tallies = new HashMap<>();
        for (String key : tallyKeys) {
            // Ensure that the entry exists.
            int tally = model.board.tallies.containsKey(key) ? model.board.tallies.get(key) : 0;
            tally += value;
            model.board.tallies.put(key, tally);
            if (tally == X_WIN_VALUE) return TicTacToe.X_WINS;
            if (tally == O_WIN_VALUE) return TicTacToe.O_WINS;
        }

        return ACTIVE;
    }

    /** Return the FAM menu (empty) - the FAB operates as a button here. */
    private List<MenuEntry> getTTTMenu() {
        return new ArrayList<>();
    }

    /** Handle a click on a given tile by updating the value on the tile and start the next turn. */
    private void handleClick(final String buttonTag) {
        // Ensure that the click occurred on a grid button and that the data model is not empty.
        // Abort if not.
        View view = mLayout.findViewWithTag(buttonTag);
        TicTacToe model = getModel();
        if (view == null || !(view instanceof Button) || model == null)
            return;

        // Handle the button click based on the current state.
        Button button = (Button) view;
        switch (model.state) {
            case ACTIVE:
                // Ensure that the click occurred on an empty button.
                if (button.getText().length() != 0) {
                    // The click occurred on a played button.  Warn the User and ignore the play.
                    NotificationManager.instance.notify(this, R.string.InvalidButton);
                    return;
                }
                break;
            default:
                // In all other cases, clear the board to start a new game.
                initBoard(model);
                model.board = null;
                model.state = ACTIVE;
                NotificationManager.instance.notify(this, R.string.StartNewGame);
                break;
        }

        // Update the database with the collected changes.
        if (model.board == null) model.board = new TTTBoard();
        model.board.grid.put(buttonTag, model.getSymbolText());
        model.state = getState(model, buttonTag);
        model.setWinCount();
        model.toggleTurn();
        ExpHelper.updateModel(mExperience);
    }

    /** Handle a new game by resetting the data model. */
    private void handleNewGame() {
        // Ensure that the data model exists and is valid.
        TicTacToe model = getModel();
        if (model == null) {
            Log.e(TAG, "Null TTT data model.", new Throwable());
            return;
        }

        // Reset the data model, update the database and clear the notification manager one-shot.
        model.board = null;
        model.state = ACTIVE;
        ExpHelper.updateModel(mExperience);
    }

    /** Initialize the board model and values and clear the winner text. */
    private void initBoard(@NonNull final TicTacToe model) {
        // Ensure that the layout has been established. Abort if not.
        if (mLayout == null) return;

        // Clear the board, the winner text, the board evaluation support and all current X's and
        // O's.
        if (model.board != null) model.board.clear();
        TextView winner = (TextView) mLayout.findViewById(R.id.status);
        if (winner != null) winner.setText("");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // Seed the array with a value that will guarantee the X vs O win and a tie will be
                // calculated correctly; clear the grid button.
                String tag = String.format(Locale.US, "button%d%d", i, j);
                TextView button = (TextView) mLayout.findViewWithTag(tag);
                if (button != null) button.setText("");
            }
        }
    }

    /** Process a resumption by testing and waiting for the experience. */
    private void resume() {
        if (mExperience == null) {
            // Disable the layout and startup the spinner.
            mLayout.setVisibility(View.GONE);
        } else {
            // Start the game and update the views using the current state of the experience.
            mLayout.setVisibility(View.VISIBLE);
            updateUiFromExperience();
        }
    }

    /** Handle the turn indicator management by manipulating the turn icon size and decorations. */
    private void setPlayerIcons(final boolean turn) {
        // Alternate the decorations on each player symbol.
        if (turn)
            // Make player1's decorations the more prominent.
            setPlayerIcons(R.id.player1Symbol, R.id.leftIndicator1, R.id.rightIndicator1,
                           R.id.player2Symbol, R.id.leftIndicator2, R.id.rightIndicator2);
        else
            // Make player2's decorations the more prominent.
            setPlayerIcons(R.id.player2Symbol, R.id.leftIndicator2, R.id.rightIndicator2,
                           R.id.player1Symbol, R.id.leftIndicator1, R.id.rightIndicator1);
    }

    /** Manage a particular player's symbol decorations. */
    private void setPlayerIcons(final int large, final int largeLeft, final int largeRight,
                                final int small, final int smallLeft, final int smallRight) {
        final float LARGE = 60.0f;
        final float SMALL = 45.0f;

        // Collect all the pertinent textViews.
        TextView tvLarge = (TextView) getActivity().findViewById(large);
        TextView tvLargeLeft = (TextView) getActivity().findViewById(largeLeft);
        TextView tvLargeRight = (TextView) getActivity().findViewById(largeRight);
        TextView tvSmall = (TextView) getActivity().findViewById(small);
        TextView tvSmallLeft = (TextView) getActivity().findViewById(smallLeft);
        TextView tvSmallRight = (TextView) getActivity().findViewById(smallRight);

        // Deal with the tvLarger symbol's decorations.
        tvLarge.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE);
        tvLarge.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        tvLargeLeft.setVisibility(View.VISIBLE);
        tvLargeRight.setVisibility(View.VISIBLE);

        // Deal with the tvSmall symbol's decorations.
        tvSmall.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL);
        tvSmall.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        tvSmallLeft.setVisibility(View.INVISIBLE);
        tvSmallRight.setVisibility(View.INVISIBLE);
    }

    /** Set the name for a given player index. */
    private void setPlayerName(final int resId, final int index, final TicTacToe model) {
        // If the name text view exists, set the value from the model, otherwise abort.
        TextView name = (TextView) mLayout.findViewById(resId);
        if (name == null)
            return;
        name.setText(model.players.get(index).name);
        // If a user is assigned, don't allow click (no popup menu) and remove down-arrow drawable
        // used on player-2. But don't do anything if we're playing offline.
        if (model.key.equals(OFFLINE_EXPERIENCE_KEY))
            return;
        if (model.players.get(index).id != null && !model.players.get(index).id.equals("")) {
            name.setClickable(false);
            name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            name.setClickable(true);
            Resources resources = getBaseFragment(model).getActivity().getResources();
            int downArrowResId = R.drawable.ic_arrow_drop_down_white_24px;
            Drawable downArrow = ResourcesCompat.getDrawable(resources, downArrowResId, null);
            name.setCompoundDrawablesWithIntrinsicBounds(null, null, downArrow, null);
        }
    }

    /** Set the sigil (X or O) for a given player. */
    private void setPlayerSymbol(final int resId, final int index, final TicTacToe model) {
        // Ensure that the sigil text view exists.  Abort if not, set the value from the data
        // model if it does.
        TextView symbol = (TextView) mLayout.findViewById(resId);
        if (symbol == null) return;
        symbol.setText(model.players.get(index).symbol);
    }

    /** Set the name for a given player index. */
    private void setPlayerWinCount(final int resId, final int index, final TicTacToe model) {
        // Ensure that the win count text view exists. Abort if not.  Set the value from the model
        // if it does.
        TextView winCount = (TextView) mLayout.findViewById(resId);
        if (winCount == null) return;
        winCount.setText(String.valueOf(model.players.get(index).winCount));
    }

    /** Update the game state. */
    private void setState(final TicTacToe model) {
        // Generate a message string appropriate for a win or tie, or nothing if the game is active.
        String message = null;
        switch (model.state) {
            case TicTacToe.X_WINS:
            case TicTacToe.O_WINS:
                // do other stuff.
                String name = model.getWinningPlayerName();
                String format = getString(R.string.WinMessageFormat);
                message = String.format(Locale.getDefault(), format, name);
                break;
            case TicTacToe.TIE:
                // Reveal a tie game.
                message = getString(R.string.TieMessage);
                break;
            default:
                // keep playing or waiting for a new game.
                break;
        }

        // Determine if the game has ended (winner or tie).  Abort if not.
        if (message == null)
            return;

        // Update the UI to celebrate the winner or a tie and update the database game state to
        // pending.
        TextView winner = (TextView) mLayout.findViewById(R.id.status);
        winner.setText(message);
        winner.setVisibility(View.VISIBLE);
        NotificationManager.instance.notifyGameDone(this, getDoneMessage(model));
        model.state = TicTacToe.PENDING;
        ExpHelper.updateModel(mExperience);
    }

    /** Set up the game board based on the data model state. */
    private void setGameBoard(@NonNull final TicTacToe model) {
        Log.d(TAG, "setGameBoard() - mLayout width=" + mLayout.getWidth());

        // Determine if the model has any pieces to put on the board.  If not reset the board.
        if (model.board == null)
            // Initialize the board state.
            initBoard(model);
        else
            // Place the X and O symbols on the grid.
            for (String tag : model.board.grid.keySet()) {
                // Determine if the position denoted by the suffix is valid and has not yet been
                // updated.  If so, then update the position.
                String value = model.board.grid.get(tag);
                Button button = (Button) mLayout.findViewWithTag(tag);
                if (button != null && button.getText().equals("")) button.setText(value);
            }
    }

    /** Update the UI using the current experience state from the database. */
    private void updateUiFromExperience() {
        // Ensure that a valid experience exists.  Abort if not.
        if (mExperience == null || !(mExperience instanceof TicTacToe)) return;

        // A valid experience is available. Use the data model to populate the UI and check if the
        // game is finished.
        TicTacToe model = (TicTacToe) mExperience;
        ExpHelper.setRoomName(mExperience);
        setPlayerName(R.id.player1Name, 0, model);
        setPlayerName(R.id.player2Name, 1, model);
        setPlayerWinCount(R.id.player1WinCount, 0, model);
        setPlayerWinCount(R.id.player2WinCount, 1, model);
        setPlayerSymbol(R.id.player1Symbol, 0, model);
        setPlayerSymbol(R.id.player2Symbol, 1, model);
        setPlayerIcons(model.turn);
        setGameBoard(model);
        setState(model);
    }
}
