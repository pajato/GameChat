/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.game;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.BackPressEvent;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.Subscribe;

import static com.pajato.android.gamechat.game.GameManager.ORDINAL_KEY;

public class SettingsFragment extends BaseGameFragment {

    // Public instance methods.

    /** Handle a back press event by canceling out of the settings fragment. */
    @Subscribe(priority=1)
    public void onBackPressed(final BackPressEvent event) {
        // Re-enable the FAB and return to the start fragment after disabling further event
        // subscribers.
        AppEventManager.instance.cancel(event);
        FabManager.game.setState(this, View.VISIBLE);
        GameManager.instance.sendNewGame(GameManager.NO_GAMES_INDEX, getActivity());
    }

    /** Handle button clicks to pass control to set up to play a particular mode. */
    @Subscribe public void onClick(final ClickEvent event) {
        int index = getFragmentIndex(event.view.getId());
        if (index != -1) {
            // Start a new game by passing the fragment index and context.
            GameManager.instance.sendNewGame(index, getActivity());
            return;
        }

        // This mode is a future feature, let the User know with an opportunity to volunteer.
        int resId = mGame != null ? mGame.futureResId : -1;
        if (resId != -1) showFutureFeatureMessage(resId);
    }

    /** Satisfy the base game fragment contract with a nop message handler. */
    @Override public void messageHandler(final String message) {}

    /** Provide the construction time arguments to be delivered by the fragment manager. */
    @Override public void setArguments(final Bundle args) {
        // Grab the game argument that we are accepting and creating settings for.
        if (args != null && args.containsKey(ORDINAL_KEY)) {
            // Get the game emum value.
            int ordinal = args.getInt(ORDINAL_KEY, -1);
            mGame = ordinal != -1 ? Game.values()[ordinal] : null;
        }
        super.setArguments(args);
    }

    /** Set the layout file. */
    @Override public int getLayout() {return R.layout.fragment_settings;}

    @Override public void onResume() {
        // Hide the FAB and set the title string and the icon source.
        super.onResume();
        getActivity().findViewById(R.id.gameFab).setVisibility(View.GONE);
        TextView title = (TextView) mLayout.findViewById(R.id.settings_title);
        title.setText(mGame != null ? mGame.titleResId : R.string.GameError);
        ImageButton icon = (ImageButton) mLayout.findViewById(R.id.settings_icon);
        int imageResId = mGame != null ? mGame.iconResId : R.drawable.ic_launcher;
        icon.setImageResource(imageResId);
    }

    // Private instance methods.

    /** Return the fragument index for the selected game mode. */
    private int getFragmentIndex(final int viewId) {
        switch (viewId) {
            case R.id.settings_local_button: return mGame.localFragmentIndex;
            case R.id.settings_online_button: return mGame.onlineFragmentIndex;
            case R.id.settings_computer_button: return mGame.computerFragmentIndex;
            default: return -1;
        }
    }

}
