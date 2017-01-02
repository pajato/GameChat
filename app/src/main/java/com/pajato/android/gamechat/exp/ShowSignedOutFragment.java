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

import android.os.Bundle;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.exp.GameFragment.GAME_HOME_FAM_KEY;

public class ShowSignedOutFragment extends BaseGameFragment {

    @Subscribe public void onClick(final ClickEvent event) {
        // todo add some code here.
        logEvent("onClick (showSignedOut)");
    }

    /** Satisfy the base game fragment contract with a nop message handler. */
    @Override public void messageHandler(final String message) {}

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setLayoutId(R.layout.fragment_exp_signed_out);
    }

    /** Initialize the fragment by setting in the FAB. */
    @Override public void onStart() {
        super.onStart();
        // Set up the FAB.
        FabManager.game.init(this);
    }

    /** Reset the FAM to use the game home menu. */
    @Override public void onResume() {
        super.onResume();
        FabManager.game.setMenu(this, GAME_HOME_FAM_KEY);
    }
}
