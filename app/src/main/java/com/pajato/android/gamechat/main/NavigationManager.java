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
import android.net.Uri;
import android.support.design.widget.NavigationView;
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
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.BackPressEvent;
import com.pajato.android.gamechat.event.NavDrawerOpenEvent;

import org.greenrobot.eventbus.Subscribe;

import static android.graphics.Shader.TileMode.CLAMP;


/** Provide a singleton to manage the app navigation provided by the navigation drawer. */
public enum NavigationManager {
    instance;

    // Private class constants

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

        // Set up the handle navigation menu item clicks and register this manager with the app
        // event manager.
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(activity);
    }

    /** Handle a back press event. */
    @Subscribe(priority = 1)
    public void onBackPressed(final BackPressEvent event) {
        // Detect and close an open nav drawer.  Cancel propagation if the drawer is open.
        if (closeDrawerIfOpen(event.activity)) AppEventManager.instance.cancel(event);
    }

    /** Process a given button click event handling the nav drawer closing. */
    @Subscribe public void onClick(final NavDrawerOpenEvent event) {
        // The nav drawer is probably open so close it.
        closeDrawerIfOpen(event.activity);
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
        loadAccountIcon(account, header);
        setDisplayName(account, header);
        TextView email = (TextView) header.findViewById(R.id.currentAccountEmail);
        email.setText(account.email);
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

    // Private instance methods.

    /** Check for an open navigation drawer and close it if one is found. */
    private boolean closeDrawerIfOpen(final Activity activity) {
        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        return false;
    }

    /** Load the account icon, if available, using a placeholder otherwise. */
    private void loadAccountIcon(final Account account, final View header) {
        // Determine if there is an image to be loaded.
        ImageView icon = (ImageView) header.findViewById(R.id.currentAccountIcon);
        Uri imageUri = Uri.parse(account.url);
        if (imageUri != null) {
            // There is an image to load.  Use Glide to do the heavy lifting.
            icon.setImageURI(imageUri);
            Glide.with(header.getContext())
                .load(account.url)
                .transform(new CircleTransform(header.getContext()))
                .into(icon);
        } else {
            // There is no image.  Use an anonymous image.
            icon.setImageResource(R.drawable.ic_account_circle_black_24dp);
        }
        icon.setVisibility(View.VISIBLE);
    }


    /** Load the display name, if available, using a placeholder otherwise. */
    private void setDisplayName(final Account account, final View header) {
        // Determine if there is a display name to use.
        String name = account.displayName;
        if (name == null) {
            // There is no display name, use a conjured name based on the email address, i.e. the
            // username part of the email address with an initial cap.
            name = account.email;
            int index = name.indexOf('@');
            name = name.substring(0, index);
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        TextView view = (TextView) header.findViewById(R.id.currentAccountDisplayName);
        view.setText(name);
        view.setVisibility(View.VISIBLE);
    }

    // Inner classes.

    /**
     * Provide a Glide transform that allows for circular image containers.  Provided by Harsha
     * Vardhan via the stack Stack Overflow answer at
     * http://stackoverflow.com/questions/25278821/how-do-rounded-image-with-glide-library
     */
    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override protected Bitmap transform(final BitmapPool pool, final Bitmap toTransform,
                                             final int outWidth, final int outHeight) {
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
            paint.setShader(new BitmapShader(squared, CLAMP, CLAMP));
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
