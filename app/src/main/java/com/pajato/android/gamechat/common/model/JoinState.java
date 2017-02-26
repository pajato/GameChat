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

package com.pajato.android.gamechat.common.model;

import com.google.firebase.database.Exclude;

import static com.pajato.android.gamechat.common.model.JoinState.JoinType.active;
import static com.pajato.android.gamechat.common.model.JoinState.JoinType.chat;
import static com.pajato.android.gamechat.common.model.JoinState.JoinType.exp;
import static com.pajato.android.gamechat.common.model.JoinState.JoinType.inactive;

/**
 * Defines a Firebase suitable POJO for maintaining the join state for a given User.
 *
 * @author Paul Michael Reilly on 2/26/17
 */
public class JoinState {

    /** Provide a set of values to define the User join state for a given room or group. */
    public enum JoinType {
        active,                     // The User is "joined" to both the group and room.
        chat,                       // The User is "joined" only to the group and room chat.
        exp,                        // The User is "joined" only to the group and room experience.
        inactive                   // The User is not joined to either the group or room.
    }

    // Private instance variables.

    /** The current join type. */
    private JoinType mType = inactive;

    // Public instance methods.

    /** Provide a getter for Firebase. */
    @SuppressWarnings("unused")
    public String getJoinState() {
        return mType.name();
    }

    /** Return the current state a a JoinType. */
    @Exclude public JoinType getType() {
        return mType != null ? mType : inactive;
    }

    /** Provide a setter for Firebase. */
    @SuppressWarnings("unused")
    public void setJoinState(final String value) {
        mType = getType(value);
    }

    /** Set the join type to the given value. */
    @Exclude public void setType(final JoinType value) {
        mType = value;
    }

    // Private instance methods.

    /** Return 'inactive' or the state type corresponding to the given value. */
    private JoinType getType(String value) {
        if (value == null || value.isEmpty())
            return inactive;

        switch (value) {
            case "active":
                return active;
            case "chat":
                return chat;
            case "exp":
                return exp;
            default:
                return inactive;
        }
    }
}
