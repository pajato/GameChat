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
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.JoinManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.FragmentType.createExpGroup;
import static com.pajato.android.gamechat.common.FragmentType.protectedUsers;
import static com.pajato.android.gamechat.common.FragmentType.selectExpGroupsRooms;

/**
 * Provides a class to allow the currently signed in User to select another User for a two-player
 * game experience.
 *
 * @author Paul Michael Reilly
 */
public class SelectUserFragment extends BaseExperienceFragment {

    // Public constants.

    /** The lookup key for the FAB experience selection menu. */
    public static final String SELECT_USER_FAM_KEY = "selectUserFamKey";

    // Public instance methods

    /** Process a given button click event looking for one on the game fab button */
    @Subscribe public void onClick(final ClickEvent event) {
        // Determine if the event needs to be processed.
        if (!mActive)
            return;
        logEvent("onClick (selectUser)");
        int id = event.view.getId();
        switch (id) {
            case R.id.saveButton:
                processSave();
                break;
            case R.id.selector:
            case R.id.altSelector:
                processSelector(event.view);
            default:
                processClickEvent(event.view, "selectUser");
                break;
        }
    }

    /** Process a menu click event ... */
    @Subscribe public void onClick(final TagClickEvent event) {
        Object payload = event.view.getTag();
        if (payload == null || !(payload instanceof MenuEntry))
            return;

        // The event represents a menu entry.  Close the FAM and case on the title id.
        FabManager.game.dismissMenu(this);
        MenuEntry entry = (MenuEntry) payload;
        switch (entry.titleResId) {
            case R.string.CreateGroupMenuTitle:
                DispatchManager.instance.chainFragment(getActivity(), createExpGroup);
                break;
            case R.string.InviteFriendFromChat:
                DispatchManager.instance.chainFragment(getActivity(), selectExpGroupsRooms);
                break;
            case R.string.ManageRestrictedUserTitle:
                if (AccountManager.instance.isRestricted()) {
                    String protectedWarning = "Protected Users cannot manage other Protected Users.";
                    Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
                    break;
                }
                DispatchManager.instance.chainFragment(getActivity(), protectedUsers);
                break;
            default:
                break;
        }
    }

    /** Initialize FAB and adapter list */
    @Override public void onResume() {
        super.onResume();
        updateAdapterList();
    }

    /** Initialize the fragment by setting up the FAB and toolbar. */
    @Override public void onStart() {
        super.onStart();
        ToolbarManager.instance.init(this, R.string.SelectUserToolbarTitle);
        FabManager.game.setMenu(SELECT_USER_FAM_KEY, getSelectMenu());
        FabManager.game.init(this);
        FabManager.game.setImage(R.drawable.ic_add_white_24dp);
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getSelectMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        if (!AccountManager.instance.isRestricted()) {
            menu.add(getTintEntry(R.string.CreateGroupMenuTitle,
                    R.drawable.ic_group_add_black_24dp));
            menu.add(getTintEntry(R.string.ManageRestrictedUserTitle,
                    R.drawable.ic_verified_user_black_24dp));
        }
        menu.add(getTintEntry(R.string.InviteFriendFromChat, R.drawable.ic_share_black_24dp));
        return menu;
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
}
