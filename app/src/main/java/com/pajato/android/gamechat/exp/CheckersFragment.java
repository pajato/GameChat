package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.model.Checkers;
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
import static com.pajato.android.gamechat.exp.ExpFragmentType.chess;
import static com.pajato.android.gamechat.exp.ExpFragmentType.tictactoe;
import static com.pajato.android.gamechat.exp.model.Checkers.ACTIVE;
import static com.pajato.android.gamechat.exp.model.Checkers.PRIMARY_WINS;
import static com.pajato.android.gamechat.exp.model.Checkers.SECONDARY_WINS;

/**
 * A simple Checkers game for use in GameChat.
 *
 * @author Bryan Scott
 */
public class CheckersFragment extends BaseGameExpFragment {
    // We refer to the two sides as primary and secondary to differentiate between the two players.
    // (Primary pieces belong to player1, secondary belong to player 2).
    public static final String PRIMARY_PIECE = "pp";
    public static final String PRIMARY_KING = "pk";
    public static final String SECONDARY_PIECE = "sp";
    public static final String SECONDARY_KING = "sk";


    public ImageButton mHighlightedTile;
    public boolean mIsHighlighted = false;
    public ArrayList<Integer> mPossibleMoves;

    /** Visual layout of checkers board objects */
    private GridLayout grid;

    /** The lookup key for the FAB chess memu. */
    public static final String CHECKERS_FAM_KEY = "CheckersFamKey";

    /** logcat TAG */
    private static final String TAG = CheckersFragment.class.getSimpleName();

    // Public instance methods.

    /** Handle a FAM or Snackbar Checkers click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Determine if this event is for this fragment.  Abort if not.
        if (GameManager.instance.getCurrent() != ExpFragmentType.checkers.ordinal()) return;

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

    /** Handle an experience posting event to see if this is a checkers experience. */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Check the payload to see if this is not checkers.  Abort if not.
        if (event.experience == null || event.experience.getExperienceType() != ExpType.checkers) return;

        // The experience is a checkers experience.  Start the game.
        mExperience = event.experience;
        resume();
    }

    /** Setup the Player Controls. The Board setup will be done later. */
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setLayoutId(R.layout.fragment_checkers);
    }

    @Override public void onStart() {
        super.onStart();
        FabManager.game.setMenu(CHECKERS_FAM_KEY, getCheckersMenu());

        grid = (GridLayout) mLayout.findViewById(board);

        // Color the player icons.
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

    /** Return a default, partially populated, Checkers experience. */
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
        Checkers model = new Checkers(key, id, 0, name, tstamp, groupKey, roomKey, players);
        ExperienceManager.instance.createExperience(model);
    }

    /** Return a possibly null list of player information for a checkers experience (always 2 players) */
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
        inflater.inflate(R.menu.checkers_menu, menu);
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
        NotificationManager.instance.notify(this, getDoneMessage(model), true);
        model.state = Checkers.PENDING;
        ExperienceManager.instance.updateExperience(mExperience);
    }

    /** Set up the game board based on the data model state. */
    private void setGameBoard(@NonNull final Checkers model) {
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
        if (mExperience == null || !(mExperience instanceof Checkers)) return;

        // A valid experience is available. Use the data model to populate the UI and check if the
        // game is finished.
        Checkers model = (Checkers) mExperience;
        setPlayerName(R.id.player1Name, 0, model);
        setPlayerName(R.id.player2Name, 1, model);
        setPlayerWinCount(R.id.player1WinCount, 0, model);
        setPlayerWinCount(R.id.player2WinCount, 1, model);
        setPlayerIcons(model.turn);
        setGameBoard(model);
        setState(model);
    }

    // Determine if the particular cell is in an even row and even column or an odd row and odd
    // column. This will determine the background color for the cell.
    private boolean isEvenEvenOrOddOdd(int index, boolean isEven) {
        if (isEven) {
            return (index / 8) % 2 == 0;
        } else {
            return (index / 8) % 2 == 1;
        }
    }

    // Determine if the particular cell contains a piece for player 2 (secondary color)
    private boolean containsSecondaryPiece(int index, boolean isEven) {
        if ((-1 < index && index < 8) && isEven) { // first (top) row
            return true;
        } else if ((7 < index && index < 16) && !isEven) { // second row
            return true;
        } else if ((15 < index && index < 24) && isEven) { // third row
            return true;
        }
        return false;
    }

    // Determine if the particular cell contains a piece for player 1 (primary color)
    private boolean containsPrimaryPiece(int index, boolean isEven) {
        if ((39 < index && index < 48) && !isEven) { // first (top) row
            return true;
        } else if ((47 < index && index < 56) &&  isEven) { // second row
            return true;
        } else if ((55 < index && index < 64) && !isEven) { // third row
            return true;
        }
        return false;
    }

    // Set up an image button which will be a cell in the game board
    private ImageButton makeBoardButton(int index, int sideSize, Map<String, String> board, String pieceType) {
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
        String buttonTag = String.valueOf(index);
        currentTile.setTag(buttonTag);

        // Handle the checkerboard positions.
        boolean isEven = index % 2 == 0;

        // Create the checkerboard pattern on the button backgrounds.
        if (isEvenEvenOrOddOdd(index, isEven)) {
            currentTile.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
            currentTile.setImageResource(0);
        } else {
            currentTile.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            currentTile.setImageResource(0);
        }

        // If the tile is meant to contain a board piece at the start of play, give it a piece.
        if (containsSecondaryPiece(index, isEven) || (pieceType.equals(SECONDARY_PIECE) || pieceType.equals(SECONDARY_KING))) {
            currentTile.setImageResource(R.drawable.ic_account_circle_black_36dp);
            currentTile.setColorFilter(ContextCompat.getColor(getContext(),
                    R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            if(pieceType.equals(SECONDARY_KING)) {
                board.put(buttonTag, SECONDARY_KING);
            } else {
                board.put(buttonTag, SECONDARY_PIECE);
            }
        } else if (containsPrimaryPiece(index, isEven) || (pieceType.equals(PRIMARY_PIECE) || pieceType.equals(PRIMARY_KING))) {
            currentTile.setImageResource(R.drawable.ic_account_circle_black_36dp);
            currentTile.setColorFilter(ContextCompat.getColor(getContext(),
                    R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            if(pieceType.equals(PRIMARY_KING)) {
                board.put(buttonTag, PRIMARY_KING);
            } else {
                board.put(buttonTag, PRIMARY_PIECE);
            }
        }
        return currentTile;
    }

    /**
     * Handles starting game of checkers, resetting the board either for a new game or a restart
     * after loading a game board from the database.
     */
    private void startGame() {
        grid.removeAllViews();
        Checkers model = (Checkers)mExperience;
        boolean isNewBoard = false;
        if (model.board == null) {
            isNewBoard = true;
            model.board = new HashMap<>();
        }

        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
        if (winner != null) winner.setText("");

        mPossibleMoves = new ArrayList<>();

        int screenWidth = getActivity().findViewById(R.id.gameFragmentContainer).getWidth();
        Log.d(TAG, "screen width=" + screenWidth);
        int pieceSideLength = screenWidth / 8;

        // Go through and populate the GridLayout / board.
        for (int i = 0; i < 64; i++) {
            String pieceType = "";
            if(!isNewBoard) {
                pieceType = model.board.get(String.valueOf(i));
                if(pieceType == null) pieceType = "";
            }
            ImageButton currentTile = makeBoardButton(i, pieceSideLength, model.board, pieceType);
            currentTile.setOnClickListener(new CheckersClick());
            grid.addView(currentTile);
        }

        handleTurnChange(false);
    }

    /**
     * showPossibleMoves handles highlighting possible movement options of a clicked piece,
     * then on a subsequent click it removes those highlights.
     *
     * @param indexClicked the index of the tile clicked.
     */
    private void showPossibleMoves(final int indexClicked, final Map<String, String> board) {
        // If the game is over, we don't need to do anything.
        if(checkFinished(board)) {
            return;
        }

        boolean turn = ((Checkers)mExperience).turn;
        String highlightedIdxTag = (String) mHighlightedTile.getTag();
        int highlightedIndex = Integer.parseInt(highlightedIdxTag);
        findPossibleMoves(board, highlightedIndex, mPossibleMoves);

        // If a highlighted tile exists, we remove the highlight on it and its movement options.
        if(mIsHighlighted) {
            mHighlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.white));

            for (int possiblePosition : mPossibleMoves) {
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

                        } else if(!turn && (board.get(highlightedIdxTag).equals(SECONDARY_PIECE)
                                || board.get(highlightedIdxTag).equals(SECONDARY_KING))) {

                            handleMovement(board, false, indexClicked, capturesPiece);
                        }
                    }
                    // Clear the highlight off of all possible positions.
                    grid.getChildAt(possiblePosition).setBackgroundColor(ContextCompat
                            .getColor(getContext(), android.R.color.white));
                }
            }
            mHighlightedTile = null;

        // Otherwise, we need to highlight the tile clicked and its potential move squares with red.
        } else {
            mHighlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.holo_red_dark));
            for(int possiblePosition : mPossibleMoves) {
                if(possiblePosition != -1 && board.get(String.valueOf(possiblePosition)) == null) {
                    grid.getChildAt(possiblePosition).setBackgroundColor(ContextCompat
                            .getColor(getContext(), android.R.color.holo_red_light));
                }
            }
        }

        mIsHighlighted = !mIsHighlighted;
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
            String tmp = (String) board.get(String.valueOf(i));
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

        NotificationManager.instance.notify(this, winMsg, true);
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
                               final ArrayList<Integer> movementOptions) {
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
                                   final ArrayList<Integer> possibleMoves) {
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
        mHighlightedTile.setImageResource(0);
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
        ImageButton newLocation = (ImageButton) grid.getChildAt(indexClicked);
        if(board.get(indexClickedStr).equals(PRIMARY_KING) ||
                board.get(indexClickedStr).equals(SECONDARY_KING)) {
            newLocation.setImageResource(R.drawable.ic_stars_black_36dp);
        } else {
            newLocation.setImageResource(R.drawable.ic_account_circle_black_36dp);
        }

        // Color the piece according to the player.
        int color;
        if(player) {
            color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        } else {
            color = ContextCompat.getColor(getContext(), R.color.colorAccent);
        }
        newLocation.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        // Handle capturing pieces.
        boolean finishedJumping = true;
        if(capturesPiece) {
            int pieceCapturedIndex = (indexClicked + highlightedIndex) / 2;
            ImageButton capturedTile = (ImageButton) grid.getChildAt(pieceCapturedIndex);
            capturedTile.setImageResource(0);
            board.remove((String) capturedTile.getTag());

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
     */
    private void handleTurnChange(final boolean switchPlayer) {

        boolean turn = ((Checkers)mExperience).turn;
        if(switchPlayer) {
            turn = ((Checkers) mExperience).toggleTurn();
        }

        // Handle the textviews that serve as our turn indicator.
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
     * A View.OnClickListener that is called whenever a board tile is clicked.
     */
    private class CheckersClick implements View.OnClickListener {
        @Override public void onClick(final View v) {
            String tag = (String) v.getTag();
            int index = Integer.parseInt(tag);
            Map<String, String> board = ((Checkers)mExperience).board;
            if(mHighlightedTile != null) {
                showPossibleMoves(index, board);
                mHighlightedTile = null;
            } else {
                if(board.get(String.valueOf(index)) != null) {
                    mHighlightedTile = (ImageButton) v;
                    showPossibleMoves(index, board);
                }
            }
            // Save any changes that have been made to the database
            ExperienceManager.instance.updateExperience(mExperience);
        }
    }

        /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getCheckersMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getEntry(R.string.PlayTicTacToe, R.mipmap.ic_tictactoe_red, tictactoe));
        menu.add(getEntry(R.string.PlayChess, R.mipmap.ic_chess, chess));
        menu.add(getTintEntry(R.string.MyRooms, R.drawable.ic_casino_black_24dp));
        menu.add(getNoTintEntry(R.string.PlayAgain, R.mipmap.ic_checkers));
        return menu;
    }

}
