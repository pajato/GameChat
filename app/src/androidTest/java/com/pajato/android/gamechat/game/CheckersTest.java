package com.pajato.android.gamechat.game;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import com.pajato.android.gamechat.BaseTest;
import com.pajato.android.gamechat.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Tests the Checkers game feature of our MainActivity.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class CheckersTest extends BaseTest {

    private static final int mPieceDrawable = R.drawable.ic_account_circle_black_36dp;
    private static final int mKingDrawable = R.drawable.ic_stars_black_36dp;

    /** Navigate to the checkers game */
    @Before public void navigateToGameFragment() {
        // Maneuver to the game pane.
        onView(withId(R.id.toolbar_game_icon))
                .perform(click());

        // Begin the process of starting a local checkers game
        onView(withId(R.id.gameFab))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.init_checkers_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.settings_local_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Ensure we have arrived.
        onView(withId(R.id.checkers_panel))
                .check(matches(isDisplayed()));
    }

    /** Ensure there is no outside issues (i.e., with our before method) */
    @Test public void testDoNothing() {

    }

    /** Ensure that on a new game, if it was not blue's turn, it does not switch to blue's turn. */
    @Test public void testNewGameTurnChange() {
        // Move a blue piece to change the turn.
        checkTurnDisplay(true);
        tryMovingPieceToEmptyTile(41, 34, mPieceDrawable, true);

        // Start a new game.
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.new_game_checkers))
                .check(matches(isDisplayed()))
                .perform(click());

        // Ensure that the piece we moved is back where it started and it's not blue's turn.
        onView(withTagValue(is((Object) 41)))
                .check(matches(isDisplayed()))
                .check(matches(withDrawable(mPieceDrawable)));
        checkTurnDisplay(false);
        // Move a non-blue piece.
        tryMovingPieceToEmptyTile(16, 25, mPieceDrawable, true);
    }

    /** Ensure that pieces can only move on their turns, and cannot move into another piece. */
    @Test public void testPieceMovement() {
        // Ensure that it's currently blue's turn, then try to move a non-blue piece.
        checkTurnDisplay(true);
        tryMovingPieceToEmptyTile(16, 25, mPieceDrawable, false);
        // This should not accomplish anything. Now try to move a blue piece.
        checkTurnDisplay(true);
        tryMovingPieceToEmptyTile(41, 34, mPieceDrawable, true);

        // Ensure that the turn changes to non-blue. Now try to move a blue piece again.
        checkTurnDisplay(false);
        tryMovingPieceToEmptyTile(43, 36, mPieceDrawable, false);
        // This should not accomplish anything. Now try to move a non-blue piece.
        checkTurnDisplay(false);
        tryMovingPieceToEmptyTile(16, 25, mPieceDrawable, true);
        // Lastly, ensure it is blue's turn again.
        checkTurnDisplay(true);
    }

    /** Ensure that jumping a piece correctly removes the piece. */
    @Test public void testSingleJump() {
        // Setup the scenario where we can jump a non-blue piece with a blue piece.
        tryMovingPieceToEmptyTile(41, 34, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(16, 25, mPieceDrawable, true);
        // Initiate the jump and then ensure the non-blue piece was deleted.
        tryMovingPieceToEmptyTile(34, 16, mPieceDrawable, true);
        onView(withTagValue(is((Object) 25)))
                .check(matches(isDisplayed()))
                .check(matches(noDrawable()));
    }

    /** Ensure that jumping a piece does not swap the turn if there is another jump available. */
    @Test public void testChainJumps() {
        // Setup the scenario where we can jump two blue pieces with a non-blue piece.
        tryMovingPieceToEmptyTile(43, 36, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(16, 25, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(36, 27, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(22, 31, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(47, 38, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(25, 32, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(54, 47, mPieceDrawable, true);

        // Ensure it is not blue's turn.
        checkTurnDisplay(false);
        // Make jump one, then ensure it is still not blue's turn.
        tryMovingPieceToEmptyTile(18, 36, mPieceDrawable, true);
        onView(withTagValue(is((Object) 27)))
                .check(matches(isDisplayed()))
                .check(matches(noDrawable()));
        checkTurnDisplay(false);

        // Then jump again, and assert that it's blue's turn now.
        tryMovingPieceToEmptyTile(36, 54, mPieceDrawable, true);
        onView(withTagValue(is((Object) 45)))
                .check(matches(isDisplayed()))
                .check(matches(noDrawable()));
        checkTurnDisplay(true);
    }


    /** Ensure that kinging a piece works correctly. */
    @Test public void testKingPiece() {
        // Setup the situation for non-blue to get a king piece.
        tryMovingPieceToEmptyTile(41, 34, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(18, 27, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(43, 36, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(27, 41, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(52, 43, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(16, 25, mPieceDrawable, true);
        tryMovingPieceToEmptyTile(59, 52, mPieceDrawable, true);

        // Ensure that the movement to the final row makes a king.
        onView(withTagValue(equalTo((Object) 41)))
                .perform(click());
        onView(withTagValue(equalTo((Object) 59)))
                .perform(click())
                .check(matches(withDrawable(mKingDrawable)));

        // Swap the turns so the king piece can move.
        tryMovingPieceToEmptyTile(45, 38, mPieceDrawable, true);

        // Ensure that the king can then move backwards.
        tryMovingPieceToEmptyTile(59, 50, mKingDrawable, true);
    }

    /**
     * A utility testing method that facilitates piece movement to empty tiles. Trying to use this
     * method to move a piece into a tile that already contains another piece will likely cause
     * problems and false readings. Its intended use is solely for moving a piece to an empty tile.
     *
     * @param startingPosition the position the piece begins in.
     * @param endingPosition the position we are attempting to move the piece to.
     * @param expectedDrawable the drawable that represents our current tile.
     * @param shouldMove true if we expect the piece to move, false otherwise.
     */
    private void tryMovingPieceToEmptyTile(final int startingPosition, final int endingPosition,
                                           final int expectedDrawable, final boolean shouldMove) {
        // The starting tile should have a piece.
        onView(withTagValue(equalTo((Object) startingPosition)))
                .check(matches(isDisplayed()))
                .check(matches(withDrawable(expectedDrawable)))
                .perform(click());
        // Move it to the ending position
        onView(withTagValue(equalTo((Object) endingPosition)))
                .check(matches(isDisplayed()))
                .perform(click());

        if(shouldMove) {
            onView(withTagValue(equalTo((Object) endingPosition)))
                    .check(matches(withDrawable(expectedDrawable)));
        } else {
            // Ensure that it does not contain the piece.
            onView(withTagValue(equalTo((Object) endingPosition)))
                    .check(matches(noDrawable()));
        }
    }

    /**
     * Checks if the associated turn display views are visible or invisible depending on
     * the turn parameter.
     *
     * @param turn true for blue's turn, false otherwise.
     */
    private void checkTurnDisplay(final boolean turn) {
        if(turn) {
            // Ensure the player one indicators are visible and the player two indicators are not.
            onView(withId(R.id.leftIndicator1))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.rightIndicator2))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.leftIndicator2))
                    .check(matches(not(isDisplayed())));
            onView(withId(R.id.rightIndicator2))
                    .check(matches(not(isDisplayed())));
        } else {
            // Ensure the player two indicators are visible and the player one indicators are not.
            onView(withId(R.id.leftIndicator2))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.rightIndicator2))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.leftIndicator1))
                    .check(matches(not(isDisplayed())));
            onView(withId(R.id.rightIndicator2))
                    .check(matches(not(isDisplayed())));
        }
    }

}
