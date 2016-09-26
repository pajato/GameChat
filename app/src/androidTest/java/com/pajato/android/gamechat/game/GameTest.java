package com.pajato.android.gamechat.game;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.BaseTest;
import com.pajato.android.gamechat.R;

import org.junit.Before;
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
public class GameTest extends BaseTest {
    /** Navigates to the game panel before each test is run. */
    @Before public void navigateToInitOnStart() {
        onView(withId(R.id.toolbar_game_icon))
                .perform(click());
        onView(withId(R.id.game_pane_fragment_container))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_panel))
                .check(matches(isDisplayed()));
        onView(withId(R.id.gameFab))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    /** Ensure that switching between two different game panels works properly */
    @Test public void testGameSwitcher() {
        // Open the action bar overflow menu, then initiate a new TTT game.
        onView(withId(R.id.init_ttt_button))
                .check(matches(isDisplayed()))
                .perform(click());
        // This should cause the settings panel to appear, at which point we will play locally.
        onView(withId(R.id.settings_local_button))
                .check(matches(isDisplayed()))
                .perform(click());
        // The board should then appear.
        onView(withId(R.id.board))
                .check(matches(isDisplayed()));
        returnToInit();
    }

    /** Ensure that our initial panel creates the settings panel properly. */
    @Test public void testSettingsPanelTitle() {
        // Click on the new Tic-Tac-Toe game section, which should create the settings panel.
        onView(withId(R.id.init_ttt_button))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.settings_panel))
                .check(matches(isDisplayed()));
        // Ensure that the settings panel title properly changes with each game. First, Tic-Tac-Toe.
        onView(withId(R.id.settings_title))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.playing_ttt)));
        returnToInit();
        // Then, checkers.
        onView(withId(R.id.init_checkers_button))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.settings_title))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.playing_checkers)));
    }

    /** Ensure that the online play functionality cannot launch until a partner is chosen. */
    @Test public void testSettingsPanelOnline() {
        onView(withId(R.id.init_ttt_button))
                .check(matches(isDisplayed()))
                .perform(click());
        // Trying to click the online button without choosing a partner first should fail.
        onView(withId(R.id.settings_online_button))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.settings_panel))
                .check(matches(isDisplayed()));
        // Now, choose a group, then try to play online.
        onView(withId(R.id.settings_group_spinner))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withText("Family"))
                .check(matches(isDisplayed()))
                .perform(click());
        // Upon choosing a group, a default user target will be chosen for us.
        onView(withText("Mom"))
                .check(matches(isDisplayed()));
        // As we have chosen a player, this should launch a game. Ensure that the board is visible.
        onView(withId(R.id.settings_online_button))
                .perform(click());
        onView(withId(R.id.ttt_panel))
                .check(matches(isDisplayed()));
        onView(withId(R.id.board))
                .check(matches(isDisplayed()));
    }

    /** Return to the initial panel using the overflow menu option. */
    private void returnToInit() {
        // Open the action bar overflow menu, then return to the initial pane.
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.go_to_rooms))
                .check(matches(isDisplayed()))
                .perform(click());
        // Ensure the init panel is present and open the FAB
        onView(withId(R.id.init_panel))
                .check(matches(isDisplayed()));
        onView(withId(R.id.gameFab))
                .check(matches(isDisplayed()))
                .perform(click());
    }
}
