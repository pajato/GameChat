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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

/**
 * Handles testing the win conditions of the Tic-Tac-Toe game.
 *
 * @author Bryan Scott -- bryan@pajato.com
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class TTTWinConditionsTest {

    private String xValue = "X";
    private String oValue = "O";
    private String spaceValue = "";

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
     * Ensure that the game successfully ends with a tie.
     */
    @Test
    public void testTie() {
        // X - O - X in the top row.
        // This row is no longer winnable.
        onView(withTagValue(is((Object) "button00")))
                .perform(click());
        onView(withTagValue(is((Object) "button01")))
                .perform(click());
        onView(withTagValue(is((Object) "button02")))
                .perform(click());
        // O - X - O in the bottom row.
        // Diagonal wins are no longer possible, nor are vertical wins, nor is a win in this row.
        onView(withTagValue(is((Object) "button20")))
                .perform(click());
        onView(withTagValue(is((Object) "button21")))
                .perform(click());
        onView(withTagValue(is((Object) "button22")))
                .perform(click());
        // X - O - X in the middle row.
        // Wins are no longer possible in this row, or at all.
        onView(withTagValue(is((Object) "button10")))
                .perform(click());
        onView(withTagValue(is((Object) "button11")))
                .perform(click());
        onView(withTagValue(is((Object) "button12")))
                .perform(click());
        // Ensure the Winner view displays a tie.
        onView(withId(R.id.Winner)).
                check(matches(withText("Tie!")));
    }

    /**
     * Ensure that after the game has ended, the value of a button cannot be changed.
     */
    @Test
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
                // Assert that the command is not executed.
                .check(matches(withText(spaceValue)));
    }


    /**
     * Each of the following tests determine if the line of 3 mentioned in the respective
     * test's title correctly ends the game for the player mentioned in the title.
     * Possibilities Tested: X and O for...
     *  Top, Mid, and Bottom Rows
     *  Left, Center, and Right Columns,
     *  and the Left and Right Diagonals.
     */
    // X Tests
    @Test
    public void testXTopRow() {
        // 00 - 01 - 02
        // Click Top Left
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(xValue)));

        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button22")))
                .perform((click()));

        // Click Top Center
        onView(withTagValue(is((Object) "button01")))
                .perform(click())
                .check(matches(withText(xValue)));

        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));

        // Click Top Right
        onView(withTagValue(is((Object) "button02")))
                .perform(click())
                .check(matches(withText(xValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
    }
    @Test
    public void testXMidRow() {
        // 10 - 11 - 12
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

        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
    }
    @Test
    public void testXBotRow() {
        // 20 - 21 - 22
        // Click Bottom Left
        onView(withTagValue(is((Object) "button20")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button02")))
                .perform((click()));
        // Click Bottom Center
        onView(withTagValue(is((Object) "button21")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button00")))
                .perform((click()));
        // Click Bottom Right
        onView(withTagValue(is((Object) "button22")))
                .perform(click())
                .check(matches(withText(xValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
    }
    @Test
    public void testXLeftCol() {
        // 00 - 10 - 20
        // Click Top Left
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button22")))
                .perform((click()));
        // Click Middle Left
        onView(withTagValue(is((Object) "button10")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button21")))
                .perform((click()));
        // Click Bottom Left
        onView(withTagValue(is((Object) "button20")))
                .perform(click())
                .check(matches(withText(xValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
    }
    @Test
    public void testXCenterCol() {
        // 01 - 11 - 21
        // Click Top Center
        onView(withTagValue(is((Object) "button01")))
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
        // Click Bottom Center
        onView(withTagValue(is((Object) "button21")))
                .perform(click())
                .check(matches(withText(xValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
    }
    @Test
    public void testXRightCol() {
        // 02 - 12 - 22
        // Click Top Right
        onView(withTagValue(is((Object) "button02")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));
        // Click Middle Right
        onView(withTagValue(is((Object) "button12")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button21")))
                .perform((click()));
        // Click Bottom Right
        onView(withTagValue(is((Object) "button22")))
                .perform(click())
                .check(matches(withText(xValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
    }
    @Test
    public void testXLeftDiag() {
        // 00 - 11 - 22
        // Click Top Left
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button21")))
                .perform((click()));
        // Click Middle Center
        onView(withTagValue(is((Object) "button11")))
                .perform(click())
                .check(matches(withText(xValue)));
        // Play an O in a non-relevant spot
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));
        // Click Bottom Right
        onView(withTagValue(is((Object) "button22")))
                .perform(click())
                .check(matches(withText(xValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
    }
    @Test
    public void testXRightDiag() {
        // 02 - 11 - 20
        // Click Bottom Left
        onView(withTagValue(is((Object) "button02")))
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
        onView(withTagValue(is((Object) "button21")))
                .perform((click()));
        // Click Top Right
        onView(withTagValue(is((Object) "button20")))
                .perform(click())
                .check(matches(withText(xValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("X Wins!")));
    }

    // O Tests
    @Test
    public void testOTopRowO() {
        // 00 - 01 - 02
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

        onView(withId(R.id.Winner))
                .check(matches(withText("O Wins!")));
    }
    @Test
    public void testOMidRowO() {
        // 10 - 11 - 12
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button00")))
                .perform((click()));

        // Click Middle Left
        onView(withTagValue(is((Object) "button10")))
                .perform(click())
                .check(matches(withText(oValue)));

        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button01")))
                .perform((click()));

        // Click Middle Center
        onView(withTagValue(is((Object) "button11")))
                .perform(click())
                .check(matches(withText(oValue)));

        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));

        // Click Middle Right
        onView(withTagValue(is((Object) "button12")))
                .perform(click())
                .check(matches(withText(oValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("O Wins!")));
    }
    @Test
    public void testOBotRowO() {
        // 20 - 21 - 22
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button12")))
                .perform((click()));
        // Click Bottom Left
        onView(withTagValue(is((Object) "button20")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button10")))
                .perform((click()));
        // Click Bottom Middle
        onView(withTagValue(is((Object) "button21")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button00")))
                .perform((click()));
        // Click Bottom Right
        onView(withTagValue(is((Object) "button22")))
                .perform(click())
                .check(matches(withText(oValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("O Wins!")));
    }
    @Test
    public void testOLeftCol() {
        // 00 - 10 - 20
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button12")))
                .perform((click()));
        // Click Top Left
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button01")))
                .perform((click()));
        // Click Middle Left
        onView(withTagValue(is((Object) "button10")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button21")))
                .perform((click()));
        // Click Bottom Left
        onView(withTagValue(is((Object) "button20")))
                .perform(click())
                .check(matches(withText(oValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("O Wins!")));
    }
    @Test
    public void testOCenterCol() {
        // 01 - 11 - 21
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button00")))
                .perform((click()));
        // Click Top Center
        onView(withTagValue(is((Object) "button01")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));
        // Click Middle Center
        onView(withTagValue(is((Object) "button11")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button02")))
                .perform((click()));
        // Click Bottom Center
        onView(withTagValue(is((Object) "button21")))
                .perform(click())
                .check(matches(withText(oValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("O Wins!")));
    }
    @Test
    public void testORightCol() {
        // 02 - 12 - 22
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button00")))
                .perform((click()));
        // Click Top Right
        onView(withTagValue(is((Object) "button02")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button01")))
                .perform((click()));
        // Click Middle Right
        onView(withTagValue(is((Object) "button12")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button11")))
                .perform((click()));
        // Click Bottom Right
        onView(withTagValue(is((Object) "button22")))
                .perform(click())
                .check(matches(withText(oValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("O Wins!")));
    }
    @Test
    public void testOLeftDiag() {
        // 00 - 11 - 22
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button01")))
                .perform((click()));
        // Click Top Right
        onView(withTagValue(is((Object) "button00")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button02")))
                .perform((click()));
        // Click Middle Right
        onView(withTagValue(is((Object) "button11")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button20")))
                .perform((click()));
        // Click Bottom Right
        onView(withTagValue(is((Object) "button22")))
                .perform(click())
                .check(matches(withText(oValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("O Wins!")));
    }
    @Test
    public void testORightDiag() {
        // 02 - 11 - 20
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button01")))
                .perform((click()));
        // Click Top Right
        onView(withTagValue(is((Object) "button02")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button00")))
                .perform((click()));
        // Click Middle Right
        onView(withTagValue(is((Object) "button11")))
                .perform(click())
                .check(matches(withText(oValue)));
        // Play an X in a non-relevant spot
        onView(withTagValue(is((Object) "button22")))
                .perform((click()));
        // Click Bottom Right
        onView(withTagValue(is((Object) "button20")))
                .perform(click())
                .check(matches(withText(oValue)));

        onView(withId(R.id.Winner))
                .check(matches(withText("O Wins!")));
    }
}
