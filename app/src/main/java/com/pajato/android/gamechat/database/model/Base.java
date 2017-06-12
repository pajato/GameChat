/*
 * Copyright (C) 2017 Pajato Technologies, Inc.
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

package com.pajato.android.gamechat.database.model;

import java.util.HashMap;
import java.util.Map;

/** Provide a Firebase model class representing a chat group, a collection of members and rooms. */
public abstract class Base {

    // Private class constants.

    /** The default database API version for this client. */
    private static final int API_VERSION = 1;

    /** The minimum client API version compatible with the database API version. */
    private static final int API_MIN_CLIENT_VERSION = 1;

    // Public instance variables.

    /** The database API version. */
    public final int apiVersion = API_VERSION;

    /** The minimum client API version. */
    public final int apiMinClientVersion = API_MIN_CLIENT_VERSION;

    /** The creation timestamp. */
    public /*final*/ long createTime;

    /** The group push key value. */
    public /*final*/ String key;

    /** The group name. */
    public /*final*/ String name;

    /** The last modification timestamp. */
    public long modTime;

    /** The group owner/creator. */
    public /*final*/ String owner;

    /** Build an empty args constructor for the database. */
    public Base() {}

    /** Build a default Group. */
    public Base(final String key, final String owner, final String name, final long createTime) {
        this.createTime = createTime;
        this.key = key;
        this.name = name;
        this.owner = owner;
        modTime = 0;
    }

    /** Provide a default map for a Firebase create/update. */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("apiVersion", apiVersion);
        result.put("apiMinClientVersion", apiMinClientVersion);
        result.put("createTime", createTime);
        result.put("key", key);
        result.put("name", name);
        result.put("modTime", modTime);
        result.put("owner", owner);
        return result;
    }
}
