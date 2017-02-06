package com.pajato.android.gamechat.event;

import java.util.List;

/**
 * Provides an event class indicating that the current user has been joined to one or more groups.
 */
public class GroupJoinedEvent {

    /** Null if more then one group joined; otherwise the name of the joined group */
    public String groupName;

    /** Optional list of room names within the group */
    public List<String> rooms;

    /** Build the event with a group name and string containing list of rooms */
    public GroupJoinedEvent(String groupName, List<String> rooms) {
        this.groupName = groupName;
        this.rooms = rooms;
    }
}
