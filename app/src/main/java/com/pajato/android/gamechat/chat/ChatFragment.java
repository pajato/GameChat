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

package com.pajato.android.gamechat.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.BaseFragment;

/**
 * Provide a fragment class that decides which alternative chat fragment to show to the User.
 * Indecision will result in a default "flummoxed" message being displayed.
 *
 * @author Paul Michael Reilly (based on GameFragment written by Bryan Scott)
 */
public class ChatFragment extends BaseFragment {

    // Public instance methods.

    /** Create the view to do essentially nothing. Things will happen in the onStart() method. */
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Inflate the layout, and initialize the game manager.
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    /** Kick off fragment processing by having the chat manager decide what to do. */
    @Override public void onStart() {
        super.onStart();
        // Turn it over to the chat manager to decide how to proceed.
        ChatManager.instance.init(this.getActivity());
    }
}
