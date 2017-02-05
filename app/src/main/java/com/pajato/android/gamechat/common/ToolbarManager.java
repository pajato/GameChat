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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.main.MainActivity;
import com.pajato.android.gamechat.main.NavigationManager;

import static android.view.Menu.NONE;

/** Provide a singleton to manage the rooms panel fab button. */
public enum ToolbarManager {
    instance;

    // Private class constants.

    /** The menu item flag that will never show the item as an action button. */
    private static final int NEVER = MenuItem.SHOW_AS_ACTION_NEVER;

    /** The menu item flag that will show the item if room is available. */
    private static final int IF_ROOM = MenuItem.SHOW_AS_ACTION_IF_ROOM;

    // Public enums

    /** The set of menu item types. */
    public enum MenuItemType {
        chat (R.string.SwitchToChat, 0, IF_ROOM, R.drawable.ic_chat_bubble_outline_white_24px),
        game (R.string.SwitchToExp, 0, IF_ROOM, R.drawable.ic_games_white),
        helpAndFeedback (R.string.MenuItemHelpAndFeedback, 55, NEVER, -1), // should always be included
        invite (R.string.InviteFriendsOverflow, 20, IF_ROOM, R.drawable.ic_share_white_24dp),
        search (R.string.MenuItemSearch, 20, IF_ROOM, R.drawable.ic_search_white_24px),
        settings (R.string.MenuItemSettings, 0, NEVER, -1); // should always be included

        // Instance variables.

        /** The menu item resource id (for both the menu item and the menu title. */
        int itemResId;

        /** The order in the menu. */
        int order;

        /** The display flag. */
        int flag;

        /** The icon resource id. */
        int iconResId;

        // Constructors.

        /** Build the default menu item. */
        MenuItemType(final int itemId, final int order, final int flag, final int iconId) {
            this.itemResId = itemId;
            this.order = order;
            this.flag = flag;
            this.iconResId = iconId;
        }
    }

    /** The toolbar types. */
    public enum ToolbarType {
        chatChain (R.drawable.ic_more_vert_white_24dp, R.drawable.ic_arrow_back_white_24dp),
        chatMain (R.drawable.ic_more_vert_white_24dp),
        createGroupTT (R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp),
        createRoomTT (R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp),
        expMain (R.drawable.ic_more_vert_white_24dp),
        expMoveTT (R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp),
        expChain (R.drawable.ic_more_vert_white_24dp, R.drawable.ic_arrow_back_white_24dp),
        joinRoomTT (R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp),
        selectInviteTT(R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp),
        none ();

        // Instance variables.

        /** The overflow menu icon resource id. */
        int overflowMenuIconResourceId;

        /** The overflow menu resource id. */
        int overflowMenuResourceId;

        /** The navigation icon resource id. */
        int navigationIconResourceId;

        // Constructors.

        /** Build a default instance. */
        ToolbarType() {}

        /** Build an instance using just the action menu icon resource id. */
        ToolbarType(final int overflowMenuIconResourceId) {
            this.overflowMenuIconResourceId = overflowMenuIconResourceId;
            overflowMenuResourceId = R.menu.overflow_main_menu;
        }

        /** Build an instance using the given arguments. */
        ToolbarType(final int overflowMenuIconResourceId, final int navigationIconResourceId) {
            this(overflowMenuIconResourceId);
            this.navigationIconResourceId = navigationIconResourceId;
        }
    }

    // Private class constants.

    ///** The logcat tag. */
    //private static final String TAG = ToolbarManager.class.getSimpleName();

    // Private instance variables.

    /** The standard overflow item click handler that posts an app event. */
    private final OverflowMenuItemHandler mOverflowMenuItemClickHandler;

    // Sole Constructor.

    /** Build the instance with the given resource ids. */
    ToolbarManager() {
        // Create the overflow menu item click handler.
        mOverflowMenuItemClickHandler = new OverflowMenuItemHandler();
    }

    // Public instance methods

    /** Initialize the toolbar for a given fragment and menu entries. */
    public void init(@NonNull final BaseFragment fragment, final MenuItemType... menuEntries) {
        init(fragment, null, null, menuEntries);
    }

    /** Initialize the toolbar for a given fragment, list item and menu entries. */
    public void init(@NonNull final BaseFragment fragment, ListItem item,
                     final MenuItemType... menuEntries) {
        // Determine if the group name or the room name should be the title.
        String title = item.key == null
            ? GroupManager.instance.getGroupName(item.groupKey)
            : RoomManager.instance.getRoomName(item.key);
        String subtitle = item.key != null
            ? GroupManager.instance.getGroupName(item.groupKey) : null;
        init(fragment, title, subtitle, menuEntries);
    }

    /** Initialize the toolbar for a given fragment, title resource id and menu entries. */
    public void init(@NonNull final BaseFragment fragment, int resId,
                     final MenuItemType... menuEntries) {
        init(fragment, fragment.getString(resId), null, menuEntries);
    }

    /** Initialize the toolbar for a given fragment, title, subtitle, and menu entries. */
    public void init(@NonNull final BaseFragment fragment, final String title, final String subtitle,
                     final MenuItemType... menuEntries) {
        // Determine if this fragment exists and supports a managed toolbar.  Abort if not,
        // otherwise handle the toolbar based on the fragment's toolbar type.
        Toolbar toolbar = fragment.getToolbar();
        ToolbarType toolbarType = toolbar != null ? fragment.getToolbarType() : null;
        if (toolbar == null || toolbarType == null)
            return;
        switch (toolbarType) {
            case chatMain: // Setup the group (home) toolbar using the navigation manager.
                NavigationManager.instance.init(fragment.getActivity(), toolbar);
                break;
            case none:          // There is no toolbar.  Abort.
                return;
            default:            // Deal with all other types using the toolbar type.
                setupToolbar(fragment, toolbar, toolbarType);
                break;
        }

        // Set the title, subtitle and add the menu items to the action menu.
        setTitles(toolbar, title, subtitle);
        for (MenuItemType value : menuEntries)
            addMenuItem(toolbar, value);
    }

    /** Add a menu item to the toolbar's action menu. */
    private void addMenuItem(@NonNull final Toolbar toolbar, @NonNull final MenuItemType type) {
        // Ensure that the menu item can be added.  Abort if not, otherwise add the fully populated
        // item.
        Menu menu = toolbar.getMenu();
        boolean add = menu != null && menu.findItem(type.itemResId) == null;
        MenuItem item = add ? menu.add(NONE, type.itemResId, type.order, type.itemResId) : null;
        if (item == null)
            return;
        if (type.iconResId != -1) {
            item.setIcon(type.iconResId);
        }
        item.setShowAsAction(type.flag);
    }

    /** Reset the current toolbar overflow menu */
    public void resetOverflowMenu(@NonNull Resources resources, final ToolbarType type,
                                  final Toolbar toolbar) {
        int id = type.overflowMenuIconResourceId;
        if (toolbar.getMenu() != null) {
            toolbar.getMenu().clear();
        }
        toolbar.inflateMenu(type.overflowMenuResourceId);
        toolbar.setOverflowIcon(VectorDrawableCompat.create(resources, id, null));
        toolbar.setOnMenuItemClickListener(mOverflowMenuItemClickHandler);
    }

    // Private instance methods.

    /** Set the titles in the given toolbar using the given (possibly null) titles. */
    private void setTitles(@NonNull final Toolbar bar, final String title, final String subtitle) {
        // Apply the given titles to the toolbar; nulls will clear the fields.
        bar.setTitle(title);
        bar.setSubtitle(subtitle);
    }

    /** Setup a default toolbar, one that provides an overflow menu and a navigation icon. */
    private void setupToolbar(@NonNull BaseFragment fragment, @NonNull final Toolbar toolbar,
                              @NonNull final ToolbarType toolbarType) {
        // Reset the current toolbar overflow menu and determine if the navigation icon should be
        // set up.  Abort it not, otherwise set it up.
        resetOverflowMenu(fragment.getResources(), toolbarType, toolbar);
        if (toolbarType.navigationIconResourceId <= 0)
            return;
        toolbar.setNavigationIcon(toolbarType.navigationIconResourceId);
        MainActivity mainActivity = (MainActivity) fragment.getActivity();
        View.OnClickListener upHandler = mainActivity.getUpHandler();
        toolbar.setNavigationOnClickListener(upHandler);
    }

    // Private inner classes.

    /** Provide a class to post overflow menu item click events. */
    private class OverflowMenuItemHandler implements Toolbar.OnMenuItemClickListener {
        @Override public boolean onMenuItemClick(final MenuItem item) {
            AppEventManager.instance.post(new MenuItemEvent(item));
            return true;
        }
    }
}
