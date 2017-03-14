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

import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.BaseFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FragmentType;

import static com.pajato.android.gamechat.exp.NotificationManager.NotifyType.experience;

/**
 * A View.OnClickListener that is called whenever a board tile is clicked.
 */
public class TileClickHandler implements View.OnClickListener {

    // Private instance variables.

    /** The experience model class. */
    private Experience mModel;

    // Public instance methods.

    /** Handle a click event by validating the player doing the click and processing the move. */
    @Override public void onClick(final View v) {
        // Detect a player playing out of turn.  If so, notify politely with a snackbar and
        // abort.
        int position = Integer.parseInt((String)v.getTag());
        if (isPlayingOutOfTurn(position)) {
            FragmentType fragmentType = mModel.getExperienceType().getFragmentType();
            BaseFragment fragment = DispatchManager.instance.getFragment(fragmentType);
            int id = R.string.PlayOutOfTurnMessageText;
            NotificationManager.instance.notifyNoAction(fragment, id, experience);
            return;
        }

        // Let the game engine process the click.
        Engine engine = mModel.getExperienceType().getEngine();
        if (engine == null)
            return;
        ExpHelper.processTileClick(position, mModel, engine);
    }

    /** Establish the model for this handler. */
    public void setModel(final Experience model) {
        mModel = model;
    }

    // Private instance methods.

    /** Return TRUE iff the piece selected is from the other team and not being captured. */
    private boolean isPlayingOutOfTurn(final int position) {
        // Ensure that the piece played is correct according to the turn or is being captured.
        // If the tile clicked has no piece, return false. Otherwise ensure it's not being played
        // out of turn.
        Board board = mModel.getBoard();
        Team team = board.getTeam(position);
        if (team == Team.NONE)
            return false;
        boolean turn = mModel.getTurn();
        boolean isOwnPlayer = (team == Team.PRIMARY && turn) || (team == Team.SECONDARY && !turn);
        boolean isCapture = !isOwnPlayer && board.isHighlighted(position);
        return !isOwnPlayer && !isCapture;
    }
}
