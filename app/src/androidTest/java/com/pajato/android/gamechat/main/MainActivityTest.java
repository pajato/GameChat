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

package com.pajato.android.gamechat.main;

import com.pajato.android.gamechat.BaseTest;
import com.pajato.android.gamechat.common.FragmentKind;
import com.pajato.android.gamechat.common.model.Account;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

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

    /** Test that the toMap() base method gets exercised. */
    @Test public void testToMap() {
        Account account = new Account();
        Map<String, Object> map = account.toMap();
        Assert.assertEquals("The empty account map size is wrong.", 15, map.size());
    }

    //@Test public void onAuthenticationChange() throws Exception {}

    //@Test public void onGroupJoined() throws Exception {}

    //@Test public void onBackPressed1() throws Exception {}

    @Test public void onClick() throws Exception {
        // Trigger the onClick(View) method using a click on the chat envelope's FAB button.
        // Open up the FAB menu
        //onView(withId(R.id.chatFab)).check(matches(isDisplayed())).perform(click());
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
