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
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.ListItem.DateHeaderType;
import com.pajato.android.gamechat.common.adapter.ListItem.ItemType;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.ExperiencesChangeHandler;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.event.ExpListChangeEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.ExperienceDeleteEvent;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.common.adapter.ListItem.DateHeaderType.old;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.date;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.expList;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.resourceHeader;
import static com.pajato.android.gamechat.event.BaseChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.BaseChangeEvent.REMOVED;
import static com.pajato.android.gamechat.main.NetworkManager.OFFLINE_EXPERIENCE_KEY;

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

    /** The experience map associating an experience with its self reference key. */
    public Map<String, Experience> experienceMap = new HashMap<>();

    // Private instance variables.

    /** A map associating date header type values with lists of group push keys. */
    private Map<DateHeaderType, List<String>> mDateHeaderGroupMap = new HashMap<>();

    /** A map associating date header type values with lists of room push keys, by group. */
    private Map<String, Map<DateHeaderType, List<String>>> mDateHeaderRoomMap = new HashMap<>();

    /** A map associating date header type values and experience push keys, by group and room. */
    private Map<String, Map<String, Map<DateHeaderType, List<String>>>> mDateHeaderExpMap =
            new HashMap<>();

    /** A map associating a group key with its most recently changed experience. */
    private Map<String, Experience> mGroupToRecentMap = new HashMap<>();

    /** A map of maps associating most recently modified experience in a room in a group. */
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

    public void deleteExperience(final ListItem item) {
        // Delete experience from database
        String path = String.format(Locale.US, EXPERIENCE_PATH, item.groupKey, item.roomKey,
                item.experienceKey);
        FirebaseDatabase.getInstance().getReference().child(path).removeValue();

        // Delete experience from various lists
        experienceMap.remove(item.experienceKey);

        if (expGroupMap.containsKey(item.groupKey)) {
            Map<String, Map<String, Experience>> roomsMap = expGroupMap.get(item.groupKey);
            if (roomsMap.containsKey(item.roomKey)) {
                Map<String, Experience> experiencesMap = roomsMap.get(item.roomKey);
                if (experiencesMap.containsKey(item.experienceKey)) {
                    experiencesMap.remove(item.experienceKey);
                }
                if (experiencesMap.size() == 0) {
                    roomsMap.remove(item.roomKey);
                }
            }
            if (roomsMap.size() == 0)
                expGroupMap.remove(item.groupKey);
        }

        Map<String, Map<DateHeaderType, List<String>>> groupMap = mDateHeaderExpMap.get(item.groupKey);
        Map<DateHeaderType, List<String>> roomMap = groupMap.get(item.roomKey);
        for (DateHeaderType dht : roomMap.keySet()) {
            List<String> experiences = roomMap.get(dht);
            if (experiences.contains(item.experienceKey)) {
                experiences.remove(item.experienceKey);
                if (experiences.size() == 0)
                    roomMap.remove(dht);
                else
                    roomMap.put(dht, experiences);
                break;
            }
        }
        if (roomMap.size() == 0)
            groupMap.remove(item.roomKey);
        if (groupMap.size() == 0)
            mDateHeaderExpMap.remove(item.groupKey);
        Map<String, Experience> recentExpMap = mRoomToRecentMap.get(item.groupKey);
        for(Map.Entry entry : recentExpMap.entrySet())
            if (entry.getValue().equals(item.roomKey))
                recentExpMap.remove(entry.getKey().toString());

        removeWatcher(item.experienceKey);

        AppEventManager.instance.post(new ExperienceDeleteEvent(item.experienceKey));

    }

    /** Return an experience push key to use with a subsequent room object persistence. */
    public String getExperienceKey() {
        return FirebaseDatabase.getInstance().getReference().child(EXPERIENCE_PATH).push().getKey();
    }

    /** Return null or an experience based on the specified experience key */
    public Experience getExperience(@NonNull final String experienceKey) {
        return experienceMap.get(experienceKey);
    }

    /** Return null or an experience of the given type from the given group and room. */
    public Experience getExperience(@NonNull final String groupKey, @NonNull final String roomKey,
                                    @NonNull final ExpType expType) {
        // Determine if there are any experiences in the given room.  If not, return null.
        Map<String, Map<String, Experience>> groupMap = expGroupMap.get(groupKey);
        Map<String, Experience> roomMap;
        roomMap = groupMap != null ? groupMap.get(roomKey) : null;
        if (roomMap == null || roomMap.size() == 0)
            return null;

        // Return the first experience of the given type in the room.  This imposes a one experience
        // per type per room model which seems reasonable.
        for (Experience experience : roomMap.values())
            if (experience.getExperienceType() == expType)
                return experience;
        return null;
    }

    /** Get the data as a set of list items for all groups. */
    public List<ListItem> getGroupListItemData() {
        // Determine whether to handle no groups (a set of welcome list items), one group (a set of
        // group rooms) or more than one group (a set of groups).
        List<ListItem> result = new ArrayList<>();
        switch (expGroupMap.size()) {
            case 0:
                return result;
            case 1:             // Get the experiences from the rooms in the joined group that have
                                // experiences and the me room if it has any experiences.
                String groupKey = expGroupMap.keySet().iterator().next();
                String meGroupKey = AccountManager.instance.getMeGroupKey();
                String meRoomKey = AccountManager.instance.getMeRoomKey();
                result.addAll(getItemListRooms(groupKey));
                if (groupKey.equals(meRoomKey) || groupKey.equals(meGroupKey))
                    return result;
                if (expGroupMap.containsKey(meGroupKey))
                    result.addAll(getItemListRooms(meGroupKey));
                return result;
            default:
                result.addAll(getItemListGroups());
                result.addAll(getItemListRooms(AccountManager.instance.getMeGroupKey()));
                return result;
        }
    }

    /** Return a list of items showing the rooms in a given group. */
    public List<ListItem> getRoomListItemData(@NonNull final String groupKey) {
        List<ListItem> result = new ArrayList<>();
        result.addAll(getItemListRooms(groupKey));
        return result;
    }

    /** Move an experience from one room to another. */
    public void move(@NonNull final Experience experience, final String groupKey, final String roomKey) {
        experience.setGroupKey(groupKey);
        experience.setRoomKey(roomKey);
        experience.setExperienceKey(null);
        createExperience(experience);
        // Notify the dispatch manager so that back navigation is reoriented to this new group/room.
        DispatchManager.instance.moveExperience(experience);
    }

    /** Handle a account change event by setting up or clearing variables. */
    @Subscribe public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.  If so, do nothing, otherwise clear the
        // message list for the logged out User.
        if (event.account != null) return;
        expGroupMap.clear();
        experienceMap.clear();
        mGroupToRecentMap.clear();
        mDateHeaderGroupMap.clear();
        mDateHeaderExpMap.clear();
        mDateHeaderRoomMap.clear();
        mRoomToRecentMap.clear();
    }

    /** Handle an experience change event by updating the date headers. */
    @Subscribe public void onExperienceChangeEvent(@NonNull final ExperienceChangeEvent event) {
        // Update the date headers for this message and post an event to trigger an adapter refresh.
        updateAllMaps(event.experience, event.changeType);
        AppEventManager.instance.post(new ExpListChangeEvent());
    }

    /** Remove a listener for experience changes in the given room */
    public void removeWatcher(final String roomKey) {
        String name = DBUtils.getHandlerName(EXPERIENCE_LIST_CHANGE_HANDLER, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name))
            DatabaseRegistrar.instance.unregisterHandler(name);
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

    /** Persist the experience. Handle offline behavior by short-circuiting the Firebase update. */
    public void updateExperience(final Experience experience) {
        experience.setModTime(new Date().getTime());
        String groupKey = experience.getGroupKey();
        String roomKey = experience.getRoomKey();
        String expKey = experience.getExperienceKey();
        if (groupKey == null && roomKey == null && expKey.equals(OFFLINE_EXPERIENCE_KEY)) {
            AppEventManager.instance.post(new ExperienceChangeEvent(experience, CHANGED));
            return;
        }
        String path = String.format(Locale.US, EXPERIENCE_PATH, groupKey, roomKey, expKey);
        FirebaseDatabase.getInstance().getReference().child(path).setValue(experience.toMap());
    }

    // Private instance methods.

    /** Add a group list item for the given kind and group. */
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
                result.add(new ListItem(itemType, room.groupKey, room.key, room.getName(), count, null));
                break;
            case expList:
                Experience exp = experienceMap.get(key);
                String groupKey = exp.getGroupKey();
                String roomKey = exp.getRoomKey();
                ListItem item = new ListItem(itemType, groupKey, roomKey, exp.getName(), 0, null);
                item.iconResId = getIconResId(exp.getExperienceType());
                item.experienceKey = key;
                result.add(item);
                break;
            default:
                break;
        }
    }

    /** Return the icon resource id corresponding to the given experience type. */
    private int getIconResId(final ExpType type) {
        switch (type) {
            case checkersET:
                return R.mipmap.ic_checkers;
            case chessET:
                return R.mipmap.ic_chess;
            case tttET:
            default:
                return R.mipmap.ic_tictactoe_red;
        }
    }

    /** Return a list of experience items for the group and room in the given configuration. */
    public List<ListItem> getItemListExperiences(@NonNull final Dispatcher dispatcher) {
        // Ensure that there are experience items to show.  Return an empty list if not.
        List<ListItem> result = new ArrayList<>();
        Map<DateHeaderType, List<String>> expMap;
        Map<String, Map<DateHeaderType, List<String>>> roomMap;
        roomMap = dispatcher.groupKey != null ? mDateHeaderExpMap.get(dispatcher.groupKey) : null;
        expMap = roomMap != null && dispatcher.roomKey != null ? roomMap.get(dispatcher.roomKey) : null;
        if (expMap == null || expMap.size() == 0)
            return result;
        processHeaders(result, expList, expMap);
        if (result.size() == 0)
            // Add a header indicating no experiences
            result.add(new ListItem(resourceHeader, R.string.NoExperiencesMessage));
        return result;
    }

    /** Return a list of experience group items, excluding the 'me' group */
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

    /** Return TRUE iff this experience has a new move that has not been seen. */
    public boolean isNew(@NonNull final Experience experience) {
        // Return true iff the account holder's id is on the unseen list.
        List<String> unseenList = experience.getUnseenList();
        String accountId = AccountManager.instance.getCurrentAccountId();
        return accountId == null || unseenList == null || unseenList.contains(accountId);
    }

    /** Process all headers and associated items for a given map. Exclude the 'me' group. */
    private void processHeaders(final List<ListItem> result, ItemType itemType,
                                final Map<DateHeaderType, List<String>> map) {
        String meGroupKey = AccountManager.instance.getMeGroupKey();
        // Walk through the set of date header types to collect the list items.
        for (ListItem.DateHeaderType dht : ListItem.DateHeaderType.values()) {
            List<String> list = map.get(dht);
            if (list != null && list.size() > 0) {
                // Add the header item followed by all the items from the given map.  However, if
                // the only item in the list is the 'me' group, don't include either the header or
                // the group item (we only show the 'me' room, never the 'me' group.
                if(list.size() > 1 || !list.get(0).equals(meGroupKey))
                    result.add(new ListItem(date, dht.resId));
                for (String key : list)
                    // Don't include the me group
                    if (!key.equals(meGroupKey))
                        // Add relevant items based on itemType
                        addItem(result, itemType, key);
            }
        }
     }

    /** Update the timestamp ordered map of experiences in a given group and room. */
    private void updateExpMap(@NonNull final String groupKey, @NonNull final String roomKey,
                              final long nowTimestamp) {
        // Ensure that the group map exists for the given group key.
        Map<String, Map<DateHeaderType, List<String>>> groupMap = mDateHeaderExpMap.get(groupKey);
        if (groupMap == null) {
            groupMap = new HashMap<>();
            mDateHeaderExpMap.put(groupKey, groupMap);
        }

        // Ensure that the room map exists for the given room key.
        Map<DateHeaderType, List<String>> roomMap;
        roomMap = groupMap.get(roomKey);
        if (roomMap == null) {
            roomMap = new HashMap<>();
            groupMap.put(roomKey, roomMap);
        }

        // Associate each experience in the given group and room with the correct timestamp.
        Map<String, Map<String, Experience>> expRoomMap = expGroupMap.get(groupKey);
        Set<Map.Entry<String, Experience>> entrySet =
                expRoomMap != null ? expRoomMap.get(roomKey).entrySet() : null;
        if (entrySet == null)
            return;
        for (Map.Entry<String, Experience> entry : entrySet)
            updateMap(nowTimestamp, entry.getValue().getModTime(), entry.getKey(), roomMap);
    }

    /** Update the various maps used to track experiences. */
    private void updateAllMaps(final Experience experience, final int changeType) {
        // Deal with a changed experience (like a turn, for example) by making the given experience
        // the most recent in both the group and room recent experience maps. If the change was to
        // remove the experience, delete it from various maps.
        String groupKey = experience.getGroupKey();

        // Remember the last updated experience in the group and room.
        String roomKey = experience.getRoomKey();
        if (changeType == REMOVED) {
            Map<String, Map<String, Experience>> groupMap = expGroupMap.get(groupKey);
            if (groupMap != null && groupMap.containsKey(groupKey)) {
                groupMap.remove(groupKey);
            }
        } else
            mGroupToRecentMap.put(groupKey, experience);

        Map<String, Experience> roomMap = mRoomToRecentMap.get(groupKey);
        if (changeType == REMOVED) {
            if (roomMap != null && roomMap.containsKey(roomKey)) {
                roomMap.remove(roomKey);
            }
        }  else {
            if (roomMap == null)
                roomMap = new HashMap<>();
            roomMap.put(roomKey, experience);
            mRoomToRecentMap.put(groupKey, roomMap);
        }

        // Update the group list headers for each group and room with at least one experience.
        mDateHeaderGroupMap.clear();
        mDateHeaderRoomMap.clear();
        mDateHeaderExpMap.clear();
        long nowTimestamp = new Date().getTime();
        for (String key : mGroupToRecentMap.keySet())
            updateHeaders(key, nowTimestamp);
    }

    /** Update the headers for the given group and all of its rooms. */
    private void updateHeaders(final String groupKey, final long nowTimestamp) {
        // Determine which date header type the current group should be associated with using
        // the time stamp from the most recent experience in the group.
        long groupTimestamp = mGroupToRecentMap.get(groupKey).getModTime();
        updateMap(nowTimestamp, groupTimestamp, groupKey, mDateHeaderGroupMap);

        // for all rooms in the group, update the mDateHeaderRoomMap
        Map<String, Experience> map = mRoomToRecentMap.get(groupKey);
        Map<DateHeaderType, List<String>> roomMap = new HashMap<>();
        for (String roomKey : map.keySet()) {
            long roomTimestamp = map.get(roomKey).getModTime();
            updateMap(nowTimestamp, roomTimestamp, roomKey, roomMap);
            if (roomMap.size() == 0)
                continue;
            updateExpMap(groupKey, roomKey, nowTimestamp);
        }
        mDateHeaderRoomMap.put(groupKey, roomMap);
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
