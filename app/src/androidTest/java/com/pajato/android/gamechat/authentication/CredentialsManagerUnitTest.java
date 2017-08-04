package com.pajato.android.gamechat.authentication;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import com.firebase.ui.auth.IdpResponse;
import com.pajato.android.gamechat.preferences.Preference;
import com.pajato.android.gamechat.preferences.PreferencesProvider;
import com.pajato.android.gamechat.preferences.TestPreferencesProvider;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * All tests are based on the the following documentation:
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class CredentialsManagerUnitTest {

    // Private instance variables.

    private PreferencesProvider mPrefs;

    // Public instance methods.

    /** Initialize the credentials manager instance under test using the test prefs provider  */
    @Before public void setup() {
        mPrefs = new TestPreferencesProvider();
        CredentialsManager.instance.init(mPrefs);
    }

    @After public void teardown() {}

    /** Cover the enum "extra" methods. */
    @Test public void testEnum() {
        Assert.assertEquals("Enumeration size is wrong!", 1, CredentialsManager.values().length);
        boolean value = CredentialsManager.valueOf("instance") == CredentialsManager.instance;
        Assert.assertTrue("Enumeration value is wrong!", value);
    }

    /** Ensure that the initialization with an empty preferences provider is as expected. */
    @Test public void testInitEmpty() {
        // Initialize the manager and confirm that the credentials map is empty.
        mPrefs = new TestPreferencesProvider();
        CredentialsManager.instance.init(mPrefs);
        Map<String, Credentials> map = CredentialsManager.instance.getMap();
        Assert.assertTrue("The credentials manager map is null.", map != null);
        Assert.assertEquals("The credentials manager is not initialized.", 0, map.size());
    }

    /** Ensure that the initialization with an non-empty preferences provider is as expected. */
    @Test public void testInitNonEmpty() {
        // Initialize the manager and confirm that the credentials map is empty.
        mPrefs = new TestPreferencesProvider();
        Credentials credentials = new Credentials("google.com", "foo@gc.com", null, null);
        Set<String> stringSet = new TreeSet<>();
        stringSet.add("Some invalid string");
        stringSet.add(credentials.toString());
        credentials.email = "foo@gc.com";
        credentials.provider = null;
        stringSet.add(credentials.toString());
        List<Preference> list = new ArrayList<>();
        list.add(new Preference(CredentialsManager.CREDENTIALS_SET_KEY, stringSet));
        mPrefs.persist(list);
        CredentialsManager.instance.init(mPrefs);
        Map<String, Credentials> map = CredentialsManager.instance.getMap();
        Assert.assertTrue("The credentials manager map is null.", map != null);
        Assert.assertEquals("The credentials manager is not initialized.", 1, map.size());
    }

    /** Test the identity provider (IDP) persistence... */
    @Test public void testPersistence() {
        IdpResponse response = new IdpResponse("google.com", "foo@gc.com", "abcd", "password");
        CredentialsManager.instance.persist(response);
        Map<String, Credentials> map = CredentialsManager.instance.getMap();
        Assert.assertEquals("Credentials did not get added!", 1, map.size());
    }

    /** Test the photo URL update... */
    @Test public void testUpdate() {
        // Test the case when the credentials photo URL is changed from null to non-null.
        IdpResponse response = new IdpResponse("google.com", "foo@gc.com", "abcd", "password");
        CredentialsManager.instance.persist(response);
        Uri uri = Uri.parse("http://www.abc.com/photo.jpg");
        CredentialsManager.instance.update(response.getEmail(), uri);
        Map<String, Credentials> map = CredentialsManager.instance.getMap();
        Assert.assertEquals("Credentials did not get added!", 1, map.size());

        // Test the case when the credentials non-null photo URL is not changed at all.
        CredentialsManager.instance.update(response.getEmail(), uri);
        map = CredentialsManager.instance.getMap();
        Assert.assertEquals("Credentials did not get added!", 1, map.size());

        // Test the case when the credentials non-null photo URL is changed to another non-null
        // value.
        uri = Uri.parse("http://www.abc.com/photo2.png");
        CredentialsManager.instance.update(response.getEmail(), uri);
        map = CredentialsManager.instance.getMap();
        Assert.assertEquals("Credentials did not get added!", 1, map.size());

        // Test the case when the credentials photo URL is changed from non-null to null.
        CredentialsManager.instance.update(response.getEmail(), null);
        map = CredentialsManager.instance.getMap();
        Assert.assertEquals("Credentials did not get added!", 1, map.size());

        // Test the case when the credentials photo URL is null and not changed.
        CredentialsManager.instance.update(response.getEmail(), null);
        map = CredentialsManager.instance.getMap();
        Assert.assertEquals("Credentials did not get added!", 1, map.size());

        // Test the case when the credentials provider is null.
        Credentials credentials = map.get(response.getEmail());
        credentials.provider = null;
        uri = Uri.parse("http://www.abc.com/photo3.png");
        CredentialsManager.instance.update(response.getEmail(), uri);
        map = CredentialsManager.instance.getMap();
        Assert.assertEquals("Credentials did not get added!", 1, map.size());
    }
}
