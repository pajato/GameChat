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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.database.handler.DatabaseEventHandler;
import com.pajato.android.gamechat.database.handler.ExpProfileListChangeHandler;
import com.pajato.android.gamechat.database.handler.ExperienceChangeHandler;
import com.pajato.android.gamechat.event.AuthenticationChangeEvent;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.model.ExpProfile;

import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.R.attr.type;

/**
 * Provide a class to manage the experience database objects.
 *
 * @author Paul Michael Reilly
 */
public enum ExperienceManager {
    instance;

    // Public class constants.

    // Database paths, often used as format strings.
    public static final String EXP_PROFILE_LIST_PATH =
            RoomManager.ROOMS_PATH + "%s/profile/expProfileList";
    public static final String EXPERIENCES_PATH = RoomManager.ROOMS_PATH + "%s/experiences/";
    public static final String EXPERIENCE_PATH = EXPERIENCES_PATH + "%s/";

    // Private class constants.

    /** The experience profile change handler base name. */
    private static final String EXP_PROFILE_LIST_CHANGE_HANDLER = "expProfileListChangeHandler";

    /** The experience change handler base name. */
    private static final String EXPERIENCES_CHANGE_HANDLER = "experiencesChangeHandler";

    // Public instance variables.

    /** The map associating group and room push keys with a map of experience profiles. */
    public Map<String, Map<String, Map<String, ExpProfile>>> expProfileMap = new HashMap<>();

    /** The experience map. */
    public Map<String, Experience> experienceMap = new HashMap<>();

    // Public instance methods.

    /** Persist the given experience to the database. */
    public void createExperience(final Experience experience) {
        // Ensure that the requisite keys exist.  Abort if either key does not exist.
        String groupKey = experience.getGroupKey();
        String roomKey = experience.getRoomKey();
        if (groupKey == null || roomKey == null) return;

        // Get the name and type for the given experience.  Abort if either does not exist.
        String name = experience.getName();
        ExpType expType = experience.getExperienceType();
        if (name == null || type == -1) return;

        // Persist the experience.
        String path = String.format(Locale.US, EXPERIENCES_PATH, groupKey, roomKey);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(path).push();
        experience.setExperienceKey(ref.getKey());
        ref.setValue(experience.toMap());

        // Persist the experience profile so that the room's experience profile list watcher will
        // append it.  Finally set the experience watcher as, presumbably, the User is creating the
        // experience to enjoy it asap.
        path = String.format(Locale.US, EXP_PROFILE_LIST_PATH, groupKey, roomKey);
        ref = FirebaseDatabase.getInstance().getReference().child(path).push();
        String key = ref.getKey();
        String expKey = experience.getExperienceKey();
        ExpProfile profile = new ExpProfile(key, name, expType, groupKey, roomKey, expKey);
        ref.setValue(profile.toMap());
        setExperienceWatcher(profile);
    }

    /** Return the database path to an experience for a given experience profile. */
    public String getExperiencePath(final ExpProfile profile) {
        String key = profile.expKey;
        return String.format(Locale.US, EXPERIENCE_PATH, profile.groupKey, profile.roomKey, key);
    }

    /** Return the database path to a experience profile for a given room and profile key. */
    public String getExpProfilesPath(final String groupKey, final String roomKey) {
        return String.format(Locale.US, EXP_PROFILE_LIST_PATH, groupKey, roomKey);
    }

    /** Handle a account change event by setting up or clearing variables. */
    @Subscribe public void onAuthenticationChange(@NonNull final AuthenticationChangeEvent event) {
        // Determine if a User has been authenticated.  If so, do nothing, otherwise clear the
        // message list for the logged out User.
        if (event.account != null) return;
        expProfileMap.clear();
        experienceMap.clear();
    }

    /** Setup a listener for experience changes in the given room. */
    public void setExperienceWatcher(final ExpProfile profile) {
        // Set up a watcher in the given room for experiences changes.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String name = DBUtils.instance.getHandlerName(EXPERIENCES_CHANGE_HANDLER, profile.expKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ExperienceChangeHandler(name, profile);
        DatabaseRegistrar.instance.registerHandler(handler);
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
    public void setExpProfileListWatcher(final String groupKey, final String roomKey) {
        // Obtain a room and set watchers on all the experience profiles in that room.
        // Determine if a handle already exists. Abort if so.  Register a new handler if not.
        String name = DBUtils.instance.getHandlerName(EXP_PROFILE_LIST_CHANGE_HANDLER, roomKey);
        if (DatabaseRegistrar.instance.isRegistered(name)) return;
        DatabaseEventHandler handler = new ExpProfileListChangeHandler(name, groupKey, roomKey);
        DatabaseRegistrar.instance.registerHandler(handler);
    }
}
