package com.pajato.android.gamechat.event;

import java.util.List;

/**
 * Provides a event class indicating that the current user has been joined to one or more groups.
 */
public class GroupJoinedEvent {

    /**
     * The name of the group which has been joined. If the group is null, more than one group
     * has been joined.
     */
    public String groupName;

    /**
     * Build the event and indicate the specified group. If the group is null, more than one
     * group has been joined.
     */
    public GroupJoinedEvent(String grp) {
        groupName = grp;
    }
}
