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

import android.support.v4.view.ViewPager;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.ExperienceDeleteEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentKind.exp;
import static com.pajato.android.gamechat.common.FragmentType.selectExpGroupsRooms;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.game;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.search;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.expList;
import static com.pajato.android.gamechat.event.BaseChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.BaseChangeEvent.NEW;
import static com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment.GAME_HOME_FAM_KEY;

public class ShowExperiencesFragment extends BaseExperienceFragment {

    // Public instance methods.

    /** Process a given button click event looking for a FAM menu item or a list view click. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Delegate the event to the base class.
        processClickEvent(event.view, "expShowExperiences");
    }

    /** Handle a FAM or Snackbar click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Delegate the event to the base class.
        processTagClickEvent(event, "exp list");
    }

    /** Handle an experience list change event by dispatching again. */
    @Subscribe public void onExperienceListChangeEvent(ExperienceChangeEvent event) {
        switch (event.changeType) {
            case CHANGED:
            case NEW:
                DispatchManager.instance.startNextFragment(getActivity(), exp);
                break;
            default:
                break;
        }
    }

    /** Handle a deleted experience by updating the recycler adapter list. */
    @Subscribe public void onExperienceDelete(final ExperienceDeleteEvent event) {
        String format = "onExperienceDelete with event {%s}";
        logEvent(String.format(Locale.US, format, event));
        if (mActive)
            updateAdapterList();
    }

    /** Handle a menu item selection. */
    @Subscribe public void onMenuItem(final MenuItemEvent event) {
        if (!this.mActive)
            return;
        // Case on the item resource id if there is one to be had.
        switch (event.item != null ? event.item.getItemId() : -1) {
            case R.string.InviteFriendsOverflow:
                if (isInMeGroup())
                    DispatchManager.instance.chainFragment(getActivity(), selectExpGroupsRooms);
                else
                    InvitationManager.instance.extendGroupInvitation(getActivity(),
                            mExperience.getGroupKey());
                break;
            case R.string.SwitchToChat:
                // If the toolbar chat icon is clicked, on smart phone devices we can change panes.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if (viewPager != null)
                    viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                break;
            default:
                break;
        }
    }

    /** Deal with the fragment's activity's lifecycle by managing the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the group name only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home experience menu; and display a list of groups
        // with experiences showing the rooms and highlighting new experiences, much like messages
        // in the chat group display.
        super.onResume();
        FabManager.game.setImage(R.drawable.ic_add_white_24dp);
        FabManager.game.init(this, GAME_HOME_FAM_KEY);
    }

    /** Initialize the fragment by setting up the FAB and toolbar. */
    @Override public void onStart() {
        // Ensure that this is not a pass-through to a particular experience fragment.  If not, then
        // initialize the FAB manager and set up the toolbar.
        super.onStart();
        if (mDispatcher.expType == null) {
            FabManager.game.init(this);
            ToolbarManager.instance.init(this, mItem, helpAndFeedback, game, search, invite, settings);
            return;
        }

        // Handle a pass through by handing to the target fragment type while leaving an item object
        // in place for a back press or exit from the experience back to the experience list display.
        mDispatcher.type = mDispatcher.expType.getFragmentType();
        DispatchManager.instance.chainFragment(getActivity(), mDispatcher);
        mItem = new ListItem(expList, mDispatcher.groupKey, mDispatcher.roomKey, null, 0, null);
    }
}
