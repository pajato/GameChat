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

import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.MemberManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.PlayModeChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    /** The current play mode popup menu for the experience being enjoyed. */
    private PopupMenu mPlayModePopup;

    // Private  instance variables

    /** The listener for menu item clicks on the play mode popup menu */
    private PlayModeClickListener mListener = new PlayModeClickListener();

    // Public instance methods.

    /** Dismiss the play mode menu if it is not null */
    public void dismissPlayModeMenu() {
        if (mPlayModePopup != null)
            mPlayModePopup.dismiss();
    }

    /** Return null or a list of Users or rooms which the current user can access. */
    public List<ListItem> getListItemData(@NonNull final FragmentType type) {
        switch(type) {
            case selectRoom:
            case selectUser:
                return getUserItems();
            default:
                return null;
        }
    }

    /**
     * Create and show the play mode popup menu. The popup menu must be created with the anchor in
     * the current fragment layout, so it cannot be shared across fragments.
     */
    public void showPlayModeMenu(View anchorView) {
        mPlayModePopup = new PopupMenu(anchorView.getContext(), anchorView);
        mPlayModePopup.getMenuInflater().inflate(R.menu.player2_menu, mPlayModePopup.getMenu());
        mPlayModePopup.setOnMenuItemClickListener(mListener);
        mPlayModePopup.show();
    }

    // Private instance methods.

    /** Return a possibly empty list of Users the current User can access. */
    private List<ListItem> getUserItems() {
        List<ListItem> result = new ArrayList<>();
        Account account = AccountManager.instance.getCurrentAccount();
        if (!AccountManager.accountHasFriends(account))
            // No users exist so create a header stating this.
            result.add(new ListItem(resourceHeader, R.string.NoFriendsFound));
        else
            for (String groupKey : account.joinList)
                for (Account member : MemberManager.instance.getMemberList(groupKey))
                    if (!member.id.equals(account.id)) {
                        String nickName = member.getNickName();
                        String name = String.format(Locale.US, "%s (%s)", nickName, member.email);
                        String text = GroupManager.instance.getGroupName(groupKey);
                        String url = member.url;
                        result.add(new ListItem(selectUser, groupKey, member.id, name, text, url));
                    }
        return result;
    }

    /** Menu item click listener for play-mode menu items */
    private class PlayModeClickListener implements PopupMenu.OnMenuItemClickListener {
        /** Just dispatch an event to any listeners */
        public boolean onMenuItemClick(MenuItem item) {
            PlayModeChangeEvent event;
            switch (item.getItemId()) {
                case R.id.playComputer:
                    event = new PlayModeChangeEvent(PlayModeType.computer);
                    break;
                case R.id.playUser:
                    event = new PlayModeChangeEvent(PlayModeType.user);
                    break;
                case R.id.playLocal:
                default:
                    event = new PlayModeChangeEvent(PlayModeType.local);
                    break;
            }
            AppEventManager.instance.post(event);
            return true;
        }
    }
}
