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

import android.support.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.ListItem.DateHeaderType;
import com.pajato.android.gamechat.common.adapter.ListItem.ItemType;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.ExperiencesChangeHandler;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.ExpListChangeEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.common.adapter.ListItem.DateHeaderType.old;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.date;

/**
 * Provide a class to manage the experience database objects.
 *
 * @author Paul Michael Reilly
 */
public enum ExperienceManager {
    instance;

    // Public class constants.

    // Database paths, often used as format strings.
    public static final String EXPERIENCES_PATH = RoomManager.ROOMS_PATH + "%s/experiences/";
    public static final String EXPERIENCE_PATH = EXPERIENCES_PATH + "%s/";

    // Private class constants.

    /** The experience profile change handler base name. */
    private static final String EXPERIENCE_LIST_CHANGE_HANDLER = "experienceListChangeHandler";

    // Public instance variables.

    /** The map associating group and room push keys with a map of experiences. */
    public Map<String, Map<String, Map<String, Experience>>> expGroupMap = new HashMap<>();

    /** The experience map associating an experience with it's self reference key. */
    public Map<String, Experience> experienceMap = new HashMap<>();

    // Private instance variables.

    /** A map associating date header type values with lists of group push keys. */
    private Map<DateHeaderType, List<String>> mDateHeaderGroupMap = new HashMap<>();

    /** A map associating date header type values with lists of room push keys, by group. */
    private Map<String, Map<DateHeaderType, List<String>>> mDateHeaderRoomMap = new HashMap<>();

    /** A map associating a group push key with it's most recent changed experience. */
    private Map<String, Experience> mGroupToRecentMap = new HashMap<>();

    /** A map of maps associating recent experiences with rooms in a group. */
    private Map<String, Map<String, Experience>> mRoomToRecentMap = new HashMap<>();

    // Public instance methods.

    /** Persist the given experience to the database. */
    public void createExperience(final Experience experience) {
        // Ensure that the requisite keys, name and type all exist.  Abort if any do not exist.
        String groupKey = experience.getGroupKey();
        String roomKey = experience.getRoomKey();
        String name = experience.getName();
        ExpType expType = experience.getExperienceType();
        if (groupKey == null || roomKey == null || name == null || expType == null) return;

        // Persist the experience obtaining a push key if necessary.
        if (experience.getExperienceKey() == null)
            experience.setExperienceKey(getExperienceKey());
        String key = experience.getExperienceKey();
        String path = String.format(Locale.US, EXPERIENCE_PATH, groupKey, roomKey, key);
        DBUtils.updateChildren(path, experience.toMap());
    }

    /** Return the number of experiences for the given type. */
    public List<Experience> getExperienceList(FragmentType type) {
        // First approximation is to generate the count brute force.
        List<Experience> result = new ArrayList<>();
        for (Experience exp : experienceMap.values()) {
            if (exp.getExperienceType() == type.expType) result.add(exp);
        }
        return result;
    }

    /** Return a room push key to use with a subsequent room object persistence. */
    public String getExperienceKey() {
        return FirebaseDatabase.getInstance().getReference().child(EXPERIENCE_PATH).push().getKey();
    }

    /** Get the data as a set of list items for all groups. */
    public List<ListItem> getListItemData() {
        // Determine whether to handle no groups (a set of welcome list items), one group (a set of
        // group rooms) or more than one group (a set of groups).
        List<ListItem> result = new ArrayList<>();
        switch (expGroupMap.size()) {
            case 0:
            case 1:             // Get the experiences from the rooms in the joined group that have
                                // experiences and the me room if it has any experiences.
                String roomKey = expGroupMap.keySet().iterator().next();
                String meRoomKey = AccountManager.instance.getMeGroupKey();
                result.addAll(getItemListRooms(roomKey));
                if (!roomKey.equals(meRoomKey))
                    result.addAll(getItemListRooms(meRoomKey));
                return result;
            default:
                result.addAll(getItemListGroups());
                result.addAll(getItemListRooms(AccountManager.instance.getMeGroupKey()));
                return result;
        }
    }

    /** Handle a account change event by setting up or clearing variables. */
    @Subscribe public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.  If so, do nothing, otherwise clear the
        // message list for the logged out User.
        if (event.account != null) return;
        expGroupMap.clear();
        experienceMap.clear();
    }

    /** Handle an experience change event by updating the date headers ... */
    @Subscribe public void onExperienceChangeEvent(@NonNull final ExperienceChangeEvent event) {
        // Update the date headers for this message and post an event to trigger an adapter refresh.
        updateHeaderMaps(event.experience);
        AppEventManager.instance.post(new ExpListChangeEvent());
    }

    /** Persist the given experience. */
    public void updateExperience(final Experience experience) {
        // Persist the experience.
        experience.setModTime(new Date().getTime());
        String groupKey = experience.getGroupKey();
        String roomKey = experience.getRoomKey();
        String expKey = experience.getExperienceKey();
        String path = String.format(Locale.US, EXPERIENCE_PATH, groupKey, roomKey, expKey);
        FirebaseDatabase.getInstance().getReference().child(path).setValue(experience.toMap());
    }

    /** Setup a listener for experience changes in the given room. */
    public void setWatcher(final String groupKey, final String roomKey) {
        // Obtain a room and set watchers on all the experience profiles in that room.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String name = DBUtils.getHandlerName(EXPERIENCE_LIST_CHANGE_HANDLER, roomKey);
        String path = String.format(Locale.US, EXPERIENCES_PATH, groupKey, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ExperiencesChangeHandler(name, path);
        DatabaseRegistrar.instance.registerHandler(handler);
    }

    /** Move an experience from one room to another. */
    public void move(@NonNull final Experience experience, final String gKey, final String rKey) {
        //String srcGroupKey = experience.getGroupKey();
        //String srcRoomKey = experience.getRoomKey();
        experience.setGroupKey(gKey);
        experience.setRoomKey(rKey);
        experience.setExperienceKey(null);
        createExperience(experience);
    }

    // Private instance methods.

    /** Return TRUE iff this experience has a new move that has not been seen. */
    public boolean isNew(@NonNull final Experience experience) {
        // Return true iff the account holder's id is on the unseen list.
        List<String> unseenList = experience.getUnseenList();
        String accountId = AccountManager.instance.getCurrentAccountId();
        return accountId == null || unseenList == null || unseenList.contains(accountId);
    }

    // Private instance methods.

    /** Add a group list item for the given kind (chat message or game experience) and group. */
    private void addItem(@NonNull final List<ListItem> result, @NonNull final ItemType itemType,
                         @NonNull final String key) {
        Map<String, Integer> countMap = new HashMap<>();
        int count = 0;
        String text;
        switch (itemType) {
            case expGroup:
                String name = GroupManager.instance.getGroupName(key);
                count = DBUtils.getUnseenExperienceCount(key, countMap);
                text = DBUtils.getText(countMap);
                result.add(new ListItem(itemType, key, null, name, count, text));
                break;
            case expRoom:
                Room room = RoomManager.instance.getRoomProfile(key);
                result.add(new ListItem(itemType, room.groupKey, room.key, room.name, count, null));
                break;
            default:
                break;
        }
    }

    /** Return a list of experience group items. */
    private List<ListItem> getItemListGroups() {
        // Generate a list of items to render in the group list by extracting the items based
        // on the date header type ordering.
        List<ListItem> result = new ArrayList<>();
        processHeaders(result, ItemType.expGroup, mDateHeaderGroupMap);
        return result;
    }

    /** Return a list of room items for a single group and/or the me room. */
    private List<ListItem> getItemListRooms(final String groupKey) {
        // Determine if there are any items from the given group.
        List<ListItem> result = new ArrayList<>();
        Map<DateHeaderType, List<String>> map =
                groupKey != null ? mDateHeaderRoomMap.get(groupKey) : null;
        if (map == null)
            return result;
        processHeaders(result, ItemType.expRoom, map);
        return result;
    }

    /** Process all the headers for a given map to determine */
    private void processHeaders(final List<ListItem> result, ItemType itemType,
                                final Map<DateHeaderType, List<String>> map) {
        // Walk through the set of date header types to collect the list items.
        for (ListItem.DateHeaderType dht : ListItem.DateHeaderType.values()) {
            List<String> list = map.get(dht);
            if (list != null && list.size() > 0) {
                // Add the header item followed by all the items from the given map.
                result.add(new ListItem(date, dht.resId));
                for (String key : list)
                    if (!(key.equals(AccountManager.instance.getMeGroupKey())))
                        addItem(result, itemType, key);
            }
        }
    }

    /** Update the headers used to bracket the messages in the main list. */
    private void updateHeaderMaps(final Experience experience) {
        // Deal with a changed experience (like a turn, for example) by making the given experience
        // the most recent in both the group and room recent experience maps.
        String groupKey = experience.getGroupKey();
        String roomKey = experience.getRoomKey();
        mGroupToRecentMap.put(groupKey, experience);
        Map<String, Experience> roomMap = mRoomToRecentMap.get(groupKey);
        if (roomMap == null) {
            roomMap = new HashMap<>();
            roomMap.put(roomKey, experience);
        }
        mRoomToRecentMap.put(groupKey, roomMap);

        // Update the group list headers for each group with at least one experience.
        mDateHeaderGroupMap.clear();
        long nowTimestamp = new Date().getTime();
        for (String key : mGroupToRecentMap.keySet())
            updateHeaders(key, nowTimestamp);
    }

    /** Update the headers for the given group and all of it's rooms. */
    private void updateHeaders(final String groupKey, final long nowTimestamp) {
        // Determine which date header type the current group should be associated with using
        // the time stamp from the most recent experience in the group.
        long groupTimestamp = mGroupToRecentMap.get(groupKey).getModTime();
        updateMap(nowTimestamp, groupTimestamp, groupKey, mDateHeaderGroupMap);
        // for all rooms in the group, update the mDateHeaderRoomMap
        mDateHeaderRoomMap.clear();
        Map<String, Experience> map = mRoomToRecentMap.get(groupKey);
        for (String roomKey : map.keySet()) {
            long roomTimestamp = map.get(roomKey).getModTime();
            Map<DateHeaderType, List<String>> roomMap = new HashMap<>();
            updateMap(nowTimestamp, roomTimestamp, roomKey, roomMap);
            if (roomMap.size() == 0)
                continue;
            mDateHeaderRoomMap.put(groupKey, roomMap);
        }
    }

    /** Update a list in a map keyed by the closest matching time code. */
    private void updateMap(final long nowTimestamp, final long testTimestamp, final String key,
                           final Map<DateHeaderType, List<String>> map) {
        // Run through all the time code constants to find the right category based on the given
        // timestamps.  Then update list in the map.
        for (DateHeaderType dht : DateHeaderType.values()) {
            // Determine if the group fits the constraints of the current date header type.  The
            // declaration of DateHeaderType is ordered so that this algorithm will work.
            if (dht == old || nowTimestamp - testTimestamp <= dht.limit) {
                // This is the one.  Add this group to the associated list.
                List<String> list = map.get(dht);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(dht, list);
                }
                list.add(key);
                return;
            }
        }
    }
}
