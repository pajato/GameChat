package com.pajato.android.gamechat;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ImageView;

import com.pajato.android.gamechat.main.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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

    /** Provide access to our custom withDrawable matcher, that matches a view's drawable. */
    protected static Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

    /** Provide access to our custom noDrawable matcher. */
    protected static Matcher<View> noDrawable() {
        return new DrawableMatcher(-1);
    }

    /**
     * DrawableMatcher, a custom TypeSafeMatcher that facilitates the scanning of ImageViews to
     * determine their contents during espresso-related testing.
     *
     * @author Daniele Bottillo
     * @see "https://medium.com/@dbottillo/android-ui-test-espresso-matcher-for-imageview-1a28c832626f#.mnqccdvry"
     */
    private static class DrawableMatcher extends TypeSafeMatcher<View> {

        private final int expectedId;
        private String resourceName;

        DrawableMatcher(final int expectedId) {
            super(View.class);
            this.expectedId = expectedId;
        }

        @Override protected boolean matchesSafely(final View target) {
            if (!(target instanceof ImageView)){
                return false;
            }
            ImageView imageView = (ImageView) target;
            if (expectedId < 0){
                return imageView.getDrawable() == null;
            }
            Resources resources = target.getContext().getResources();
            Drawable expectedDrawable = resources.getDrawable(expectedId);
            resourceName = resources.getResourceEntryName(expectedId);

            if (expectedDrawable == null) {
                return false;
            }

            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            Bitmap otherBitmap = ((BitmapDrawable) expectedDrawable).getBitmap();
            return bitmap.sameAs(otherBitmap);
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("with drawable from resource id: ");
            description.appendValue(expectedId);
            if (resourceName != null) {
                description.appendText("[");
                description.appendText(resourceName);
                description.appendText("]");
            }
        }
    }

}
