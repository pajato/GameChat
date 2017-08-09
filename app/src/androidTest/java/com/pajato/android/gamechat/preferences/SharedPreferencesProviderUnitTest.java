package com.pajato.android.gamechat.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pajato.android.gamechat.intro.IntroActivity;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Provide a unit test for the SharedPreferencesProvider class.
 *
 * Created by pmr on 8/4/17.
 */
@RunWith(AndroidJUnit4.class)
public class SharedPreferencesProviderUnitTest {

    // Public instance variables.

    /** Set up the rule instance variable to allow for having intent extras passed in. */
    @Rule
    public ActivityTestRule<IntroActivity> mRule =
        new ActivityTestRule<>(IntroActivity.class, true, false);

    /** Launch the intro activity at the start of every test. */
    @Before public void setUp() {
        Intent intent = new Intent();
        mRule.launchActivity(intent);
    }

    /** Gracefully shut down the test (intro) activity. */
    @After public void tearDown() {
        mRule.getActivity().finish();
    }

    @Test public void testAll() throws Exception {
        final Activity activity = mRule.getActivity();
        final String name = "IntroPrefs";
        final int mode = Context.MODE_PRIVATE;
        PreferencesProvider prefs = new SharedPreferencesProvider(activity, name, mode);
        prefs.persist(null);
        Map<String, Preference> map = prefs.getAll();
        Assert.assertTrue("The shared preferences file is not empty!", map.size() == 0);

        // Next test...
        final String BOOL_KEY = "boolKey";
        final String STRING_SET_KEY = "stringSetKey";
        List<Preference> list = new ArrayList<>();
        list.add(new Preference(BOOL_KEY, true));
        list.add(new Preference(STRING_SET_KEY, new TreeSet<String>()));
        prefs.persist(list);
        Assert.assertEquals("The shared preferences file size is wrong!", 2, map.size());
        Assert.assertEquals("The boolean value is wrong.", true, prefs.getBoolean(BOOL_KEY, false));
    }
}
