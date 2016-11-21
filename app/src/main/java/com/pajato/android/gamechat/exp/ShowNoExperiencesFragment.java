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

package com.pajato.android.gamechat.exp;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.event.ExpProfileListChangeEvent;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.event.BaseChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.BaseChangeEvent.NEW;
import static com.pajato.android.gamechat.exp.GameFragment.GAME_HOME_FAM_KEY;

public class ShowNoExperiencesFragment extends BaseGameFragment {

    // Public instance methods.

    /** Establish the layout file to indicate that no experiences are available. */
    @Override public int getLayout() {return R.layout.fragment_game_no_games;}

    /** Satisfy the base game fragment contract with a nop message handler. */
    @Override public void messageHandler(final String message) {}

    /** Handle an experience profile list change event. */
    @Subscribe public void onExpProfileListChangeEvent(ExpProfileListChangeEvent event) {
        switch (event.changeType) {
            case CHANGED:
            case NEW:
                GameManager.instance.startNextFragment(getActivity());
                break;
            default:
                break;
        }
    }

    /** Reset the FAM to use the game home menu. */
    @Override public void onResume() {
        super.onResume();
        FabManager.game.setMenu(this, GAME_HOME_FAM_KEY);
    }
}