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

package com.pajato.android.gamechat.exp;

import com.pajato.android.gamechat.exp.model.Player;

import java.util.List;
import java.util.Map;

/**
 * Each experience is expected to provide a name, the associated experience type, group push key,
 * room push key and experience push key as well as a map of the experience properties. And
 * experience must also be able to set the modification timestamp and the win count.
 *
 * @author Paul Michael Reilly
 */
public interface Experience {

    /** Return the board, if any, associated with the experience. */
    Board getBoard();

    /** Return the experience create time. */
    long getCreateTime();

    /** Return the experience push key. */
    String getExperienceKey();

    /** Return the experience type. */
    ExpType getExperienceType();

    /** Return the experience group push key. */
    String getGroupKey();

    /** Return the experience modification time. */
    long getModTime();

    /** Return the experience name. */
    String getName();

    /** Return the list of players for this experience. */
    List<Player> getPlayers();

    /** Return the experience room push key. */
    String getRoomKey();

    /** Return the experience state. */
    State getStateType();

    /** Return the current player turn. */
    boolean getTurn();

    /** Return the unseen list. */
    List<String> getUnseenList();

    /** Reset an experience. */
    void reset();

    /** Set the experience push key. */
    void setExperienceKey(String key);

    /** Set the experience group key. */
    void setGroupKey(String gKey);

    /** Set the room key. */
    void setRoomKey(String key);

    /** Set the modification timestamp. */
    void setModTime(long value);

    /** Set the experience state. */
    void setStateType(State value);

    /** Set the win count. */
    void setWinCount();

    /** Get the winning player. */
    Player getWinningPlayer();

    /** Provide a map of experience properties. */
    Map<String, Object> toMap();

    /** Toggle the turn for the players. */
    boolean toggleTurn();
}
