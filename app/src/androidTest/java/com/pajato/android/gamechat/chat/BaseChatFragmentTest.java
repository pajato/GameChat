package com.pajato.android.gamechat.chat;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Provide a test class to drive up the test coverage percentage for the base chat fragment class.
 * Specification notes:
 *
 * Ensure that the group list (home) page shows a hamburger menu and no overflow menu.
 *
 * From the group list page, ensure that the create group FAM item gets to the create group page.
 *
 * From the create group page, ensure that the arrow back icon exists, is black and returns to the
 * group list page which pressed.
 *
 * From the create group page, ensure that the back button returns to the group list page when
 * pressed.
 *
 * From the create group page, ensure that the title is "Create Group" and there is no subtitle.
 *
 * From the create group page, ensure that the toolbar has a button labeled "SAVE" and is disabled.
 *
 * From the create group page, ensure that the overflow menu icon exists, is black and has three
 * items displayed with pressed.
 *
 * From the group list page, ensure that the Join Rooms FAM item gets to the join room page.
 *
 * From the join room page, ensure that the arrow back icon exists, is black and returns to the
 * group list page which pressed.
 *
 * From the join room page, ensure that the back button returns to the group list page when
 * pressed.
 *
 * From the join room page, ensure that the title is "Join Rooms" and there is no subtitle.
 *
 * From the join room page, ensure that the toolbar has a button labeled "SAVE" and is disabled.
 *
 * From the join room page, ensure that the overflow menu icon exists, is black and has three
 * items displayed with pressed.
 *
 * Ensure that the Me group opens to the Me Group room list page when clicked on.
 *
 * Ensure that the opened group shows the Me room and only the Me room.
 *
 * Ensure that the Me group room list view has a arrow back (white) navigation icon and returns to
 * the group list page when pressed.
 *
 * Ensure that the Me group room list view has an overflow (white) menu icon and shows three items
 * when pressed.
 *
 * Ensure that a backpress from the Me group room list view page gets back to the group list view
 * page.
 *
 * Ensure that the Me group room list view page Create Group FAM item exists and show the Create
 * Group page when pressed.
 *
 * Ensure that the Create Group page returns to the Me Group room list page when the arrow back
 * navigation icon is pressed.
 *
 * Ensure that the Create Group page returns to the Me Group room list page when the back button is
 * pressed.
 *
 * Ensure that the Me group room list view page Create Room FAM item exists and shows the Create
 * Room page when pressed.
 *
 * Ensure that the Create Room page has a arrow back navigation icon, it is black and returns to the
 * Me Group room list page when pressed.
 *
 * Ensure that the Create Room page has a Create Room title and a Me Group subtitle.
 * navigation icon is pressed.
 *
 * Ensure that the Create Room page toolbar has a button labeled "SAVE" and that it is disabled.
 *
 * Ensure that the Create Room page toolbar has an overflow menu, it is black and is shows three
 * items when pressed.
 *
 * Ensure that the Create Room page returns to the Me Group room list page when the back button is
 * pressed.
 *
 * @author Paul Reilly
 */
@RunWith(AndroidJUnit4.class) public class BaseChatFragmentTest {

    // Public instance variables.

    /** Set up the rule instance variable to allow for having intent extras passed in. */
    @Rule public ActivityTestRule<MainActivity> mRule =
            new ActivityTestRule<>(MainActivity.class, true, false);

    @Before public void setup() {
        Intent intent = new Intent();
        mRule.launchActivity(intent);
    }

    /** Test the button click operations with a null account. */
    @Test public void testPass() throws Exception {
    }

    /** Use a back press to cancel out of the activity. */
    @Test public void testOnBackPressed() throws Exception {
    }
}
