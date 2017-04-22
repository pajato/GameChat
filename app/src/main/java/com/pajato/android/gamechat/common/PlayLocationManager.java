/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.pajato.android.gamechat.common;

import android.app.Activity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.PlayLocationMenuAdapter;
import com.pajato.android.gamechat.common.adapter.PlayLocationMenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.exp.Experience;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.LinearLayout.VERTICAL;

public enum PlayLocationManager {
/**
 * Manages the game experience location: move a game to a public (or private) room. Plagiarized
 * heavily from PlayModeManager.
 */
    instance;

    // Protected instance variables

    /** The current play location menu for the experience being enjoyed. */
    private PopupWindow mPlayLocationPopupWindow;

    // Public instance methods.

    /** Close the play location popup window */
    public void closePlayLocationMenu() {
        mPlayLocationPopupWindow.dismiss();
    }

    /** Dismiss the play location menu if it is not null */
    public void dismissPlayLocationMenu() {
        if (mPlayLocationPopupWindow != null)
            mPlayLocationPopupWindow.dismiss();
    }

    /** Handle a selection in the play location menu */
    public void handlePlayLocationSelection(View view, BaseExperienceFragment fragment) {
        Object payload = view.getTag();
        if (payload == null || !(payload instanceof PlayLocationMenuEntry))
            return;
        PlayLocationMenuEntry entry = (PlayLocationMenuEntry) payload;
        if (entry.groupKeyList == null || entry.groupKeyList.size() == 0)
            return;
        // Move the experience to the selected room
        String roomKey = ((PlayLocationMenuEntry) payload).roomKey;
        Experience experience = fragment.getExperience();
        ListItem expListItem = new ListItem(experience);
        ExperienceManager.instance.move(experience, entry.groupKeyList.get(0), roomKey);
        ExperienceManager.instance.deleteExperience(expListItem);
        closePlayLocationMenu();
    }

    /**
     * Create and show the play location popup menu. The popup menu must be created with the anchor
     * in the current fragment layout, so it cannot be shared across fragments.
     */
    public void showPlayLocationMenu(Activity activity, View anchorView) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(R.id.gamePaneLayout);
        View popupLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playmode_menu_layout,
                viewGroup, false);
        RecyclerView recycler = (RecyclerView) popupLayout.findViewById(R.id.ItemList);
        if (recycler == null)
            return;

        // Initialize the recycler view.
        PlayLocationMenuAdapter adapter = new PlayLocationMenuAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(popupLayout.getContext(), VERTICAL, false));
        recycler.setItemAnimator(new DefaultItemAnimator());

        // Inject the list of users into the recycler view
        adapter.clearEntries();
        List<PlayLocationMenuEntry> menuEntries = getMenuItems();
        adapter.addEntries(menuEntries);
        adapter.notifyDataSetChanged();
        mPlayLocationPopupWindow = new PopupWindow(popupLayout, anchorView.getWidth(),
                RecyclerView.LayoutParams.WRAP_CONTENT);
        mPlayLocationPopupWindow.showAsDropDown(anchorView);
    }

    /** Toggle the display of the play location menu. */
    public void togglePlayLocationMenu(Activity activity, View anchorView) {
        if (mPlayLocationPopupWindow != null && mPlayLocationPopupWindow.isShowing())
            closePlayLocationMenu();
        else
            showPlayLocationMenu(activity, anchorView);
    }

    // Private instance methods.

    private List<PlayLocationMenuEntry> getMenuItems() {
        List<PlayLocationMenuEntry> result = new ArrayList<>();
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null)
            return result;

        Map<String, PlayLocationMenuEntry> roomMap = new HashMap<>();

        // Find all public rooms and add them to the menu
        for (String groupKey : account.joinMap.keySet()) {
            List<Room> rooms = RoomManager.instance.getRooms(groupKey, false);
            for (Room room : rooms) {
                if (room.type == Room.RoomType.PUBLIC) {
                    roomMap.put(room.key, new PlayLocationMenuEntry(room.name, groupKey, room.key));
                }
            }
        }
        result.addAll(roomMap.values());
        return result;
    }
}
