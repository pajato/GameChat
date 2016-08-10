package com.pajato.android.gamechat;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Provide a base class that includes setting up all tests to exclude the intro activity and perform
 * a do nothing test.
 *
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseTest {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseTest.class.getSimpleName();

    // Public instance variables.

    /** Set up the rule instance variable to allow for having intent extras passed in. */
    @Rule public ActivityTestRule<MainActivity> mRule =
            new ActivityTestRule<>(MainActivity.class, true, false);

    @Before public void setup() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.SKIP_INTRO_ACTIVITY_KEY, true);
        mRule.launchActivity(intent);
    }

    /** Ensure that doing nothing breaks nothing but generates some code coverage results. */
    @Test public void testDoNothing() {
        // Do nothing initially.
    }

}
