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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.fragment.BaseFragment;

public class InitialFragment extends BaseFragment {
    private View mLayout;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        mLayout = inflater.inflate(R.layout.fragment_initial, container, false);

        // Handle Tic-Tac-Toe games.
        View ttt = mLayout.findViewById(R.id.init_ttt);
        ttt.setOnClickListener(new ClickHandler());
        View tttButton = mLayout.findViewById(R.id.init_ttt_button);
        tttButton.setOnClickListener(new ClickHandler());

        // Handle Checkers Games.
        View checkers = mLayout.findViewById(R.id.init_checkers);
        checkers.setOnClickListener(new ClickHandler());
        View checkersButton = mLayout.findViewById(R.id.init_checkers_button);
        checkersButton.setOnClickListener(new ClickHandler());

        return mLayout;
    }

    private class ClickHandler implements View.OnClickListener {
        @Override public void onClick(View v) {
            if(v.getId() == R.id.init_ttt || v.getId() == R.id.init_ttt_button) {
                GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                        getString(R.string.new_game_ttt));
            } else if (v.getId() == R.id.init_checkers || v.getId() == R.id.init_checkers_button) {
                GameManager.instance.sendNewGame(GameManager.SETTINGS_INDEX, getActivity(),
                        getString(R.string.new_game_checkers));
            }
        }
    }
}