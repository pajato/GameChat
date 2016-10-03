/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.game.event;

import com.pajato.android.gamechat.game.model.ExpProfile;

/**
 * Provides a class to model an experience profile change.
 *
 * @author Paul Michael Reilly
 */
public class ExpProfileChangeEvent {

    // Public instance variables.

    /** The experience profile changed. */
    public ExpProfile expProfile;

    /** The chagen type, one of new, changed, removed or moved. */
    public int type;

    // Public constructor.

    /** Build an instance with a given room, experience profile and change type. */
    public ExpProfileChangeEvent(final ExpProfile expProfile, final int type) {
        this.expProfile = expProfile;
        this.type = type;
    }
}
