package com.pajato.android.gamechat.preferences;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a unit test class to achieve acceptable coverage on the TestPreferencesProvider class.
 *
 * Created by pmr on 8/4/17.
 */
public class TestPreferencesProviderUnitTest {
    @Test public void testGetAndPersist() throws Exception {
        final String KEY = "key";
        TestPreferencesProvider prefs = new TestPreferencesProvider();
        boolean value = prefs.getBoolean("noValue", false);

        // Test the persist() with a list.
        Assert.assertFalse("Get boolean did not provide the default!", value);
        List<Preference> list = new ArrayList<>();
        list.add(new Preference(KEY, true));
        prefs.persist(list);
        value = prefs.getBoolean(KEY, false);
        Assert.assertEquals("Persisted the wrong number of values!", 1, prefs.getAll().size());
        Assert.assertTrue("Persisted boolean value is wrong!", value);

        // Test the persist() with a null.
        prefs.persist(null);
        value = prefs.getBoolean(KEY, false);
        Assert.assertFalse("Defaulted value is wrong!", value);
    }
}
