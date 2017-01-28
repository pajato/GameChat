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

package com.pajato.android.gamechat.chat.fragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListAdapter;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.JoinManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment.SelectionType.all;
import static com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment.SelectionType.members;
import static com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment.SelectionType.rooms;
import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.selectableMember;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.selectableRoom;

public class JoinRoomsFragment extends BaseChatFragment {

    enum SelectionType {all, members, rooms}

    // Public constants.

    /** The lookup key for the FAB chat selection memu. */
    public static final String CHAT_SELECTION_FAM_KEY = "chatSelectionFamKey";

    // Private instance variables.

    /** A map of items specifying rooms or members to join. */
    private Map<String, ListItem> mJoinMap = new HashMap<>();

    // Public instance methods.

    /** Provide a placeholder subscriber to satisfy the event bus contract. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Log the event and determine if the event looks right.  Abort if it doesn't.
        logEvent(String.format(Locale.US, "onClick (join rooms) event: {%s}.", event));
        if (event == null || event.view == null)
            return;

        // The event appears to be expected.  Confirm by finding the selector check view.
        switch (event.view.getId()) {
            case R.id.saveButton:
                // Implement the save operation.
                for (ListItem item : mJoinMap.values())
                    JoinManager.instance.joinRoom(item);
                mJoinMap.clear();
                DispatchManager.instance.startNextFragment(getActivity(), chat);
                break;
            case R.id.selectorCheck:
                processSelection(event, (CheckBox) event.view);
                break;
            default:
                break;
        }
    }

    /** Process a menu click event ... */
    @Subscribe public void onClick(final TagClickEvent event) {
        Object payload = event.view.getTag();
        if (payload == null || !(payload instanceof MenuEntry)) return;

        // The event represents a menu entry.  Close the FAM and case on the title id.
        FabManager.chat.dismissMenu(this);
        MenuEntry entry = (MenuEntry) payload;
        switch (entry.titleResId) {
            case R.string.SelectAllMenuTitle:
                updateSelections(all, true);
                break;
            case R.string.SelectMembersMenuTitle:
                updateSelections(members, true);
                break;
            case R.string.SelectRoomsMenuTitle:
                updateSelections(rooms, true);
                break;
            case R.string.ClearSelectionsMenuTitle:
                updateSelections(all, false);
                break;
            default:
                // ...
                break;
        }
    }

    /** Establish the create time state. */
    @Override public void onStart() {
        // Establish the list type and setup the toolbar.
        super.onStart();
        ToolbarManager.instance.init(this);
        FabManager.chat.setMenu(CHAT_SELECTION_FAM_KEY, getSelectionMenu());
    }

    /** Reset the FAM to use the game home menu. */
    @Override public void onResume() {
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_check_white_24dp);
        FabManager.chat.init(this);
        FabManager.chat.setVisibility(this, View.VISIBLE);
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getSelectionMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.SelectAllMenuTitle, R.drawable.ic_done_all_black_24dp));
        menu.add(getTintEntry(R.string.SelectRoomsMenuTitle, R.drawable.ic_casino_black_24dp));
        menu.add(getTintEntry(R.string.SelectMembersMenuTitle, R.drawable.vd_group_black_24px));
        menu.add(getTintEntry(R.string.ClearSelectionsMenuTitle, R.drawable.ic_clear_black_24dp));
        return menu;
    }

    /** Process a selection by toggling the selected state and managing the item map. */
    private void processSelection(@NonNull final ClickEvent event, @NonNull final CheckBox checkBox) {
        // Set the check icon visibility and get the item object from the event payload.
        ListItem item = null;
        Object payload = event.view != null ? event.view.getTag() : null;
        if (payload != null && payload instanceof ListItem)
            item = (ListItem) payload;
        if (item == null)
            return;

        // Toggle the selection state and operate accordingly on the join map.
        item.selected = !item.selected;
        checkBox.setChecked(item.selected);
        if (item.selected)
            mJoinMap.put(item.key, item);
        else
            mJoinMap.remove(item.key);
        updateSaveButton();
    }

    /** ... */
    private void updateSelections(@NonNull final SelectionType type, final boolean state) {
        RecyclerView view = (RecyclerView) mLayout.findViewById(R.id.chatList);
        ListAdapter adapter = (ListAdapter) view.getAdapter();
        List<ListItem> itemList = adapter.getItems();
        for (ListItem item : itemList) {
            if (type == all || (type == members && item.type == selectableMember) ||
                    (type == rooms && item.type == selectableRoom))
                item.selected = state;
        }
        adapter.notifyDataSetChanged();

        // Update the join map based on the update arguments.
        mJoinMap.clear();
        for (ListItem item : itemList)
            if (item.selected)
                mJoinMap.put(item.key, item);
        updateSaveButton();
    }

    /** Update the save button state based on the current join map content. */
    private void updateSaveButton() {
        View saveButton = mLayout.findViewById(R.id.saveButton);
        saveButton.setEnabled(mJoinMap.size() > 0);
    }

}
