package com.pajato.android.gamechat;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class) public class FABTest extends BaseTest {

    // Private instance variables.

    //TODO: Write more tests when the other features are implemented.
    // For example, ensuring that opening up new games works as intended.

    /** Ensure that all items in the FAB Menu are present. */
    @Test public void testFABMenuPresent() {
        // Open up the FAB menu
        onView(withId(R.id.chatFab))
                .check(matches(isDisplayed()))
                .perform(click());
        // Ensure that the speed dial Indicators are all visible.
        onView(withId(R.id.chatFam))
                .check(matches(isDisplayed()));
        onView(withText(R.string.AddGroupTitle))
                .check(matches(isDisplayed()));
    }
}
