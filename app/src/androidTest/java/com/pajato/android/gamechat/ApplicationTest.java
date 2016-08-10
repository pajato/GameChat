package com.pajato.android.gamechat;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest extends BaseTest {
    /** Ensure that the rooms panel is being displayed. */
    @Test public void testChatPaneIsVisible() {
        onView(withId(R.id.rooms_pane))
                .check(matches(isDisplayed()));
    }

    /** Ensure that the chat panel is being displayed. */
    @Test public void testGamePaneIsVisible() {
        onView(withId(R.id.rooms_pane))
                .check(matches(isDisplayed()))
                .perform(swipeLeft());
        onView(withId(R.id.game_pane_fragment_container))
                .check(matches(isDisplayed()));
    }

    /** Ensure that the toolbar buttons are displayed and function. */
    @Test public void testActionButtons() {
        // Ensure the search button is there
        onView(withId(R.id.toolbar_search_icon))
                .check(matches(isDisplayed()));
        // Ensure the game button is there. Click on it, and ensure it navigates to the game pane.
        onView(withId(R.id.toolbar_game_icon))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.game_pane_fragment_container))
                .check(matches(isDisplayed()));
        // Ensure the chat button is present. Click on it, and ensure it navigates back to the chat.
        onView(withId(R.id.toolbar_chat_icon))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.rooms_pane))
                .check(matches(isDisplayed()));
    }

    /** Ensure that the hamburger menu is dismissed on a back press. */
    @Test public void testHamburgerMenuWithBack() {
        // Test that the hamburger menu is accessible & can be opened and closed via the back button
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())
                .check(matches(isDisplayed()));
        pressBack();
    }

    /** Ensure that the hamburger menu works with the settings selection. */
    @Test public void testSettingsNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())
                .check(matches(isDisplayed()));
        onView(withId(R.id.nav_footer))
                .perform(NavigationViewActions.navigateTo(R.id.nav_settings));
        onView(withText(R.string.navigation_drawer_label_problems_settings))
                .check(matches(isDisplayed()));
    }

    /** Ensure that the hamburger menu works with the feedback selection. */
    @Test public void testFeedbackNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())
                .check(matches(isDisplayed()));
        onView(withId(R.id.nav_footer))
                .perform(NavigationViewActions.navigateTo(R.id.nav_feedback));
        onView(withText(R.string.navigation_drawer_label_problems_feedback))
                .check(matches(isDisplayed()));
    }

    /** Ensure that the hamburger menu works with the learn more selection. */
    @Test public void testLearnMoreNavigation() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())
                .check(matches(isDisplayed()));
        onView(withId(R.id.nav_footer))
                .perform(NavigationViewActions.navigateTo(R.id.nav_learn));
        onView(withText(R.string.navigation_drawer_label_problems_learn))
                .check(matches(isDisplayed()));
    }

    /** Ensure that the hamburger menu works with the manage selection. */
    @Test public void testNavigationBody() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())
                .check(matches(isDisplayed()));

        // Ensure the favorite chats header is displayed.
        onView((withText(R.string.navigation_drawer_label_favorite_chats)))
                .check(matches(isDisplayed()));

        // Swipe up, then ensure that the recent chats header is displayed.
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_recent_chats));
    }

    /** Ensure that the hamburger menu works with the share selection. */
    @Test public void testNavigationFooter() {
        // Test that the items in the hamburger menu can be exercised.
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())
                .check(matches(isDisplayed()));
        // Before anything is done, ensure that the settings, help and feedback, and learn more
        // menu items are present.
        onView(withId(R.id.nav_footer))
                .check(matches(isDisplayed()));
        onView(withText(R.string.navigation_drawer_label_problems_settings))
                .check(matches(isDisplayed()));
        onView(withText(R.string.navigation_drawer_label_problems_feedback))
                .check(matches(isDisplayed()));
        onView(withText(R.string.navigation_drawer_label_problems_learn))
                .check(matches(isDisplayed()));

        // Swipe up, then ensure that all the items are still there.
        onView(withId(R.id.nav_view))
                .perform(swipeUp());

        onView(withId(R.id.nav_footer))
                .check(matches(isDisplayed()));
        onView(withText(R.string.navigation_drawer_label_problems_settings))
                .check(matches(isDisplayed()));
        onView(withText(R.string.navigation_drawer_label_problems_feedback))
                .check(matches(isDisplayed()));
        onView(withText(R.string.navigation_drawer_label_problems_learn))
                .check(matches(isDisplayed()));
    }

}
