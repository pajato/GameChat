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
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pajato.android.gamechat.chat.adapter.ChatListItem.INVITE_COMMON_ROOM_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.INVITE_GROUP_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.INVITE_ROOM_ITEM_TYPE;
import static com.pajato.android.gamechat.common.DispatchManager.DispatcherKind.chat;

/**
 * Provide a fragment class used to choose groups and rooms to include in an invite.
 */

public class SelectForInviteFragment extends BaseChatFragment {

    // Public constants.

    /** The lookup key for the FAB chat selection memu. */
    public static final String INVITE_SELECTION_FAM_KEY = "inviteSelectionFamKey";

    // Private instance variables.

    /** The groups selected. */
    private Set<ChatListItem> mSelectedGroups = new HashSet<>();

    /** The groups selected. */
    private Set<ChatListItem> mSelectedRooms = new HashSet<>();

    // Public instance methods.

    /** Provide subscriber to listen for click events. */
    @Subscribe public void onClick(final ClickEvent event) {
        if (event == null || event.view == null)
            return;

        switch (event.view.getId()) {
            case R.id.inviteButton:
                // Handle the invitation
                InvitationManager.instance.extendAppInvitation(getActivity(), getSelections());
                mSelectedGroups.clear();
                mSelectedRooms.clear();
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
                updateSelections(true);
                break;
            case R.string.ClearSelectionsMenuTitle:
                updateSelections(false);
                break;
            default:
                break;

        }
    }

    @Override public void onStart() {
        // Establish the create type, the list type, setup the toolbar and turn off the access
        // control.
        super.onStart();
        ToolbarManager.instance.init(this);
        FabManager.chat.setMenu(INVITE_SELECTION_FAM_KEY, getSelectionMenu());
    }

    /** Deal with the fragment's lifecycle by managing the progress bar and the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the app title only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu.
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
        FabManager.chat.init(this);
        FabManager.chat.setVisibility(this, View.VISIBLE);

        // Clear selections - is this the correct place to do this (onResume)??????
        mSelectedGroups.clear();
        mSelectedRooms.clear();
    }

    // Private instance methods

    /** Return the home FAM used in the top level show games and show no games fragments. */
    private List<MenuEntry> getSelectionMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.SelectAllMenuTitle, R.drawable.ic_done_all_black_24dp));
        menu.add(getTintEntry(R.string.ClearSelectionsMenuTitle, R.drawable.ic_clear_black_24dp));
        return menu;
    }

    /** Process a selection */
    private void processSelection(@NonNull final ClickEvent event, @NonNull final CheckBox checkBox) {
        // Set the checkbox visibility and get the item object from the event payload.
        ChatListItem clickedItem = null;
        Object payload = event.view != null ? event.view.getTag() : null;
        if (payload != null && payload instanceof ChatListItem) clickedItem = (ChatListItem) payload;
        if (clickedItem == null) return;

        // Toggle the selection state and operate accordingly on the selected items lists.
        clickedItem.selected = !clickedItem.selected;
        checkBox.setChecked(clickedItem.selected);

        RecyclerView view = (RecyclerView) mLayout.findViewById(R.id.chatList);
        ChatListAdapter adapter = (ChatListAdapter) view.getAdapter();
        List <ChatListItem> adapterList = adapter.getItems();

        // If the item is a group, then if it's selected, also select it's common room. If it's
        // deselected, deselect all of it's rooms.
        if (clickedItem.type == INVITE_GROUP_ITEM_TYPE) {
            if (clickedItem.selected)
                mSelectedGroups.add(clickedItem);
            else
                mSelectedGroups.remove(clickedItem);

            for (ChatListItem adapterItem : adapterList) {
                switch (adapterItem.type) {
                    case INVITE_COMMON_ROOM_ITEM_TYPE:
                        if (adapterItem.groupKey.equals(clickedItem.key)) {
                            adapterItem.selected = clickedItem.selected;
                            if(clickedItem.selected) {
                                mSelectedRooms.add(adapterItem);
                            } else {
                                mSelectedRooms.remove(adapterItem);
                            }
                        }
                        break;
                    case INVITE_ROOM_ITEM_TYPE:
                        if (adapterItem.groupKey.equals(clickedItem.key) && !clickedItem.selected) {
                            adapterItem.selected = false;
                            mSelectedRooms.remove(adapterItem);
                        }
                        break;
                    default:
                        break;
                }
            }

        } else if (clickedItem.type == INVITE_ROOM_ITEM_TYPE) {
            // if the item is a room, and it's selection is enabled, make sure to also enable
            // the group and the group's common room.
            if (clickedItem.selected) {
                mSelectedRooms.add(clickedItem);
                for (ChatListItem adapterItem : adapterList) {
                    if (adapterItem.type == INVITE_GROUP_ITEM_TYPE &&
                            adapterItem.key.equals(clickedItem.groupKey)) {
                        mSelectedGroups.add(adapterItem);
                        adapterItem.selected = true;
                    }
                    else if (adapterItem.type == INVITE_COMMON_ROOM_ITEM_TYPE &&
                            adapterItem.groupKey.equals(clickedItem.groupKey)) {
                        adapterItem.selected = true;
                        mSelectedRooms.add(adapterItem);
                    }
                }
            } else {
                mSelectedRooms.remove(clickedItem);
            }
        }

        adapter.notifyDataSetChanged();

        // Set the 'invite' button enabled or disabled based on whether there are selections
        updateSendInviteButton();
    }

    /** Called from FAM click handling */
    private void updateSelections(final boolean state) {
        RecyclerView view = (RecyclerView) mLayout.findViewById(R.id.chatList);
        ChatListAdapter adapter = (ChatListAdapter) view.getAdapter();
        List <ChatListItem> itemList = adapter.getItems();

        // Select all or clear all items, depending on 'state'. Update all items in the adapter
        // and set group and room lists accordingly.
        mSelectedGroups.clear();
        mSelectedRooms.clear();

        for (ChatListItem item : itemList) {
            item.selected = state;
            switch (item.type) {
                case INVITE_GROUP_ITEM_TYPE:
                    if (state)
                        mSelectedGroups.add(item);
                    break;
                case INVITE_ROOM_ITEM_TYPE:
                case INVITE_COMMON_ROOM_ITEM_TYPE:
                    if (state)
                        mSelectedRooms.add(item);
                    break;
                default:
                    break;
            }
        }
        adapter.notifyDataSetChanged();

        // Set the 'invite' button enabled or disabled based on whether there are selections
        updateSendInviteButton();
    }

    /** Update the invite button state based on the current join map content. */
    private void updateSendInviteButton() {
        View inviteButton = mLayout.findViewById(R.id.inviteButton);
        inviteButton.setEnabled(mSelectedGroups.size() > 0 || mSelectedRooms.size() > 0);
    }

    private Map<String, List<String>> getSelections() {
        Map<String, List<String>> selections = new HashMap<>();
        for (ChatListItem groupItem : mSelectedGroups) {
            selections.put(groupItem.key, new ArrayList<String>());
        }
        for (ChatListItem roomItem : mSelectedRooms) {
            List<String> rooms = selections.get(roomItem.groupKey);
            rooms.add(roomItem.key);
            selections.put(roomItem.groupKey, rooms);
        }
        return selections;
    }

}
