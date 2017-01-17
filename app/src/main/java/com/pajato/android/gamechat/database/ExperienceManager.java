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
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.ExperiencesChangeHandler;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    // Public instance methods.

    /** Persist the given experience to the database. */
    public void createExperience(final Experience experience) {
        // Ensure that the requisite keys, name and type all exist.  Abort if any do not exist.
        String groupKey = experience.getGroupKey();
        String roomKey = experience.getRoomKey();
        String name = experience.getName();
        ExpType expType = experience.getExperienceType();
        if (groupKey == null || roomKey == null || name == null || expType == null) return;

        // Persist the experience.
        String key = getExperienceKey();
        String path = String.format(Locale.US, EXPERIENCE_PATH, groupKey, roomKey, key);
        experience.setExperienceKey(key);
        DBUtils.instance.updateChildren(path, experience.toMap());
    }

    /** Return the number of experiences for the givne type. */
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

    /** Perist the given experience. */
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

}
