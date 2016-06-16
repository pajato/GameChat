package com.pajato.android.gamechat;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class FABTest {

    @Rule public ActivityTestRule<MainActivity> mRule = new ActivityTestRule<>(MainActivity.class);
    private String spaceValue;

    /**
     * Swipe left to navigate to the next pane.
     */
    @Before
    public void navigateToGameFragment() throws InterruptedException {
        // Establish String Values.
        spaceValue = mRule.getActivity().getString(R.string.spaceValue);

        onView(withId(R.id.chat_pane))
                .perform(swipeLeft());
        onView(withId(R.id.board))
                .check(matches(isDisplayed()));

        // Very short sleep is required to mitigate a race condition between the tester and the code
        Thread.sleep(250);
    }

    //TODO: Write more tests when the other game features are implemented.
    // For example, ensuring that switching between games works properly is a very important test.

    /**
     * Ensure that all items in the FAB Menu are present.
     */
    @Test
    public void testFABMenuPresent() {
        // Open up the FAB menu
        onView(withId(R.id.fab_speed_dial))
                .check(matches(isDisplayed()))
                .perform(click());

        Activity rule = mRule.getActivity();
        // Ensure that the speed dial Indicators are all visible.
        onView(withText(rule.getString(R.string.new_tictactoe_game)))
                .check(matches(isDisplayed()));
        onView(withText((rule.getString(R.string.new_checkers_game))))
                .check(matches(isDisplayed()));
        onView(withText((rule.getString(R.string.new_chess_game))))
                .check(matches(isDisplayed()));
        onView(withText((rule.getString(R.string.new_game_advanced_settings))))
                .check(matches(isDisplayed()));
    }

    /**
     * Ensure that filling the board and then using the FAB to restart actually clears the board.
     */
    @Test
    public void testFABNewGameTTT() {
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

        // Open up the FAB menu and initiate a new game
        onView(withId(R.id.fab_speed_dial))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText(mRule.getActivity().getString(R.string.new_tictactoe_game)))
                .check(matches(isDisplayed()))
                .perform(click());

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
}
