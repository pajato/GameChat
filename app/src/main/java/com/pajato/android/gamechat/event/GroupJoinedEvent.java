package com.pajato.android.gamechat.event;

import java.util.List;

/**
 * Provides an event class indicating that the current user has been joined to one or more groups.
 */
public class GroupJoinedEvent {

    /** Null if more then one group joined; otherwise the name of the joined group */
    public String groupName;

    /** Build the event and indicate the specified group (or null) */
    public GroupJoinedEvent(String grp) { groupName = grp; }
}
