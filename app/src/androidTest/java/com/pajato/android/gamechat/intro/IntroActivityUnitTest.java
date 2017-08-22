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

package com.pajato.android.gamechat.intro;


import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.AlphabetIndexer;

import com.pajato.android.gamechat.R;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Provide a unit test class in order to achieve acceptable coverage on the Preference class code.
 *
 * Created by pmr on 8/2/17.
 */
@RunWith(AndroidJUnit4.class)
public class IntroActivityUnitTest {


    /** Set up the rule instance variable to allow for having intent extras passed in. */
    @Rule
    public ActivityTestRule<IntroActivity> mRule =
        new ActivityTestRule<>(IntroActivity.class, true, false);

    @Before public void setUp() throws Exception {
        Intent intent = new Intent();
        mRule.launchActivity(intent);
    }

    @After public void tearDown() throws Exception { }

    @Test public void testIntroAdapter() {
        // Test that a view pager and it's adapter both exist.
        ViewPager pager = mRule.getActivity().findViewById(R.id.intro_view_pager);
        PagerAdapter adapter = pager != null ? pager.getAdapter() : null;
        Assert.assertTrue("The view pager does not exist!", pager != null);
        Assert.assertTrue("The view pager adapter does not exist!", pager.getAdapter() != null);

        // Test that the adapter state parcel is null.
        Assert.assertEquals("The saved state parcel is non-null!", null, adapter.saveState());

        // Test that the resources are pruned as swiping takes place.
        onView(withId(R.id.intro_view_pager)).perform(swipeLeft());
        onView(withId(R.id.intro_view_pager)).perform(swipeLeft());
        onView(withId(R.id.intro_view_pager)).perform(swipeLeft());
    }
}
