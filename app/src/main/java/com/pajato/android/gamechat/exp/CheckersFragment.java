package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Account;
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
import com.pajato.android.gamechat.exp.model.CheckersBoard;
import com.pajato.android.gamechat.exp.model.ExpProfile;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.main.ProgressManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.exp.ExpFragmentType.checkers;
import static com.pajato.android.gamechat.exp.ExpFragmentType.chess;
import static com.pajato.android.gamechat.exp.ExpFragmentType.tictactoe;
import static com.pajato.android.gamechat.exp.ExpType.checkers_exp;
import static com.pajato.android.gamechat.exp.model.Checkers.ACTIVE;

/**
 * A simple Checkers game for use in GameChat.
 *
 * @author Bryan Scott
 */
public class CheckersFragment extends BaseGameExpFragment implements View.OnClickListener {
    // We refer to the two sides as primary and secondary to differentiate between the two players.
    // (Primary pieces belong to player1, secondary belong to player 2).
    private static final int PRIMARY_PIECE = 1;
    private static final int PRIMARY_KING = 2;
    private static final int SECONDARY_PIECE = 3;
    private static final int SECONDARY_KING = 4;

    // CheckersBoard Management Objects
    private GridLayout grid;
//    private SparseIntArray boardMap;
//    private ImageButton highlightedTile;
//    private boolean mIsHighlighted = false;
//    private ArrayList<Integer> possibleMoves;

    /** The lookup key for the FAB chess_exp memu. */
    public static final String CHECKERS_FAM_KEY = "CheckersFamKey";

    /** logcat TAG */
    private static final String TAG = CheckersFragment.class.getSimpleName();

    // Public instance methods.

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_checkers;}

    /** Handle a FAM or Snackbar Checkers click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Determine if this event is for this fragment.  Abort if not.
        if (GameManager.instance.getCurrent() != checkers.ordinal()) return;

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

    /** Handle a click on the checkers board by verifying the click and handing it off. */
    @Override public void onClick(final View view) {
        Object tag = view.getTag();
        // TODO: we probably don't have buttons starting with 'button' here
        if (tag instanceof String && ((String) tag).startsWith("button"))
            handleTileClick((String) tag);
    }

    /** Handle an experience posting event to see if this is a checkers experience. */
    @Subscribe public void onExperienceChange(final ExperienceChangeEvent event) {
        // Check the payload to see if this is not checkers.  Abort if not.
        if (event.experience == null || event.experience.getExperienceType() != checkers_exp) return;

        // The experience is a checkers experience.  Start the game.
        mExperience = event.experience;
        resume();
    }

    @Override public void onInitialize() {
        super.onInitialize();
        FabManager.game.setMenu(CHECKERS_FAM_KEY, getCheckersMenu());

        grid = (GridLayout) mLayout.findViewById(R.id.board);
        mTurn = true;
////////        onNewGame();

        // Color the turn tiles.
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
        FabManager.game.setMenu(this, CHECKERS_FAM_KEY);
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
        String team = context.getString(R.string.teamRed);
        result.add(new Player(name, "", team));
        name = getPlayerName(getPlayer(players, 1), context.getString(R.string.friend));
        team = context.getString(R.string.teamBlack);
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

    /** Return the game state after applying the given button move to the data model. */
    private int getState(@NonNull final Checkers model, String buttonTag) {
        // Check to see if the game is a tie, with all moves exhausted.
        // TODO: WHAT TO DO HERE
//        if (model.state == ACTIVE && model.board.grid.size() == 9) return Checkers.TIE;

        // Not a tie.  Determine the winner state based on the current play given by the button tag.
//        int value = model.getSymbolValue();
//        if (model.state == ACTIVE) return getState(model, value, buttonTag);

        // TODO: Complete this!!!!

        return model.state;
    }

    /** Return the game state after applying the given button move to the data model. */
    private int getState(@NonNull final Checkers model, final int value, final String buttonTag) {
        // TODO: IMPLEMENT THIS
        // Case on the button tag to update the appropriate tallies.
        switch (buttonTag) {
            default: break;
        }

        return model.state;
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
        // TODO: What should we do here??
//        model.board = null;
        model.state = ACTIVE;
        ExperienceManager.instance.updateExperience(mExperience);
    }

    /** Handle a click on a given tile by updating the value on the tile and start the next turn. */
    private void handleTileClick(final String buttonTag) {
        // Ensure that the click occurred on a grid button and that the data model is not empty.
        // Abort if not.
        View view = mLayout.findViewWithTag(buttonTag);
        Checkers model = getModel();
        // TODO: Implement game playing !!!
//        if (view == null || !(view instanceof Button) || model == null) return;
//
//        // Handle the button click based on the current state.
//        Button button = (Button) view;
//        switch (model.state) {
//            case ACTIVE:
//                // Ensure that the click occurred on an empty button.
//                if (button.getText().length() != 0) {
//                    // The click occurred on a played button.  Warn the User and ignore the play.
//                    NotificationManager.instance.notify(this, R.string.InvalidButton);
//                    return;
//                }
//                break;
//            default:
//                // In all other cases, clear the board to start a new game.
//                initBoard(model);
//                model.board = null;
//                model.state = ACTIVE;
//                NotificationManager.instance.notify(this, R.string.StartNewGame);
//                break;
//        }
//
//        // Update the database with the collected changes.
//        if (model.board == null) model.board = new TTTBoard();
//        model.board.grid.put(buttonTag, model.getSymbolText());
//        model.state = getState(model, buttonTag);
//        model.setWinCount();
        model.toggleTurn();
        ExperienceManager.instance.updateExperience(mExperience);
    }

    /** Initialize the board model and values and clear the winner text */
    private void initBoard(@NonNull final Checkers model) {
        // TODO: IMPLEMENT THIS
        // Ensure that the layout has been established. Abort if not.
        if (mLayout == null) return;

        // Clear the board, the winner text, the board evaluation support and reset all peices to
        // starting position.

//        if (model.board != null) model.board.clear();
//        TextView winner = (TextView) mLayout.findViewById(R.id.winner);
//        if (winner != null) winner.setText("");
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                // Seed the array with a value that will guarantee the X vs O win and a tie will be
//                // calculated correctly; clear the grid button.
//                String tag = String.format(Locale.US, "button%d%d", i, j);
//                TextView button = (TextView) mLayout.findViewWithTag(tag);
//                if (button != null) button.setText("");
//            }
//        }
    }

    /** Process a resumption by testing and waiting for the experience */
    private void resume() {
        if (mExperience == null) {
            // Disable the layout and startup the spinner.
            mLayout.setVisibility(View.GONE);
            String title = "Checkers";
            String message = "Waiting for the database to provide the game...";
            ProgressManager.instance.show(getContext(), title, message);
        } else {
            // Start the game and update the views using the current state of the experience.
            mLayout.setVisibility(View.VISIBLE);
            setTitles(mExperience.getGroupKey(), mExperience.getRoomKey());
            ProgressManager.instance.hide();
            updateExperience();
            if (getModel().board == null) {
                onNewGame();
            }
        }
    }

    /** Handle the turn indicator management by manipulating the turn icon size and decorations. */
    private void setPlayerIcons(final boolean turn) {
        // TODO: Determine if we need this & implement
        // Alternate the decorations on each player symbol.
        if (turn)
            // Make player1's decorations the more prominent.
            setPlayerIcons(R.id.player_1_icon, R.id.leftIndicator1, R.id.rightIndicator1,
                    R.id.player_2_icon, R.id.leftIndicator2, R.id.rightIndicator2);
        else
            // Make player2's decorations the more prominent.
            setPlayerIcons(R.id.player_2_icon, R.id.leftIndicator2, R.id.rightIndicator2,
                    R.id.player_1_icon, R.id.leftIndicator1, R.id.rightIndicator1);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.checkers_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
    private void setPlayerName(final int resId, final int index, final Checkers model) {
        // Ensure that the name text view exists. Abort if not.  Set the value from the model if it
        // does.
        TextView name = (TextView) mLayout.findViewById(resId);
        if (name == null) return;
        name.setText(model.players.get(index).name);
    }

    // TODO: what do do for checkers here???
    /** Set the sigil for a given player. */
    private void setPlayerSymbol(final int resId, final int index, final Checkers model) {
        // Ensure that the sigil text view exists.  Abort if not, set the value from the data
        // model if it does.
        TextView symbol = (TextView) mLayout.findViewById(resId);
        if (symbol == null) return;
        symbol.setText(model.players.get(index).symbol);
    }
    /** Set the team (red or black) for a given player. */
    // TODO: This probably needs some rework to get the team color figured out...
    private void setPlayerTeam(final int resId, final int index, final Checkers model) {
        // Ensure that the team text view exists.  Abort if not, set the value from the data
        // model if it does.
        TextView symbol = (TextView) mLayout.findViewById(resId);
        if (symbol == null) return;
        symbol.setText(model.players.get(index).team);
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
            case Checkers.RED_WINS:
            case Checkers.BLACK_WINS:
                String name = model.getWinningPlayerName();
                String format = "%1$s Wins!";
                message = String.format(Locale.getDefault(), format, name);
                break;
            case Checkers.TIE:
                message = "Tie!";
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
        // TODO: figure this out!
        if (model.board == null)
            initBoard(model);
        // else .. TODO: finish this

    }

    /** Update the UI using the current experience state from the database. */
    private void updateExperience() {
        // Ensure that a valid experience exists.  Abort if not.
        if (mExperience == null || !(mExperience instanceof Checkers)) return;

        // A valid experience is available. Use the data model to populate the UI and check if the
        // game is finished.
        Checkers model = (Checkers) mExperience;
        setPlayerName(R.id.player1Name, 0, model);
        setPlayerName(R.id.player2Name, 1, model);
        setPlayerWinCount(R.id.player1WinCount, 0, model);
        setPlayerWinCount(R.id.player2WinCount, 1, model);
        setPlayerSymbol(R.id.player_1_icon, 0, model);
        setPlayerSymbol(R.id.player_2_icon, 1, model);
        setPlayerIcons(model.turn);
        setGameBoard(model);
        setState(model);
    }

    /**
     * Handles a new game of checkers_exp, resetting the board.
     */
    private void onNewGame() {
        grid.removeAllViews();
        CheckersBoard board = getModel().board;
        if (board == null) getModel().board = new CheckersBoard();
        board.boardMap = new SparseIntArray();
        board.boardMap.clear();
        board.possibleMoves = new ArrayList<>();
        board.possibleMoves.clear();

        // Go through and populate the GridLayout / CheckersBoard.
        for(int i = 0; i < 64; i++) {
            ImageButton currentTile = new ImageButton(getContext());

            // Set up the gridlayout params, so that each cell is functionally identical.
            int screenWidth = mLayout.findViewById(R.id.checkers_panel).getWidth();
            int pieceSideLength = screenWidth / 8;
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = pieceSideLength;
            param.width = pieceSideLength;
            param.rightMargin = 0;
            param.topMargin = 0;
            param.setGravity(Gravity.CENTER);
            param.rowSpec = GridLayout.spec(i / 8);
            param.columnSpec = GridLayout.spec(i % 8);

            // Set up the Tile-specific information.
            currentTile.setLayoutParams(param);
            currentTile.setTag(i);

            // Handle the checkerboard positions.
            boolean isEven = (i % 2 == 0);
            boolean isOdd = (i % 2 == 1);
            boolean evenRowEvenColumn = ((i / 8) % 2 == 0) && isEven;
            boolean oddRowOddColumn = ((i / 8) % 2 == 1) && isOdd;

            // Handle the starting piece positions.
            boolean isTopRow = (-1 < i && i < 8) && isEven;
            boolean isSecondRow = (7 < i && i < 16) && isOdd;
            boolean isThirdRow = (15 < i && i < 24) && isEven;

            boolean isThirdBottomRow = (39 < i && i < 48) && isOdd;
            boolean isSecondBottomRow = (47 < i && i < 56) && isEven;
            boolean isBottomRow = (55 < i && i < 64) && isOdd;

            boolean containsSecondaryPiece = isTopRow || isSecondRow || isThirdRow;
            boolean containsPrimaryPiece = isThirdBottomRow || isSecondBottomRow || isBottomRow;

            // Create the checkerboard pattern on the button backgrounds.
            if(evenRowEvenColumn || oddRowOddColumn) {
                currentTile.setBackgroundColor(ContextCompat.getColor(
                        getContext(), android.R.color.white));
                currentTile.setImageResource(0);
            } else {
                currentTile.setBackgroundColor(ContextCompat.getColor(
                        getContext(), android.R.color.darker_gray));
                currentTile.setImageResource(0);
            }

            // If the tile is meant to contain a board piece at the start of play, give it a piece.
            if(containsSecondaryPiece) {
                currentTile.setImageResource(R.drawable.ic_account_circle_black_36dp);
                currentTile.setColorFilter(ContextCompat.getColor(getContext(),
                        R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                board.boardMap.put(i, SECONDARY_PIECE);
            } else if (containsPrimaryPiece) {
                currentTile.setImageResource(R.drawable.ic_account_circle_black_36dp);
                currentTile.setColorFilter(ContextCompat.getColor(getContext(),
                        R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                board.boardMap.put(i, PRIMARY_PIECE);
            }
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
    private void showPossibleMoves(final int indexClicked) {
        // If the game is over, we don't need to do anything.
        if(checkFinished()) {
            return;
        }

        CheckersBoard board = getModel().board;
        int highlightedIndex = (int) board.highlightedTile.getTag();
        findPossibleMoves(highlightedIndex, board.possibleMoves);

        // If a highlighted tile exists, we remove the highlight on it and its movement options.
        if(board.mIsHighlighted) {
            board.highlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.white));

            for (int possiblePosition : board.possibleMoves) {
                if(possiblePosition != -1 && board.boardMap.get(possiblePosition, -1) == -1) {

                    // If the tile clicked is one of the possible positions, and it's the correct
                    // turn/piece combination, the piece moves there.
                    if(indexClicked == possiblePosition) {
                        boolean capturesPiece = (indexClicked > 9 + highlightedIndex) ||
                                (indexClicked < highlightedIndex - 9);

                        if(mTurn && (board.boardMap.get(highlightedIndex) == PRIMARY_PIECE ||
                                board.boardMap.get(highlightedIndex) == PRIMARY_KING)) {

                            handleMovement(true, indexClicked, capturesPiece);

                        } else if(!mTurn && (board.boardMap.get(highlightedIndex) == SECONDARY_PIECE
                                || board.boardMap.get(highlightedIndex) == SECONDARY_KING)) {

                            handleMovement(false, indexClicked, capturesPiece);
                        }
                    }
                    // Clear the highlight off of all possible positions.
                    grid.getChildAt(possiblePosition).setBackgroundColor(ContextCompat
                            .getColor(getContext(), android.R.color.white));
                }
            }
            board.highlightedTile = null;

        // Otherwise, we need to highlight the tile clicked and its potential move squares with red.
        } else {
            board.highlightedTile.setBackgroundColor(ContextCompat.getColor(getContext(),
                    android.R.color.holo_red_dark));
            for(int possiblePosition : board.possibleMoves) {
                if(possiblePosition != -1 && board.boardMap.get(possiblePosition, -1) == -1) {
                    grid.getChildAt(possiblePosition).setBackgroundColor(ContextCompat
                            .getColor(getContext(), android.R.color.holo_red_light));
                }
            }
        }

        board.mIsHighlighted = !board.mIsHighlighted;
    }

    /**
     * Checks to see if the game is over or not by counting the number of primary / secondary pieces
     * on the board. If there are zero of one type of piece, then the other side wins.
     *
     * @return true if the game is over, false otherwise.
     */
    private boolean checkFinished() {
        // Generate win conditions. If one side runs out of pieces, the other side wins.
        int yCount = 0;
        int bCount = 0;
        for(int i = 0; i < 64; i++) {
            int tmp = getModel().board.boardMap.get(i);
            if(tmp == PRIMARY_PIECE || tmp == PRIMARY_KING) {
                bCount++;
            } else if (tmp == SECONDARY_PIECE || tmp == SECONDARY_KING) {
                yCount++;
            }
        }
        // Verify win conditions. If one passes, return true and generate an endgame snackbar.
        if(yCount == 0) {
            NotificationManager.instance.notify(this, "Game Over, Player 1 wins", true);
            return true;
        } else if (bCount == 0) {
            NotificationManager.instance.notify(this, "Game Over, Player 2 wins", true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Finds the "jumpable" pieces that the current piece could potentially capture.
     *
     * @param highlightedIndex the current index of the tile looking to jump.
     * @param jumpable the index of the tile that the piece can potentially jump to.
     */
    private void findJumpables(final int highlightedIndex, final int jumpable,
                               final ArrayList<Integer> movementOptions) {
        // Create the boolean calculations for each of our conditions.
        boolean withinBounds = jumpable < 64 && jumpable > -1;
        boolean emptySpace = getModel().board.boardMap.get(jumpable, -1) == -1;
        boolean breaksBorders = (highlightedIndex % 8 == 1 && jumpable % 8 == 7)
                || (highlightedIndex % 8 == 6 && jumpable % 8 == 0);
        boolean jumpsAlly = false;

        // Check if the piece being jumped is an ally piece.
        int highlightedPieceType = getModel().board.boardMap.get(highlightedIndex);
        int jumpedIndex = (highlightedIndex + jumpable) / 2;
        int jumpedPieceType = getModel().board.boardMap.get(jumpedIndex);

        if (highlightedPieceType == PRIMARY_PIECE || highlightedPieceType == PRIMARY_KING) {
            jumpsAlly = jumpedPieceType == PRIMARY_PIECE || jumpedPieceType == PRIMARY_KING;

        } else if (highlightedPieceType == SECONDARY_PIECE
                || highlightedPieceType == SECONDARY_KING) {
            jumpsAlly = jumpedPieceType == SECONDARY_PIECE || jumpedPieceType == SECONDARY_KING;
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
    private void findPossibleMoves(final int highlightedIndex,
                                   final ArrayList<Integer> possibleMoves) {
        if(highlightedIndex < 0 || highlightedIndex > 64) {
            return;
        }

        possibleMoves.clear();
        int highlightedPieceType = getModel().board.boardMap.get(highlightedIndex);

        // Get the possible positions, post-move, for the piece.
        int upLeft = highlightedIndex - 9;
        int upRight = highlightedIndex - 7;
        int downLeft = highlightedIndex + 7;
        int downRight = highlightedIndex + 9;

        // Handle vertical edges of the board and non-king pieces.
        if(highlightedIndex / 8 == 0 || highlightedPieceType == SECONDARY_PIECE) {
            upLeft = -1;
            upRight = -1;
        } else if (highlightedIndex / 8 == 7 || highlightedPieceType == PRIMARY_PIECE) {
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
        if(getModel().board.boardMap.get(upLeft, -1) != -1) {
            findJumpables(highlightedIndex, upLeft - 9, possibleMoves);
            upLeft = -1;
        }
        if(getModel().board.boardMap.get(upRight, -1) != -1) {
            findJumpables(highlightedIndex, upRight - 7, possibleMoves);
            upRight = -1;
        }
        if(getModel().board.boardMap.get(downLeft, -1) != -1) {
            findJumpables(highlightedIndex, downLeft + 7, possibleMoves);
            downLeft = -1;
        }
        if(getModel().board.boardMap.get(downRight, -1) != -1) {
            findJumpables(highlightedIndex, downRight + 9, possibleMoves);
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
    private void handleMovement(final boolean player, final int indexClicked,
                                final boolean capturesPiece) {
        // Reset the highlighted tile's image.
        getModel().board.highlightedTile.setImageResource(0);
        int highlightedIndex = (int) getModel().board.highlightedTile.getTag();
        int highlightedPieceType = getModel().board.boardMap.get(highlightedIndex);

        // Check to see if our piece becomes a king piece and put its value into the board map.
        if(indexClicked < 8 && highlightedPieceType == PRIMARY_PIECE) {
            getModel().board.boardMap.put(indexClicked, PRIMARY_KING);
        } else if (indexClicked > 55 && highlightedPieceType == SECONDARY_PIECE) {
            getModel().board.boardMap.put(indexClicked, SECONDARY_KING);
        } else {
            getModel().board.boardMap.put(indexClicked, highlightedPieceType);
        }

        // Find the new tile and give it a piece.
        ImageButton newLocation = (ImageButton) grid.getChildAt(indexClicked);
        if(getModel().board.boardMap.get(indexClicked) == PRIMARY_KING ||
                getModel().board.boardMap.get(indexClicked) == SECONDARY_KING) {
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
            getModel().board.boardMap.delete(pieceCapturedIndex);

            // If there are no more jumps, change turns. If there is at least one jump left, don't.
            ArrayList<Integer> possibleJumps = new ArrayList<>();
            findPossibleMoves(indexClicked, possibleJumps);
            for(int possiblePosition: possibleJumps) {
                if(possiblePosition != -1 && (possiblePosition > 9 + indexClicked
                        || (possiblePosition < indexClicked - 9))) {
                    finishedJumping = false;
                }
            }
        }

        getModel().board.boardMap.delete(highlightedIndex);
        handleTurnChange(finishedJumping);
        checkFinished();
    }

    /**
     * Handles changing the turn and turn indicator.
     */
    private void handleTurnChange(final boolean switchPlayer) {
        if(switchPlayer) {
            mTurn = !mTurn;
        }

        // Handle the textviews that serve as our turn indicator.
        TextView playerOneLeft = (TextView) mLayout.findViewById(R.id.leftIndicator1);
        TextView playerOneRight = (TextView) mLayout.findViewById(R.id.rightIndicator1);
        TextView playerTwoLeft = (TextView) mLayout.findViewById(R.id.leftIndicator2);
        TextView playerTwoRight = (TextView) mLayout.findViewById(R.id.rightIndicator2);

        if(mTurn) {
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
            int index = (int) v.getTag();
            CheckersBoard board = getModel().board;
            if(board.highlightedTile != null) {
                showPossibleMoves(index);
                board.highlightedTile = null;
            } else {
                if(board.boardMap.get(index, -1) != -1) {
                    board.highlightedTile = (ImageButton) v;
                    showPossibleMoves(index);
                }
            }
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
