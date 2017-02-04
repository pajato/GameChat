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

package com.pajato.android.gamechat.exp.fragment;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.JoinManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

/**
 * Provides a class to allow the currently signed in User to select another User for a two-player
 * game experience.
 *
 * @author Paul Michael Reilly
 */
public class SelectUserFragment extends BaseExperienceFragment {

    @Subscribe public void onClick(final ClickEvent event) {
        // Determine if the event needs to be processed.  Abort if not, otherwise log it and handle
        // it using a procedural abstraction.
        if (!mActive)
            return;
        logEvent("onClick (selectUser)");
        int id = event.view.getId();
        switch (id) {
            case R.id.saveButton:
                processSave();
                break;
            default:
                processSelector(event.view);
                break;
        }
    }

    /** .... */
    @Override public void onResume() {
        super.onResume();
        FabManager.game.init(this);
        updateAdapterList();
        ToolbarManager.instance.setTitle(this, R.string.SelectUserToolbarTitle);
    }

    /** Initialize the fragment by setting up the FAB and toolbar. */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this);
    }

    // Private instance methods.

    /** Process the save button to begin the move to the private member view. */
    private void processSave() {
        if (mItem == null)
            return;

        // The item variable is a User item.  Create the member room if necessary and move the
        // current experience to that room.  Then continue the experience in that room.
        // Determine if the member room needs to be joined.
        JoinManager.instance.joinRoom(mItem);
        ExperienceManager.instance.move(mExperience, mItem.groupKey, mItem.roomKey);
        getActivity().onBackPressed();
    }

    /** Process a selection via the checkbox/radio button or a tap on the item. */
    private void processSelector(@NonNull final View view) {
        // Determine if the payload is a list item. Abort if not, otherwise handle the click
        // according to the source: a radio button enables the save button enable state, a checkbox
        // toggles the save button enable state, and anything else will perform a click on the
        // button.
        Object payload = view.getTag();
        if (payload == null || !(payload instanceof ListItem))
            return;
        mItem = (ListItem) payload;
        boolean value;
        switch (view.getId()) {
            case R.id.selector:
                // Handle a radio button click.
                value = true;
                break;
            case R.id.altSelector:
                // Handle a checkbox click.
                value = ((CheckBox) view).isChecked();
                break;
            default:
                // Handle an item view click.
                value = getValue(view);
                break;
        }

        // Set the save button enable state using the computed value.
        View saveButton = mLayout.findViewById(R.id.saveButton);
        saveButton.setEnabled(value);
    }

    /** Return a boolean value obtained by processing the given item view. */
    private boolean getValue(@NonNull final View itemView) {
        // Handle a radio button (the most likely event.)
        View view = itemView.findViewById(R.id.selector);
        if (view != null && view instanceof RadioButton && view.getVisibility() == View.VISIBLE) {
            ((RadioButton) view).setChecked(true);
            return true;
        }

        // Handle a checkbox (an unlikely but possible event.)
        view = itemView.findViewById(R.id.altSelector);
        if (view != null && view instanceof CheckBox && view.getVisibility() == View.VISIBLE) {
            ((CheckBox) view).toggle();
            return ((CheckBox) view).isChecked();
        }

        // Handle neither by doing nothing, i.e. return the current state of the save button.
        View saveButton = mLayout.findViewById(R.id.saveButton);
        return saveButton.isEnabled();
    }
}
