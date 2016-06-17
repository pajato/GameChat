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
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

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

    // Public instance variables.

    @Rule public ActivityTestRule<MainActivity> mRule = new ActivityTestRule<>(MainActivity.class);

    /** Ensure that doing nothing breaks nothing but generates some code coverage results. */
    @Test public void testDoNothing() throws Exception {
        // Do nothing initially.
    }

    /** Ensure that the chat panel is being displayed. */
    @Test public void testChatPaneIsVisible() {
        onView(withId(R.id.chat_pane)).check(matches(isDisplayed()));
    }

    /** Ensure that the chat panel is being displayed. */
    @Test public void testGamePaneIsVisible() {
        onView(withId(R.id.chat_pane)).check(matches(isDisplayed())).perform(swipeLeft());
        onView(withId(R.id.game_pane)).check(matches(isDisplayed()));
    }

    /** Ensure that the basic navigation drawer theme operations all work as expeecte. */
    @Test public void testOverflowMenu() throws Exception {
        // Test that the overflow menu is acessible and can be clicked on. By default it does
        // nothhing.
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(OVERFLOW_MENU_ITEM_SETTINGS_TEXT)).check(matches(isDisplayed())).perform(click());
    }

    /** Ensure that the hamburger menu is dismissed on a back press. */
    @Test public void testHamburgerMenuWithBack() {
        // Test that the hamburger menu is acessible and can be opened and closed via the back
        // button.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        pressBack();
    }

    /** Ensure that the hamburger menu works with the camera selection. */
    @Test public void testCameraNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_camera));
    }

    /** Ensure that the hamburger menu works with the gallery selection. */
    @Test public void testGalleryNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_gallery));
    }

    /** Ensure that the hamburger menu works with the slideshow selection. */
    @Test public void testSlideshowNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_slideshow));
    }

    /** Ensure that the hamburger menu works with the manage selection. */
    @Test public void testManageNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_manage));
    }

    /** Ensure that the hamburger menu works with the share selection. */
    @Test public void testShareNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_share));
    }

    /** Ensure that the hamburger menu works with the send selection. */
    @Test public void testSendNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open()).check(matches(isDisplayed()));
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_send));
    }
}
