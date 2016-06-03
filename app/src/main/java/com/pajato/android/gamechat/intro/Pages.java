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

package com.pajato.android.gamechat.intro;

import com.pajato.android.gamechat.R;

/**
 * Provide an enumeration of panels used in the app.
 */
public enum Pages {
    home(R.drawable.ic_intro_home, R.string.intro_home_title, R.string.intro_home_message),
    chat(R.drawable.ic_intro_chat, R.string.intro_chat_title, R.string.intro_chat_message),
    game(R.drawable.ic_intro_game, R.string.intro_game_title, R.string.intro_game_message),
    levels(R.drawable.ic_intro_levels, R.string.intro_levels_title, R.string.intro_levels_message);

    /** The page icon resource id. */
    public int iconId;

    /** The page title resource id. */
    public int titleId;

    /** The page message id. */
    public int messageId;

    /**
     * Create the enum value instance given a title resource id, layout resource id and fragment
     * class..
     *
     * @param iconId The given title id.
     * @param layoutId The given layout id.
     * @param fragmentClass The given layout class.
     */
    Pages(final int iconId, final int titleId, final int messageId) {
        this.iconId = iconId;
        this.titleId = titleId;
        this.messageId = messageId;
    }
}
