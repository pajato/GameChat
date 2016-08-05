package com.pajato.android.gamechat;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.view.View;

import com.pajato.android.gamechat.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;


/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class SignInTest {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = SignInTest.class.getSimpleName();

    /** The overflow menu label string. */
    private static final String OVERFLOW_MENU_ITEM_SETTINGS_TEXT = "Settings";

    // Public instance variables.

    /** Specify the activity under test. */
    @Rule public ActivityTestRule<MainActivity> mRule = new ActivityTestRule<>(MainActivity.class);

    /** The device being manipulate, as necessary, by UIAuotmator to support signin. */
    private UiDevice mDevice;

    /** The flag indicating that gamechattester@gmail.com is signed in to GameChat. */
    private boolean mSignedIn = false;

    @Before
    public void setup() throws UiObjectNotFoundException {
        // Determine if the sign in activity button is showing.
        View view = mRule.getActivity().findViewById(R.id.sign_in_button);
        if (view == null) {
            // Use UIAuotmator to sign in.
            mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            UiObject signInButton = mDevice.findObject(new UiSelector().text("Sign in"));
            signInButton.click();
            UiObject radioButton = mDevice.findObject(new UiSelector().text("gamechattester@gmail.com"));
            radioButton.click();
            mSignedIn = true;
        }
    }

    /** Ensure that doing nothing breaks nothing but generates some code coverage results. */
    @Test public void testDoNothing()  {
        // Do nothing initially.
        assertTrue("Expected sign in button to be present!", mSignedIn);

    }

}
