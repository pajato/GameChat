package com.pajato.android.gamechat;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class FABTest extends BaseTest {

    //TODO: Write more tests when the other features are implemented.
    // For example, ensuring that opening up new games works as intended.

    /** Ensure that all items in the FAB Menu are present. */
    @Test public void testFABMenuPresent() {
        // Open up the FAB menu
        onView(withId(R.id.room_fab_speed_dial))
                .check(matches(isDisplayed()))
                .perform(click());
        // Ensure that the speed dial Indicators are all visible.
        onView(withText(R.string.room_new))
                .check(matches(isDisplayed()));
        onView(withText(R.string.room_favorite))
                .check(matches(isDisplayed()));
    }

    @Test public void testFabFunctionality() {
        onView(withId(R.id.rooms_pane))
                .check(matches(isDisplayed()));
        // Open up the FAB menu and click on the new Tic-Tac-Toe game option
        onView(withId(R.id.room_fab_speed_dial))
                .check(matches(isDisplayed()))
                .perform(click());
        // Todo: test out the menu functionality.
    }

    /** Ensure that, when navigating to the game pane, the FAB disappears. */
    @Test public void testFabInHierarchy() {
        // Ensure the FAB is present. Then, navigate to the game fragment.
        onView(withId(R.id.rooms_pane))
                .check(matches(isDisplayed()))
                .check(matches(withChild(withId(R.id.room_fab_speed_dial))));
        onView(withId(R.id.toolbar_game_icon))
                .perform(click());
        // Once there, ensure the FAB is no longer displayed.
        onView(withId(R.id.game_pane_fragment_container))
                .check(matches(not(withChild(withId(R.id.room_fab_speed_dial)))));
    }
}
