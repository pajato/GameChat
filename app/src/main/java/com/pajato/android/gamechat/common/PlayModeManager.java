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

import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.UserItem;
import com.pajato.android.gamechat.common.model.Account;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.MemberManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the game experience modes: such as playing against a local friend (non-User), the
 * computer or another online User.
 *
 * @author Paul Reilly
 */
public enum PlayModeManager {
    instance;

    // Public enums.

    /** Identifies the types of play. */
    public enum PlayModeType {computer, local, user}

    // Public instance methods.

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

    // Private instance methods.

    /** Return a possibly empty list of Users the current User can access. */
    private List<ListItem> getUserItems() {
        List<ListItem> result = new ArrayList<>();
        Account account = AccountManager.instance.getCurrentAccount();
        if (account == null || account.joinList.size() == 0)
            return null;
        for (String groupKey : account.joinList)
            for (Account member : MemberManager.instance.getMemberList(groupKey))
                if (!member.id.equals(account.id))
                    result.add(new ListItem(new UserItem(groupKey, member)));
        return result;
    }
}
