package com.pajato.android.gamechat;

import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Tests the Win Conditions of the Tic-Tac-Toe game.
 *
 * @author Bryan Scott -- bryan@pajato.com
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class TTTUiOperationsTest {
    private static final String xValue = "X";
    private static final String oValue = "O";
    private static final String spaceValue = "";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    /**
     * Initializes a new game at the start of each test.
     */
    @Before
    public void newGame() {
        onView(withId(R.id.newGame)).perform(click());
    }

    /**
     * Ensure that doing nothing breaks nothing but generates some code coverage results.
     */
    @Test
    public void testAAADoNothing() {
        // Do nothing. The Test is so named so that it is run first.
    }

    /**
     * Ensure that the buttons, when clicked, change to first X and then O
     */
    @Test
    public void testAlternatePlayers() {
        // Ensure that a tile, when clicked, displays X
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(xValue)));

        // Ensure the same is true for the opposite button and O
        onView(withTagValue(is((Object) "button01")))
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
                .perform(click())
                .check(matches(withText(xValue)))
                // Ensure that when O presses that button, it does not change the piece.
                .perform(click())
                .check(matches(withText(xValue)));
        // Confirm that the turn does not change from O's turn.
        onView(withId(R.id.turnDisplay))
                .check(matches(withText(oValue)));
    }

    /**
     * Ensure that the new game functions clear the board.
     */
    @Test
    public void testNewGame() {
        // Occupy the board with a non-ended state (ended states are handled in TTTWinConditionsTest)
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
        onView(withId(R.id.newGame)).perform(click());

        // Ensure that all buttons are empty now.
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
    @Test
    public void testNewGameTurnSwitch() {
        // Perform a click to "play" an X.
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(xValue)));
        // The first player is always X, so the turn indicator should become O.
        onView(withId(R.id.turnDisplay)).
                check(matches(withText(oValue)));

        // Initiate New Game
        onView(withId(R.id.newGame))
                .perform(click());

        // The turn should still be O's, so after a press, the turn should become X's.
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(oValue)));
        onView(withId(R.id.turnDisplay))
                .check(matches(withText(xValue)));
    }

    /**
     * Ensure that the Winner's TextView is not visible at the start of play.
     */
    @Test
    public void testWinnersInvisible() {
        onView(withId(R.id.Winner)).
                check(matches(not(isDisplayed())));
    }

}