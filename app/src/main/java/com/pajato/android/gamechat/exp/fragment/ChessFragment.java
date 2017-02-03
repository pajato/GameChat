package com.pajato.android.gamechat.exp.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
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
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.NotificationManager;
import com.pajato.android.gamechat.exp.model.Chess;
import com.pajato.android.gamechat.exp.model.ChessBoard;
import com.pajato.android.gamechat.exp.model.ChessHelper;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.main.PaneManager;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.pajato.android.gamechat.R.color.colorAccent;
import static com.pajato.android.gamechat.R.color.colorPrimary;
import static com.pajato.android.gamechat.R.id.board;
import static com.pajato.android.gamechat.common.FragmentType.checkers;
import static com.pajato.android.gamechat.common.FragmentType.selectExpGroupsRooms;
import static com.pajato.android.gamechat.common.FragmentType.tictactoe;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.newChess;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.exp.model.Chess.ACTIVE;
import static com.pajato.android.gamechat.exp.model.Chess.PRIMARY_WINS;
import static com.pajato.android.gamechat.exp.model.Chess.SECONDARY_WINS;

/**
 * A simple Chess game for use in GameChat.
 *
 * @author Bryan Scott
 */
public class ChessFragment extends BaseExperienceFragment {

    // Chess Management Objects
    private TextView mHighlightedTile;
    private boolean mIsHighlighted = false;
    private ArrayList<Integer> mPossibleMoves;

    /** Visual layout of chess board objects */
    private GridLayout grid;

    /** The lookup key for the FAB chess menu. */
    public static final String CHESS_FAM_KEY = "ChessFamKey";

    /** logcat TAG */
    private static final String TAG = ChessFragment.class.getSimpleName();

    // Public instance methods.

    /** Handle a FAM or Snackbar Chess click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Determine if this event is for this fragment.  Abort if not.
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
                    InvitationManager.instance.extendInvitation(getActivity(),
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

    /** Handle an experience posting event to see if this is a chess experience. */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Check the payload to see if this is not chess.  Abort if not, otherwise resume the game.
        if (event.experience == null || event.experience.getExperienceType() != ExpType.chessET)
            return;
        mExperience = event.experience;
        resume();
    }

    @Override public void onStart() {
        // Setup the FAM, add a new game item to the overflow menu, and obtain the board (grid).
        super.onStart();
        FabManager.game.setMenu(CHESS_FAM_KEY, getChessMenu());
        ToolbarManager.instance.init(this, helpAndFeedback, settings, chat, newChess, invite);
        grid = (GridLayout) mLayout.findViewById(board);

        // Color the Player Icons.
        ImageView playerOneIcon = (ImageView) mLayout.findViewById(R.id.player_1_icon);
        playerOneIcon.setColorFilter(ContextCompat.getColor(getContext(), colorPrimary), SRC_ATOP);
        ImageView playerTwoIcon = (ImageView) mLayout.findViewById(R.id.player_2_icon);
        playerTwoIcon.setColorFilter(ContextCompat.getColor(getContext(), colorAccent), SRC_ATOP);
    }

    /** Handle taking the foreground by updating the UI based on the current experience. */
    @Override public void onResume() {
        // Determine if there is an experience ready to be enjoyed.  If not, hide the layout and
        // present a spinner.  When an experience is posted by the app event manager, the game can
        // be shown
        super.onResume();
        resume();
    }

    /** Return a default, partially populated, Chess experience. */
    @Override
    protected void createExperience(final Context context, final List<Account> playerAccounts) {
        // Setup the default key, players, and name.
        String key = getExperienceKey();
        List<Player> players = getDefaultPlayers(context, playerAccounts);
        String name1 = players.get(0).name;
        String name2 = players.get(1).name;

        long tstamp = new Date().getTime();
        String name = String.format(Locale.US, "%s vs %s on %s", name1, name2,
                SimpleDateFormat.getDateTimeInstance().format(tstamp));

        // Set up the default group (Me Group) and room (Me Room) keys, the owner id and create the
        // object on the database.
        String groupKey = AccountManager.instance.getMeGroupKey();
        String roomKey = AccountManager.instance.getMeRoomKey();
        String id = getOwnerId();
        // TODO: DEFINE LEVEL INT ENUM VALUES - this is passing "0" for now
        Chess model = new Chess(key, id, 0, name, tstamp, groupKey, roomKey, players);
        mExperience = model;
        if (groupKey != null && roomKey != null) ExperienceManager.instance.createExperience(model);
        else reportError(context, R.string.ErrorChessCreation, groupKey, roomKey);
    }

    /** Notify the user about an error and log it. */
    private void reportError(final Context context, final int messageResId, String... args) {
        // Let the User know that something is amiss.
        String message = context.getString(messageResId);
        NotificationManager.instance.notify(this, message, false);

        // Generate a logcat item casing on the given resource id.
        String format;
        switch (messageResId) {
            case R.string.ErrorChessCreation:
                format = "Failed to create a Chess experience with group/room keys: {%s/%s}";
                Log.e(TAG, String.format(Locale.US, format, args[0], args[1]));
                break;
            default:
                break;
        }
    }

    /** Return a possibly null list of chess player information. */
    @Override
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

    /** Return a list of default Chess players. */
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

    /** Return a done message text to show in a snackbar.  The given model provides the state. */
    private String getDoneMessage(final Chess model) {
        // Determine if there is a winner.  If not, return the "tie" message.
        String name = model.getWinningPlayerName();
        if (name == null) return getString(R.string.TieMessageNotification);

        // There was a winner.  Return a congratulatory message.
        String format = getString(R.string.WinMessageNotificationFormat);
        return String.format(Locale.getDefault(), format, name);
    }

    /** Return the Chess model class, null if it does not exist. */
    private Chess getModel() {
        if (mExperience == null || !(mExperience instanceof Chess)) return null;
        return (Chess) mExperience;
    }

    /** Handle a new game by resetting the data model. */
    private void handleNewGame() {
        // Ensure that the data model exists and is valid.
        Chess model = getModel();
        if (model == null) {
            Log.e(TAG, "Null Chess data model.", new Throwable());
            return;
        }

        // Reset the data model, update the database and clear the notification manager one-shot.
        model.board = null;
        model.state = ACTIVE;
        ExperienceManager.instance.updateExperience(mExperience);
    }

    /** Process a resumption by testing and waiting for the experience. */
    private void resume() {
        if (getModel() == null) {
            // Disable the layout and startup the spinner.
            mLayout.setVisibility(View.GONE);
        } else {
            // Start the game and update the views using the current state of the experience.
            mLayout.setVisibility(View.VISIBLE);
            ProgressManager.instance.hide();
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
    private void setPlayerName(final int resId, final int index, final Chess model) {
        // Ensure that the name text view exists. Abort if not.  Set the value from the model if it
        // does.
        TextView name = (TextView) mLayout.findViewById(resId);
        if (name == null) return;
        name.setText(model.players.get(index).name);
    }

    /** Set the name for a given player index. */
    private void setPlayerWinCount(final int resId, final int index, final Chess model) {
        // Ensure that the win count text view exists. Abort if not.  Set the value from the model
        // if it does.
        TextView winCount = (TextView) mLayout.findViewById(resId);
        if (winCount == null) return;
        winCount.setText(String.valueOf(model.players.get(index).winCount));
    }

    /** Update the game state. */
    private void setState(final Chess model) {
        // Generate a message string appropriate for a win or tie, or nothing if the game is active.
        String message = null;
        switch (model.state) {
            case Chess.PRIMARY_WINS:
            case Chess.SECONDARY_WINS:
                String name = model.getWinningPlayerName();
                String format = getString(R.string.WinMessageFormat);
                message = String.format(Locale.getDefault(), format, name);
                break;
            case Chess.TIE:
                message = getString(R.string.StalemateMessage);
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
        NotificationManager.instance.notify(this, getDoneMessage(model), true);
        model.state = Chess.PENDING;
        ExperienceManager.instance.updateExperience(mExperience);
    }

    /** Set up the game board based on the data model state. */
    private void setGameBoard(@NonNull final Chess model) {
        // Determine if the model has any pieces to put on the board.  If not reset the board.
        if (model.board == null) startGame();
   }

    /** Update the UI using the current experience state from the database. */
    private void updateUiFromExperience() {
        // Ensure that a valid experience exists.  Abort if not.
        if (mExperience == null || !(mExperience instanceof Chess)) return;

        // A valid experience is available. Use the data model to populate the UI and check if the
        // game is finished.
        Chess model = (Chess) mExperience;
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
     * Set up an image button which will be a cell in the game board
     * @param index index into the board for the cell we want to add
     * @param sideSize size to use for width and height of the new item to add to the board
     * @param board a ChessBoard object representing the game board (0->63) the piece type at that location.
     */
    private TextView makeBoardButton(final int index, final int sideSize, final ChessBoard board) {
        TextView currentTile = new TextView(getContext());

        // Set up the gridlayout params, so that each cell is functionally identical.
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = sideSize;
        param.width = sideSize;
        param.rightMargin = 0;
        param.topMargin = 0;
        param.setGravity(Gravity.CENTER);
        param.rowSpec = GridLayout.spec(index / 8);
        param.columnSpec = GridLayout.spec(index % 8);

        // Set up the tile-specific information.
        currentTile.setLayoutParams(param);
        currentTile.setTag(String.valueOf(index));
        float sp = sideSize / getResources().getDisplayMetrics().scaledDensity;
        currentTile.setTextSize(COMPLEX_UNIT_SP, (float)(sp * 0.8));
        currentTile.setGravity(Gravity.CENTER);
        currentTile.setText(" ");
        handleTileBackground(index, currentTile);

        // Handle the chess starting piece positions.
        boolean containsSecondaryPlayerPiece = index < 16;
        boolean containsPrimaryPlayerPiece = index > 47;
        boolean containsPiece = containsPrimaryPlayerPiece || containsSecondaryPlayerPiece;
        ChessPiece.ChessTeam team;

        // If the tile is meant to contain a board piece at the start of play, give it a piece.
        if (containsPiece) {
            if (containsPrimaryPlayerPiece) {
                team = ChessPiece.ChessTeam.PRIMARY;
                currentTile.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            } else {
                team = ChessPiece.ChessTeam.SECONDARY;
                currentTile.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            }
            switch(index) {
                default:
                    board.add(index, ChessPiece.PieceType.PAWN, team);
                    currentTile.setText(ChessPiece.getUnicodeText(ChessPiece.PieceType.PAWN));
                    break;
                case 0: case 7:
                case 56: case 63:
                    board.add(index, ChessPiece.PieceType.ROOK, team);
                    currentTile.setText(ChessPiece.getUnicodeText(ChessPiece.PieceType.ROOK));
                    break;
                case 1: case 6:
                case 57: case 62:
                    board.add(index, ChessPiece.PieceType.KNIGHT, team);
                    currentTile.setText(ChessPiece.getUnicodeText(ChessPiece.PieceType.KNIGHT));
                    break;
                case 2: case 5:
                case 58: case 61:
                    board.add(index, ChessPiece.PieceType.BISHOP, team);
                    currentTile.setText(ChessPiece.getUnicodeText(ChessPiece.PieceType.BISHOP));
                    break;
                case 3:
                case 59:
                    board.add(index, ChessPiece.PieceType.QUEEN, team);
                    currentTile.setText(ChessPiece.getUnicodeText(ChessPiece.PieceType.QUEEN));
                    break;
                case 4:
                case 60:
                    board.add(index, ChessPiece.PieceType.KING, team);
                    currentTile.setText(ChessPiece.getUnicodeText(ChessPiece.PieceType.KING));
            }
        }

        return currentTile;
    }

    /** Handle a new chess game by resetting the board on a new game or a database reload. */
    private void startGame() {
        // Initialize the new board state.
        grid.removeAllViews();
        Chess model = (Chess)mExperience;
        if (model.board == null) model.board = new ChessBoard();
        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        if (winner != null) winner.setText("");

        // Reset the castling booleans.
        model.primaryQueenSideRookHasMoved = false;
        model.primaryKingSideRookHasMoved = false;
        model.primaryKingHasMoved = false;
        model.secondaryQueenSideRookHasMoved = false;
        model.secondaryKingSideRookHasMoved = false;
        model.secondaryKingHasMoved = false;

        mPossibleMoves = new ArrayList<>();

        // Take the smaller of width/height to adjust for tablet (landscape) view and adjust for
        // the player controls and FAB. TODO: the fab currently returns "top" value of 0...
        int screenWidth = getActivity().findViewById(R.id.expFragmentContainer).getWidth();
        ImageView v = (ImageView) getActivity().findViewById(R.id.player_1_icon);
        int screenHeight = getActivity().findViewById(R.id.expFragmentContainer).getHeight()
                - v.getBottom() - getActivity().findViewById(R.id.gameFab).getTop();

        int sideSize = Math.min(screenWidth, screenHeight);
        Log.d(TAG, "screen width=" + screenWidth + ", screen height=" + screenHeight);
        int pieceSideLength = sideSize / 8;
        Log.d(TAG, "using piece side length=" + pieceSideLength);

        // Go through and populate the GridLayout / board.
        for (int i = 0; i < 64; i++) {
            TextView currentTile = makeBoardButton(i, pieceSideLength, model.board);
            currentTile.setOnClickListener(new ChessClick());
            grid.addView(currentTile);
        }

        handleTurnChange(false);

    }

    /**
     * showPossibleMoves handles highlighting possible movement options of a clicked piece,
     * then on a subsequent click it removes those highlights.
     *
     * @param indexClicked the index of the tile clicked.
     * @param board a ChessBoard object
     * @return true if we've made any updates that should be written to the database; false otherwise
     */
    private boolean showPossibleMoves(final int indexClicked, ChessBoard board) {
        // If the game is over, we don't need to do anything, so return.  Otheriwse find the
        // possible moves for the selected piece.
        if (checkFinished(board)) return false;
        boolean hasChanged = false;
        int highlightedIndex = Integer.parseInt((String) mHighlightedTile.getTag());
        findPossibleMoves(highlightedIndex, mPossibleMoves, board);

        // If a highlighted tile exists, we remove the highlight on it and its movement options.
        if (mIsHighlighted) {
            handleTileBackground(highlightedIndex, mHighlightedTile);

            for (int possiblePosition : mPossibleMoves) {
                // If the tile clicked is one of the possible positions, and it's the correct
                // turn/piece combination, the piece moves there.
                if (indexClicked == possiblePosition) {
                    boolean capturesPiece = board.containsPiece(indexClicked);
                    handleMovement(board.getTeam(highlightedIndex), indexClicked, capturesPiece, board);
                    hasChanged = true;
                }
                handleTileBackground(possiblePosition, (TextView) grid.getChildAt(possiblePosition));
            }
            mHighlightedTile = null;

        // Otherwise, we need to highlight the tile clicked and its potential move squares with red.
        } else {
            mHighlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.holo_red_dark));
            for(int possiblePosition : mPossibleMoves) {
                grid.getChildAt(possiblePosition).setBackgroundColor(ContextCompat
                        .getColor(getContext(), android.R.color.holo_red_light));
            }
        }

        mIsHighlighted = !mIsHighlighted;

        return hasChanged;
    }

    /**
     * Checks to see if the game is over or not by counting the primary / secondary kings are
     * on the board. If one is not on the board, then the other side wins.
     * @param board a ChessBoard object
     *
     * @return true if the game is over, false otherwise.
     */
    private boolean checkFinished(ChessBoard board) {
        Chess model = getModel();
        if (model == null) {
            Log.e(TAG, "Null Chess data model.", new Throwable());
        }
        // Generate win conditions. If one side runs out of pieces, the other side wins.
        if (!board.containsSecondaryKing()) {
            NotificationManager.instance.notify(this, "Game Over! Player 1 Wins!", true);
            if(model != null) {
                model.state = PRIMARY_WINS;
                model.setWinCount();
            }
            return true;
        }
        if (!board.containsPrimaryKing()) {
            NotificationManager.instance.notify(this, "Game Over! Player 2 Wins!", true);
            if(model != null) {
                model.state = SECONDARY_WINS;
                model.setWinCount();
            }
            return true;
        }
        return false;
    }

    /**
     * A utility method that facilitates keeping the board's checker pattern in place throughout the
     * highlighting and de-higlighting process. It accepts a tile and sets its background to white
     * or dark grey, depending on its location in the board.
     *
     * @param index the index of the tile, used to determine the color of the background.
     * @param currentTile the tile whose color we are changing.
     */
    private void handleTileBackground(final int index, final TextView currentTile) {
        // Handle the checkerboard positions (where 'checkerboard' means the background pattern).
        boolean isEven = (index % 2 == 0);
        boolean isOdd = (index % 2 == 1);
        boolean evenRowEvenColumn = ((index / 8) % 2 == 0) && isEven;
        boolean oddRowOddColumn = ((index / 8) % 2 == 1) && isOdd;

        // Create the checkerboard pattern on the button backgrounds.
        if (evenRowEvenColumn || oddRowOddColumn) {
            currentTile.setBackgroundColor(ContextCompat.getColor(
                    getContext(), android.R.color.white));
        } else {
            currentTile.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorLightGray));
        }
    }

    /**
     * Locates the possible moves of the piece that is about to be highlighted.
     *
     * @param highlightedIndex the index containing the highlighted piece.
     * @param possibleMoves a list of the possible moves available to the highlighted piece.
     * @param board a HashMap representing an index on to board (0->63) the piece type at that location.
     */
    private void findPossibleMoves(final int highlightedIndex, final ArrayList<Integer> possibleMoves,
                                   ChessBoard board) {
        if (highlightedIndex < 0 || highlightedIndex > 64) {
            return;
        }

        Chess model = (Chess)mExperience;
        possibleMoves.clear();
        ChessPiece.PieceType highlightedPieceType = board.getPieceType(highlightedIndex);

        switch(highlightedPieceType) {
            case PAWN:
                ChessHelper.getPawnThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case ROOK:
                ChessHelper.getRookThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case KNIGHT:
                ChessHelper.getKnightThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case BISHOP:
                ChessHelper.getBishopThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case QUEEN:
                ChessHelper.getQueenThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case KING:
                boolean[] castlingBooleans = { model.primaryQueenSideRookHasMoved,
                        model.primaryKingSideRookHasMoved, model.primaryKingHasMoved,
                        model.secondaryQueenSideRookHasMoved, model.secondaryKingSideRookHasMoved,
                        model.secondaryKingHasMoved };
                ChessHelper.getKingThreatRange(possibleMoves, highlightedIndex, board,
                        castlingBooleans);
                break;
        }

    }

    /**
     * Handles the movement of the pieces.
     *
     * @param player indicates the current player (primary, secondary)
     * @param indexClicked the index of the clicked tile and the new position of the piece.
     * @param capturesPiece true if move captures pieces
     * @param board a ChessBoard object
     */
    private void handleMovement(final ChessPiece.ChessTeam player, final int indexClicked,
                                final boolean capturesPiece, ChessBoard board) {
        // Reset the highlighted tile's image.
        mHighlightedTile.setText(" ");
        int highlightedIndex = Integer.parseInt((String) mHighlightedTile.getTag());
        ChessPiece highlightedPiece = board.retrieve(highlightedIndex);

        // Handle capturing pieces.
        if (capturesPiece) {
            TextView capturedTile = (TextView) grid.getChildAt(indexClicked);
            capturedTile.setText(" ");
            board.delete(highlightedIndex);
        }

        // Check to see if our pawn can becomes another piece and put its value into the board map.

        if (indexClicked < 8 && highlightedPiece.isTeamPiece(ChessPiece.PieceType.PAWN, ChessPiece.ChessTeam.PRIMARY)) {
            promotePawn(indexClicked, ChessPiece.ChessTeam.PRIMARY);
        } else if (indexClicked > 55 && highlightedPiece.isTeamPiece(ChessPiece.PieceType.PAWN, ChessPiece.ChessTeam.SECONDARY)) {
            promotePawn(indexClicked, ChessPiece.ChessTeam.SECONDARY);
        } else {
            board.add(indexClicked, highlightedPiece);

            // Find the new tile and give it a piece.
            TextView newLocation = (TextView) grid.getChildAt(indexClicked);
            newLocation.setText(ChessPiece.getUnicodeText(board.getPieceType(indexClicked)));

            // Color the piece according to the player.
            if (player.equals(ChessPiece.ChessTeam.PRIMARY)) {
                newLocation.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            } else if (player.equals(ChessPiece.ChessTeam.SECONDARY)) {
                newLocation.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            }
        }

        // Handle the movement of the Rook for Castling
        boolean castlingKingSide = indexClicked == highlightedIndex + 2;
        boolean castlingQueenSide = indexClicked == highlightedIndex - 3;
        boolean isCastling = highlightedPiece.getPiece().equals(ChessPiece.PieceType.KING) &&
                (castlingKingSide || castlingQueenSide);
        if(isCastling) {
            int rookPrevIndex;
            int rookFutureIndex;
            // Handle the side-dependent pieces of the castle (king-side vs queen-side)
            if(castlingKingSide) {
                rookPrevIndex = highlightedIndex + 3;
                rookFutureIndex = highlightedIndex + 1;
            } else {
                rookPrevIndex = highlightedIndex - 4;
                rookFutureIndex = highlightedIndex - 2;
            }

            // Put a rook at the new rook position.
            board.add(rookFutureIndex, ChessPiece.PieceType.ROOK, player);
            TextView futureRook = (TextView) grid.getChildAt(rookFutureIndex);

            // Handle the player-dependent pieces of the castle (color)
            futureRook.setText(ChessPiece.getUnicodeText(ChessPiece.PieceType.ROOK));
            if (player.equals(ChessPiece.ChessTeam.PRIMARY)) {
                futureRook.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            } else if (player.equals(ChessPiece.ChessTeam.SECONDARY)) {
                futureRook.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            }

            // Get rid of the old rook.
            TextView previousRook = (TextView) grid.getChildAt(rookPrevIndex);
            previousRook.setText(" ");
            board.delete(rookPrevIndex);
        }

        // Handle the Castling Booleans.
        ChessPiece currentPiece = board.retrieve(highlightedIndex);
        Chess model = (Chess)mExperience;
        if (currentPiece != null) {
            if (currentPiece.isTeamPiece(ChessPiece.PieceType.KING, ChessPiece.ChessTeam.PRIMARY)) {
                model.primaryKingHasMoved = true;
            } else if (currentPiece.isTeamPiece(ChessPiece.PieceType.KING, ChessPiece.ChessTeam.SECONDARY)) {
                model.secondaryKingHasMoved = true;
            } else if (currentPiece.isTeamPiece(ChessPiece.PieceType.ROOK, ChessPiece.ChessTeam.PRIMARY)) {
                if (highlightedIndex == 0) {
                    model.primaryQueenSideRookHasMoved = true;
                } else if (highlightedIndex == 7) {
                    model.primaryKingSideRookHasMoved = true;
                }
            } else if (currentPiece.isTeamPiece(ChessPiece.PieceType.ROOK, ChessPiece.ChessTeam.SECONDARY)) {
                if (highlightedIndex == 56) {
                    model.secondaryQueenSideRookHasMoved = true;
                } else if (highlightedIndex == 63) {
                    model.secondaryKingSideRookHasMoved = true;
                }
            }
        }

        // Delete the piece's previous location and end the turn.
        board.delete(highlightedIndex);
        handleTurnChange(true);
        checkFinished(board);
    }

    /**
     * Handles changing the turn and turn indicator.
     * @param switchPlayer if false, just set up the UI views but don't switch the player turn.
     */
    private void handleTurnChange(final boolean switchPlayer) {
        boolean turn = ((Chess)mExperience).turn;
        if(switchPlayer) {
            turn = ((Chess) mExperience).toggleTurn();
        }

        // Handle the TextViews that serve as our turn indicator.
        TextView playerOneLeft = (TextView) mLayout.findViewById(R.id.leftIndicator1);
        TextView playerOneRight = (TextView) mLayout.findViewById(R.id.rightIndicator1);
        TextView playerTwoLeft = (TextView) mLayout.findViewById(R.id.leftIndicator2);
        TextView playerTwoRight = (TextView) mLayout.findViewById(R.id.rightIndicator2);

        if(turn) {
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

    /**
     * Handles the promotion of pawns once they reach the end of the board.
     *
     * @param position the position of the pawn when it is promoted.
     * @param team the team the pawn belongs to.
     */
    private void promotePawn(final int position, final ChessPiece.ChessTeam team) {
        // Generate an AlertDialog via the AlertDialog Builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(getString(R.string.PromotePawnMsg))
//                .setIcon(ChessPiece.getDrawableFor(PieceType.PAWN))
                .setView(R.layout.pawn_dialog);
        AlertDialog pawnChooser = alertDialogBuilder.create();
        pawnChooser.show();

        int color = (team == ChessPiece.ChessTeam.PRIMARY) ? ContextCompat.getColor(getContext(),
                R.color.colorPrimary) : ContextCompat.getColor(getContext(), R.color.colorAccent);

        // Change the Dialog's Icon color.
        int alertIconId = getActivity().getResources().getIdentifier("android:id/icon", null, null);
        ImageView alertIcon = (ImageView) pawnChooser.findViewById(alertIconId);
        if(alertIcon != null) {
            alertIcon.setColorFilter(color, SRC_ATOP);
        }
        // Setup the Queen Listeners and change color appropriate to the team.
        TextView queenIcon = (TextView) pawnChooser.findViewById(R.id.queen_icon);
        TextView queenText = (TextView) pawnChooser.findViewById(R.id.queen_text);
        if(queenIcon != null && queenText != null) {
            queenIcon.setText(ChessPiece.UC_QUEEN);
            queenIcon.setTextColor(color);
            queenIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            queenText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // Do the same for bishop.
        TextView bishopIcon = (TextView) pawnChooser.findViewById(R.id.bishop_icon);
        TextView bishopText = (TextView) pawnChooser.findViewById(R.id.bishop_text);
        if(bishopIcon != null && bishopText != null) {
            bishopIcon.setText(ChessPiece.UC_BISHOP);
            bishopIcon.setTextColor(color);
            bishopIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            bishopText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // And the same for knight.
        TextView knightIcon = (TextView) pawnChooser.findViewById(R.id.knight_icon);
        TextView knightText = (TextView) pawnChooser.findViewById(R.id.knight_text);
        if(knightIcon != null && knightText != null) {
            knightIcon.setText(ChessPiece.UC_KNIGHT);
            knightIcon.setTextColor(color);
            knightIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            knightText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // And finally, the same for rook.
        TextView rookIcon = (TextView) pawnChooser.findViewById(R.id.rook_icon);
        TextView rookText = (TextView) pawnChooser.findViewById(R.id.rook_text);
        if(rookIcon != null && rookText != null) {
            rookIcon.setText(ChessPiece.UC_ROOK);
            rookIcon.setTextColor(color);
            rookIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            rookText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
    }

    private class Promoter implements View.OnClickListener {
        AlertDialog mDialog;
        int position;
        ChessPiece.ChessTeam team;

        Promoter(final int indexClicked, final ChessPiece.ChessTeam teamNumber, AlertDialog dialog) {
            position = indexClicked;
            team = teamNumber;
            mDialog = dialog;
        }

        @Override public void onClick(final View v) {
            int id = v.getId();
            ChessPiece.PieceType pieceType;
            switch (id) {
                default:
                case R.id.queen_icon:
                case R.id.queen_text:
                    pieceType = ChessPiece.PieceType.QUEEN;
                    break;
                case R.id.bishop_icon:
                case R.id.bishop_text:
                    pieceType = ChessPiece.PieceType.BISHOP;
                    break;
                case R.id.knight_icon:
                case R.id.knight_text:
                    pieceType = ChessPiece.PieceType.KNIGHT;
                    break;
                case R.id.rook_icon:
                case R.id.rook_text:
                    pieceType = ChessPiece.PieceType.ROOK;
                    break;
            }

            ChessBoard board = ((Chess)mExperience).board;
            board.add(position, pieceType, team);
            TextView promotedPieceTile = (TextView) grid.getChildAt(position);
            promotedPieceTile.setText(ChessPiece.getUnicodeText(pieceType));
            int color = team == ChessPiece.ChessTeam.PRIMARY ? R.color.colorPrimary: R.color.colorAccent;
            promotedPieceTile.setTextColor(ContextCompat.getColor(getContext(), color));

            mDialog.dismiss();
        }
    }

    /**
     * A View.OnClickListener that is called whenever a board tile is clicked.
     */
    private class ChessClick implements View.OnClickListener {
        @Override public void onClick(final View v) {
            int index = Integer.parseInt((String)v.getTag());
            ChessBoard board = ((Chess) mExperience).board;
            boolean changedBoard = false;
            if (mHighlightedTile != null) {
                changedBoard = showPossibleMoves(index, board);
                mHighlightedTile = null;
            } else {
                if (board.retrieve(index) != null) {
                    mHighlightedTile = (TextView) v;
                    changedBoard = showPossibleMoves(index, board);
                }
            }
            if(changedBoard) {
                // Save any changes that have been made to the database
                ExperienceManager.instance.updateExperience(mExperience);
            }
        }
    }

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getChessMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getEntry(R.string.PlayTicTacToe, R.mipmap.ic_tictactoe_red, tictactoe));
        menu.add(getEntry(R.string.PlayCheckers, R.mipmap.ic_checkers, checkers));
        menu.add(getTintEntry(R.string.MyRooms, R.drawable.ic_casino_black_24dp));
        menu.add(getNoTintEntry(R.string.PlayAgain, R.mipmap.ic_chess));
        return menu;
    }

}
