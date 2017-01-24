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

import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.MenuItemEvent;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.main.MainActivity;
import com.pajato.android.gamechat.main.NavigationManager;

import static android.view.Menu.NONE;
import static com.pajato.android.gamechat.common.ToolbarManager.ToolbarType.chatMain;

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
        checkers (R.string.NewGameCheckers, 20, NEVER, -1),
        chess (R.string.NewGameChess, 20, NEVER, -1),
        game (R.string.SwitchToExp, 0, IF_ROOM, R.drawable.ic_games_white),
        search (R.string.MenuItemSearch, 20, IF_ROOM, R.drawable.ic_search_white_24px);

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
    enum ToolbarType {
        chatChain (R.drawable.ic_more_vert_white_24dp, R.drawable.ic_arrow_back_white_24dp),
        chatMain (),
        createGroupTT (R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp,
                      R.string.CreateGroupMenuTitle),
        createRoomTT (R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp,
                     R.string.CreateRoomMenuTitle),
        expMain (),
        expChain (),
        joinRoomTT (R.drawable.ic_more_vert_black_24dp, R.drawable.ic_arrow_back_black_24dp,
                R.string.JoinRoomsMenuTitle),
        none ();

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

    // Private instance variables.

    /** The standard overlow item click handler that posts an app event. */
    private final OverflowMenuItemHandler mOverflowMenuItemClickHandler;

    // Sole Constructor.

    /** Build the instance with the given resource ids. */
    ToolbarManager() {
        // Create the overflowm menu item click handler.
        mOverflowMenuItemClickHandler = new OverflowMenuItemHandler();
    }

    // Public instance methods

    /** Initialize the toolbar for a given fragment. */
    public void init(@NonNull final BaseFragment fragment, final MenuItemType... menuEntries) {
        // Determine if this fragment exists and supports a managed toolbar.  Abort if not,
        // otherwise handle the toolbar based on the fragment's toolbar type.
        Toolbar toolbar = fragment.getToolbar();
        ToolbarType toolbarType = toolbar != null ? fragment.getToolbarType() : null;
        if (toolbar == null || toolbarType == null)
            return;
        if (toolbarType == chatMain)
            // Setup the group (home) toolbar using the naviation manager.
            NavigationManager.instance.init(fragment.getActivity(), toolbar);
        else
            // Deal with all other types using the toolbar type.
            setupNonHomeToolbar(fragment, toolbar, toolbarType);

        // Handle any extra menu items to add to the action menu.
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
        item.setIcon(type.iconResId);
        item.setShowAsAction(type.flag);
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
            case none:
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

    /** Setup a non home toolbar, one that provides an overflow menu and a back/cancel nav icon. */
    private void setupNonHomeToolbar(@NonNull BaseFragment fragment, @NonNull final Toolbar toolbar,
                                     @NonNull final ToolbarType toolbarType) {
        // Reset the current toolbar overflow menu.
        int id = toolbarType.overflowMenuIconResourceId;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(toolbarType.overflowMenuResourceId);
        toolbar.setOverflowIcon(VectorDrawableCompat.create(fragment.getResources(), id, null));
        toolbar.setOnMenuItemClickListener(mOverflowMenuItemClickHandler);

        // Set up the navigation menu icon.
        toolbar.setNavigationIcon(toolbarType.navigationIconResourceId);
        MainActivity mainActivity = (MainActivity) fragment.getActivity();
        View.OnClickListener upHandler = mainActivity.getUpHandler();
        toolbar.setNavigationOnClickListener(upHandler);
    }

    // Private inner classes.

    /** Provide a class to post overflown menu item click events. */
    private class OverflowMenuItemHandler implements Toolbar.OnMenuItemClickListener {
        @Override public boolean onMenuItemClick(final MenuItem item) {
            AppEventManager.instance.post(new MenuItemEvent(item));
            return true;
        }
    }
}
