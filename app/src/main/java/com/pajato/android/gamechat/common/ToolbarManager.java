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

package com.pajato.android.gamechat.common;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.main.MainActivity;
import com.pajato.android.gamechat.main.NavigationManager;

/** Provide a singleton to manage the rooms panel fab button. */
public enum ToolbarManager {
    instance;

    // Public enums

    /** The toolbar types. */
    enum ToolbarType {
        chatChain(R.drawable.ic_more_vert_white_24dp, R.drawable.ic_arrow_back_white_24dp),
        chatMain(),
        createGroupTT(R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp,
                      R.string.CreateGroupMenuTitle),
        createRoomTT(R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp,
                     R.string.CreateRoomMenuTitle),
        joinRoomTT(R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp,
                   R.string.JoinRoomsMenuTitle),
        expMain(),
        expChain();

        // Instance variables.

        /** The overflow menu icon resource id. */
        int overflowMenuIconResourceId;

        /** The overflow menu resource id. */
        int overflowMenuResourceId;

        /** The navigation icon resource id. */
        int navigationIconResourceId;

        /** The toolbar title resource id. */
        int titleResourceId;

        // Constructors.

        /** Build a default instance. */
        ToolbarType() {}

        /** Build an instance using the given arguments. */
        ToolbarType(final int overflowMenuIconResourceId, final int navigationIconResourceId) {
            this.overflowMenuIconResourceId = overflowMenuIconResourceId;
            this.navigationIconResourceId = navigationIconResourceId;
            overflowMenuResourceId = R.menu.overflow_main_menu;
        }

        /** Build an instance using all possible arguments. */
        ToolbarType(final int overflowResId, final int navResId, final int titleResId) {
            this(overflowResId, navResId);
            titleResourceId = titleResId;
        }
    }

    // Private class constants.

    ///** The logcat tag. */
    //private static final String TAG = ToolbarManager.class.getSimpleName();

    // Sole Constructor.

    /** Build the instance with the given resource ids. */
    ToolbarManager() {}

    // Public instance methods

    /** Initialize the toolbar for all pages. */
    public void init(@NonNull final BaseFragment fragment) {
        // Determine if this fragment exists and supports a managed toolbar.  Abort if not,
        // otherwise handle the toolbar based on the fragment's toolbar type.
        Toolbar toolbar = fragment.getToolbar();
        ToolbarType toolbarType = toolbar != null ? fragment.getToolbarType() : null;
        if (toolbar == null || toolbarType == null)
            return;
        switch (toolbarType) {
            case chatMain:      // Setup the group (home) toolbar using the naviation manager.
                NavigationManager.instance.init(fragment.getActivity(), toolbar);
                break;
            default:            // Deal with all other types using type arguments.
                int id = toolbarType.overflowMenuIconResourceId;
                Resources resources = fragment.getResources();
                toolbar.getMenu().clear();
                toolbar.inflateMenu(toolbarType.overflowMenuResourceId);
                toolbar.setOverflowIcon(VectorDrawableCompat.create(resources, id, null));
                toolbar.setNavigationIcon(toolbarType.navigationIconResourceId);
                MainActivity mainActivity = (MainActivity) fragment.getActivity();
                View.OnClickListener upHandler = mainActivity.getUpHandler();
                toolbar.setNavigationOnClickListener(upHandler);
                break;
        }
    }

    /** Set the titles in the toolbar for the given title and subtitle. */
    public void setTitles(@NonNull final BaseFragment fragment, final Experience experience) {
        // Ensure that the toolbar exists.  Abort if not, otherwise set the titles accordingly.
        Toolbar bar = fragment.getToolbar();
        if (bar == null)
            return;
        String title = experience.getGroupKey();
        String subtitle = experience.getRoomKey();
        setTitles(bar, title, subtitle);
    }

    /** Set the titles in the toolbar based on the list type. */
    public void setTitles(@NonNull final BaseFragment fragment, final ChatListItem item) {
        // Ensure that there is an accessible toolbar at this point.  Abort if not, otherwise case
        // on the list type to apply the titles.
        Toolbar bar = fragment.getToolbar();
        if (bar == null)
            return;
        ToolbarType toolbarType = fragment.getToolbarType();
        switch (toolbarType) {
            case createGroupTT:
            case createRoomTT:
            case joinRoomTT: // Set the title to the given resource and the subtitle to the group
                           // name, if one is available.
                int resId = toolbarType.titleResourceId;
                setTitles(fragment, bar, resId, item);
                break;
            case chatMain:
            case chatChain:     // Set the title and subtitle based on the item content.
                setTitles(fragment, bar, item);
                break;
            default:
                setTitles(fragment, bar, R.string.app_name, null);
                break;
        }
    }

    // Private instance methods.

    /** Set the titles in the given toolbar using the given item. */
    private void setTitles(@NonNull final BaseFragment fragment, @NonNull final Toolbar bar,
                           final ChatListItem item) {
        // Use the item content to set the title and subtitle.
        if (item == null || (item.groupKey == null && item.key == null)) {
            setTitles(fragment, bar, R.string.app_name, null);
            return;
        }

        // Determine if the group name should be the title.
        String title = item.key == null
            ? GroupManager.instance.getGroupName(item.groupKey)
            : RoomManager.instance.getRoomName(item.key);
        String subtitle = item.key != null
            ? GroupManager.instance.getGroupName(item.groupKey) : null;
        setTitles(bar, title, subtitle);
    }

    /** Set the title to the given resource and the subtitle to the group name, if available. */
    private void setTitles(@NonNull final BaseFragment fragment, @NonNull final Toolbar bar,
                           final int resourceId, final ChatListItem item) {
        String title = fragment.getResources().getString(resourceId);
        String key = item != null ? item.groupKey : null;
        String subtitle = key != null ? GroupManager.instance.getGroupName(key) : null;
        setTitles(bar, title, subtitle);
    }

    /** Set the titles in the given toolbar using the given (possibly null) titles. */
    private void setTitles(@NonNull final Toolbar bar, final String title, final String subtitle) {
        // Apply the given titles to the toolbar; nulls will clear the fields.
        bar.setTitle(title);
        bar.setSubtitle(subtitle);
    }

}
