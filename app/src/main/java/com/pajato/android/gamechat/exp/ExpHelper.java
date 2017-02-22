package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.support.annotation.NonNull;
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
        experience.reset();
        ExperienceManager.instance.updateExperience(experience);
    }

    /** Handle changing the turn and turn indicator for a given turn state. */
    public static void handleTurnChange(final Experience model, final boolean switchPlayer) {
        // Ensure that there is a valid layout for the base fragment.  Abort if not, otherwise
        // process the data model turn property.
        View layout = ExpHelper.getBaseFragment(model).getView();
        if (layout == null)
            return;
        boolean turn = model.getTurn();
        if (switchPlayer)
            turn = model.toggleTurn();

        // Handle the TextViews that serve as the turn indicator.
        TextView playerOneLeft = (TextView) layout.findViewById(R.id.leftIndicator1);
        TextView playerOneRight = (TextView) layout.findViewById(R.id.rightIndicator1);
        TextView playerTwoLeft = (TextView) layout.findViewById(R.id.leftIndicator2);
        TextView playerTwoRight = (TextView) layout.findViewById(R.id.rightIndicator2);

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

    // Private class methods.

    /** Return a done message text to show in a snackbar.  The given model provides the state. */
    private static String getDoneMessage(final Experience model) {
        // Determine if there is a winner.  If not, return the "tie" message.
        String name = model.getWinningPlayer().name;
        String format = name != null ? getString(model, R.string.WinMessageNotificationFormat)
                : null;
        String message = format != null ? String.format(Locale.getDefault(), format, name) : null;
        return message != null ? message : getString(model, R.string.TieMessageNotification);
    }

    private static String getString(final Experience model, final int resId) {
        BaseFragment fragment = getBaseFragment(model);
        Context context = fragment != null ? fragment.getContext() : null;
        return context != null ? context.getString(resId) : null;
    }

    private static TextView getTextView(final Experience model, final int resId) {
        BaseFragment fragment = getBaseFragment(model);
        View layout = fragment != null ? fragment.getView() : null;
        return layout != null ? (TextView) layout.findViewById(resId) : null;

    }

    /** Set the name for a given player index. */
    private static void setPlayerName(final int resId, final int index, final Experience model) {
        // Ensure that the name text view exists. Abort if not.  Set the value from the model if it
        // does.
        TextView name = getTextView(model, resId);
        if (name == null)
            return;
        name.setText(model.getPlayers().get(index).name);
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

    /** Handle the turn indicator management by manipulating the turn icon size and decorations. */
    private static void setPlayerIcons(final Experience model) {
        // Alternate the decorations on each player symbol.
        if (model == null)
            return;
        if (model.getTurn())
            // Make player1's decorations the more prominent.
            setPlayerIcons(model, R.id.leftIndicator1, R.id.rightIndicator1,
                    R.id.leftIndicator2, R.id.rightIndicator2);
        else
            // Make player2's decorations the more prominent.
            setPlayerIcons(model, R.id.leftIndicator2, R.id.rightIndicator2,
                    R.id.leftIndicator1, R.id.rightIndicator1);
    }

    /** Manage a particular player's symbol decorations. */
    private static void setPlayerIcons(@NonNull final Experience model, final int largeLeft,
                                       final int largeRight, final int smallLeft,
                                       final int smallRight) {
        // Collect all the pertinent textViews.
        TextView tvLargeLeft = getTextView(model, largeLeft);
        TextView tvLargeRight = getTextView(model, largeRight);
        TextView tvSmallLeft = getTextView(model, smallLeft);
        TextView tvSmallRight = getTextView(model, smallRight);
        if (tvLargeLeft == null || tvLargeRight == null || tvSmallLeft == null
                || tvSmallRight == null)
            return;

        // Deal with the tvLarger symbol's decorations.
        tvLargeLeft.setVisibility(View.VISIBLE);
        tvLargeRight.setVisibility(View.VISIBLE);

        // Deal with the tvSmall symbol's decorations.
        tvSmallLeft.setVisibility(View.INVISIBLE);
        tvSmallRight.setVisibility(View.INVISIBLE);
    }

    /** Update the game state. */
    private static void setState(final Experience model) {
        // Generate a message string appropriate for a win or tie, or nothing if the game is active.
        if (model == null)
            return;
        String message = null;
        State state = model.getStateType();
        switch (state) {
            case primary_wins:
            case secondary_wins:
                String name = model.getWinningPlayer().name;
                String format = name != null ? getString(model, state.resId) : null;
                message = format != null ? String.format(Locale.getDefault(), format, name) : null;
                break;
            case check:
            case tie:
                message = getString(model, state.resId);
                break;
            default:
                // keep playing or waiting for a new game
                break;
        }
        // Determine if the game has ended (winner or time). Abort if not.
        if (message == null)
            return;

        // Update the UI to celebrate the winner or a tie and update the database game state to
        // pending.
        TextView winner = getTextView(model, R.id.winner);
        if (winner == null)
            return;
        winner.setText(message);
        winner.setVisibility(View.VISIBLE);
        NotificationManager.instance.notifyGameDone(getBaseFragment(model), getDoneMessage(model));
        model.setStateType(State.pending);
        ExperienceManager.instance.updateExperience(model);
    }

    /** Update the UI using the current experience state from the database. */
    public static void updateUiFromExperience(final Experience model, final Checkerboard board) {
        // Ensure that a valid experience exists.  Abort if not.
        BaseFragment fragment = getBaseFragment(model);
        Context context = fragment != null ? fragment.getContext() : null;
        if (context == null)
            return;

        // A valid experience is available. Use the data model to populate the UI and check if the
        // game is finished.
        setRoomName(model);
        setPlayerName(R.id.player1Name, 0, model);
        setPlayerName(R.id.player2Name, 1, model);
        setWinCount(R.id.player1WinCount, 0, model);
        setWinCount(R.id.player2WinCount, 1, model);
        setPlayerIcons(model);
        if (board != null)
            board.setBoardFromModel(context, model.getBoard());
        setState(model);
    }
}
