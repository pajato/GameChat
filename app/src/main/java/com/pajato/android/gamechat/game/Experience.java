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

import java.util.Map;

/**
 * Each experience is expected to provide the associated fragment type, group push key, room push
 * key and experience push key as well as a map of the experience properties.
 *
 * @author Paul Michael Reilly
 */
public interface Experience {

    String getGroupKey();

    String getRoomKey();

    String getExperienceKey();

    String getName();

    ExpType getExperienceType();

    void setExperienceKey(String key);

    Map<String, Object> toMap();

}
