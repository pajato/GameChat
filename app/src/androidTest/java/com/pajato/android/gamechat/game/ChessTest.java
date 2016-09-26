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
import static org.hamcrest.Matchers.not;

/**
 * Tests the Chess feature of GameChat
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ChessTest extends BaseTest {

    /** Navigate to the chess game. */
    @Before public void navigateToGameFragment() {
        // Maneuver to the game pane.
        onView(withId(R.id.toolbar_game_icon))
                .perform(click());

        // Begin the process of starting a local checkers game
        onView(withId(R.id.gameFab))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.init_chess_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.settings_local_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Ensure we have arrived.
        onView(withId(R.id.checkers_panel))
                .check(matches(isDisplayed()));
    }

    /** Ensure that castling on the short side of the board works properly. */
    @Test public void testCastlingKingSide() {
        // Move the pawns to allow the bishops to move.
        tryToMovePiece(52, 44, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(12, 20, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        // Move the bishops.
        tryToMovePiece(61, 16, true, ChessPiece.getDrawableFor(ChessPiece.BISHOP));
        tryToMovePiece(5, 40, true, ChessPiece.getDrawableFor(ChessPiece.BISHOP));
        // Move the knights.
        tryToMovePiece(62, 47, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT));
        tryToMovePiece(6, 23, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT));
        // Castle for blue and ensure the Rook moves.
        tryToMovePiece(60, 62, true, ChessPiece.getDrawableFor(ChessPiece.KING));
        onView(withTagValue(equalTo((Object) 63)))
                .check(matches(noDrawable()));
        onView(withTagValue(equalTo((Object) 61)))
                .check(matches(withDrawable(ChessPiece.getDrawableFor(ChessPiece.ROOK))));
        // Castle for purple and ensure the Rook moves.
        tryToMovePiece(4, 6, true, ChessPiece.getDrawableFor(ChessPiece.KING));
        onView(withTagValue(equalTo((Object) 7)))
                .check(matches(noDrawable()));
        onView(withTagValue(equalTo((Object) 5)))
                .check(matches(withDrawable(ChessPiece.getDrawableFor(ChessPiece.ROOK))));
    }

    /** Ensure that castling on the long side of the board works properly. */
    @Test public void testCastlingQueenSide() {
        // Move the pawn to get the queen and bishop out of the way.
        tryToMovePiece(51, 35, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(11, 27, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        // Move the queens.
        tryToMovePiece(59, 43, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN));
        tryToMovePiece(3, 19, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN));
        // Move the bishops.
        tryToMovePiece(58, 44, true, ChessPiece.getDrawableFor(ChessPiece.BISHOP));
        tryToMovePiece(2, 20, true, ChessPiece.getDrawableFor(ChessPiece.BISHOP));
        // Move the knights.
        tryToMovePiece(57, 42, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT));
        tryToMovePiece(1, 18, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT));
        // Castle for blue and ensure the rook moves.
        tryToMovePiece(60, 57, true, ChessPiece.getDrawableFor(ChessPiece.KING));
        onView(withTagValue(equalTo((Object) 56)))
                .check(matches(noDrawable()));
        onView(withTagValue(equalTo((Object) 58)))
                .check(matches(withDrawable(ChessPiece.getDrawableFor(ChessPiece.ROOK))));
        // Castle for purple and ensure the rook moves.
        tryToMovePiece(4, 1, true, ChessPiece.getDrawableFor(ChessPiece.KING));
        onView(withTagValue(equalTo((Object) 0)))
                .check(matches(noDrawable()));
        onView(withTagValue(equalTo((Object) 2)))
                .check(matches(withDrawable(ChessPiece.getDrawableFor(ChessPiece.ROOK))));
    }

    /** Ensure that bishops can move diagonally in any direction, not horizontally or vertically. */
    @Test public void testMovementRangeBishop() {
        // Move the pawns out of the way of the bishops.
        tryToMovePiece(52, 44, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(12, 20, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));

        // Ensure that bishops can move diagonally but not vertically or horizontally.
        tryToMovePiece(61, 25, true, ChessPiece.getDrawableFor(ChessPiece.BISHOP));  // UpLeft
        tryToMovePiece(5, 40, true, ChessPiece.getDrawableFor(ChessPiece.BISHOP));   // DownLeft

        tryToMovePiece(25, 24, false, ChessPiece.getDrawableFor(ChessPiece.BISHOP)); // !Left
        tryToMovePiece(25, 33, false, ChessPiece.getDrawableFor(ChessPiece.BISHOP)); // !Down
        tryToMovePiece(25, 34, true, ChessPiece.getDrawableFor(ChessPiece.BISHOP));  // DownRight

        tryToMovePiece(40, 32, false, ChessPiece.getDrawableFor(ChessPiece.BISHOP)); // !Up
        tryToMovePiece(40, 41, false, ChessPiece.getDrawableFor(ChessPiece.BISHOP)); // !Right
        tryToMovePiece(40, 33, true, ChessPiece.getDrawableFor(ChessPiece.BISHOP));  // UpRight
    }

    /** Ensure that the king can move in any direction. */
    @Test public void testMovementRangeKing() {
        // Move the pawns out of the way of the Kings.
        tryToMovePiece(52, 44, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(12, 20, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));

        tryToMovePiece(60, 52, true, ChessPiece.getDrawableFor(ChessPiece.KING)); // Up
        tryToMovePiece(4, 12, true, ChessPiece.getDrawableFor(ChessPiece.KING)); // Down
        tryToMovePiece(52, 45, true, ChessPiece.getDrawableFor(ChessPiece.KING)); // UpRight
        tryToMovePiece(12, 19, true, ChessPiece.getDrawableFor(ChessPiece.KING)); // DownLeft
        tryToMovePiece(45, 36, true, ChessPiece.getDrawableFor(ChessPiece.KING)); // UpLeft
        tryToMovePiece(19, 28, true, ChessPiece.getDrawableFor(ChessPiece.KING)); // DownRight
        tryToMovePiece(36, 35, true, ChessPiece.getDrawableFor(ChessPiece.KING)); // Left
        tryToMovePiece(28, 29, true, ChessPiece.getDrawableFor(ChessPiece.KING)); // Right
    }

    /**
     * Ensure that the Knight's specific two-square-one-direction, one-square-another movement range
     * functions properly, and that the knights cannot move any other way.
     */
    @Test public void testMovementRangeKnight() {
        // Ensure that knights can move in their designated L shaped directions but not in lines.
        tryToMovePiece(62, 45, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // UpUpLeft
        tryToMovePiece(6, 21, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT));  // DownDownLeft
        tryToMovePiece(57, 42, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // UpUpRight
        tryToMovePiece(1, 18, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT));  // DownDownRight
        tryToMovePiece(45, 35, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // LeftLeftUp
        tryToMovePiece(21, 27, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // LeftLeftDown
        tryToMovePiece(42, 36, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // RightRightUp
        tryToMovePiece(18, 28, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // RightRightDown

        // Try moving in lines or diagonally with blue pieces.
        tryToMovePiece(35, 34, false, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // !Left
        tryToMovePiece(35, 42, false, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // !DownLeft
        tryToMovePiece(35, 43, false, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // !Down
        tryToMovePiece(35, 44, false, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // !DownRight
        // Pass the turn to purple and try to move in lines or diagonally with purple pieces.
        tryToMovePiece(48, 40, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(28, 29, false, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // !Right
        tryToMovePiece(28, 21, false, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // !UpRight
        tryToMovePiece(28, 20, false, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // !Up
        tryToMovePiece(28, 19, false, ChessPiece.getDrawableFor(ChessPiece.KNIGHT)); // !UpLeft
    }

    /** Ensure that the queen can move in any cardinal direction. */
    @Test public void testMovementRangeQueen() {
        // Move the pawns out of the way of the Queens.
        tryToMovePiece(52, 44, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(12, 20, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));

        tryToMovePiece(59, 38, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN)); // UpRight
        tryToMovePiece(3, 30, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN));  // DownRight
        tryToMovePiece(38, 39, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN)); // Right
        tryToMovePiece(30, 29, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN)); // Left
        tryToMovePiece(39, 31, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN)); // Up
        tryToMovePiece(29, 37, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN)); // Down
        tryToMovePiece(31, 38, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN)); // DownLeft
        tryToMovePiece(37, 28, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN)); // UpLeft
    }

    /** Ensure that Rooks can move horizontally and vertically  */
    @Test public void testMovementRangeRook() {
        // Move the pawns out of the way.
        tryToMovePiece(48, 32, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(8, 24, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        // Ensure that Rooks can move vertically and horizontally, but not diagonally.
        tryToMovePiece(56, 40, true, ChessPiece.getDrawableFor(ChessPiece.ROOK));
        tryToMovePiece(0, 16, true, ChessPiece.getDrawableFor(ChessPiece.ROOK));
        tryToMovePiece(40, 33, false, ChessPiece.getDrawableFor(ChessPiece.ROOK));
        tryToMovePiece(40, 42, true, ChessPiece.getDrawableFor(ChessPiece.ROOK));
        tryToMovePiece(16, 25, false, ChessPiece.getDrawableFor(ChessPiece.ROOK));
        tryToMovePiece(16, 18, true, ChessPiece.getDrawableFor(ChessPiece.ROOK));
    }

    /** Ensure the pawn moves properly and that it's various movement quirks function correctly. */
    @Test public void testMovementRangePawn() {
        // Ensure Pawns can move one place forward, but can't move when blocked.
        tryToMovePiece(52, 44, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(12, 20, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        // Ensure pawns can move two places forward but only when at their starting positions
        tryToMovePiece(44, 28, false, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(48, 32, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(20, 36, false, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(8, 24, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));

        // Setup pawns for a capture, then ensure pawns can capture.
        tryToMovePiece(49, 33, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(9, 25, true , ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(32, 25, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(24, 33, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
    }


    /** Ensures that pawn promotion occurs properly. */
    @Test public void testPawnPromotion() {
        // Setup a scenario where a pawn moves to the end of the board.
        tryToMovePiece(55, 39, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(14, 30, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(39, 30, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(6, 21, true, ChessPiece.getDrawableFor(ChessPiece.KNIGHT));
        tryToMovePiece(30, 22, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(15, 23, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(22, 14, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(23, 31, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));

        // Send the actual piece forward.
        onView(withTagValue(equalTo((Object) 14)))
                .check(matches(isDisplayed()))
                .perform(click());
        // Move it to the ending position
        onView(withTagValue(equalTo((Object) 6)))
                .check(matches(isDisplayed()))
                .perform(click());

        // Choose the queen in the pawn promotion window.
        onView(withId(R.id.pawn_promotion_window))
                .check(matches(isDisplayed()));
        onView(withText(R.string.chess_promotion_queen))
                .check(matches(isDisplayed()))
                .perform(click());
        // Ensure the pawn was replaced with a queen.
        onView(withTagValue(equalTo((Object) 6)))
                .check(matches(isDisplayed()))
                .check(matches(withDrawable(ChessPiece.getDrawableFor(ChessPiece.QUEEN))));
        // Swap the piece back to blue, then ensure the queen moves properly.
        tryToMovePiece(8, 16, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        tryToMovePiece(6, 22, true, ChessPiece.getDrawableFor(ChessPiece.QUEEN));
    }


    /** Ensure the turn is preserved on a new game. */
    @Test public void testNewGame() {
        // Ensure the turn is blue's.
        onView(withId(R.id.player_1_left_indicator))
                .check(matches(isDisplayed()));
        onView(withId(R.id.player_1_right_indicator))
                .check(matches(isDisplayed()));
        onView(withId(R.id.player_2_left_indicator))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.player_2_right_indicator))
                .check(matches(not(isDisplayed())));

        // Change turn and ensure the turn is purple's.
        tryToMovePiece(48, 40, true, ChessPiece.getDrawableFor(ChessPiece.PAWN));
        onView(withId(R.id.player_2_left_indicator))
                .check(matches(isDisplayed()));
        onView(withId(R.id.player_2_right_indicator))
                .check(matches(isDisplayed()));
        onView(withId(R.id.player_1_left_indicator))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.player_1_right_indicator))
                .check(matches(not(isDisplayed())));

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.new_game_chess))
                .check(matches(isDisplayed()))
                .perform(click());

        // Ensure the turn is still purple's.
        onView(withId(R.id.player_2_left_indicator))
                .check(matches(isDisplayed()));
        onView(withId(R.id.player_2_right_indicator))
                .check(matches(isDisplayed()));
        onView(withId(R.id.player_1_left_indicator))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.player_1_right_indicator))
                .check(matches(not(isDisplayed())));

        // Check that the piece we moved before is not there.
        onView(withTagValue(equalTo((Object) 48)))
                .check(matches(withDrawable(ChessPiece.getDrawableFor(ChessPiece.PAWN))));
        onView(withTagValue(equalTo((Object) 40)))
                .check(matches(noDrawable()));

    }


    /**
     * A helper method that attempts to move a piece from one position to another ensures that the
     * drawable icon for the piece ends up in the right spot (whether or not the piece has actually
     * moved).
     *
     * @param startingPosition the initial index of the piece we are moving.
     * @param endingPosition the index of the tile we want to try to move the piece to.
     * @param shouldMove true if we expect the piece to end up at the endingPosition, false if we don't.
     * @param expectedDrawable a drawable ID for our piece.
     */
    private void tryToMovePiece(final int startingPosition, final int endingPosition,
                                final boolean shouldMove, final int expectedDrawable) {
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
            onView(withTagValue(equalTo((Object) startingPosition)))
                    .check(matches(noDrawable()));
        } else {
            // Ensure that it does not contain the piece.
            onView(withTagValue(equalTo((Object) endingPosition)))
                    .check(matches(noDrawable()));
        }
    }

}
