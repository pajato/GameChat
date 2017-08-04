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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/** Provide a singleton to manage showing and hiding the initial loading status. */
public enum ProgressManager {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ProgressManager.class.getSimpleName();

    // Private instance variables.

    /** Indicates if the dialog is showing. */
    private boolean mIsShowing;

    // Public instance methods

    /** Dismiss the initial loading dialog if one is showing. */
    public void hide() {
        Log.d(TAG, "Attempting to hide the progress dialog.");
        if (mIsShowing) {
            //mProgressBar.setVisibility(View.GONE);
            //mProgressBar = null;
            mIsShowing = false;
        }
    }

    /** Return TRUE iff the progress spinner is being shown. */
    public boolean isShowing() {
        return mIsShowing;
    }

    /** Show the initial loading dialog. */
    public void show(@NonNull final Context context) {
        show(context, "Starting...", "Please wait while the app starts up...");
    }

    /** Show a loading dialog with a given message. */
    public void show(@SuppressWarnings("unused") final Context context,
                     @SuppressWarnings("unused") final String title,
                     @SuppressWarnings("unused") final String message) {
        // Create and display the progress dialog.
        Log.d(TAG, "Turning on progress spinner now.");
        //mProgressBar = new ProgressBar(context);
        //mProgressBar.setTitle(title);
        //mProgressBar.setMessage(message);
        //mProgressBar.setVisibility(View.VISIBLE);
        mIsShowing = true;
    }

}
