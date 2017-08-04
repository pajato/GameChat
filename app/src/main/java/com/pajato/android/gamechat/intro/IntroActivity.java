/*
 * Copyright (C) 2016 Pajato Technologies LLC.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see http://www.gnu.org/licenses
 */

package com.pajato.android.gamechat.intro;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.authentication.AuthenticationManager;
import com.pajato.android.gamechat.main.MainActivity;

import static android.view.animation.AnimationUtils.loadAnimation;
import static com.pajato.android.gamechat.main.MainActivity.RC_SIGN_IN;

/**
 * Provide an intro activity ala Telegram.
 *
 * @author Paul Michael Reilly
 * @author Bryan Scott
 */
public class IntroActivity extends AppCompatActivity {

    // Private class variables.

    /** Handle signing into an existing account by invoking the sign-in activity. */
    public void doSignIn(final View view) {
        // Start the Firebase sign-in process using the standard sign-in intent.
        startActivityForResult(AuthenticationManager.getAuthIntent(), RC_SIGN_IN);
    }

    // Protected instance methods.

    /** Process the activity result by passing sign-in success onto the main activity. */
    @Override
    protected void onActivityResult(final int request, final int result, final Intent intent) {
        // Handle a successful result directly and a failed result by passing it back to the
        // invoking activity via the intent.
        super.onActivityResult(request, result, intent);
        if (result == RESULT_OK && request == RC_SIGN_IN)
            MainActivity.processSignIn(intent);
        setResult(result, intent);
        finish();
    }

    /** Create the intro activity to highlight some features and provide a get started operation. */
    @Override protected void onCreate(final Bundle savedInstanceState) {
        // Establish the activity state and set up the intro layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Set up icon switching animation.
        ImageView topImage1 = findViewById(R.id.icon_image1);
        ImageView topImage2 = findViewById(R.id.icon_image2);
        ViewGroup pageMonitor = findViewById(R.id.page_monitor);
        topImage2.setVisibility(View.GONE);

        // Set up the view pager adapter, change handler and the view pager.
        IntroAdapter adapter = new IntroAdapter(pageMonitor);
        ViewPager pager = findViewById(R.id.intro_view_pager);
        pager.setAdapter(adapter);
        pager.setPageMargin(0);
        pager.setOffscreenPageLimit(1);
        PageChangeHandler handler = new PageChangeHandler(topImage1, topImage2, pageMonitor, pager);
        pager.addOnPageChangeListener(handler);
    }

    // Private instance methods.

    /** Update a given page monitor for a given selected position. */
    private void updatePageMonitor(final ViewGroup pageMonitor, final int position) {
        // Walk the list of child nodes to set the size of the selected page circle icon to be twice
        // the size of an unselected page circle icon.
        final float LARGE = 30.0f;
        final float SMALL = 15.0f;
        int count = pageMonitor.getChildCount();
        for (int index = 0; index < count; index++) {
            TextView child = (TextView) pageMonitor.getChildAt(index);
            child.setText(R.string.intro_page_circle);
            float size = index == position ? LARGE : SMALL;
            child.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    // Nested classes.

    /** Provide a nested fragment pager adapter class to manage the pages. */
    private class IntroAdapter extends PagerAdapter {

        // Private instance variables.

        /** The page monitor array of circles, a closed circle represents the selected page. */
        private ViewGroup mPageMonitor;

        // Public constructor.

        /** Build the intro adapter with a given page monitor. */
        IntroAdapter(final ViewGroup pageMonitor) {
            mPageMonitor = pageMonitor;
        }

        /** Satisfy the getCount() interface. */
        @Override public int getCount() {
            return Pages.values().length;
        }

        /** Satisfy the instantiateItem interface to provide the page. */
        @Override public Object instantiateItem(final ViewGroup container, final int position) {
            View view = View.inflate(container.getContext(), R.layout.intro_page, null);
            TextView headerTextView = view.findViewById(R.id.header_text);
            TextView messageTextView = view.findViewById(R.id.message_text);
            container.addView(view, 0);
            Pages page = Pages.values()[position];
            headerTextView.setText(view.getContext().getString(page.titleId));
            messageTextView.setText(view.getContext().getString(page.messageId));

            return view;
        }

        @Override public void destroyItem(final ViewGroup container, final int position,
                                          final Object object) {
            container.removeView((View) object);
        }

        @Override public void setPrimaryItem(final ViewGroup container, final int position,
                                             final Object object) {
            super.setPrimaryItem(container, position, object);
            updatePageMonitor(mPageMonitor, position);
        }

        @Override public boolean isViewFromObject(final View view, final Object object) {
            return view.equals(object);
        }

        @Override public Parcelable saveState() {
            return null;
        }

        @Override public void unregisterDataSetObserver(final DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    /** Provide a nested class to handle page changes with animation. */
    private class PageChangeHandler extends ViewPager.SimpleOnPageChangeListener {

        // Private instance variables.

        /** The index for the last page displayed. */
        private int mLastPagePosition;

        /** The first top image ??? */
        private ImageView mTopImage1;

        /** The second top image ??? */
        private ImageView mTopImage2;

        /** The view pager parent. */
        private ViewPager mPager;

        // Public constructor.

        /** Build the page change handler for a given set of top images and a page monitor. */
        PageChangeHandler(final ImageView topImage1, final ImageView topImage2,
                          final ViewGroup pageMonitor, final ViewPager pager) {
            mTopImage1 = topImage1;
            mTopImage2 = topImage2;
            mPager = pager;
            updatePageMonitor(pageMonitor, 0);
        }

        /** Implement to achieve an animated page switch. */
        @Override public void onPageScrollStateChanged(final int i) {
            // Determine if the view pager is in a state conducive to animation.
            if ((i == ViewPager.SCROLL_STATE_IDLE || i == ViewPager.SCROLL_STATE_SETTLING)
                && (mLastPagePosition != mPager.getCurrentItem())) {
                // Animate the transition by saving the current item and setting the fade in and
                // fade out images,
                mLastPagePosition = mPager.getCurrentItem();
                final ImageView fadeOutImage;
                final ImageView fadeInImage;
                if (mTopImage1.getVisibility() == View.VISIBLE) {
                    fadeOutImage = mTopImage1;
                    fadeInImage = mTopImage2;
                } else {
                    fadeOutImage = mTopImage2;
                    fadeInImage = mTopImage1;
                }

                // Initialize the fade-in and fadeout images.
                fadeInImage.bringToFront();
                fadeInImage.setImageResource(Pages.values()[mLastPagePosition].iconId);
                fadeInImage.clearAnimation();
                fadeOutImage.clearAnimation();

                // Animate the icon switching.
                Context context = mPager.getContext();
                Animation outAnimation = loadAnimation(context, R.anim.icon_fade_out);
                outAnimation.setAnimationListener(new AnimationHandler(null, fadeOutImage));
                Animation inAnimation = loadAnimation(context, R.anim.icon_fade_in);
                inAnimation.setAnimationListener(new AnimationHandler(fadeInImage, null));
                fadeOutImage.startAnimation(outAnimation);
                fadeInImage.startAnimation(inAnimation);
            }
        }
    }

    /** Provide a class to handle fade in or fade out animation. */
    private static class AnimationHandler implements Animation.AnimationListener {

        // Private instance variables.

        /** The fade-in image. */
        private final ImageView mFadeInImage;

        /** The fadeout image. */
        private final ImageView mFadeoutImage;

        /** Build the animation handler. */
        AnimationHandler(final ImageView fadeInImage, final ImageView fadeoutImage) {
            mFadeInImage = fadeInImage;
            mFadeoutImage = fadeoutImage;
        }

        /** Implement the start of animation only if there is a fade-in image. */
        @Override public void onAnimationStart(final Animation animation) {
            if (mFadeInImage != null) mFadeInImage.setVisibility(View.VISIBLE);
        }

        /** Implement the end of animation only if there is a fadeout image. */
        @Override public void onAnimationEnd(final Animation animation) {
            if (mFadeoutImage != null) mFadeoutImage.setVisibility(View.GONE);
        }

        @Override public void onAnimationRepeat(final Animation animation) {}
    }

}
