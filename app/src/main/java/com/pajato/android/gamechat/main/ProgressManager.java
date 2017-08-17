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

import android.support.annotation.NonNull;
import android.util.Log;

import com.pajato.android.gamechat.chat.fragment.ChatEnvelopeFragment;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ProgressSpinnerEvent;
import com.pajato.android.gamechat.event.RegistrationChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static java.lang.String.format;

/** Provide a singleton to manage showing and hiding the initial loading status. */
public enum ProgressManager {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ProgressManager.class.getSimpleName();

    // Private instance variables.

    /** The current progress state: true => a long running task is active; false => inactive. */
    private boolean mProgressState = false;

    /** The chat envelope fragment is paying attention to progress spinner events. */
    private boolean mIsRegistered = false;

    // Public instance methods

    /** Process an event bus registration. */
    @Subscribe public void onRegistrationChangeEvent(@NonNull RegistrationChangeEvent event) {
        // Track the state of the ChatEnvelopeRegistration class event bus registration.  When the
        // class is registered update it with the current progress spinner state.
        if (event.name != null && event.name.equals(ChatEnvelopeFragment.class.getName()))
            mIsRegistered = event.changeType == RegistrationChangeEvent.REGISTERED;
        if (mIsRegistered)
            post(mProgressState);
        String status = mIsRegistered ? "is" : "is not";
        Log.d(TAG, format(Locale.US, "The chat envelope %s registered.", status));
    }

    /** Dismiss the initial loading dialog if one is showing. */
    public void hide() {
        post(false);
    }

    /** Initialize the progress manager. */
    public void init() {
        // Initialize by ensuring that the progress manager is registered with the app event bus
        // and that the current chat enveelope registration state has been captured.
        AppEventManager.instance.register(this);
        String name = ChatEnvelopeFragment.class.getName();
        mIsRegistered = AppEventManager.instance.isRegistered(name);
    }

    /** Show the initial loading dialog. */
    public void show() {
        post(true);
    }

    // Private instance methods.

    /** Post the current progress spinner state to the chat envelope fragment. */
    private void post(final boolean state) {
        // Persist the given state and determine if the app event bus can be used to handle the
        // request to provide the User with feedback on a time consuming activity using the chat
        // envelope.
        mProgressState = state;
        if (mIsRegistered)
            AppEventManager.instance.post(new ProgressSpinnerEvent(state));
        String status = mIsRegistered ? "has" : "has not";
        String format = "The progress spinner state {%s} %s been posted.";
        Log.d(TAG, format(Locale.US, format, state, status));
    }
}
