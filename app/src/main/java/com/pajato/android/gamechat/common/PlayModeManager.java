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
import android.content.Context;
import android.support.annotation.NonNull;
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
import com.pajato.android.gamechat.common.model.JoinState;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MemberManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.widget.LinearLayout.VERTICAL;
import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.resourceHeader;
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

    /** Identifies the types of play. */
    public enum PlayModeType {computer, local, user}

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

    /** Return null or a list of Users or rooms which the current user can access. */
    public List<ListItem> getListItemData(@NonNull final FragmentType type) {
        switch(type) {
            case selectRoom:
                return getUserItems();
            default:
                return null;
        }
    }

    /**
     * Create and show the play mode popup menu. The popup menu must be created with the anchor in
     * the current fragment layout, so it cannot be shared across fragments.
     */
    public void showPlayModeMenu(Activity activity, View anchorView) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(R.id.gamePaneLayout);
        View popupLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playmode_menu_layout,
                viewGroup, false);
        View view = popupLayout.findViewById(R.id.ItemList);
        if (view == null)
            return;
        RecyclerView recycler = (RecyclerView) view;

        // Initialize the recycler view.
        PlayModeMenuAdapter adapter = new PlayModeMenuAdapter();
        recycler.setAdapter(adapter);
        Context context = popupLayout.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
        recycler.setLayoutManager(layoutManager);
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
        for (String groupKey : account.joinMap.keySet()) {
            List<Account> accountList = MemberManager.instance.getMemberList(groupKey);
            for(Account member : accountList) {
                if (!account.id.equals(member.id))
                    result.add(new PlayModeMenuEntry(member.getDisplayName(), member.id, groupKey));
            }
        }
        return result;
    }

    /** Return a possibly empty list of Users the current User can access. */
    private List<ListItem> getUserItems() {
        List<ListItem> result = new ArrayList<>();
        Account account = AccountManager.instance.getCurrentAccount();
        if (!AccountManager.hasSelectableMembers(account))
            // No users exist so create a header stating this.
            result.add(new ListItem(resourceHeader, R.string.NoUsersFound));
        else
            for (Map.Entry<String, JoinState> entry : account.joinMap.entrySet()) {
                String groupKey = entry.getKey();
                for (Account member : MemberManager.instance.getMemberList(groupKey))
                    if (!member.id.equals(account.id)) {
                        String nickName = member.getNickName();
                        String name = String.format(Locale.US, "%s (%s)", nickName, member.email);
                        String text = GroupManager.instance.getGroupName(groupKey);
                        String url = member.url;
                        result.add(new ListItem(selectUser, groupKey, member.id, name, text, url));
                    }
            }
        return result;
    }
}
