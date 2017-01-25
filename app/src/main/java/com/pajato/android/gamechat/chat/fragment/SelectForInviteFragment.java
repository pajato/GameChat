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

import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.ToolbarManager;

import java.util.List;

/**
 * Provide a fragment class used to choose groups and rooms to include in an invite.
 */

public class SelectForInviteFragment extends BaseChatFragment {
    // Private instance variables.

    /** The groups selected. */
    private List<Group> mGroups;

    /** The groups selected. */
    private List<Room> mRooms;

    @Override public void onStart() {
        // Establish the create type, the list type, setup the toolbar and turn off the access
        // control.
        super.onStart();
        ToolbarManager.instance.init(this);
//        FabManager.chat.setMenu(CHAT_GROUP_FAM_KEY, getGroupMenu());



    }

    /** Deal with the fragment's lifecycle by managing the progress bar and the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the app title only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu; initialize the ad view; and set up
        // the group list display.
        super.onResume();
//        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
//        FabManager.chat.init(this, CHAT_GROUP_FAM_KEY);
//        FabManager.chat.setVisibility(this, View.VISIBLE);
    }


}
