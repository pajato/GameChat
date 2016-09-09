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

package com.pajato.android.gamechat.game;

import android.view.View;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseFragment;

public class InitialFragment extends BaseFragment {

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_initial;}

    @Override public void onInitialize() {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        // Setup the No Rooms Message.
        TextView message = (TextView) mLayout.findViewById(R.id.game_message);
        message.setText(R.string.NoRoomsMessageText);
        // Return the fab back to visible.
        getActivity().findViewById(R.id.games_fab).setVisibility(View.VISIBLE);
    }

}
