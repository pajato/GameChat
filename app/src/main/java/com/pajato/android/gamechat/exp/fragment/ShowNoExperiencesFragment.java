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

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.common.DispatchManager;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.exp;
import static com.pajato.android.gamechat.event.BaseChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.BaseChangeEvent.NEW;

public class ShowNoExperiencesFragment extends BaseExperienceFragment {

    // Public instance methods.

    /** Establish the layout file to indicate that no experiences are available. */
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setLayoutId(R.layout.fragment_game_no_games);
    }

    /** Handle an experience profile list change event. */
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
}