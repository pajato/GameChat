/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
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

package com.pajato.android.gamechat.exp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.TagClickEvent;

/**
 * Manages the game related aspects of the GameChat application. These include the creation of new
 * game instances, notifications, and game settings.
 *
 * @author Bryan Scott
 */
enum NotificationManager {
    instance;

    // Public instance methods.

    /** Create and show a Snackbar notification based on the given parameters. */
    public void notify(@NonNull final Fragment fragment, final String text, final boolean done) {
        // Ensure that the fragment is attached and has a view.  Abort if it does not.
        if (fragment.getView() == null) return;

        // Determine if the experience is finished.
        /* The notifcation snackbar. */
        Snackbar snackbar;
        if (done) {
            // The game is ended so generate a notification that could start a new game.
            snackbar = Snackbar.make(fragment.getView(), text, Snackbar.LENGTH_LONG);
            final String playAgain = fragment.getContext().getString(R.string.PlayAgain);
            snackbar.setAction(playAgain, new SnackbarActionHandler(fragment));
        } else {
            // The game hasn't ended so generate a notification without an action.
            snackbar = Snackbar.make(fragment.getView(), text, Snackbar.LENGTH_SHORT);
        }

        // Use a primary color background with white text for the snackbar and hide the FAB button
        // while the snackbar is presenting.
        int color = ContextCompat.getColor(fragment.getContext(), R.color.colorPrimaryDark);
        snackbar.getView().setBackgroundColor(color);
        snackbar.setActionTextColor(ColorStateList.valueOf(Color.WHITE))
            .setCallback(new SnackbarChangeHandler(fragment))
            .show();
    }

    /** Put up a snackbar for the given fragment and resource string. */
    public void notify(final Fragment fragment, final int resId) {
        String message = fragment.getContext().getString(resId);
        notify(fragment, message, false);
    }

    // Inner classes.

    /** Provide a handler to show/hide the FAB for snackbar messaging. */
    private class SnackbarChangeHandler extends Snackbar.Callback {

        // Instance variables.

        /** The calling fragment. */
        Fragment mFragment;

        // Constructors

        /** Build an instance with a given fragment. */
        SnackbarChangeHandler(final Fragment fragment) {
            mFragment = fragment;
        }

        @Override public void onDismissed(final Snackbar snackbar, final int event) {
            FabManager.game.show(mFragment);
        }

        @Override public void onShown(final android.support.design.widget.Snackbar snackbar) {
            FabManager.game.hide(mFragment);
        }
    }

    /** Handle a snackbar action click. */
    private class SnackbarActionHandler implements View.OnClickListener {

        // Instance variables.

        /** The tag value to post with the event to the app. */
        String mClassName;

        // Constructor.

        /** Build an instance with a given tag value. */
        SnackbarActionHandler(final Fragment fragment) {
            mClassName = fragment.getClass().getSimpleName();
        }

        /** Handle an action click from the snackbar by posting the tag to the app. */
        @Override public void onClick(final View view) {
            // Post the saved tag (the originating fragment's tag) to the app.
            view.setTag(mClassName);
            AppEventManager.instance.post(new TagClickEvent(view));
        }

    }
}
