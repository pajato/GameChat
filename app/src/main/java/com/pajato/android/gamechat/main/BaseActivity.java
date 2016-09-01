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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Locale;

/**
 * Provide a base activity (abstract) to log lifecycle events.  This is being done to manage the
 * complexity around when to turn off and turn on backend (Firebase) listeners.  Logcat will
 * intersperse the activity and fragment lifecycle events.  This will allow a visualization of what
 * is happening when and in so doing provide a better base on deciding when to do what and why.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Private class constants.

    /** The logcat tag constant. */
    private static final String TAG = BaseActivity.class.getSimpleName();

    // Public instance methods

    /** Log the onCreate() state. */
    @Override protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        String format = "Main activity {%s} created with %sempty saved initialization state.";
        Log.d(TAG, String.format(Locale.US, format, this, bundle == null ? "" : "non-"));
    }

    /** Log the onDestroy() state. */
    @Override protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, String.format(Locale.US, "Main activity {%s} is dying.", this));
    }

    /** Log the onPause() state. */
    @Override protected void onPause() {
        super.onPause();
        Log.d(TAG, String.format(Locale.US, "Main activity {%s} is pausing.", this));
    }

    /** Log the onRestart() state. */
    @Override protected void onRestart() {
        super.onRestart();
        Log.d(TAG, String.format(Locale.US, "Main activity {%s} is restarting.", this));
    }

    /** Log the onResume() state. */
    @Override protected void onResume() {
        super.onResume();
        Log.d(TAG, String.format(Locale.US, "Main activity {%s} is resuming.", this));
    }

    /** Log the onStart() state. */
    @Override protected void onStart() {
        super.onStart();
        Log.d(TAG, String.format(Locale.US, "Main activity {%s} is starting.", this));
    }

    /** Log the onStop() state. */
    @Override protected void onStop() {
        super.onStop();
        Log.d(TAG, String.format(Locale.US, "Main activity {%s} is stopping.", this));
    }

}
