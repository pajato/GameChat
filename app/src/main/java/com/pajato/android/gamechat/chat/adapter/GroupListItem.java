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

package com.pajato.android.gamechat.chat.adapter;

/**
 * Provide a POJO to encapsulate a recycler view list item: either a date label view or a room list
 * view showing the rooms in a group with messages characterized by a preceding date label view.
 *
 * @author Paul Michael Reilly
 */
public class GroupListItem {

    // Public instance variables.

    /** The item type. */
    public int type;

    /** The item name. */
    public String name;

    /** The number of new messages in the group rooms. */
    public int count;

    /** The list of rooms with unread messages. */
    public String rooms;

    // Public constructors.

    /** Build an instance for the given group. */
    public GroupListItem(final String groupKey) {
        // TODO: flesh this out to populate the class using the Firebase data from the given group.
        switch(Integer.parseInt(groupKey)) {
            case 0:
                name = "JCL Group";
                count = 17;
                rooms = "group, Sandy Scott, The Shovel, ChessWhiz, ...";
                break;
            case 1:
                name = "ExaGrid";
                count = 8;
                rooms = "group, Harald Scardell, Lee Atwood, Matt Dunkirk, IT";
                break;
            case 2:
                name = "The Jim Clemens Family";
                count = 0;
                rooms = "group, Sean Clemens, Sam Davis, Dad, ...";
                break;
            case 3:
                name = "CA";
                count = 1;
                rooms = "group, Walt Corey, The Idiot";
                break;
            case 4:
                name = "The Warren Campaign";
                count = 0;
                rooms = "group";
                break;
            case 5:
                name = "PT LLC";
                count = 0;
                rooms = "group, Bryan Scott, Paul Matthew Reilly";
                break;
            case 6:
                name = "Edwin B. Newman School";
                count = 0;
                rooms = "group, Tommy Rooney, Pamela Reilly";
                break;
            case 7:
                name = "Ebeneezer Street Church";
                count = 0;
                rooms = "group, Pastor Frank, The Choir";
                break;
        }

    }

    // Public instance methods.

}
