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
import android.widget.LinearLayout;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.chat.ChatManager;
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.database.DatabaseManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.pajato.android.gamechat.chat.adapter.ChatListItem.SELECTABLE_MEMBER_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.SELECTABLE_ROOM_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment.SelectionType.all;
import static com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment.SelectionType.members;
import static com.pajato.android.gamechat.chat.fragment.JoinRoomsFragment.SelectionType.rooms;

public class JoinRoomsFragment extends BaseChatFragment {

    public enum SelectionType {all, members, rooms}

    // Public constants.

    /** The lookup key for the FAB chat selection memu. */
    public static final String CHAT_SELECTION_FAM_KEY = "chatSelectionFamKey";

    // Private instance variables.

    /** A map of items specifying rooms or members to join. */
    private Map<String, ChatListItem> mJoinMap = new HashMap<>();

    // Public instance methods.

    /** Establish the layout file to show that the app is offline due to network loss. */
    @Override public int getLayout() {return R.layout.fragment_chat_join_rooms;}

    /** Provide a placeholder subscriber to satisfy the event bus contract. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Log the event and determine if the event looks right.  Abort if it doesn't.
        logEvent(String.format(Locale.US, "onClick (join rooms) event: {%s}.", event));
        if (event == null || event.view == null) return;

        // The event appears to be expected.  Confirm by finding the selector check view.
        switch (event.view.getId()) {
            case R.id.saveButton:
                // Implement the save operation.
                for (ChatListItem item : mJoinMap.values()) DatabaseManager.instance.joinRoom(item);
                ChatManager.instance.startNextFragment(getActivity());
                break;
            default:
                // Determine if the view might be a click on a list view row.  Abort if not, look
                // for a checkbox if so and if that is found, process the selection.
                if (!(event.view instanceof LinearLayout)) return;
                CheckBox checkBox = (CheckBox) event.view.findViewById(R.id.selectorCheck);
                if (checkBox != null) processSelection(event, checkBox);
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
    @Override public void onInitialize() {
        // Establish the list type and setup the toolbar.
        super.onInitialize();
        mItemListType = DatabaseListManager.ChatListType.joinRoom;
        initToolbar();
        FabManager.chat.setMenu(CHAT_SELECTION_FAM_KEY, getSelectionMenu());
    }

    /** Reset the FAM to use the game home menu. */
    @Override public void onResume() {
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_check_white_24dp);
        FabManager.chat.init(this);
        FabManager.chat.setVisibility(this, View.VISIBLE);
        FabManager.chat.setMenu(this, CHAT_SELECTION_FAM_KEY);
    }

    // Private instance methods.

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getSelectionMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.SelectAllMenuTitle, R.drawable.ic_done_all_black_24dp));
        menu.add(getTintEntry(R.string.SelectRoomsMenuTitle, R.drawable.vd_casino_black_24px));
        menu.add(getTintEntry(R.string.SelectMembersMenuTitle, R.drawable.vd_group_black_24px));
        menu.add(getTintEntry(R.string.ClearSelectionsMenuTitle, R.drawable.ic_clear_black_24dp));
        return menu;
    }

    /** Process a selection by toggling the selected state and managing the item map. */
    private void processSelection(@NonNull final ClickEvent event, @NonNull final CheckBox checkBox) {
        // Set the check icon visility and get the item object from the event payload.
        ChatListItem item = null;
        Object payload = event.view != null ? event.view.getTag() : null;
        if (payload != null && payload instanceof ChatListItem) item = (ChatListItem) payload;
        if (item == null) return;

        // Toggle the selection state and operate accordingly on the join map.
        item.selected = !item.selected;
        checkBox.setChecked(item.selected);
        if (item.selected) mJoinMap.put(item.key, item);
        else mJoinMap.remove(item.key);
        updateSaveButton();
    }

    /** ... */
    private void updateSelections(@NonNull final SelectionType type, final boolean state) {
        RecyclerView view = (RecyclerView) mLayout.findViewById(R.id.chatList);
        ChatListAdapter adapter = (ChatListAdapter) view.getAdapter();
        List<ChatListItem> itemList = adapter.getItems();
        for (ChatListItem item : itemList) {
            if (type == all || (type == members && item.type == SELECTABLE_MEMBER_ITEM_TYPE) ||
                    (type == rooms && item.type == SELECTABLE_ROOM_ITEM_TYPE))
                item.selected = state;
        }
        adapter.notifyDataSetChanged();

        // Update the join map based on the update arguments.
        mJoinMap.clear();
        for (ChatListItem item : itemList) if (item.selected) mJoinMap.put(item.key, item);
        updateSaveButton();
    }

    /** Update the save button state based on the current join map content. */
    private void updateSaveButton() {
        View saveButton = mLayout.findViewById(R.id.saveButton);
        saveButton.setEnabled(mJoinMap.size() > 0);
    }

}
