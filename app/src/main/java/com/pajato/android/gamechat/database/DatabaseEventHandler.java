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

package com.pajato.android.gamechat.database;

/**
 * Provide a base class to encapsulate a name and Firebase path.  Subclasses will implement a
 * Firebase value or child event listener.
 *
 * @author Paul Michael Reilly
 */
public abstract class  DatabaseEventHandler {

    // Public instance variables.

    /** The name used to identify this listener. */
    public String name;

    /** The relevant Firebase path. */
    public String path;

    public DatabaseEventHandler(final String name, final String path) {
        this.name = name;
        this.path = path;
    }
}
