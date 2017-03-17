package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.main.MainService;

import java.util.List;
import java.util.Locale;

/**
 * Provide a helper class for experience fragment classes.
 *
 * @author Paul Michael Reilly on 2/19/17.
 */

public class ExpHelper {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ExpHelper.class.getSimpleName();

    // Public and package private class methods.

    /** Return the base fragment associated with the data model. */
    public static BaseFragment getBaseFragment(final Experience model) {
        if (model == null)
            return null;
        FragmentType type = model.getExperienceType().getFragmentType();
        return DispatchManager.instance.getFragment(type);
    }

    /** Handle a new game by resetting the data model. */
    static void handleNewGame(final String name, final Experience experience) {
        // Ensure that the data model exists and is valid. Abort if not, logging an error message,
        // otherwise reset the data model and update the database.
        if (experience == null) {
            Log.e(TAG, String.format(Locale.US, "Null %s data model.", name));
            return;
        }
        if (experience.reset(getBaseFragment(experience)))
            ExperienceManager.instance.updateExperience(experience);
    }

    /** Return TRUE if this experience is in the "me" group. */
    @SuppressWarnings("unused")
    public static boolean isInMeGroup(final Experience experience) {
        // If either the 'me' group key or the current experience group key is null, return true
        // (assume we're in the 'me' situation), otherwise compare the account holder's me group
        // key to the experience's group key.
        String meGroupKey = AccountManager.instance.getMeGroupKey();
        String expGroupKey = experience != null ? experience.getGroupKey() : null;
        return meGroupKey == null || expGroupKey == null || meGroupKey.equals(expGroupKey);
    }

    /** Return TRUE iff the User has requested to play again. */
    public static boolean isPlayAgain(final Object tag, final String className) {
        // Determine if the given tag is the class name, i.e. a snackbar action request to play
        // again.
        return ((tag instanceof String && className.equals(tag)) ||
                (tag instanceof MenuEntry && ((MenuEntry) tag).titleResId == R.string.PlayAgain));
    }

    /** Process a tile click to establish or clear the selected position and the possible moves. */
    static void processTileClick(final int position, @NonNull final Experience model,
                                 @NonNull final Engine engine) {
        // Determine if a selection is active.  If so, and the click occurred on a possible move
        // position then handle the move.  Otherwise start a move by marking the selected
        // position and establishing the possible moves.
        List<Integer> possibleMoves = model.getBoard().getPossibleMoves();
        if (model.getBoard().hasSelectedPiece())
            if (possibleMoves.contains(position))
                // Handle the move to deal with cases like castling, captures, and pawn promotion.
                engine.handleMove(position);
            else
                engine.startMove(position);
        else
            engine.startMove(position);
    }

    /** Notify the user about an error and log it. */
    public static void reportError(final BaseFragment fragment, final int resId, String...
            args) {
        // Let the User know that something is amiss.
        String message = fragment.getContext().getString(resId);
        NotificationManager.instance.notifyNoAction(fragment, message);

        // Generate a logcat item casing on the given resource id.
        String format;
        switch (resId) {
            case R.string.ErrorCheckersCreation:
                format = "Failed to create a Checkers experience with group/room keys: {%s/%s}";
                Log.e(TAG, String.format(Locale.US, format, args[0], args[1]));
                break;
            default:
                break;
        }
    }

    /** Set the name for a given player index. */
    public static void setRoomName(final Experience model) {
        // Ensure that the name text view exists. Abort if not.  Set the value from the model if it
        // does.
        TextView name = getTextView(model, R.id.roomName);
        if (name == null)
            return;
        name.setText(RoomManager.instance.getRoomName(model.getRoomKey()));
    }

    /** Update the move on the database and generate notifications to room members. */
    public static void updateModel(@NonNull Experience model) {
        ExperienceManager.instance.updateExperience(model);
        BaseFragment fragment = getBaseFragment(model);
        Intent intent = new Intent(fragment.getActivity(), MainService.class);
        //intent.putExtra(MainService.NOTIFICATION_KEY, getJsonData(model));
        intent.putExtra(MainService.ROOM_KEY, model.getRoomKey());
        fragment.getContext().startService(intent);
    }

    // Private class methods.

    private static TextView getTextView(final Experience model, final int resId) {
        BaseFragment fragment = getBaseFragment(model);
        View layout = fragment != null ? fragment.getView() : null;
        return layout != null ? (TextView) layout.findViewById(resId) : null;

    }

    /** Set the virtual board (UI) from the data model. */
    private static void setBoard(@NonNull final Context context, @NonNull final Experience model,
                                 @NonNull final Checkerboard board) {
        // Reset all the cells to showing empty text with correct highlighting from the data model.
        int selectedPosition = model.getBoard().getSelectedPosition();
        if (selectedPosition != -1)
            board.handleTileBackground(context, selectedPosition);
        for (int index = 0; index < 64; index++) {
            board.getCell(index).setText("");
            board.handleTileBackground(context, index);
        }
        board.setHighlight(context, selectedPosition, model.getBoard().getPossibleMoves());

        // Add the pieces from the data model.
        for (String key : model.getBoard().getKeySet()) {
            int position = model.getBoard().getPosition(key);
            if (position == -1)
                continue;
            TextView view = board.getCell(position);
            Piece piece = model.getBoard().getPiece(position);
            view.setText(piece.getText());
            view.setTextColor(ContextCompat.getColor(context, piece.getTeam().color));
            view.setTypeface(null, piece.getTypeface());
        }
    }

    /** Set the name for a given player index. */
    private static void setPlayerName(final int resId, final int index, final Experience model) {
        // If the name text view exists, set the value from the model, otherwise abort.
        TextView name = getTextView(model, resId);
        if (name == null)
            return;
        name.setText(model.getPlayers().get(index).name);
        // If a user is assigned, don't allow click (no popup menu) and remove down-arrow drawable
        // used on player-2
        if (model.getPlayers().get(index).id != null && !model.getPlayers().get(index).id.equals("")) {
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

    /** Set the given visibility state on the give list of identifiers. */
    private static void setVisibility(final Experience model, final int state, final int... ids) {
        for (int resId : ids) {
            TextView view = getTextView(model, resId);
            if (view != null)
                view.setVisibility(state);
        }
    }

    /** Handle the turn indicator management by manipulating the turn icon size and decorations. */
    private static void setPlayerControls(final Experience model) {
        // Alternate the decorations on each player symbol.
        // Handle the TextViews that serve as the turn indicator.
        if (model.getTurn()) {
            // Make the primary team's decorations the more prominent.
            setVisibility(model, View.VISIBLE, R.id.leftIndicator1, R.id.rightIndicator1);
            setVisibility(model, View.INVISIBLE, R.id.leftIndicator2, R.id.rightIndicator2);
        } else {
            // Make the secondary team's decorations the more prominent.
            setVisibility(model, View.INVISIBLE, R.id.leftIndicator1, R.id.rightIndicator1);
            setVisibility(model, View.VISIBLE, R.id.leftIndicator2, R.id.rightIndicator2);
        }
    }

    /** Update the game state. */
    private static void setState(final Experience model) {
        // Generate a message string appropriate for a win or tie, or nothing if the game is active.
        State state = model != null ? model.getStateType() : null;
        TextView status = getTextView(model, R.id.status);
        if (state == null || status == null)
            return;
        Player winningPlayer = model.getWinningPlayer();
        String message = state.getMessage(getBaseFragment(model).getContext(), winningPlayer);
        status.setText(message);
    }

    /** Set the name for a given player index. */
    private static void setWinCount(final int resId, final int index, final Experience model) {
        // Ensure that the win count text view exists. Abort if not.  Set the value from the model
        // if it does.
        TextView winCount = getTextView(model, resId);
        if (winCount == null)
            return;
        winCount.setText(String.valueOf(model.getPlayers().get(index).winCount));
    }

    /** Update the UI using the current experience state from the database. */
    static void updateUiFromExperience(final Experience model, final Checkerboard board) {
        // Obtain a context to use to update the UI.  Abort if unable to do so.
        BaseFragment fragment = getBaseFragment(model);
        Context context = fragment != null ? fragment.getContext() : null;
        if (context == null)
            return;

        // A valid experience is available. Use the data model to populate the UI.
        setRoomName(model);
        setPlayerName(R.id.player1Name, 0, model);
        setPlayerName(R.id.player2Name, 1, model);
        setWinCount(R.id.player1WinCount, 0, model);
        setWinCount(R.id.player2WinCount, 1, model);
        setPlayerControls(model);
        setBoard(context, model, board);
        setState(model);
    }
}
