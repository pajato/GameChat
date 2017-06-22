package com.pajato.android.gamechat.main;

import com.pajato.android.gamechat.BaseTest;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FragmentKind;

import org.junit.Assert;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Provide a placeholder for testing the main activity class.
 *
 * Created by pmr on 7/15/16.
 */
public class MainActivityTest extends BaseTest {

    @Test public void getUpHandler() throws Exception {
        // Trigger both kinds twice to exercise both paths.
        Assert.assertTrue(mRule.getActivity().getUpHandler(FragmentKind.chat) != null);
        Assert.assertTrue(mRule.getActivity().getUpHandler(FragmentKind.exp) != null);
        Assert.assertTrue(mRule.getActivity().getUpHandler(FragmentKind.chat) != null);
        Assert.assertTrue(mRule.getActivity().getUpHandler(FragmentKind.exp) != null);
    }

    //@Test public void onAuthenticationChange() throws Exception {}

    //@Test public void onGroupJoined() throws Exception {}

    //@Test public void onBackPressed1() throws Exception {}

    @Test public void onClick() throws Exception {
        // Trigger the onClick(View) method using a click on the chat envelope's FAB button.
        // Open up the FAB menu
        onView(withId(R.id.chatFab)).check(matches(isDisplayed())).perform(click());
    }

    //@Test public void onClick1() throws Exception {}

    //@Test public void onClick2() throws Exception {}

    //@Test public void onNavigationItemSelected1() throws Exception {}

    //@Test public void onMenuItem() throws Exception {}

    //@Test public void onProtectedUserAuthFailureEvent() throws Exception {}

    //@Test public void showOkCancelDialog() throws Exception {}

    //@Test public void showAlertDialog() throws Exception {}

    //@Test public void onRequestPermissionsResult() throws Exception {}

    //@Test public void onActivityResult() throws Exception {}

    //@Test public void onCreate() throws Exception {}

    //@Test public void onBackPressed() throws Exception {}

    //@Test public void onNavigationItemSelected() throws Exception {}

    //@Test public void tileOnClick() throws Exception {}
}
