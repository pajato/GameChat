package com.pajato.android.gamechat.help;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;

import com.pajato.android.gamechat.BaseTest;
import com.pajato.android.gamechat.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static android.support.test.espresso.web.model.Atoms.getCurrentUrl;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.CoreMatchers.containsString;

public class WebViewTest extends BaseTest {
    @Test
    public void testWebViewUrl() {
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())
                .check(matches(isDisplayed()));
        onView(withText("Help & Feedback"))
                .perform(click());
        onView(withId(R.id.helpItemList))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .check(matches(isDisplayed()));
        onView(withText("Get started with GameChat"))
                .check(matches(isDisplayed()))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .perform(click());

        onWebView()
                .check(webMatches(getCurrentUrl(), containsString("file")));
    }
}
