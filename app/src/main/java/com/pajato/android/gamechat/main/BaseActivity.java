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

import com.pajato.android.gamechat.database.DatabaseRegistrar;
import com.pajato.android.gamechat.event.AppEventManager;

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

    /** The lifecycle event format with no bundle. */
    private static final String FORMAT_NO_BUNDLE = "Activity: %s; Lifecycle event: %s.";

    /** The lifecycle event format with a bundle provided. */
    private static final String FORMAT_WITH_BUNDLE =
            "Activity: %s; Lifecycle event: %s; Bundle: %s.";

    /** The logcat tag constant. */
    private static final String TAG = BaseActivity.class.getSimpleName();

    // Protected instance methods.

    /** Log a lifecycle event that has no bundle. */
    protected void logEvent(final String event) {
        Log.v(TAG, String.format(Locale.US, FORMAT_NO_BUNDLE, this, event));
    }

    /** Log a lifecycle event that has a bundle. */
    protected void logEvent(final String event, final Bundle bundle) {
        Log.v(TAG, String.format(Locale.US, FORMAT_WITH_BUNDLE, this, event, bundle));
    }

    /** Log the onCreate() state. */
    @Override protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        logEvent("onCreate", bundle);
    }

    /** Log the onDestroy() state. */
    @Override protected void onDestroy() {
        logEvent("onDestroy");
        AppEventManager.instance.unregisterAll();
        DatabaseRegistrar.instance.unregisterAll();
        try {
            super.onDestroy();
        } catch (IndexOutOfBoundsException exc) {
            Log.d(TAG, "Got the oob exception while dieing.", exc);
        }
    }

    /** Log the onPause() state. */
    @Override protected void onPause() {
        super.onPause();
        logEvent("onPause");
    }

    /** Log the onRestart() state. */
    @Override protected void onRestart() {
        super.onRestart();
        logEvent("onRestart");
    }

    /** Log the onResume() state. */
    @Override protected void onResume() {
        super.onResume();
        logEvent("onResume");
    }

    /** Log the lifecycle event. */
    @Override protected void onSaveInstanceState(final Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("savingData", true);
        logEvent("onSaveInstanceState", bundle);
    }

    /** Log the lifecycle event. */
    @Override protected void onRestoreInstanceState(final Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        logEvent("onRestoreInstanceState", bundle);
    }

    /** Log the onStart() state. */
    @Override protected void onStart() {
        super.onStart();
        logEvent("onStart");
    }

    /** Log the onStop() state. */
    @Override protected void onStop() {
        super.onStop();
        logEvent("onStop");
    }

}
