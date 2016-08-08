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

package com.pajato.android.gamechat.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.account.Account;


/** Provide a singleton to manage the app navigation provided by the navigation drawer. */
public enum NavigationManager {
    instance;

    // Private class constants

    /** The logcat tag constant. */
    private static final String TAG = NavigationManager.class.getSimpleName();

    // Navigation drawer action constants.
    private static final int OPEN_ID = R.string.navigation_drawer_action_open;
    private static final int CLOSE_ID = R.string.navigation_drawer_action_close;

    // Public instance methods

    /** Initialize the navigation drawer. */
    public void init(final MainActivity activity, final Toolbar toolbar) {
        // Set up the action bar drawer toggle.
        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle;
        toggle = new ActionBarDrawerToggle(activity, drawer, toolbar, OPEN_ID, CLOSE_ID);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /** Set up the navigation header to show an account. */
    public void setAccount(final Account account, final View header) {
        // Hide the sign in text and show the current profile and the sign
        // out text.
        View view = header.findViewById(R.id.signIn);
        view.setVisibility(View.GONE);
        view = header.findViewById(R.id.signOut);
        view.setVisibility(View.VISIBLE);
        view = header.findViewById(R.id.currentProfile);
        view.setVisibility(View.VISIBLE);

        // Load the account image, display name and email address.
        ImageView icon = (ImageView) header.findViewById(R.id.currentAccountIcon);
        icon.setVisibility(View.VISIBLE);
        icon.setImageURI(account.getAccountUrl());
        Glide.with(header.getContext())
                .load(account.getAccountUrl())
                .transform(new CircleTransform(header.getContext()))
                .into(icon);
        TextView name = (TextView) header.findViewById(R.id.currentAccountDisplayName);
        name.setText(account.getDisplayName());
        TextView email = (TextView) header.findViewById(R.id.currentAccountEmail);
        email.setText(account.getAccountId());
    }

    /** Set up the navigation header to show the sign in button. */
    public void setNoAccount(final View header) {
        // There is no current user account.  Make the sign in text visible, hide the sign out text
        // and do not show a current profile.
        View view = header.findViewById(R.id.signIn);
        view.setVisibility(View.VISIBLE);
        view = header.findViewById(R.id.signOut);
        view.setVisibility(View.GONE);
        view = header.findViewById(R.id.currentProfile);
        view.setVisibility(View.GONE);
    }

    /** Check for an open navigation drawer and close it if one is found. */
    public boolean closeDrawerIfOpen(final Activity activity) {
        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        return false;
    }

    /**
     * Provide a Glide transform that allows for circular image containers.  Provided by Harsha
     * Vardhan via the stack Stack Overflow answer at
     * http://stackoverflow.com/questions/25278821/how-do-rounded-image-with-glide-library
     */
    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName();
        }
    }

}
