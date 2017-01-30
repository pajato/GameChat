package com.pajato.android.gamechat.common.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A POJO and Firebase model to hold group/room selections while an invitation is processed.
 */

@IgnoreExtraProperties
public class GroupInviteData {

    /** The group key */
    public String groupKey;

    /** The group name */
    public String groupName;

    /** The group common room key */
    public String commonRoomKey;

    /** A list of selected room keys */
    public List<String> rooms;

    // Booleans for invitation processing, used to determine when all processing is complete

    /** Indicates that the group has been added to the account in the database */
    public boolean addedToAccountJoinList;

    /** Indicates that the account has been added to the group member list in the database */
    public boolean addedToGroupMemberList;

    /** Indicates that the account has been added to the common room member list in the database */
    public boolean addedToCommRoomMemberList;

    /** Number of rooms for which processing has been completed; doesn't include the common room */
    public int roomsCompleted;

    /** Empty args constructor for the database */
    public GroupInviteData() {}

    /** Constructor */
    public GroupInviteData(String groupKey, String groupName) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        addedToAccountJoinList = false;
        addedToGroupMemberList = false;
        addedToCommRoomMemberList = false;
        rooms = new ArrayList<>();
        this.roomsCompleted = 0;
    }

    /** Constructor */
    public GroupInviteData(String groupKey, String groupName, String commonRoomKey, List<String> rooms) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.commonRoomKey = commonRoomKey;
        addedToAccountJoinList = false;
        addedToGroupMemberList = false;
        addedToCommRoomMemberList = false;
        this.rooms = rooms;
        this.roomsCompleted = 0;
    }

    /** Constructor */
    public GroupInviteData(String groupKey, String groupName, String commonRoomKey) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.commonRoomKey = commonRoomKey;
        addedToAccountJoinList = false;
        addedToGroupMemberList = false;
        addedToCommRoomMemberList = false;
        this.rooms = new ArrayList<>();
        this.roomsCompleted = 0;
    }

    /** Provide a default map for Firebase create/update */
    @Exclude public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("groupKey", groupKey);
        result.put("groupName", groupName);
        result.put("commonRoomKey", commonRoomKey);
        result.put("rooms", rooms);
        result.put("addedToAccountJoinList", addedToAccountJoinList);
        result.put("addedToGroupMemberList", addedToGroupMemberList);
        result.put("addedToCommRoomMemberList", addedToCommRoomMemberList);
        result.put("roomsCompleted", roomsCompleted);
        return result;
    }

    /** Determine if all processing of an invitation is complete */
    @Exclude public boolean isDone() {
        return addedToAccountJoinList && addedToGroupMemberList && addedToCommRoomMemberList &&
                roomsCompleted == rooms.size();
    }
}
