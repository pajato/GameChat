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
import com.pajato.android.gamechat.common.FragmentType;
import com.pajato.android.gamechat.common.adapter.ListItem.DateHeaderType;
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

    /** The experience map. */
    public Map<String, Experience> experienceMap = new HashMap<>();

    // Private instance variables.

    /** A map associating date header type values with lists of group push keys. */
    private Map<DateHeaderType, List<String>> mDateHeaderTypeToGroupListMap = new HashMap<>();

    /** A map associating a group push key with it's most recent new message. */
    private Map<String, Experience> mGroupToLastNewExpMap = new HashMap<>();

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
        DBUtils.instance.updateChildren(path, experience.toMap());
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

    /** Handle a account change event by setting up or clearing variables. */
    @Subscribe public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.  If so, do nothing, otherwise clear the
        // message list for the logged out User.
        if (event.account != null) return;
        expGroupMap.clear();
        experienceMap.clear();
    }

    /** Handle an experience change event by updating the date headers ... */
    @Subscribe public void onEperienceChangeEvent(@NonNull final ExperienceChangeEvent event) {
        // Update the date headers for this message and post an event to trigger an adapter refresh.
        updateGroupHeaders(event.experience);
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
        String name = DBUtils.instance.getHandlerName(EXPERIENCE_LIST_CHANGE_HANDLER, roomKey);
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

    /** Update the headers used to bracket the messages in the main list. */
    private void updateGroupHeaders(final Experience experience) {
        // Deal with a message ...
        mGroupToLastNewExpMap.put(experience.getGroupKey(), experience);
        mDateHeaderTypeToGroupListMap.clear();
        long nowTimestamp = new Date().getTime();
        for (String key : mGroupToLastNewExpMap.keySet()) {
            // Determine which date header type the current group should be associated with.
            long groupTimestamp = mGroupToLastNewExpMap.get(key).getCreateTime();
            for (DateHeaderType dht : DateHeaderType.values()) {
                // Determine if the current group fits the constraints of the current date header
                // type.  The declaration of DateHeaderType is ordered so that this algorithm will
                // work.
                if (dht == old || nowTimestamp - groupTimestamp <= dht.limit) {
                    // This is the one.  Add this group to the associated list.
                    List<String> list = mDateHeaderTypeToGroupListMap.get(dht);
                    if (list == null) {
                        list = new ArrayList<>();
                        mDateHeaderTypeToGroupListMap.put(dht, list);
                    }
                    list.add(key);
                    break;
                }
            }
        }
    }

    /** Return TRUE iff this experience has a new move that has not been seen? */
    public boolean isNew(@NonNull final Experience experience) {
        // TODO: figure this one out, but use a placeholder hack for now.
        return experienceMap.get(experience.getExperienceKey()) == null;
    }
}
