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

import android.content.res.Resources;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.chat.ChatManager;
import com.pajato.android.gamechat.database.DatabaseListManager;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

public class JoinRoomsFragment extends BaseChatFragment implements View.OnClickListener {

    // Public instance methods.

    /** Provide a click handler for aborting the fragment. */
    @Override public void onClick(final View view) {
        ChatManager.instance.startNextFragment(getActivity());
    }

    /** Provide a placeholder subscriber to satisfy the event bus contract. */
    @Subscribe public void onClick(final ClickEvent event) {
        // Use a logging placeholder.
        logEvent(String.format(Locale.US, "onClick (join rooms) event: {%s}.", event));
    }

    /** Establish the layout file to show that the app is offline due to network loss. */
    @Override public int getLayout() {return R.layout.fragment_chat_join_rooms;}

    /** Establish the create time state. */
    @Override public void onInitialize() {
        // Establish the list type and setup the toolbar.
        super.onInitialize();
        mItemListType = DatabaseListManager.ChatListType.joinRoom;
        Toolbar toolbar = (Toolbar) mLayout.findViewById(R.id.toolbar);
        toolbar.setTitle(getActivity().getString(R.string.JoinRoomsMenuTitle));
        toolbar.setNavigationIcon(R.drawable.vd_arrow_back_black_24px);
        toolbar.setNavigationOnClickListener(this);
        toolbar.inflateMenu(R.menu.add_group_menu);
        Resources resources = getResources();
        int id = R.drawable.vd_more_vert_black_24px;
        toolbar.setOverflowIcon(VectorDrawableCompat.create(resources, id, null));
        //ContactManager.instance.init(getContext());
    }

    /** Reset the FAM to use the game home menu. */
    @Override public void onResume() {
        super.onResume();
        setSubtitle();
    }

    // Private instance methods.

    /** Set the toolbar subtitle after ensuring that there is a value to use. */
    private void setSubtitle() {
        Toolbar toolbar = (Toolbar) mLayout.findViewById(R.id.toolbar);
        String key = mItem != null ? mItem.groupKey : null;
        String subtitle = key != null ? DatabaseListManager.instance.getGroupName(key) : null;
        if (subtitle != null) toolbar.setSubtitle(subtitle);
    }
}
