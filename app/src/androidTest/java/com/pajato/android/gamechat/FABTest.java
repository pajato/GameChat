/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

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
        //onView(withId(R.id.chatFab)).check(matches(isDisplayed())).perform(click());
    }
}
