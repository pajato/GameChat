package com.pajato.android.gamechat;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.pajato.android.gamechat.main.MainActivity.SKIP_INTRO_ACTIVITY_KEY;
import static com.pajato.android.gamechat.main.MainActivity.TEST_USER_KEY;

/**
 * Provide a base class that includes setting up all tests to exclude the intro activity and perform
 * a do nothing test.
 *
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseTest {

    // Private class constants.

    /** The test password key. */
    private static final String TEST_PASSWORD_KEY = "testPasswordKey";

    /** The test user provider. */
    private static final String TEST_PROVIDER_KEY = "testProviderKey";

    // Private class constants.

    ///** The logcat tag. */
    //private static final String TAG = BaseTest.class.getSimpleName();

    // Public instance variables.

    /** Set up the rule instance variable to allow for having intent extras passed in. */
    @Rule public ActivityTestRule<MainActivity> mRule =
            new ActivityTestRule<>(MainActivity.class, true, false);

    @Before public void setup() {
        Intent intent = new Intent();
        intent.putExtra(SKIP_INTRO_ACTIVITY_KEY, true);
        intent.putExtra(TEST_USER_KEY, getProperty(BuildConfig.GC_TEST_EMAIL_KEY, "nobody@gamechat.com"));
        intent.putExtra(TEST_PROVIDER_KEY, getProperty(BuildConfig.GC_TEST_PROVIDER_KEY, "email"));
        intent.putExtra(TEST_PASSWORD_KEY, getProperty(BuildConfig.GC_TEST_PASSWORD_KEY, null));
        mRule.launchActivity(intent);
    }

    @After public void teardown() {
        mRule.getActivity().finish();
    }

    /** Ensure that doing nothing breaks nothing but generates some code coverage results. */
    @Test public void testDoNothing() {
        // Do nothing initially.
    }

    // Protected methods.

    // Private instance methods.

    /** Return a named system property or the given default value if there is no such property. */
    private String getProperty(final String propName, final String defaultValue) {
        String result = System.getProperty(propName);
        return result != null ? result : defaultValue;
    }
}
