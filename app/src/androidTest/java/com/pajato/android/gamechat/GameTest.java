package com.pajato.android.gamechat;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Tests the higher level game-associated portions of GameChat.
 */
@RunWith(AndroidJUnit4.class)
public class GameTest {

    @Rule public ActivityTestRule<MainActivity> mRule = new ActivityTestRule<>(MainActivity.class);

    /** Ensure that switching between two different game panels works properly */
    @Test public void testGameSwitcher() {
        // Move to the game pane and ensure that we are starting on the settings panel.
        onView(withId(R.id.toolbar_game_icon))
                .perform(click());
        onView(withId(R.id.init_panel))
                .check(matches(isDisplayed()));
        // Open the action bar overflow menu, then initiate a new TTT game. Ensure it functions.
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.new_game_ttt))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.board))
                .check(matches(isDisplayed()));
        // Reopen the action bar overflow menu, then ensure returning to the settings pane works.
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.new_game_init))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.init_panel))
                .check(matches(isDisplayed()));
    }

    /** Ensure that we can get to our Tic-Tac-Toe game from the main settings menu */
    @Test public void testSettingsNewTTT() {
        // Navigate to the settings pane.
        onView(withId(R.id.toolbar_game_icon))
                .perform(click());
        onView(withId(R.id.game_pane_fragment_container))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_panel))
                .check(matches(isDisplayed()));
        // Click on the new Tic-Tac-Toe game section
        onView(withId(R.id.init_ttt))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.ttt_panel))
                .check(matches(isDisplayed()));
    }
}
