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

package com.pajato.android.gamechat.exp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.exp;

public class ExpShowSignedOutFragment extends BaseExperienceFragment {

    @Subscribe public void onClick(final ClickEvent event) {
        // todo add some code here.
        logEvent("onClick (showSignedOut)");
    }

    /** Establish the layout file to show that the user is signed out and cannot chat. */
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setLayoutId(R.layout.fragment_exp_signed_out);
    }

    /** Handle a group profile change by trying again to start a better fragment. */
    @Subscribe public void onExperienceChange(@NonNull final ExperienceChangeEvent event) {
        // An experience event has occurred.  Ensure that we are in the right fragment.
        DispatchManager.instance.startNextFragment(this.getActivity(), exp);
    }

    /** Initialize the fragment by setting up the FAB/FAM. */
    @Override public void onStart() {
        // Set up the FAB.
        super.onStart();
        FabManager.game.init(this);
    }
}
