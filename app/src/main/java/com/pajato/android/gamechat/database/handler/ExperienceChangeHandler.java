/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.database.handler;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.exp.ExpType;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.model.Checkers;
import com.pajato.android.gamechat.exp.model.Chess;
import com.pajato.android.gamechat.exp.model.ExpProfile;
import com.pajato.android.gamechat.exp.model.TicTacToe;

/**
 * Provide a class to handle new and changed experiences inside a group and room.
 *
 * @author Paul Michael Reilly
 */
public class ExperienceChangeHandler extends DatabaseEventHandler implements ValueEventListener {

    // Private constants.

    /** The logcat TAG. */
    private static final String TAG = ExperienceChangeHandler.class.getSimpleName();

    // Private instance variables.

    /** The experience key value. */
    private ExpProfile mProfile;

    // Public constructors.

    /** Build a handler with the given name and path. */
    public ExperienceChangeHandler(final String name, final ExpProfile profile) {
        super(name, ExperienceManager.instance.getExperiencePath(profile));
        mProfile = profile;
    }

    // Public instance methods.

    /** Get the current set of active rooms using a list of room identifiers. */
    @Override public void onDataChange(final DataSnapshot dataSnapshot) {
        // Ensure that some data exists.
        if (dataSnapshot.exists()) {
            // There is data.  Ensure that it will map to a valid experience.  Fail quietly for now.
            // TODO: handle an invalid data snapshot.
            Experience experience = getExperience(dataSnapshot);
            if (experience == null) return;

            // Update the experience on the database list manager and post the change event to the
            // app.
            ExperienceManager.instance.experienceMap.put(mProfile.expKey, experience);
            AppEventManager.instance.post(new ExperienceChangeEvent(experience));
        } else {
            // TODO: should we remove the key here?
            Log.e(TAG, "Invalid key.  No value returned.");
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // TODO: deal with this...
        Log.e(TAG, "some kind of error");
    }

    // Private instance methods.

    /** Return a class to hold the new experience, null for an invalid profile. */
    private Experience getExperience(final DataSnapshot snapshot) {
        if (mProfile == null) return null;

        // Case on the profile type to get the desired class.
        ExpType type = ExpType.values()[mProfile.type];
        switch (type) {
            case ttt_exp: return snapshot.getValue(TicTacToe.class);
            case checkers_exp: return snapshot.getValue(Checkers.class);
            case chess_exp: return snapshot.getValue(Chess.class);
            default:
                break;
        }

        return null;
    }

}
