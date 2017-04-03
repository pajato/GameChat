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
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.exp.model.Player;
import com.pajato.android.gamechat.main.NetworkManager;

import static com.pajato.android.gamechat.exp.NotificationManager.NotifyType.experience;

/**
 * A View.OnClickListener that is called whenever a board tile is clicked.
 */
public class TileClickHandler implements View.OnClickListener {

    // Private instance variables.

    /** The experience model class. */
    private Experience mModel;

    private String friendName;

    private String youName;

    // Constructor

    TileClickHandler(String friend, String you) {
        friendName = friend;
        youName = you;
    }

    // Public instance methods.

    /** Handle a click event by validating the player doing the click and processing the move. */
    @Override public void onClick(final View v) {
        // If the player is not a valid player, then notify them that they need to make or join
        // their own game via a snackbar and abort.
        if (isNotAPlayer()) {
            FragmentType fragmentType = mModel.getExperienceType().getFragmentType();
            BaseFragment fragment = DispatchManager.instance.getFragment(fragmentType);
            int id = R.string.NotAPlayerMessageText;
            NotificationManager.instance.notifyNoAction(fragment, id, experience);
            return;
        }

        // Detect a player playing out of turn.  If so, notify politely with a snackbar and abort.
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

    /** Return true if the current user is a player in the current experience. Otherwise, false. */
    private boolean isNotAPlayer() {
        for (Player player :  mModel.getPlayers()) {
            if(player.id != null && player.id.equals(AccountManager.instance.getCurrentAccountId()))
                return false;
            // Handle offline case
            if (player.id == null &&
                    mModel.getExperienceKey().equals(NetworkManager.OFFLINE_EXPERIENCE_KEY))
                return false;
        }
        return true;
    }

    /** Return TRUE iff the piece selected is from the other team and not being captured. */
    private boolean isPlayingOutOfTurn(final int position) {
        // Ensure that the piece played is correct according to the turn or is being captured.
        // If the tile clicked has no piece, return false. Otherwise ensure it's not being played
        // out of turn.
        Board board = mModel.getBoard();
        Team team = board.getTeam(position);
        if (team == Team.NONE)
            return false;

        // Ensure that the current user is choosing a piece that belongs to their team. If they are
        // playing with a "Friend", then we don't need to worry about confirming players' identities
        Team playerTeam = team;
        for (Player player :  mModel.getPlayers()) {
            if(player.id == null && player.name.equals(friendName)) {
                playerTeam = team;
                break;
            } else if (player.id == null && player.name.equals(youName)) {
                playerTeam = team;
                break;
            } else if(player.id.equals(AccountManager.instance.getCurrentAccountId())) {
                playerTeam = Team.valueOf(player.team.toUpperCase());
            }
        }

        boolean turn = mModel.getTurn();
        boolean isOwnPlayer = (team == Team.PRIMARY && playerTeam == Team.PRIMARY && turn)
                || (team == Team.SECONDARY && playerTeam == Team.SECONDARY && !turn);
        boolean isCapture = !isOwnPlayer && board.isHighlighted(position);
        return !isOwnPlayer && !isCapture;
    }
}
