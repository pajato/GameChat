package com.pajato.android.gamechat.preferences;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.pajato.android.gamechat.preferences.Preference.Type.bool;
import static com.pajato.android.gamechat.preferences.Preference.Type.stringset;
import static com.pajato.android.gamechat.preferences.Preference.Type.valueOf;
import static com.pajato.android.gamechat.preferences.Preference.Type.values;

/**
 * Provide a unit test class in order to achieve acceptable coverage on the Preference class code.
 *
 * Created by pmr on 8/2/17.
 */
public class PreferenceUnitTest {
    @Before public void setUp() throws Exception { }

    @After public void tearDown() throws Exception { }

    @Test public void testType() {
        // Test the implicit enum methods.
        Assert.assertEquals("The type size is wrong!", 2, values().length);
        Assert.assertEquals("The value is wrong!", bool, valueOf("bool"));
        Assert.assertEquals("The value is wrong!", stringset, valueOf("stringset"));
    }
}
