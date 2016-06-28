package com.pajato.android.gamechat;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class FABTest {

    @Rule public ActivityTestRule<MainActivity> mRule = new ActivityTestRule<>(MainActivity.class);

    //TODO: Write more tests when the other features are implemented.
    // For example, ensuring that opening up new games works as intended.

    /** Ensure that all items in the FAB Menu are present. */
    @Test public void testFABMenuPresent() {
        // Open up the FAB menu
        onView(withId(R.id.fab_speed_dial))
                .check(matches(isDisplayed()))
                .perform(click());
        // Ensure that the speed dial Indicators are all visible.
        onView(withText(R.string.new_chat_select_user))
                .check(matches(isDisplayed()));
        onView(withText(R.string.new_game_settings))
                .check(matches(isDisplayed()));
        onView(withText(R.string.new_chat_favorite_room))
                .check(matches(isDisplayed()));
    }

    /** Ensure that, when navigating to the game pane, the FAB disappears. *
     * NOTE: This test is problematic on tablets, and has been commented out until the time comes
     * that a better test is written to check this functionality.
    @Test public void testFABNotDisplayedGameFragment() {
        // Ensure the FAB is present. Then, Navigate to the game fragment.
        onView(withId(R.id.fab_speed_dial))
                .check(matches(isDisplayed()));
        onView(withId(R.id.toolbar_game_icon))
                .perform(click());
        // Once there, ensure the FAB is no longer displayed.
        onView(withId(R.id.fab_speed_dial))
                .check(matches(not(isDisplayed())));
    }
    */
}
