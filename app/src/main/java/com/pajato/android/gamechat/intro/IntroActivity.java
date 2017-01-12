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

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.os.Build;
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

import com.firebase.ui.auth.AuthUI;
import com.pajato.android.gamechat.R;

import java.util.Arrays;

import static android.view.animation.AnimationUtils.loadAnimation;

/**
 * Provide an intro activity ala Telegram.
 *
 * @author Paul Michael Reilly
 */
public class IntroActivity extends AppCompatActivity {

    // Private class constants.

    /** The request code passed into the sign in activity. */
    private static final int RC_SIGN_IN = 1;

    // Private class variables.

    /** Handle signing into an existing account by invoking the signin activity. */
    public void doSignIn(final View view) {
        invokeSignIn("signin");
    }

    // Protected instance methods.

    /** Handle the sign in activity result, if any. */
    @Override protected void onActivityResult(final int requestCode, final int resultCode,
                                              final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            // Pass the intent obtained from the sign in activity through to the calling intent.
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    /** Create the intro activity to highlight some features and provide a get started opertion. */
    @Override protected void onCreate(final Bundle savedInstanceState) {
        // Establish the activity state and set up the intro layout.
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_intro);

        // Determine whether or not to enable button elevation animation. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // From lollippop on, enable the button elevation animation.
            final StateListAnimator animator = new StateListAnimator();
            final TextView button = (TextView) findViewById(R.id.register_button);
            addState(animator, android.R.attr.state_pressed, button, 2.0f, 4.0f);
            addState(animator, -1, button, 4.0f, 2.0f);
            button.setStateListAnimator(animator);
        }

        // Set up icon switching animation.
        ImageView topImage1 = (ImageView) findViewById(R.id.icon_image1);
        ImageView topImage2 = (ImageView) findViewById(R.id.icon_image2);
        ViewGroup pageMonitor = (ViewGroup) findViewById(R.id.page_monitor);
        topImage2.setVisibility(View.GONE);

        // Set up the view pager adapter, the page change handler and the view pager.
        IntroAdapter adapter = new IntroAdapter(pageMonitor);
        ViewPager pager = (ViewPager) findViewById(R.id.intro_view_pager);
        pager.setAdapter(adapter);
        pager.setPageMargin(0);
        pager.setOffscreenPageLimit(1);
        PageChangeHandler handler = new PageChangeHandler(topImage1, topImage2, pageMonitor, pager);
        pager.addOnPageChangeListener(handler);
    }

    // Private instance methods.

    /** Add an animation state to a given state list animator.  Only called on Lollipop. */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addState(final StateListAnimator animator, final int viewStateId, final View view,
                          final float... heights) {
        // Animate the Z property on the given view over the given heights for 200 milliseconds.
        final int DURATION = 200;
        final String PROP = "z";
        int [] viewState = viewStateId != -1 ? new int[] {viewStateId} : new int[] {};
        ObjectAnimator viewAnimator = ObjectAnimator.ofFloat(view, PROP, heights);
        animator.addState(viewState, viewAnimator.setDuration(DURATION));
    }

    /** Finish the intro screen and handle the given mode in a new activity. */
    private void invokeSignIn(final String mode) {
        // Get an instance of AuthUI based on the default app
        AuthUI.SignInIntentBuilder intentBuilder = AuthUI.getInstance().createSignInIntentBuilder();
        intentBuilder.setProviders(Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()));
        intentBuilder.setLogo(R.drawable.signin_logo);
        intentBuilder.setTheme(R.style.signInTheme);
        // Disable Smart Lock for development purposes -- to ensure logging in processes work correctly.
        intentBuilder.setIsSmartLockEnabled(false);

        Intent intent = intentBuilder.build();
        intent.putExtra(mode, true);
        startActivityForResult(intent, RC_SIGN_IN);
    }

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
            if (index == position) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE);
            } else {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL);
            }
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
            TextView headerTextView = (TextView) view.findViewById(R.id.header_text);
            TextView messageTextView = (TextView) view.findViewById(R.id.message_text);
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
                final ImageView fadeoutImage;
                final ImageView fadeinImage;
                if (mTopImage1.getVisibility() == View.VISIBLE) {
                    fadeoutImage = mTopImage1;
                    fadeinImage = mTopImage2;
                } else {
                    fadeoutImage = mTopImage2;
                    fadeinImage = mTopImage1;
                }

                // Initialize the fadein and fadeout images.
                fadeinImage.bringToFront();
                fadeinImage.setImageResource(Pages.values()[mLastPagePosition].iconId);
                fadeinImage.clearAnimation();
                fadeoutImage.clearAnimation();

                // Animate the icon switching.
                Context context = mPager.getContext();
                Animation outAnimation = loadAnimation(context, R.anim.icon_fade_out);
                outAnimation.setAnimationListener(new AnimationHandler(null, fadeoutImage));
                Animation inAnimation = loadAnimation(context, R.anim.icon_fade_in);
                inAnimation.setAnimationListener(new AnimationHandler(fadeinImage, null));
                fadeoutImage.startAnimation(outAnimation);
                fadeinImage.startAnimation(inAnimation);
            }
        }
    }

    /** Provide a class to handle fade in or fade out animation. */
    private static class AnimationHandler implements Animation.AnimationListener {

        // Private instance variables.

        /** The fadein image. */
        private final ImageView mFadeinImage;

        /** The fadout image. */
        private final ImageView mFadeoutImage;

        /** Build the animation handler. */
        AnimationHandler(final ImageView fadeinImage, final ImageView fadeoutImage) {
            mFadeinImage = fadeinImage;
            mFadeoutImage = fadeoutImage;
        }

        /** Implement the start of animation only if there is a fadein image. */
        @Override public void onAnimationStart(final Animation animation) {
            if (mFadeinImage != null) mFadeinImage.setVisibility(View.VISIBLE);
        }

        /** Implement the end of animation only if there is a fadeout image. */
        @Override public void onAnimationEnd(final Animation animation) {
            if (mFadeoutImage != null) mFadeoutImage.setVisibility(View.GONE);
        }

        @Override public void onAnimationRepeat(final Animation animation) {}
    }

}
