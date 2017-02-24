/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
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

package com.pajato.android.gamechat.exp;

import android.content.Context;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.exp.model.Player;

import java.util.Locale;

/**
 * The team constants, providing the default text color.
 *
 * @author Paul Michael Reilly on 2/17/2017
 */
public enum State {
    active (-1),
    check (R.string.CheckText),
    checkMate (R.string.CheckMateText),
    primary_wins (R.string.WinMessageFormat),
    secondary_wins (R.string.WinMessageFormat),
    tie (R.string.TieMessage),
    pending (-1);

    public int resId;

    State(int resId) {
        this.resId = resId;
    }

    /** Return an empty string or a string indicating the experience state. */
    public String getMessage(final Context context, final Player winningPlayer) {
        switch(this) {
            case primary_wins:
            case secondary_wins:
                String format = context.getString(resId);
                String name = winningPlayer.name;
                return String.format(Locale.getDefault(), format, name);
            case active:
            case pending:
                return "";
            default:
                return context.getString(resId);
        }
    }

    /** Return TRUE iff one of the players wins. */
    public boolean isWin() {
        return this == primary_wins || this == secondary_wins;
    }

    /** Return TRUE iff there is a tie. */
    public boolean isTie() {
        return this == tie;
    }
}
