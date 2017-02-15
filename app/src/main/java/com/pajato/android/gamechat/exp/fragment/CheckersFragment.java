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

package com.pajato.android.gamechat.exp.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.exp.Checkerboard;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.NotificationManager;
import com.pajato.android.gamechat.exp.model.Checkers;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static com.pajato.android.gamechat.R.color.colorAccent;
import static com.pajato.android.gamechat.R.color.colorPrimary;
import static com.pajato.android.gamechat.R.id.player_1_icon;
import static com.pajato.android.gamechat.common.FragmentType.chess;
import static com.pajato.android.gamechat.common.FragmentType.selectExpGroupsRooms;
import static com.pajato.android.gamechat.common.FragmentType.tictactoe;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.exp.model.Checkers.ACTIVE;
import static com.pajato.android.gamechat.exp.model.Checkers.PRIMARY_WINS;
import static com.pajato.android.gamechat.exp.model.Checkers.SECONDARY_WINS;

/**
 * A simple Checkers game for use in GameChat.
 *
 * @author Bryan Scott
 */
public class CheckersFragment extends BaseExperienceFragment {

    // Public class constants.

    public static final String PRIMARY_PIECE = "pp";
    public static final String PRIMARY_KING = "pk";
    public static final String SECONDARY_PIECE = "sp";
    public static final String SECONDARY_KING = "sk";

    public static final String KING_UNICODE = "\u26c1";
    public static final String PIECE_UNICODE = "\u26c0";

    /** The lookup key for the FAB checkers menu. */
    public static final String CHECKERS_FAM_KEY = "CheckersFamKey";

    /** Logcat TAG */
    private static final String TAG = CheckersFragment.class.getSimpleName();

    // Public instance variables.

    public TextView mHighlightedTile;
    public boolean mIsHighlighted;

    // Private instance variables.

    /** Visual layout of checkers board objects */
    private Checkerboard mBoard = new Checkerboard();

    /** A click handler for the board tiles. */
    private View.OnClickListener mTileClickHandler = new TileClickHandler();

    // Public instance methods.

    /** Process a given button click event looking for one on the game fab button. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Delegate the event to the base class.
        processClickEvent(event.view, "tictactoe");
    }

    /** Handle a FAM or Snackbar Checkers click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Determine if this fragment has the foreground.  Abort if not, otherwise the event is
        // either a snackbar action (start a new game) or a FAM menu entry.  Detect and handle a
        // snackbar action first.
        if (!mActive)
            return;
        Object tag = event.view.getTag();
        if (isPlayAgain(tag, TAG)) {
            // Dismiss the FAB (assuming it was the source of the click --- being wrong is ok, and
            // setup a new game.
            FabManager.game.dismissMenu(this);
            handleNewGame();
        }
    }

    /** Handle a menu item selection. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        if (!this.mActive)
            return;
        // Case on the item resource id if there is one to be had.
        switch (event.item != null ? event.item.getItemId() : -1) {
            case R.string.InviteFriendsOverflow:
                if (isInMeGroup())
                    DispatchManager.instance.chainFragment(getActivity(), selectExpGroupsRooms, null);
                else
                    InvitationManager.instance.extendGroupInvitation(getActivity(),
                            mExperience.getGroupKey());
                break;
            case R.string.SwitchToChat:
                // If the toolbar chat icon is clicked, on smart phone devices we can change panes.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if (viewPager != null) viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                break;
            default:
                break;
        }
    }

    /** Handle an experience posting event to see if this is a checkers experience. */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Check the payload to see if this is not checkers.  Ignore the event if not, otherwise
        // resume the game.
        if (event.experience == null || event.experience.getExperienceType() != ExpType.checkersET)
            return;
        mExperience = event.experience;
        resume();
    }

    /** Handle taking the foreground by updating the UI based on the current experience. */
    @Override public void onResume() {
        // Determine if there is an experience ready to be enjoyed.  If not, hide the layout and
        // present a spinner.  When an experience is posted by the app event manager, the game can
        // be shown
        super.onResume();
        resume();
    }

    @Override public void onStart() {
        // Setup the FAM, add a new game item to the overflow menu, and obtain the board.
        super.onStart();
        FabManager.game.setMenu(CHECKERS_FAM_KEY, getCheckersMenu());
        ToolbarManager.instance.init(this, helpAndFeedback, settings, chat, invite);
        mBoard.init(this); // = (GridLayout) mLayout.findViewById(board);

        // Color the player icons and create a tile click handler.
        ImageView playerOneIcon = (ImageView) mLayout.findViewById(player_1_icon);
        playerOneIcon.setColorFilter(ContextCompat.getColor(getContext(), colorPrimary), SRC_ATOP);
        ImageView playerTwoIcon = (ImageView) mLayout.findViewById(R.id.player_2_icon);
        playerTwoIcon.setColorFilter(ContextCompat.getColor(getContext(), colorAccent), SRC_ATOP);
    }

    /** Return a default, partially populated, Checkers experience. */
    protected void createExperience(final Context context, final List<Account> playerAccounts) {
        // Setup the default key, players, and name.
        String key = getExperienceKey();
        List<Player> players = getDefaultPlayers(context, playerAccounts);
        String name1 = players.get(0).name;
        String name2 = players.get(1).name;
        long tStamp = new Date().getTime();
        String date = SimpleDateFormat.getDateTimeInstance().format(tStamp);
        String name = String.format(Locale.US, "%s vs %s on %s", name1, name2, date);

        // Set up the default group (Me Group) and room (Me Room) keys, the owner id and create the
        // object on the database.
        String groupKey = AccountManager.instance.getMeGroupKey();
        String roomKey = AccountManager.instance.getMeRoomKey();
        String id = getOwnerId();
        // TODO: DEFINE LEVEL INT ENUM VALUES - this is passing "0" for now
        Checkers model = new Checkers(key, id, 0, name, tStamp, groupKey, roomKey, players);
        mExperience = model;
        if (groupKey != null && roomKey != null)
            ExperienceManager.instance.createExperience(model);
        else
            reportError(context, R.string.ErrorCheckersCreation, groupKey, roomKey);
    }

    /** Notify the user about an error and log it. */
    private void reportError(final Context context, final int messageResId, String... args) {
        // Let the User know that something is amiss.
        String message = context.getString(messageResId);
        NotificationManager.instance.notifyNoAction(this, message);

        // Generate a logcat item casing on the given resource id.
        String format;
        switch (messageResId) {
            case R.string.ErrorCheckersCreation:
                format = "Failed to create a Checkers experience with group/room keys: {%s/%s}";
                Log.e(TAG, String.format(Locale.US, format, args[0], args[1]));
                break;
            default:
                break;
        }
    }

    /** Return a possibly null list of player information for a checkers experience (always 2 players) */
    protected List<Account> getPlayers(final Dispatcher dispatcher) {
        // Determine if this is an offline experience in which no accounts are provided.
        Account player1 = AccountManager.instance.getCurrentAccount();
        if (player1 == null) return null;

        // This is an online experience.  Use the current signed in User as the first player.
        List<Account> players = new ArrayList<>();
        players.add(player1);

        // Determine the second account, if any, based on the room.
        String key = dispatcher.roomKey;
        Room room = key != null ? RoomManager.instance.roomMap.get(key) : null;
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

    /** Return a list of default Checkers players. */
    protected List<Player> getDefaultPlayers(final Context context, final List<Account> players) {
        List<Player> result = new ArrayList<>();
        String name = getPlayerName(getPlayer(players, 0), context.getString(R.string.player1));
        String team = context.getString(R.string.primaryTeam);
        result.add(new Player(name, "", team));
        name = getPlayerName(getPlayer(players, 1), context.getString(R.string.friend));
        team = context.getString(R.string.secondaryTeam);
        result.add(new Player(name, "", team));
        return result;
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getCheckersMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getEntry(R.string.PlayTicTacToe, R.mipmap.ic_tictactoe_red, tictactoe));
        menu.add(getEntry(R.string.PlayChess, R.mipmap.ic_chess, chess));
        menu.add(getNoTintEntry(R.string.PlayAgain, R.mipmap.ic_checkers));
        return menu;
    }

    /** Return a done message text to show in a snackbar.  The given model provides the state. */
    private String getDoneMessage(final Checkers model) {
        // Determine if there is a winner.  If not, return the "tie" message.
        String name = model.getWinningPlayerName();
        if (name == null) return getString(R.string.TieMessageNotification);

        // There was a winner.  Return a congratulatory message.
        String format = getString(R.string.WinMessageNotificationFormat);
        return String.format(Locale.getDefault(), format, name);
    }

    /** Return the Checkers model class, null if it does not exist. */
    private Checkers getModel() {
        if (mExperience == null || !(mExperience instanceof Checkers)) return null;
        return (Checkers) mExperience;
    }

    /** Handle a new game by resetting the data model. */
    private void handleNewGame() {
        // Ensure that the data model exists and is valid.
        Checkers model = getModel();
        if (model == null) {
            Log.e(TAG, "Null Checkers data model.", new Throwable());
            return;
        }

        // Reset the data model, update the database and clear the notification manager one-shot.
        model.board = null;
        model.state = ACTIVE;
        ExperienceManager.instance.updateExperience(mExperience);
    }

    /** Process a resumption by testing and waiting for the experience */
    private void resume() {
        if (getModel() == null) {
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
            setPlayerIcons(R.id.leftIndicator1, R.id.rightIndicator1,
                    R.id.leftIndicator2, R.id.rightIndicator2);
        else
            // Make player2's decorations the more prominent.
            setPlayerIcons(R.id.leftIndicator2, R.id.rightIndicator2,
                    R.id.leftIndicator1, R.id.rightIndicator1);
    }

    /** Manage a particular player's symbol decorations. */
    private void setPlayerIcons(final int largeLeft, final int largeRight,
                                final int smallLeft, final int smallRight) {

        // Collect all the pertinent textViews.
        TextView tvLargeLeft = (TextView) getActivity().findViewById(largeLeft);
        TextView tvLargeRight = (TextView) getActivity().findViewById(largeRight);
        TextView tvSmallLeft = (TextView) getActivity().findViewById(smallLeft);
        TextView tvSmallRight = (TextView) getActivity().findViewById(smallRight);

        // Deal with the tvLarger symbol's decorations.
        tvLargeLeft.setVisibility(View.VISIBLE);
        tvLargeRight.setVisibility(View.VISIBLE);

        // Deal with the tvSmall symbol's decorations.
        tvSmallLeft.setVisibility(View.INVISIBLE);
        tvSmallRight.setVisibility(View.INVISIBLE);
    }

    /** Set the name for a given player index. */
    private void setPlayerName(final int resId, final int index, final Checkers model) {
        // Ensure that the name text view exists. Abort if not.  Set the value from the model if it
        // does.
        TextView name = (TextView) mLayout.findViewById(resId);
        if (name == null) return;
        name.setText(model.players.get(index).name);
    }

    /** Set the name for a given player index. */
    private void setPlayerWinCount(final int resId, final int index, final Checkers model) {
        // Ensure that the win count text view exists. Abort if not.  Set the value from the model
        // if it does.
        TextView winCount = (TextView) mLayout.findViewById(resId);
        if (winCount == null) return;
        winCount.setText(String.valueOf(model.players.get(index).winCount));
    }

    /** Update the game state. */
    private void setState(final Checkers model) {
        // Generate a message string appropriate for a win or tie, or nothing if the game is active.
        String message = null;
        switch (model.state) {
            case Checkers.PRIMARY_WINS:
            case Checkers.SECONDARY_WINS:
                String name = model.getWinningPlayerName();
                String format = getString(R.string.WinMessageFormat);
                message = String.format(Locale.getDefault(), format, name);
                break;
            case Checkers.TIE:
                message = getString(R.string.TieMessage);
                break;
            default:
                // keep playing or waiting for a new game
                break;
        }
        // Determine if the game has ended (winner or time). Abort if not.
        if (message == null) return;

        // Update the UI to celebrate the winner or a tie and update the database game state to
        // pending.
        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        winner.setText(message);
        winner.setVisibility(View.VISIBLE);
        NotificationManager.instance.notifyGameDone(this, getDoneMessage(model));
        model.state = Checkers.PENDING;
        ExperienceManager.instance.updateExperience(mExperience);
    }

    /** Set up the game board based on the data model state. */
    private void setGameBoard(@NonNull final Checkers model) {
        // Determine if the model has any pieces to put on the board.  If not reset the board.
        if (model.board == null)
            startGame();
    }

    /** Update the UI using the current experience state from the database. */
    private void updateUiFromExperience() {
        // Ensure that a valid experience exists.  Abort if not.
        if (mExperience == null || !(mExperience instanceof Checkers)) return;

        // A valid experience is available. Use the data model to populate the UI and check if the
        // game is finished.
        Checkers model = (Checkers) mExperience;
        setRoomName(mExperience);
        setPlayerName(R.id.player1Name, 0, model);
        setPlayerName(R.id.player2Name, 1, model);
        setPlayerWinCount(R.id.player1WinCount, 0, model);
        setPlayerWinCount(R.id.player2WinCount, 1, model);
        setPlayerIcons(model.turn);
        setGameBoard(model);
        setState(model);
    }

    /**
     * Handles starting game of checkers, resetting the board either for a new game or a restart
     * after loading a game board from the database.
     */
    private void startGame() {
        Checkers model = (Checkers) mExperience;
        boolean isNewBoard = false;
        if (model.board == null) {
            isNewBoard = true;
            model.board = new HashMap<>();
        }
        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        if (winner != null)
            winner.setText("");

        // Go through and populate the GridLayout / board.
        mBoard.reset();
        int cellSize = mBoard.getCellSize();
        for (int i = 0; i < 64; i++) {
            String pieceType = "";
            if (!isNewBoard) {
                pieceType = model.board.get(String.valueOf(i));
                if (pieceType == null)
                    pieceType = "";
            }
            TextView tile = mBoard.getCellView(getContext(), i, cellSize, model.board, pieceType);
            tile.setOnClickListener(mTileClickHandler);
            mBoard.addCell(tile);
        }

        handleTurnChange(false);
    }

    /**
     * showPossibleMoves handles highlighting possible movement options of a clicked piece,
     * then on a subsequent click it removes those highlights.
     *
     * @param indexClicked the index of the tile clicked.
     * @param board a HashMap representing a board index (0->63) to the piece type at that location.
     * @return true if we've made any updates that should be written to the database; false otherwise
     */
    private boolean showPossibleMoves(final int indexClicked, final Map<String, String> board) {
        // If the game is over, we don't need to do anything.
        if(checkFinished(board)) {
            return false;
        }

        boolean hasChanged = false;
        boolean turn = ((Checkers) mExperience).turn;
        String highlightedIdxTag = (String) mHighlightedTile.getTag();
        int highlightedIndex = Integer.parseInt(highlightedIdxTag);
        List<Integer> possibleMoves = new ArrayList<>();
        findPossibleMoves(board, highlightedIndex, possibleMoves);

        // If a highlighted tile exists, we remove the highlight on it and its movement options.
        if(mIsHighlighted) {
            mHighlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.white));

            for (int possiblePosition : possibleMoves) {
                // It is important to note for these algorithms that the Views at each index on the
                // board have a tag equal to the string value of the index into the cell on the board.
                if(possiblePosition != -1 && board.get(String.valueOf(possiblePosition)) == null) {

                    // If the tile clicked is one of the possible positions, and it's the correct
                    // turn/piece combination, the piece moves there.
                    if(indexClicked == possiblePosition) {
                        boolean capturesPiece = (indexClicked > 9 + highlightedIndex) ||
                                (indexClicked < highlightedIndex - 9);

                        if(turn && (board.get(highlightedIdxTag).equals(PRIMARY_PIECE) ||
                                board.get(highlightedIdxTag).equals(PRIMARY_KING))) {

                            handleMovement(board, true, indexClicked, capturesPiece);
                            hasChanged = true;

                        } else if(!turn && (board.get(highlightedIdxTag).equals(SECONDARY_PIECE)
                                || board.get(highlightedIdxTag).equals(SECONDARY_KING))) {

                            handleMovement(board, false, indexClicked, capturesPiece);
                            hasChanged = true;
                        }
                    }
                    // Clear the highlight off of all possible positions.
                    mBoard.setHighlight(getContext(), possiblePosition, android.R.color.white);
                }
            }
            mHighlightedTile = null;

        } else {
            // Highlight the tile clicked and its potential move squares with red.
            Context ctx = getContext();
            int id = android.R.color.holo_red_dark;
            mHighlightedTile.setBackgroundColor(ContextCompat.getColor(ctx, id));
            id = android.R.color.holo_red_light;
            for(int position : possibleMoves)
                if (position != -1 && board.get(String.valueOf(position)) == null)
                    mBoard.setHighlight(ctx, position, id);
        }

        mIsHighlighted = !mIsHighlighted;

        return hasChanged;
    }

    /**
     * Checks to see if the game is over or not by counting the number of primary / secondary pieces
     * on the board. If there are zero of one type of piece, then the other side wins.
     *
     * @return true if the game is over, false otherwise.
     */
    private boolean checkFinished(Map<String, String> board) {
        // Generate win conditions. If one side runs out of pieces, the other side wins.
        int yCount = 0;
        int bCount = 0;
        for (int i = 0; i < 64; i++) {
            String tmp = board.get(String.valueOf(i));
            if(tmp == null) continue;
            if(tmp.equals(PRIMARY_PIECE) || tmp.equals(PRIMARY_KING)) {
                bCount++;
            } else if (tmp.equals(SECONDARY_PIECE) || tmp.equals(SECONDARY_KING)) {
                yCount++;
            }
        }
        // Verify win conditions. If one passes, return true and generate an endgame snackbar.
        if (yCount == 0) {
            setWinCount(PRIMARY_WINS);
            return true;
        } else if (bCount == 0) {
            setWinCount(SECONDARY_WINS);
            return true;
        } else {
            return false;
        }
    }

    private void setWinCount(final int state) {
        Checkers model = getModel();
        if (model == null) {
            Log.e(TAG, "Null Checkers data model.", new Throwable());
            return;
        }

        String winMsg = "Game Over, Player ";
        if(state == PRIMARY_WINS) {
            winMsg += "1 wins";
        } else if(state == SECONDARY_WINS) {
            winMsg += "2 wins";
        } else {
            return;
        }

        NotificationManager.instance.notifyGameDone(this, winMsg);
        model.state = state;
        model.setWinCount();
    }

    /**
     * Finds the "jumpable" pieces that the current piece could potentially capture.
     *
     * @param highlightedIndex the current index of the tile looking to jump.
     * @param jumpable the index of the tile that the piece can potentially jump to.
     */
    private void findJumpables(Map<String, String> board, final int highlightedIndex, final int jumpable,
                               final List<Integer> movementOptions) {
        // Create the boolean calculations for each of our conditions.
        boolean withinBounds = jumpable < 64 && jumpable > -1;
        boolean emptySpace = board.get(String.valueOf(jumpable)) == null;
        boolean breaksBorders = (highlightedIndex % 8 == 1 && jumpable % 8 == 7)
                || (highlightedIndex % 8 == 6 && jumpable % 8 == 0);
        boolean jumpsAlly = false;

        // Check if the piece being jumped is an ally piece.
        String highlightedPieceType = board.get(String.valueOf(highlightedIndex));
        int jumpedIndex = (highlightedIndex + jumpable) / 2;
        String jumpedPieceType = board.get(String.valueOf(jumpedIndex));

        if (highlightedPieceType.equals(PRIMARY_PIECE) || highlightedPieceType.equals(PRIMARY_KING)) {
            jumpsAlly = jumpedPieceType.equals(PRIMARY_PIECE) || jumpedPieceType.equals(PRIMARY_KING);

        } else if (highlightedPieceType.equals(SECONDARY_PIECE) || highlightedPieceType.equals(SECONDARY_KING)) {
            jumpsAlly = jumpedPieceType.equals(SECONDARY_PIECE) || jumpedPieceType.equals(SECONDARY_KING);
        }

        if(withinBounds && emptySpace && !breaksBorders && !jumpsAlly) {
            movementOptions.add(jumpable);
        }
    }

    /**
     * Locates the possible moves of the piece that is about to be highlighted.
     *
     * @param highlightedIndex the index containing the highlighted piece.
     */
    private void findPossibleMoves(final Map<String, String> board, final int highlightedIndex,
                                   final List<Integer> possibleMoves) {
        if(highlightedIndex < 0 || highlightedIndex > 64) {
            return;
        }

        possibleMoves.clear();
        String highlightedPieceType = board.get(String.valueOf(highlightedIndex));

        // Get the possible positions, post-move, for the piece.
        int upLeft = highlightedIndex - 9;
        int upRight = highlightedIndex - 7;
        int downLeft = highlightedIndex + 7;
        int downRight = highlightedIndex + 9;

        // Handle vertical edges of the board and non-king pieces.
        if(highlightedIndex / 8 == 0 || highlightedPieceType.equals(SECONDARY_PIECE)) {
            upLeft = -1;
            upRight = -1;
        } else if (highlightedIndex / 8 == 7 || highlightedPieceType.equals(PRIMARY_PIECE)) {
            downLeft = -1;
            downRight = -1;
        }

        // Handle horizontal edges of the board.
        if(highlightedIndex % 8 == 0) {
            upLeft = -1;
            downLeft = -1;
        } else if(highlightedIndex % 8 == 7) {
            upRight = -1;
            downRight = -1;
        }

        // Handle tiles that already contain other pieces. You can jump over enemy pieces,
        // but not allied pieces.
        if(board.get(String.valueOf(upLeft)) != null) {
            findJumpables(board, highlightedIndex, upLeft - 9, possibleMoves);
            upLeft = -1;
        }
        if(board.get(String.valueOf(upRight)) != null) {
            findJumpables(board, highlightedIndex, upRight - 7, possibleMoves);
            upRight = -1;
        }
        if(board.get(String.valueOf(downLeft)) != null) {
            findJumpables(board, highlightedIndex, downLeft + 7, possibleMoves);
            downLeft = -1;
        }
        if(board.get(String.valueOf(downRight)) != null) {
            findJumpables(board, highlightedIndex, downRight + 9, possibleMoves);
            downRight = -1;
        }

        // Put the values in our int array to return
        possibleMoves.add(upLeft);
        possibleMoves.add(upRight);
        possibleMoves.add(downLeft);
        possibleMoves.add(downRight);
    }

    /**
     * Handles the movement of the pieces.
     *
     * @param player indicates the current player. True is Primary, False is Secondary.
     * @param indexClicked the index of the clicked tile and the new position of the piece.
     */
    private void handleMovement(Map<String, String> board, final boolean player, final int indexClicked,
                                final boolean capturesPiece) {
        // Reset the highlighted tile's image.
        mHighlightedTile.setText(" ");
        int highlightedIndex = Integer.parseInt((String)mHighlightedTile.getTag());
        String highlightedPieceType = board.get(mHighlightedTile.getTag());
        String indexClickedStr = String.valueOf(indexClicked);

        // Check to see if our piece becomes a king piece and put its value into the board.
        if(indexClicked < 8 && highlightedPieceType.equals(PRIMARY_PIECE)) {
            board.put(indexClickedStr, PRIMARY_KING);
        } else if (indexClicked > 55 && highlightedPieceType.equals(SECONDARY_PIECE)) {
            board.put(indexClickedStr, SECONDARY_KING);
        } else {
            board.put(indexClickedStr, highlightedPieceType);
        }

        // Find the new tile and give it a piece.
        TextView newLoc = mBoard.getCell(indexClicked);
        if(board.get(indexClickedStr).equals(PRIMARY_KING) ||
                board.get(indexClickedStr).equals(SECONDARY_KING)) {
            newLoc.setText(KING_UNICODE);
        } else {
            newLoc.setText(PIECE_UNICODE);
        }

        // Color the piece according to the player.
        int color;
        if(player) {
            color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        } else {
            color = ContextCompat.getColor(getContext(), R.color.colorAccent);
        }
        newLoc.setTextColor(color);

        // Handle capturing pieces.
        boolean finishedJumping = true;
        if(capturesPiece) {
            int pieceCapturedIndex = (indexClicked + highlightedIndex) / 2;
            TextView capturedTile = mBoard.getCell(pieceCapturedIndex);
            capturedTile.setText(" " );
            String key = (String) capturedTile.getTag();
            if (board.containsKey(key))
                board.remove(capturedTile.getTag());

            // If there are no more jumps, change turns. If there is at least one jump left, don't.
            ArrayList<Integer> possibleJumps = new ArrayList<>();
            findPossibleMoves(board, indexClicked, possibleJumps);
            for(int possiblePosition: possibleJumps) {
                if(possiblePosition != -1 && (possiblePosition > 9 + indexClicked
                        || (possiblePosition < indexClicked - 9))) {
                    finishedJumping = false;
                }
            }
        }

        board.remove(String.valueOf(highlightedIndex));
        handleTurnChange(finishedJumping);
        checkFinished(board);
    }

    /**
     * Handles changing the turn and turn indicator.
     * @param switchPlayer if false, just set up the UI views but don't switch the player turn.
     */
    private void handleTurnChange(final boolean switchPlayer) {

        boolean turn = ((Checkers) mExperience).turn;
        if(switchPlayer) {
            turn = ((Checkers) mExperience).toggleTurn();
        }

        // Handle the text views that serve as our turn indicator.
        TextView playerOneLeft = (TextView) mLayout.findViewById(R.id.leftIndicator1);
        TextView playerOneRight = (TextView) mLayout.findViewById(R.id.rightIndicator1);
        TextView playerTwoLeft = (TextView) mLayout.findViewById(R.id.leftIndicator2);
        TextView playerTwoRight = (TextView) mLayout.findViewById(R.id.rightIndicator2);

        if (turn) {
            playerOneLeft.setVisibility(View.VISIBLE);
            playerOneRight.setVisibility(View.VISIBLE);
            playerTwoLeft.setVisibility(View.INVISIBLE);
            playerTwoRight.setVisibility(View.INVISIBLE);
        } else {
            playerOneLeft.setVisibility(View.INVISIBLE);
            playerOneRight.setVisibility(View.INVISIBLE);
            playerTwoLeft.setVisibility(View.VISIBLE);
            playerTwoRight.setVisibility(View.VISIBLE);
        }
    }

    /** A View.OnClickListener that is called whenever a board tile is clicked. */
    private class TileClickHandler implements View.OnClickListener {
        @Override public void onClick(final View v) {
            int index = Integer.parseInt((String)v.getTag());
            boolean changedBoard = false;
            Map<String, String> board = ((Checkers) mExperience).board;
            if (mHighlightedTile != null) {
                changedBoard = showPossibleMoves(index, board);
                mHighlightedTile = null;
            } else {
                if (board.get(String.valueOf(index)) != null) {
                    mHighlightedTile = (TextView) v;
                    changedBoard = showPossibleMoves(index, board);
                }
            }
            if (changedBoard)
                // Save any changes that have been made to the database
                ExperienceManager.instance.updateExperience(mExperience);
        }
    }

}
