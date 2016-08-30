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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.FabManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.EventBusManager;
import com.pajato.android.gamechat.fragment.BaseFragment;

import org.greenrobot.eventbus.Subscribe;

public class InitialFragment extends BaseFragment {

    private FloatingActionButton mFab;

    /** Process a given button click event looking for one on the rooms fab button. */
    @Subscribe public void buttonClickHandler(final ClickEvent event) {
        int v = event.getView() != null ? event.getView().getId() : 0;

        if(v == R.id.init_ttt || v == R.id.init_ttt_button) {
            GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                    getString(R.string.new_game_ttt));
        } else if (v == R.id.init_checkers || v == R.id.init_checkers_button) {
            GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                    getString(R.string.new_game_checkers));
        } else if (v == R.id.init_chess || v == R.id.init_chess_button) {
            GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                    getString(R.string.new_game_chess));
        } else if (v == R.id.games_fab) {
            FabManager.game.toggle(mFab, getView());
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View layout = inflater.inflate(R.layout.fragment_initial, container, false);

        EventBusManager.instance.register(this);
        FabManager.game.init(layout);
        mFab = (FloatingActionButton) layout.findViewById(R.id.games_fab);
        return layout;
    }

}
