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

import android.content.Context;
import android.support.v4.view.ViewPager;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.Dispatcher;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ExperienceChangeEvent;
import com.pajato.android.gamechat.event.ExperienceDeleteEvent;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.event.TagClickEvent;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.main.PaneManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.FragmentType.selectGroupsRooms;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.chat;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.invite;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.search;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;
import static com.pajato.android.gamechat.event.BaseChangeEvent.CHANGED;
import static com.pajato.android.gamechat.event.BaseChangeEvent.NEW;
import static com.pajato.android.gamechat.exp.fragment.ExpEnvelopeFragment.GAME_HOME_FAM_KEY;

public class ShowExperiencesFragment extends BaseExperienceFragment {

    // Public instance methods.

    /** Return null or a list to be displayed by a list adapter */
    public List<ListItem> getList() {
        return ExperienceManager.instance.getItemListExperiences(mDispatcher);
    }

    /** Get the toolbar subTitle, or null if none is used */
    public String getToolbarSubtitle() {
        if (AccountManager.instance.isMeGroup(mDispatcher.groupKey))
            return getString(R.string.MyGameRoomToolbarTitle);
        return GroupManager.instance.getGroupName(mDispatcher.groupKey);
    }

    /** Get the toolbar title */
    public String getToolbarTitle() {
        // Determine if the group is the me group and give it special handling.
        if (AccountManager.instance.isMeGroup(mDispatcher.groupKey))
            return getString(R.string.MyExperiencesToolbarTitle);
        return RoomManager.instance.getRoomName(mDispatcher.roomKey);
    }

    /** Handle a button click event by delegating the event to the base class. */
    @Subscribe public void onClick(final ClickEvent event) {
        processClickEvent(event.view, this.type);
    }

    /** Handle a FAM or Snackbar click event. */
    @Subscribe public void onClick(final TagClickEvent event) {
        // Delegate the event to the base class.
        processTagClickEvent(event, this.type);
    }

    /** Handle an experience list change event by dispatching again. */
    @Subscribe public void onExperienceListChangeEvent(ExperienceChangeEvent event) {
        switch (event.changeType) {
            case CHANGED:
            case NEW:
                if (mActive)
                    updateAdapterList();
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
            case R.string.InviteFriendMessage:
                // If not on a tablet, make sure that we switch to the chat perspective
                if (!PaneManager.instance.isTablet()) {
                    ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                    if (viewPager != null)
                        viewPager.setCurrentItem(PaneManager.CHAT_INDEX);
                }
                if (isInMeGroup())
                    DispatchManager.instance.dispatchToFragment(this, selectGroupsRooms, type);
                else
                    InvitationManager.instance.extendGroupInvitation(getActivity(),
                            mExperience.getGroupKey());
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

    /** Setup the fragment configuration using the specified dispatcher. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        // The experiences in a room require both the group and room keys.  Determine if the
        // group is the me group and give it special handling.
        String meGroupKey = AccountManager.instance.getMeGroupKey();
        dispatcher.roomKey = meGroupKey != null && meGroupKey.equals(dispatcher.groupKey)
                ? AccountManager.instance.getMeRoomKey() : dispatcher.roomKey;
        mDispatcher = dispatcher;
    }

    /** Initialize the fragment by setting up the FAB and toolbar. */
    @Override public void onStart() {
        // If the dispatcher has an experience type set, then this is a pass-through to an
        // experience fragment. If not, then set up the toolbar and FAB.
        super.onStart();
        if (mDispatcher.expType == null) {
            FabManager.game.init(this);
            ToolbarManager.instance.init(this, helpAndFeedback, chat, search, invite, settings);
            return;
        }
        // Handle a pass-through to an experience.
        DispatchManager.instance.dispatchToGame(this, mDispatcher.expType.getFragmentType());
    }
}
