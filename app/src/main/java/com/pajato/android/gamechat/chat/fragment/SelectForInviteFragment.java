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
import com.pajato.android.gamechat.common.InvitationManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.ListAdapter;
import com.pajato.android.gamechat.common.adapter.ListItem;
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

import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.inviteCommonRoom;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.inviteGroup;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.inviteRoom;
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
    private Set<ListItem> mSelectedGroups = new HashSet<>();

    /** The groups selected. */
    private Set<ListItem> mSelectedRooms = new HashSet<>();

    // Public instance methods.

    /** Provide subscriber to listen for click events. */
    @Subscribe public void onClick(final ClickEvent event) {
        if (event == null || event.view == null)
            return;

        switch (event.view.getId()) {
            case R.id.inviteButton:
                // Handle the invitation
                InvitationManager.instance.extendInvitation(getActivity(), getSelections());
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

    /** When a group is selected, find it's common room and insure that it is also selected in
     * the adapter list and add it to the list of selected rooms. */
    private void selectGroupForInvite(ListItem groupItem, List<ListItem> adapterList) {
        mSelectedGroups.add(groupItem);
        for (ListItem adapterItem : adapterList) {
            if (adapterItem.type == inviteCommonRoom &&
                    adapterItem.groupKey.equals(groupItem.groupKey)) {
                adapterItem.selected = true;
                mSelectedRooms.add(adapterItem);
            }
        }
    }

    /** When a group is deselected, also deselect all of it's rooms (including the common room). */
    private void deselectGroupForInvite(ListItem groupItem, List<ListItem> adapterList) {
        mSelectedGroups.remove(groupItem);
        for (ListItem adapterItem : adapterList) {
            if ((adapterItem.type == inviteCommonRoom ||
                    adapterItem.type == inviteRoom) &&
                    adapterItem.groupKey.equals(groupItem.groupKey)) {
                adapterItem.selected = false;
                mSelectedRooms.remove(adapterItem);
            }
        }
    }

    /** When a non-common room is selected, make sure its group and common room are also selected
     * and added to the respective list of selected groups/rooms */
    private void selectRoomForInvite(ListItem groupItem, List<ListItem> adapterList) {
        mSelectedRooms.add(groupItem);
        for (ListItem adapterItem : adapterList) {
            if (adapterItem.type == inviteGroup && adapterItem.key.equals(groupItem.groupKey)) {
                adapterItem.selected = true;
                mSelectedGroups.add(adapterItem);
            } else if (adapterItem.type == inviteCommonRoom &&
                    adapterItem.groupKey.equals(groupItem.groupKey)) {
                adapterItem.selected = true;
                mSelectedRooms.add(adapterItem);
            }
        }
    }

    /** Process a selection */
    private void processSelection(@NonNull final ClickEvent event, @NonNull final CheckBox checkBox) {
        // Set the checkbox visibility and get the item object from the event payload.
        ListItem clickedItem = null;
        Object payload = event.view != null ? event.view.getTag() : null;
        if (payload != null && payload instanceof ListItem) clickedItem = (ListItem) payload;
        if (clickedItem == null) return;

        // Toggle the selection state and operate accordingly on the selected items lists.
        clickedItem.selected = !clickedItem.selected;
        checkBox.setChecked(clickedItem.selected);

        RecyclerView view = (RecyclerView) mLayout.findViewById(R.id.chatList);
        ListAdapter adapter = (ListAdapter) view.getAdapter();
        List <ListItem> adapterList = adapter.getItems();

        // If the item is a group, then if it's selected, also select it's common room. If it's
        // deselected, deselect all of it's rooms.
        if (clickedItem.type == inviteGroup) {
            if (clickedItem.selected) {
                selectGroupForInvite(clickedItem, adapterList);
            }
            else {
                deselectGroupForInvite(clickedItem, adapterList);
            }
        } else if (clickedItem.type == inviteRoom) {
            if (clickedItem.selected) {
                selectRoomForInvite(clickedItem, adapterList);
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
        ListAdapter adapter = (ListAdapter) view.getAdapter();
        List <ListItem> itemList = adapter.getItems();

        // Select all or clear all items, depending on 'state'. Update all items in the adapter
        // and set group and room lists accordingly.
        mSelectedGroups.clear();
        mSelectedRooms.clear();

        for (ListItem item : itemList) {
            item.selected = state;
            switch (item.type) {
                case inviteGroup:
                    if (state)
                        mSelectedGroups.add(item);
                    break;
                case inviteRoom:
                case inviteCommonRoom:
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
        for (ListItem groupItem : mSelectedGroups) {
            selections.put(groupItem.key, new ArrayList<String>());
        }
        for (ListItem roomItem : mSelectedRooms) {
            List<String> rooms = selections.get(roomItem.groupKey);
            rooms.add(roomItem.key);
            selections.put(roomItem.groupKey, rooms);
        }
        return selections;
    }

}
