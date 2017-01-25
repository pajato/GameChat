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

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.exp;
import static com.pajato.android.gamechat.event.BaseChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.BaseChangeEvent.NEW;
import static com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment.GAME_HOME_FAM_KEY;

public class ShowNoExperiencesFragment extends BaseExperienceFragment {

    // Public instance methods.

    /** Handle an experience list change event. */
    @Subscribe public void onExperienceListChangeEvent(ExperienceChangeEvent event) {
        switch (event.changeType) {
            case CHANGED:
            case NEW:
                DispatchManager.instance.startNextFragment(getActivity(), exp);
                break;
            default:
                break;
        }
    }

    /** Initialize the fragment by setting up the FAB and toolbar. */
    @Override public void onStart() {
        super.onStart();
        FabManager.game.init(this);
        ToolbarManager.instance.init(this);
    }

    /** Deal with the fragment's activity's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the group name only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu; initialize the ad view; and set up
        // the group list display.
        super.onResume();
        FabManager.game.setImage(R.drawable.ic_add_white_24dp);
        FabManager.game.init(this, GAME_HOME_FAM_KEY);
        ToolbarManager.instance.setTitle(this, R.string.NoGamesTitleText);
    }
}
