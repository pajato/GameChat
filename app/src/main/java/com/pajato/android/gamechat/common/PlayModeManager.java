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
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.PlayModeMenuAdapter;
import com.pajato.android.gamechat.common.adapter.PlayModeMenuEntry;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.JoinManager;
import com.pajato.android.gamechat.database.MemberManager;
import com.pajato.android.gamechat.exp.BaseExperienceFragment;
import com.pajato.android.gamechat.exp.Experience;
import com.pajato.android.gamechat.exp.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.widget.LinearLayout.VERTICAL;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.selectUser;

/**
 * Manages the game experience modes: such as playing against a local friend (non-User), the
 * computer or another online User.  Also manages the play mode menu and related events and
 * listeners.
 *
 * @author Paul Reilly
 */
public enum PlayModeManager {
    instance;

    // Public instance variables

    // Protected instance variables

    /** The current play mode menu for the experience being enjoyed. */
    private PopupWindow mPlayModePopupWindow;

    // Public instance methods.

    /** Close the play-mode popup window */
    public void closePlayModeMenu() {
        mPlayModePopupWindow.dismiss();
    }

    /** Dismiss the play mode menu if it is not null */
    public void dismissPlayModeMenu() {
        if (mPlayModePopupWindow != null)
            mPlayModePopupWindow.dismiss();
    }

    /** Handle a use selection in the play mode menu */
    public void handlePlayModeUserSelection(View view, BaseExperienceFragment fragment) {
        Object payload = view.getTag();
        if (payload == null || !(payload instanceof PlayModeMenuEntry))
            return;
        PlayModeMenuEntry entry = (PlayModeMenuEntry) payload;
        if (entry.groupKeyList == null || entry.groupKeyList.size() == 0)
            return;
        Account member = MemberManager.instance.getMember(entry.groupKeyList.get(0), entry.accountKey);
        if (member == null)
            return;
        // Handle selecting another User by moving the experience to a new room with an appropriate
        // name, and continuing the game in that room with the current state.
        String userName = String.format(Locale.US, "%s (%s)", member.getNickName(), member.email);
        ListItem selectUserListItem = new ListItem(selectUser, entry.groupKeyList.get(0),
                entry.accountKey, userName, GroupManager.instance.getGroupName(entry.groupKeyList.get(0)),
                member.url);
        Experience experience = fragment.getExperience();
        // Create a list item before 'experience' is modified so we don't delete the new experience.
        ListItem expListItem = new ListItem(experience);
        JoinManager.instance.joinRoom(selectUserListItem);
        for (Player p : experience.getPlayers()) {
            if (p.id == null) {
                p.id = member.id;
                p.name = member.getNickName();
                break;
            }
        }
        experience.setName(fragment.createTwoPlayerName(experience.getPlayers(),
                experience.getCreateTime()));
        ExperienceManager.instance.move(experience, entry.groupKeyList.get(0), selectUserListItem.roomKey);
        ExperienceManager.instance.deleteExperience(expListItem);
        PlayModeManager.instance.closePlayModeMenu();
    }

    /**
     * Create and show the play mode popup menu. The popup menu must be created with the anchor in
     * the current fragment layout, so it cannot be shared across fragments.
     */
    public void showPlayModeMenu(Activity activity, View anchorView) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(R.id.gamePaneLayout);
        View popupLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playmode_menu_layout,
                viewGroup, false);
        RecyclerView recycler = (RecyclerView) popupLayout.findViewById(R.id.ItemList);
        if (recycler == null)
            return;

        // Initialize the recycler view.
        PlayModeMenuAdapter adapter = new PlayModeMenuAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(popupLayout.getContext(), VERTICAL, false));
        recycler.setItemAnimator(new DefaultItemAnimator());

        // Inject the list of users into the recycler view
        adapter.clearEntries();
        List<PlayModeMenuEntry> menuEntries = getMenuItems(activity);
        adapter.addEntries(menuEntries);
        adapter.notifyDataSetChanged();
        mPlayModePopupWindow = new PopupWindow(popupLayout, RecyclerView.LayoutParams.WRAP_CONTENT, // anchorView.getWidth(),
                RecyclerView.LayoutParams.WRAP_CONTENT);
        mPlayModePopupWindow.showAsDropDown(anchorView);
    }

    /** Toggle the display of the play mode menu. */
    public void togglePlayModeMenu(Activity activity, View anchorView) {
        if (mPlayModePopupWindow != null && mPlayModePopupWindow.isShowing())
            closePlayModeMenu();
        else
            showPlayModeMenu(activity, anchorView);
    }

    // Private instance methods.

    private List<PlayModeMenuEntry> getMenuItems(Activity activity) {
        List<PlayModeMenuEntry> result = new ArrayList<>();
        result.add(new PlayModeMenuEntry(activity.getString(R.string.PlayModeLocalMenuTitle), null, null));
        result.add(new PlayModeMenuEntry(activity.getString(R.string.PlayModeComputerMenuTitle), null, null));

        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null)
            return result;

        Map<String, PlayModeMenuEntry> memberMap = new HashMap<>();
        for (String groupKey : account.joinMap.keySet()) {
            List<Account> accountList = MemberManager.instance.getMemberList(groupKey);
            for(Account member : accountList) {
                if (account.id.equals(member.id))
                    continue;

                // If a member exists in more than one group, only add that member once
                if (memberMap.containsKey(member.id)) {
                    PlayModeMenuEntry entry = memberMap.get(member.id);
                    entry.groupKeyList.add(groupKey);
                } else
                    memberMap.put(member.id, new PlayModeMenuEntry(member.getDisplayName(),
                            member.id, groupKey));
            }
        }
        result.addAll(memberMap.values());
        return result;
    }
}
