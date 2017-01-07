package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.model.Checkers;
import com.pajato.android.gamechat.exp.model.Chess;
import com.pajato.android.gamechat.exp.model.ExpProfile;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.R.id.board;
import static com.pajato.android.gamechat.exp.ExpFragmentType.checkers;
import static com.pajato.android.gamechat.exp.ExpFragmentType.tictactoe;
import static com.pajato.android.gamechat.exp.model.Chess.ACTIVE;

/**
 * A simple Chess game for use in GameChat.
 *
 * @author Bryan Scott
 */
public class ChessFragment extends BaseGameExpFragment {

    // Chess Management Objects
    private ImageButton mHighlightedTile;
    private boolean mIsHighlighted = false;
    private ArrayList<Integer> mPossibleMoves;

    // Castle Management Objects
    // TODO: these should go into database?
    private boolean mPrimaryQueenSideRookHasMoved;
    private boolean mPrimaryKingSideRookHasMoved;
    private boolean mPrimaryKingHasMoved;
    private boolean mSecondaryQueenSideRookHasMoved;
    private boolean mSecondaryKingSideRookHasMoved;
    private boolean mSecondaryKingHasMoved;

    /** Visual layout of chess board objects */
    private GridLayout grid;

    /** The lookup key for the FAB chess memu. */
    public static final String CHESS_FAM_KEY = "ChessFamKey";

    /** logcat TAG */
    private static final String TAG = ChessFragment.class.getSimpleName();

    // Public instance methods.

    /** Handle a FAM or Snackbar Chess click event. */
    @Subscribe
    public void onClick(final TagClickEvent event) {
        // Determine if this event is for this fragment.  Abort if not.
        if (GameManager.instance.getCurrent() != ExpType.chess.ordinal()) return;

        // The event is either a snackbar action (start a new game) or a FAM menu entry.  Detect and
        // handle a snackbar action first.
        Object tag = event.view.getTag();
        if (isPlayAgain(tag, TAG)) {
            // Dismiss the FAB (assuming it was the source of the click --- being wrong is ok, and
            // setup a new game.
            FabManager.game.dismissMenu(this);
            handleNewGame();
        }
    }

    /** Handle an experience posting event to see if this is a chess experience. */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Check the payload to see if this is not chess.  Abort if not.
        if (event.experience == null || event.experience.getExperienceType() != ExpType.chess) return;

        // The experience is a chess experience.  Start the game.
        mExperience = event.experience;
        resume();
    }

    /** Setup the Player Controls. The Board setup will be done later. */
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setLayoutId(R.layout.fragment_checkers);
    }

    @Override public void onStart() {
        // Setup the board and start a new game to create the board.
        super.onStart();
        FabManager.game.setMenu(CHESS_FAM_KEY, getChessMenu());

        grid = (GridLayout) mLayout.findViewById(board);

        // Color the Player Icons.
        ImageView playerOneIcon = (ImageView) mLayout.findViewById(R.id.player_1_icon);
        playerOneIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary),
                PorterDuff.Mode.SRC_ATOP);

        ImageView playerTwoIcon = (ImageView) mLayout.findViewById(R.id.player_2_icon);
        playerTwoIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent),
                PorterDuff.Mode.SRC_ATOP);
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
    protected void createExperience(final Context context, final List<Account> playerAccounts) {
        // Setup the default key, players, and name.
        String key = getExperienceKey();
        List<Player> players = getDefaultPlayers(context, playerAccounts);
        String name1 = players.get(0).name;
        String name2 = players.get(1).name;
        long tstamp = new Date().getTime();
        String name = String.format(Locale.US, "%s vs %s on %s", name1, name2, tstamp);

        // Set up the default group and room keys, the owner id and return the value.
        String groupKey = GroupManager.instance.getGroupKey();
        String roomKey = RoomManager.instance.getRoomKey(groupKey);
        String id = getOwnerId();
        // TODO: DEFINE LEVEL INT ENUM VALUES - this is passing "0" for now
        Chess model = new Chess(key, id, 0, name, tstamp, groupKey, roomKey, players);
        ExperienceManager.instance.createExperience(model);
    }

    /** Return a possibly null list of player information for a chess experience (always 2 players) */
    protected List<Account> getPlayers(final Dispatcher<ExpFragmentType, ExpProfile> dispatcher) {
        // Determine if this is an offline experience in which no accounts are provided.
        Account player1 = AccountManager.instance.getCurrentAccount();
        if (player1 == null) return null;

        // This is an online experience.  Use the current signed in User as the first player.
        List<Account> players = new ArrayList<>();
        players.add(player1);

        // Determine the second account, if any, based on the room.
        String key = dispatcher.roomKey;
        Room room = key != null ? RoomManager.instance.roomMap.get(key) : null;
        int type = room != null ? room.type : -1;
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
            String title = "Chess";
            String message = "Waiting for the database to provide the game...";
            ProgressManager.instance.show(getContext(), title, message);
        } else {
            // Start the game and update the views using the current state of the experience.
            mLayout.setVisibility(View.VISIBLE);
            setTitles(mExperience.getGroupKey(), mExperience.getRoomKey());
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

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chess_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        if (model.board == null)
            startGame();
        else {
            // TODO: handle a game reloaded from the database - startGame should work here, but it isn't tested
            // startGame();
        }
   }

    /** Update the UI using the current experience state from the database. */
    private void updateUiFromExperience() {
        // Ensure that a valid experience exists.  Abort if not.
        if (mExperience == null || !(mExperience instanceof Chess)) return;

        // A valid experience is available. Use the data model to populate the UI and check if the
        // game is finished.
        Chess model = (Chess) mExperience;
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
     * @param index
     * @param sideSize
     * @param board a HashMap representing an index on to board (0->63) the piece type at that location.
     * @param pieceType
     */
    private ImageButton makeBoardButton(int index, int sideSize, Map<String, ChessPiece> board, String pieceType) {
        ImageButton currentTile = new ImageButton(getContext());

        // Set up the gridlayout params, so that each cell is functionally identical.
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = sideSize;
        param.width = sideSize;
        param.rightMargin = 0;
        param.topMargin = 0;
        param.setGravity(Gravity.CENTER);
        param.rowSpec = GridLayout.spec(index / 8);
        param.columnSpec = GridLayout.spec(index % 8);

        // Set up the Tile-specific information.
        currentTile.setLayoutParams(param);
        currentTile.setTag(String.valueOf(index));
        handleTileBackground(index, currentTile); // ??????????????????????? TODO: look into this!

        // Handle the chess starting piece positions.
        boolean containsSecondaryPlayerPiece = index < 16;
        boolean containsPrimaryPlayerPiece = index > 47;
        boolean containsPiece = containsPrimaryPlayerPiece || containsSecondaryPlayerPiece;
        int team;
        int color;

        // If the tile is meant to contain a board piece at the start of play, give it a piece.
        if (containsPiece) {
            if (containsPrimaryPlayerPiece) {
                team = ChessPiece.PRIMARY_TEAM;
                color = R.color.colorPrimary;
            } else {
                team = ChessPiece.SECONDARY_TEAM;
                color = R.color.colorAccent;
            }
            switch(index) {
                default:
                    board.put(String.valueOf(index), new ChessPiece(ChessPiece.PAWN, team));
                    currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.PAWN));
                    break;
                case 0: case 7:
                case 56: case 63:
                    board.put(String.valueOf(index), new ChessPiece(ChessPiece.ROOK, team));
                    currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.ROOK));
                    break;
                case 1: case 6:
                case 57: case 62:
                    board.put(String.valueOf(index), new ChessPiece(ChessPiece.KNIGHT, team));
                    currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.KNIGHT));
                    break;
                case 2: case 5:
                case 58: case 61:
                    board.put(String.valueOf(index), new ChessPiece(ChessPiece.BISHOP, team));
                    currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.BISHOP));
                    break;
                case 3:
                case 59:
                    board.put(String.valueOf(index), new ChessPiece(ChessPiece.QUEEN, team));
                    currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.QUEEN));
                    break;
                case 4:
                case 60:
                    board.put(String.valueOf(index), new ChessPiece(ChessPiece.KING, team));
                    currentTile.setImageResource(ChessPiece.getDrawableFor(ChessPiece.KING));
            }
            currentTile.setColorFilter(ContextCompat.getColor(getContext(), color),
                    PorterDuff.Mode.SRC_ATOP);
        }

        return currentTile;
    }

    /**
     * Handles starting game of chess, resetting the board either for a new game or a restart
     * after loading a game board from the database.
     */
    private void startGame() {
        grid.removeAllViews();
        Chess model = (Chess)mExperience;
        boolean isNewBoard = false;
        if (model.board == null) {
            isNewBoard = true;
            model.board = new HashMap<>();
        }

        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        if (winner != null) winner.setText("");

        // Reset the castling booleans.
        mPrimaryQueenSideRookHasMoved = false;
        mPrimaryKingSideRookHasMoved = false;
        mPrimaryKingHasMoved = false;
        mSecondaryQueenSideRookHasMoved = false;
        mSecondaryKingSideRookHasMoved = false;
        mSecondaryKingHasMoved = false;

        mPossibleMoves = new ArrayList<>();

        int screenWidth = getActivity().findViewById(R.id.gameFragmentContainer).getWidth();
        Log.d(TAG, "screen width=" + screenWidth);
        int pieceSideLength = screenWidth / 8;

        // Go through and populate the GridLayout / board.
        for (int i = 0; i < 64; i++) {
            String pieceType = "";
            if(!isNewBoard) {
                pieceType = model.board.get(String.valueOf(i)).getPiece();
                if(pieceType == null) pieceType = "";
            }
            ImageButton currentTile = makeBoardButton(i, pieceSideLength, model.board, pieceType);
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
     * @param board a HashMap representing a board index (0->63) to the piece type at that location.
     * @return true if we've made any updates that should be written to the database; false otherwise
     */
    private boolean showPossibleMoves(final int indexClicked, Map<String, ChessPiece> board) {
        // If the game is over, we don't need to do anything.
        if (checkFinished(board)) {
            return false;
        }

        boolean hasChanged = false;
        boolean turn = ((Chess)mExperience).turn;
        int highlightedIndex = Integer.parseInt((String) mHighlightedTile.getTag());
        findPossibleMoves(highlightedIndex, mPossibleMoves, board);

        // If a highlighted tile exists, we remove the highlight on it and its movement options.
        if (mIsHighlighted) {
            handleTileBackground(highlightedIndex, mHighlightedTile);

            for (int possiblePosition : mPossibleMoves) {
                // If the tile clicked is one of the possible positions, and it's the correct
                // turn/piece combination, the piece moves there.
                if (indexClicked == possiblePosition) {
                    boolean capturesPiece = (indexClicked > 9 + highlightedIndex) ||
                            (indexClicked < highlightedIndex - 9);

                    if (turn && (board.get(String.valueOf(highlightedIndex)).getTeam()
                            == ChessPiece.PRIMARY_TEAM)) {
                        handleMovement(true, indexClicked, capturesPiece, board);
                        hasChanged = true;

                    } else if (!turn && (board.get(String.valueOf(highlightedIndex)).getTeam()
                            == ChessPiece.SECONDARY_TEAM)) {
                        handleMovement(false, indexClicked, capturesPiece, board);
                        hasChanged = true;
                    }
                }
                handleTileBackground(possiblePosition, (ImageButton) grid.getChildAt(possiblePosition));
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
     * Checks to see if the game is over or not by counting the number of primary / secondary pieces
     * on the board. If there are zero of one type of pieces, then the other side wins.
     * @param board a HashMap representing an index on to board (0->63) the piece type at that location.
     *
     * @return true if the game is over, false otherwise.
     */
    private boolean checkFinished(Map<String, ChessPiece> board) {
        // Generate win conditions. If one side runs out of pieces, the other side wins.
        int secondaryKing = 0;
        int primaryKing = 0;

        for(int i = 0; i < 64; i++) {
            ChessPiece tmp = board.get(String.valueOf(i));
            if (tmp != null) {
                if (tmp.getPiece().equals(ChessPiece.KING)) {
                    if (tmp.getTeam() == ChessPiece.PRIMARY_TEAM) {
                        primaryKing++;
                    } else if (tmp.getTeam() == ChessPiece.SECONDARY_TEAM) {
                        secondaryKing++;
                    }
                }
            }
        }
        // Verify win conditions. If one passes, return true and generate an endgame snackbar.
        if (secondaryKing == 0) {
            NotificationManager.instance.notify(this, "Game Over! Player 1 Wins!", true);
            return true;
        } else if (primaryKing == 0) {
            NotificationManager.instance.notify(this, "Game Over! Player 2 Wins!", true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * A utility method that facilitates keeping the board's checker pattern in place throughout the
     * highlighting and de-higlighting process. It accepts a tile and sets its background to white
     * or dark grey, depending on its location in the board.
     *
     * @param index the index of the tile, used to determine the color of the background.
     * @param currentTile the tile whose color we are changing.
     */
    private void handleTileBackground(final int index, final ImageButton currentTile) {
        // Handle the checkerboard positions.
        boolean isEven = (index % 2 == 0);
        boolean isOdd = (index % 2 == 1);
        boolean evenRowEvenColumn = ((index / 8) % 2 == 0) && isEven;
        boolean oddRowOddColumn = ((index / 8) % 2 == 1) && isOdd;

        // Create the checkerboard pattern on the button backgrounds.
        if (evenRowEvenColumn || oddRowOddColumn) {
            currentTile.setBackgroundColor(ContextCompat.getColor(
                    getContext(), android.R.color.white));
        } else {
            currentTile.setBackgroundColor(ContextCompat.getColor(
                    getContext(), android.R.color.darker_gray));
        }
    }

    /**
     * Locates the possible moves of the piece that is about to be highlighted.
     *
     * @param highlightedIndex the index containing the highlighted piece.
     * @param possibleMoves
     * @param board a HashMap representing an index on to board (0->63) the piece type at that location.
     */
    private void findPossibleMoves(final int highlightedIndex, final ArrayList<Integer> possibleMoves,
                                   Map<String, ChessPiece> board) {
        if (highlightedIndex < 0 || highlightedIndex > 64) {
            return;
        }

        possibleMoves.clear();
        String highlightedPieceType = board.get(String.valueOf(highlightedIndex)).getPiece();

        switch(highlightedPieceType) {
            case ChessPiece.PAWN:
                ChessPiece.getPawnThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case ChessPiece.ROOK:
                ChessPiece.getRookThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case ChessPiece.KNIGHT:
                ChessPiece.getKnightThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case ChessPiece.BISHOP:
                ChessPiece.getBishopThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case ChessPiece.QUEEN:
                ChessPiece.getQueenThreatRange(possibleMoves, highlightedIndex, board);
                break;
            case ChessPiece.KING:
                boolean[] castlingBooleans = { mPrimaryQueenSideRookHasMoved,
                        mPrimaryKingSideRookHasMoved, mPrimaryKingHasMoved,
                        mSecondaryQueenSideRookHasMoved, mSecondaryKingSideRookHasMoved,
                        mSecondaryKingHasMoved };
                ChessPiece.getKingThreatRange(possibleMoves, highlightedIndex, board,
                        castlingBooleans);
                break;
        }

    }

    /**
     * Handles the movement of the pieces.
     *
     * @param player indicates the current player. True is primary, False is secondary.
     * @param indexClicked the index of the clicked tile and the new position of the piece.
     * @param capturesPiece
     * @param board a HashMap representing an index on to board (0->63) the piece type at that location.
     */
    private void handleMovement(final boolean player, final int indexClicked,
                                final boolean capturesPiece, Map<String, ChessPiece> board) {
        // Reset the highlighted tile's image.
        mHighlightedTile.setImageResource(0);
        int highlightedIndex = Integer.parseInt((String) mHighlightedTile.getTag());
        ChessPiece highlightedPiece = board.get(String.valueOf(highlightedIndex));

        // Handle capturing pieces.
        if (capturesPiece) {
            ImageButton capturedTile = (ImageButton) grid.getChildAt(indexClicked);
            capturedTile.setImageResource(0);
            board.remove(String.valueOf(indexClicked));
        }

        // Check to see if our pawn can becomes another piece and put its value into the board map.
        if (indexClicked < 8 && highlightedPiece.getPiece().equals(ChessPiece.PAWN) &&
                highlightedPiece.getTeam() == ChessPiece.PRIMARY_TEAM) {
            promotePawn(indexClicked, ChessPiece.PRIMARY_TEAM);
        } else if (indexClicked > 55 && highlightedPiece.getPiece().equals(ChessPiece.PAWN) &&
                highlightedPiece.getTeam() == ChessPiece.SECONDARY_TEAM) {
            promotePawn(indexClicked, ChessPiece.SECONDARY_TEAM);
        } else {
            board.put(String.valueOf(indexClicked), highlightedPiece);

            // Find the new tile and give it a piece.
            ImageButton newLocation = (ImageButton) grid.getChildAt(indexClicked);
            newLocation.setImageResource(ChessPiece.getDrawableFor(
                    board.get(String.valueOf(indexClicked)).getPiece()));

            // Color the piece according to the player.
            int color;
            if (player) {
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            } else {
                color = ContextCompat.getColor(getContext(), R.color.colorAccent);
            }
            newLocation.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }

        // Handle the movement of the Rook for Castling
        boolean castlingKingSide = indexClicked == highlightedIndex + 2;
        boolean castlingQueenSide = indexClicked == highlightedIndex - 3;
        boolean isCastling = board.get(String.valueOf(highlightedIndex)).getPiece().equals(ChessPiece.KING) &&
                (castlingKingSide || castlingQueenSide);
        if(isCastling) {
            int color;
            int team;
            // Handle the player-dependent pieces of the castle.
            if(player) {
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
                team = ChessPiece.PRIMARY_TEAM;
            } else {
                color = ContextCompat.getColor(getContext(), R.color.colorAccent);
                team = ChessPiece.SECONDARY_TEAM;
            }
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
            board.put(String.valueOf(rookFutureIndex), new ChessPiece(ChessPiece.ROOK, team));
            ImageButton futureRook = (ImageButton) grid.getChildAt(rookFutureIndex);
            futureRook.setImageResource(ChessPiece.getDrawableFor(ChessPiece.ROOK));
            futureRook.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            // Get rid of the old rook.
            ImageButton previousRook = (ImageButton) grid.getChildAt(rookPrevIndex);
            previousRook.setImageResource(0);
            board.remove(String.valueOf(rookPrevIndex));
        }

        // Handle the Castling Booleans.
        ChessPiece currentPiece = board.get(String.valueOf(highlightedIndex));
        if(currentPiece.getPiece().equals(ChessPiece.KING)) {
            if(currentPiece.getTeam() == ChessPiece.PRIMARY_TEAM) {
                mPrimaryKingHasMoved = true;
            } else {
                mSecondaryKingHasMoved = true;
            }
        } else if (currentPiece.getPiece().equals(ChessPiece.ROOK)) {
            if(currentPiece.getTeam() == ChessPiece.PRIMARY_TEAM) {
                if(highlightedIndex == 0) {
                    mPrimaryQueenSideRookHasMoved = true;
                } else if (highlightedIndex == 7) {
                    mPrimaryKingSideRookHasMoved = true;
                }
            } else {
                if(highlightedIndex == 56) {
                    mSecondaryQueenSideRookHasMoved = true;
                } else if (highlightedIndex == 63) {
                    mSecondaryKingSideRookHasMoved = true;
                }
            }
        }

        // Delete the piece's previous location and end the turn.
        board.remove(String.valueOf(highlightedIndex));
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
    private void promotePawn(final int position, final int team) {
        // Generate an AlertDialog via the AlertDialog Builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Promote Your Pawn:")
                .setIcon(ChessPiece.getDrawableFor(ChessPiece.PAWN))
                .setView(R.layout.pawn_dialog);
        AlertDialog pawnChooser = alertDialogBuilder.create();
        pawnChooser.show();

        int color = (team == ChessPiece.PRIMARY_TEAM) ? ContextCompat.getColor(getContext(),
                R.color.colorPrimary) : ContextCompat.getColor(getContext(), R.color.colorAccent);

        // Change the Dialog's Icon color.
        int alertIconId = getActivity().getResources().getIdentifier("android:id/icon", null, null);
        ImageView alertIcon = (ImageView) pawnChooser.findViewById(alertIconId);
        if(alertIcon != null) {
            alertIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        // Setup the Queen Listeners and change color appropriate to the team.
        ImageView queenIcon = (ImageView) pawnChooser.findViewById(R.id.queen_icon);
        TextView queenText = (TextView) pawnChooser.findViewById(R.id.queen_text);
        if(queenIcon != null && queenText != null) {
            queenIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            queenIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            queenText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // Do the same for bishop.
        ImageView bishopIcon = (ImageView) pawnChooser.findViewById(R.id.bishop_icon);
        TextView bishopText = (TextView) pawnChooser.findViewById(R.id.bishop_text);
        if(bishopIcon != null && bishopText != null) {
            bishopIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            bishopIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            bishopText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // And the same for knight.
        ImageView knightIcon = (ImageView) pawnChooser.findViewById(R.id.knight_icon);
        TextView knightText = (TextView) pawnChooser.findViewById(R.id.knight_text);
        if(knightIcon != null && knightText != null) {
            knightIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            knightIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            knightText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
        // And finally, the same for rook.
        ImageView rookIcon = (ImageView) pawnChooser.findViewById(R.id.rook_icon);
        TextView rookText = (TextView) pawnChooser.findViewById(R.id.rook_text);
        if(rookIcon != null && rookText != null) {
            rookIcon.setOnClickListener(new Promoter(position, team, pawnChooser));
            rookIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            rookText.setOnClickListener(new Promoter(position, team, pawnChooser));
        }
    }

    private class Promoter implements View.OnClickListener {
        AlertDialog mDialog;
        int position;
        int team;

        Promoter(final int indexClicked, final int teamNumber, AlertDialog dialog) {
            position = indexClicked;
            team = teamNumber;
            mDialog = dialog;
        }

        @Override public void onClick(final View v) {
            int id = v.getId();
            String pieceType;
            switch (id) {
                default:
                case R.id.queen_icon:
                case R.id.queen_text:
                    pieceType = ChessPiece.QUEEN;
                    break;
                case R.id.bishop_icon:
                case R.id.bishop_text:
                    pieceType = ChessPiece.BISHOP;
                    break;
                case R.id.knight_icon:
                case R.id.knight_text:
                    pieceType = ChessPiece.KNIGHT;
                    break;
                case R.id.rook_icon:
                case R.id.rook_text:
                    pieceType = ChessPiece.ROOK;
                    break;
            }

            Map<String, ChessPiece> board = ((Chess)mExperience).board;
            board.put(String.valueOf(position), new ChessPiece(pieceType, team));
            ImageButton promotedPieceTile = (ImageButton) grid.getChildAt(position);
            promotedPieceTile.setImageResource(ChessPiece.getDrawableFor(pieceType));
            boolean teamColor = team == ChessPiece.PRIMARY_TEAM;
            int color = teamColor ? R.color.colorPrimary: R.color.colorAccent;
            promotedPieceTile.setColorFilter(ContextCompat.getColor(getContext(), color),
                    PorterDuff.Mode.SRC_ATOP);

            mDialog.dismiss();
        }
    }

    /**
     * A View.OnClickListener that is called whenever a board tile is clicked.
     */
    private class ChessClick implements View.OnClickListener {
        @Override public void onClick(final View v) {
            int index = Integer.parseInt((String)v.getTag());
            Map<String, ChessPiece> board = ((Chess) mExperience).board;
            boolean changedBoard = false;
            if (mHighlightedTile != null) {
                changedBoard = showPossibleMoves(index, board);
                mHighlightedTile = null;
            } else {
                if (board.get(String.valueOf(index)) != null) {
                    mHighlightedTile = (ImageButton) v;
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
