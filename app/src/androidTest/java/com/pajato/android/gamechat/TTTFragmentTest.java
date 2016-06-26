package com.pajato.android.gamechat;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Tests the Tic-Tac-Toe game feature of our MainActivity.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class TTTFragmentTest {

    @Rule public ActivityTestRule<MainActivity> mRule = new ActivityTestRule<>(MainActivity.class);
    private String xValue;
    private String oValue;
    private String spaceValue;

    /**
     * Swipe left to navigate to the next pane.
     */
    @Before
    public void navigateToGameFragment() throws InterruptedException {
        // Establish String Values.
        xValue = mRule.getActivity().getString(R.string.xValue);
        oValue = mRule.getActivity().getString(R.string.oValue);
        spaceValue = mRule.getActivity().getString(R.string.spaceValue);

        onView(withId(R.id.toolbar_game_icon))
                .perform(click());
        onView(withId(R.id.board))
                .check(matches(isDisplayed()));
    }

    /**
     * Ensure that the @Before method does not break anything
     * and that there are no other outside issues.
     */
    @Test public void testDoNothing() {

    }

    /**
     * Ensure that the buttons, when clicked, change to first X and then O
     */
    @Test public void testAlternatePlayers() {
        // Ensure that a tile, when clicked, displays X
        onView(withTagValue(is((Object) "button00")))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(withText(xValue)));

        // Ensure the same is true for the opposite button and O
        onView(withTagValue(is((Object) "button01")))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(withText(oValue)));
    }

    /**
     * Ensure that buttons cannot overwrite already placed, and pressing a button
     * that already has a value does not change the turn.
     */
    @Test public void testButtonsDoNotOverwrite() {
        // X puts a piece on a button
        onView(withTagValue(is((Object) "button00")))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(withText(xValue)))
                // Ensure that when O presses that button, it does not change the turn.
                .perform(click())
                .check(matches(withText(xValue)));
        // Confirm that the turn does not change from O's turn.
        onView(withId(R.id.turnDisplay))
                .check(matches(withText(oValue)));
        //TODO: test for a bug that causes the piece played to switch despite the turn value not changing.
    }

    /**
     * Ensure that after the game has ended, the value of a button cannot be changed.
     */
    //@Test: disable until Bryan fixes this.
    public void testButtonsOffPostGame() {
        // Set X to win with the top row (00 - 01 - 02).
        // Click Top Left
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(xValue)));

        // Play an O in a non-relevant spot (Bottom Right)
        onView(withTagValue(is((Object) "button22")))
                .perform((click()));

        // Click Top Center
        onView(withTagValue(is((Object) "button01")))
                .perform(click())
                .check(matches(withText(xValue)));

        // Play an O in a non-relevant spot (Bottom Left)
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));

        // Click Top Right
        onView(withTagValue(is((Object) "button02")))
                .perform(click())
                .check(matches(withText(xValue)));

        // Ensure game has ended
        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));

        // Attempt to click Bottom Center (to get 3 in a row for O).
        onView(withTagValue(is((Object) "button21")))
                .perform(click())
                // Ensure that the command is not executed.
                .check(matches(withText(spaceValue)));
    }

    /**
     * Ensure that the new game functions clear the board.
     */
    @Test
    public void testNewGame() {
        // Fill the board with a non-ended state
        onView(withTagValue(is((Object) "button00")))
                .perform(click());
        onView(withTagValue(is((Object) "button01")))
                .perform(click());
        onView(withTagValue(is((Object) "button02")))
                .perform(click());
        onView(withTagValue(is((Object) "button10")))
                .perform(click());
        onView(withTagValue(is((Object) "button20")))
                .perform(click());
        onView(withTagValue(is((Object) "button21")))
                .perform(click());
        onView(withTagValue(is((Object) "button22")))
                .perform(click());
        onView(withTagValue(is((Object) "button12")))
                .perform(click());

        // Perform a new game.
        getNewGame();

        // Ensure that all buttons are now empty.
        onView(withTagValue(is((Object) "button00")))
                .check(matches(withText(spaceValue)));
        onView(withTagValue(is((Object) "button01")))
                .check(matches(withText(spaceValue)));
        onView(withTagValue(is((Object) "button02")))
                .check(matches(withText(spaceValue)));

        onView(withTagValue(is((Object) "button10")))
                .check(matches(withText(spaceValue)));
        onView(withTagValue(is((Object) "button11")))
                .check(matches(withText(spaceValue)));
        onView(withTagValue(is((Object) "button12")))
                .check(matches(withText(spaceValue)));

        onView(withTagValue(is((Object) "button20")))
                .check(matches(withText(spaceValue)));
        onView(withTagValue(is((Object) "button21")))
                .check(matches(withText(spaceValue)));
        onView(withTagValue(is((Object) "button22")))
                .check(matches(withText(spaceValue)));
    }

    /**
     * Ensure that creating a new game keeps the turn swapped (i.e., if the last game ended with
     * X placing a piece, then the first person to go in the new game would be O).
     */
    @Test public void testNewGameTurnSwitch() {
        // The first player is always X, so the turn indicator should become O
        onView(withTagValue(is((Object) "button00")))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(withText(xValue)));
        onView(withId(R.id.turnDisplay))
                .check(matches(withText(oValue)));

        getNewGame();

        // The turn should still be O's, so after a press, the turn should become X's.
        onView(withTagValue(is((Object) "button00")))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(withText(oValue)));
        onView(withId(R.id.turnDisplay))
                .check(matches(withText(xValue)));
    }

    /**
     * Ensure that a win for P2 (O) is handled correctly.
     */
    //@Test: disable until Bryan fixes this.
    public void testOWins() {
        // Fill a win in for O in the top row. Buttons 00 - 01 - 02
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button22")))
                .perform((click()));

        // Click Top Left
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(oValue)));

        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));

        // Click Top Center
        onView(withTagValue(is((Object) "button01")))
                .perform(click())
                .check(matches(withText(oValue)));

        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button11")))
                .perform((click()));

        // Click Top Right
        onView(withTagValue(is((Object) "button02")))
                .perform(click())
                .check(matches(withText(oValue)));

        // Ensure that the endgame messages appear.
        onView(withId(R.id.Winner))
                .check(matches(isDisplayed()))
                .check(matches(withText("O Wins!")));
        onView((withId(android.support.design.R.id.snackbar_text)))
                .check(matches(isDisplayed()))
                .check(matches(withText("Player 2 (" + oValue + ") Wins!")));
    }

    /**
     * Ensure that the game successfully ends with a tie.
     */
    //@Test: disable until Bryan fixes this.
    public void testTie() {
        // Layout the board in a tie.
        onView(withTagValue(is((Object) "button00")))
                .perform(click());
        onView(withTagValue(is((Object) "button01")))
                .perform(click());
        onView(withTagValue(is((Object) "button02")))
                .perform(click());

        onView(withTagValue(is((Object) "button20")))
                .perform(click());
        onView(withTagValue(is((Object) "button21")))
                .perform(click());
        onView(withTagValue(is((Object) "button22")))
                .perform(click());

        onView(withTagValue(is((Object) "button10")))
                .perform(click());
        onView(withTagValue(is((Object) "button11")))
                .perform(click());
        onView(withTagValue(is((Object) "button12")))
                .perform(click());

        // Ensure that the endgame messages appear.
        onView(withId(R.id.Winner))
                .check(matches(withText("Tie!")));
        onView((withId(android.support.design.R.id.snackbar_text)))
                .check(matches(isDisplayed()))
                .check(matches(withText("It's a Tie!")));
    }

    /**
     * Ensure that the Winner's TextView is not visible at the start of play.
     */
    @Test
    public void testWinnersInvisible() {
        onView(withId(R.id.Winner)).check(matches(not(isDisplayed())));
    }

    /**
     * Ensure that a P1 win (X) is handled properly.
     */
    //@Test: disable until Bryan fixes this.
    public void testXWins() {
        // Fill a win in for X in the middle row. Buttons 10 - 11 - 12
        // Click Middle Left
        onView(withTagValue(is((Object) "button10")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button22")))
                .perform((click()));
        // Click Middle Center
        onView(withTagValue(is((Object) "button11")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));
        // Click Middle Right
        onView(withTagValue(is((Object) "button12")))
                .perform(click())
                .check(matches(withText(xValue)));

        // Ensure that the endgame messages appear.
        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
        onView((withId(android.support.design.R.id.snackbar_text)))
                .check(matches(isDisplayed()))
                .check(matches(withText("Player 1 (" + xValue + ") Wins!")));

    }

    /**
     * A helper method that creates a new game using the Floating Action Button
     */
    private void getNewGame() {
        // Open up the FAB menu and initiate a new game
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.new_game_ttt))
                .check(matches(isDisplayed()))
                .perform(click());
    }

}
