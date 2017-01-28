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

package com.pajato.android.gamechat.common;

import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.exp.Experience;

import java.util.List;

/**
 * The fragment dispatcher provides mediation between the experience or chat managers and the main
 * (envelope, experience or chat) fragment. It captures all information a delegated fragment will
 * need to instantiate and take the foreground.
 *
 * @author Paul Michael Reilly
 */
public class Dispatcher {

    // Public instance variables.

    /** The experience or message key. */
    public String key;

    /** The experience payload. */
    public Experience experiencePayload;

    /** The group key. */
    public String groupKey;

    /** The room key. */
    public String roomKey;

    /** The fragment type denoting the fragment index and the experience type. */
    public FragmentType type;

    // Public Constructors.

    /** Build an instance given a type. */
    Dispatcher(final FragmentType type) {
        // Capture the type and handle any of the experience types.
        this.type = type;
        if (type != null) processType();
    }

    /** Build an instance given a chat list item. */
    Dispatcher(final FragmentType type, final ListItem item) {
        this.type = type;
        if (item != null) {
            groupKey = item.groupKey;
            roomKey = item.key;
        }
    }

    // Private instance methods.

    /** Handle one of the main experience types. */
    private void processExperienceType() {
        // There are three cases to be handled: 1) there are no experiences of the given type;
        // 2) there is exactly one experience of the given type; or 3) there are multiple
        // experiences of the given type.
        List<Experience> experienceList = ExperienceManager.instance.getExperienceList(type);
        switch (experienceList.size()) {
            case 0: // There is no experiences of this type.  One will be created shortly.
                break;
            case 1: // There is exactly one experience of this type.  Use it.
                experiencePayload = experienceList.get(0);
                groupKey = experiencePayload.getGroupKey();
                roomKey = experiencePayload.getRoomKey();
                key = experiencePayload.getExperienceKey();
                break;
            default: // There are multiple experiences of this type.  Present a list of
                // them by changing the type to the corresponding list type.
                type = FragmentType.experienceList;
                break;
        }
    }

    /** Handle the non-null type to refine the choices. */
    private void processType() {
        switch (type) {
            case checkers:
            case chess:
            case tictactoe:  // Process one of the game types.
                processExperienceType();
                break;
            default: break;
        }
    }
}
