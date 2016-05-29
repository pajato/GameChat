package com.pajato.android.gamechat;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ApplicationTest.class.getSimpleName();

    /** The overflow menu label string. */
    private static final String OVERFLOW_MENU_ITEM_SETTINGS_TEXT = "Settings";

    /** The nav drawer import from camera item label string. */
    private static final String IMPORT_MENU_ITEM_TEXT = "Import";

    /** The snack bar text. */
    private static final String SNACKBAR_MESSAGE_TEXT = "Replace with your own action";

    // Public instance variables.

    @Rule public ActivityTestRule<MainActivity> mRule = new ActivityTestRule<>(MainActivity.class);

    /** Ensure that doing nothing breaks nothing but generates some code coverage results. */
    @Test public void testDoNothing() throws Exception {
        // Do nothing initially.
    }

    /** Ensure that the basic navigation drawer theme operations all work as expeecte. */
    @Test public void testBasicThemeOperations() throws Exception {
        // Test that the overflow menu is acessible and can be clicked on. By default it does
        // nothhing.
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(OVERFLOW_MENU_ITEM_SETTINGS_TEXT)).check(matches(isDisplayed())).perform(click());

        // Test that the hamburger menu is acessible and can be opened and closed via the back
        // button.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        pressBack();

        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_camera));
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_gallery));
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_slideshow));
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_manage));
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_share));
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_send));
        Thread.sleep(1000);

        // Ensure that the FAB button is visible and click on it, verifying that a snackbar message is displayed and
        // removing the snackbar message.
        onView(withId(R.id.fab)).perform(click());
        onView(withText(SNACKBAR_MESSAGE_TEXT)).check(matches(isDisplayed()));
    }
}
