package com.pajato.android.gamechat.chat;

import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;


/** Test the group add activty. */
@RunWith(AndroidJUnit4.class) public class AddGroupActivityTest {

    // Public instance variables.

    /** Set up the rule instance variable to allow for having intent extras passed in. */
    @Rule public ActivityTestRule<AddGroupActivity> mRule =
            new ActivityTestRule<>(AddGroupActivity.class, true, false);

    @Before public void setup() {
        Intent intent = new Intent();
        mRule.launchActivity(intent);
    }

    /** Test the button click operations with a null account. */
    @Test public void testButtonClickHandlerNoAccount() throws Exception {
        // Exercise the placeholder functions.
        onView(withId(R.id.addGroupMembers)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.setGroupIcon)).check(matches(isDisplayed())).perform(click());

        // Test that the group name edit text field is visible and empty by default, and that the
        // save button is not clickable.
        onView(withId(R.id.groupNameText)).check(matches(isDisplayed()));
        onView(withId(R.id.groupNameText)).check(matches(withText("")));
        onView(withId(R.id.saveGroupButton)).check(matches(isDisplayed()));
        onView(withId(R.id.saveGroupButton)).check(matches(not(isEnabled())));

        // Set the group name field value to non empty and ensure that the save button is enabled.
        onView(withId(R.id.groupNameText)).perform(replaceText("Test Group"));
        onView(withId(R.id.saveGroupButton)).check(matches(isEnabled()));

        // Reset the group name field value using the clear button and ensure that the save button
        // is disabled again.
        onView(withId(R.id.clearGroupName)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.saveGroupButton)).check(matches(not(isEnabled())));

        // Finally enable the save button again and click on it to ensure the add group activity
        // finishes.
        onView(withId(R.id.groupNameText)).perform(replaceText("Test Group"));
        onView(withId(R.id.saveGroupButton)).perform(click());
    }

    /** Use a back press to cancel out of the activity. */
    @Test public void testOnBackPressed() throws Exception {
        try {
            Espresso.pressBack();
            fail("Did not take the expected NoActivityResumedException.");
        } catch (NoActivityResumedException exc) {
            // Quietly pass the test.
        }
    }

    /** Test that the settings overflow menu item does the right thing. */
    @Test public void testSettingsOverflowMenuItem() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        String text = context.getString(R.string.MenuItemSettings);
        Espresso.openActionBarOverflowOrOptionsMenu(context);
        onView(withText(text)).check(matches(isDisplayed()));
    }

    /** Test that the learn more overflow menu item does the right thing. */
    @Test public void testHomeOverflowMenuItem() throws Exception {
        // TODO: implement this test using UIAutomator rather than an unreliable solution.
        // onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
    }

    /** ... */
    @Test public void onOptionsItemSelected() throws Exception {
        //fail("Not yet fully implemented.");
    }

    /** ... */
    @Test public void onCreate() throws Exception {
        //fail("Not yet fully implemented.");
    }

    /** ... */
    @Test public void onPause() throws Exception {
        //fail("Not yet fully implemented.");
    }

    /** ... */
    @Test public void onResume() throws Exception {
        //fail("Not yet fully implemented.");
    }
}
