/*
 * Copyright (C) 2017 Pajato Technologies LLC.
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

import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.ProtectedUserChangeEvent;
import com.pajato.android.gamechat.event.ProtectedUserDeleteEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to manage the protected users associated with the current account.
 */

public class ManageProtectedUsersFragment extends BaseChatFragment {

    // Public class constants.

    /**
     * The lookup key for the FAB game home menu.
     */
    private static final String MANAGE_PROTECTED_USERS_FAM_KEY = "manageProtectedUsersFamKey";

    // Public instance methods.

    /**
     * Handle click events.
     */
    @Subscribe
    public void onClick(final ClickEvent event) {
        logEvent(String.format(Locale.US, "onClick (join rooms) event: {%s}.", event));
        if (event == null || event.view == null)
            return;

        // The event appears to be expected.  Confirm by finding the selector check view.
        switch (event.view.getId()) {
            default:
                processClickEvent(event.view, "manageProtectedUsers");
                break;
        }
    }

    /**
     * Process a FAM menu click event
     */
    @Subscribe
    public void onClick(final TagClickEvent event) {
        Object payload = event.view.getTag();
        if (payload == null || !(payload instanceof MenuEntry)) return;

        // The event represents a menu entry.  Close the FAM and case on the title id.
        FabManager.chat.dismissMenu(this);
        MenuEntry entry = (MenuEntry) payload;
        switch (entry.titleResId) {
            case R.string.CreateRestrictedUserTitle:
                if (AccountManager.instance.getCurrentAccount().chaperone != null) {
                    String protectedWarning = "Protected Users cannot make other Protected Users.";
                    Toast.makeText(getActivity(), protectedWarning, Toast.LENGTH_SHORT).show();
                    break;
                }
                AccountManager.instance.mChaperone = AccountManager.instance.getCurrentAccountId();
                FirebaseAuth.getInstance().signOut();
                AccountManager.instance.signIn(getContext());
                break;
            default:
                break;
        }

    }

    /** Handle protected user deleted events by updating the adapter */
    @Subscribe public void onProtectedUserDeleted(ProtectedUserDeleteEvent event) {
        if (!mActive)
            return;
        updateAdapterList();
    }

    /** Handle protected user changed events by updating the adapter */
    @Subscribe public void onProtectedUserChange(ProtectedUserChangeEvent event) {
        if (!mActive)
            return;
        updateAdapterList();
    }


    /** Deal with the fragment's lifecycle by managing the progress bar and the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the app title only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu.
        super.onResume();
        FabManager.chat.setImage(R.drawable.ic_add_white_24dp);
        FabManager.chat.init(this, MANAGE_PROTECTED_USERS_FAM_KEY);
        FabManager.chat.setVisibility(this, View.VISIBLE);
    }

    /** Set up toolbar and FAM */
    @Override public void onStart() {
        // Establish the create type, the list type, setup the toolbar and turn off the access
        // control.
        super.onStart();
        int titleResId = R.string.ProtectedUsersTitle;
        ToolbarManager.instance.init(this, titleResId, helpAndFeedback, settings);
        FabManager.chat.setMenu(MANAGE_PROTECTED_USERS_FAM_KEY, getSelectionMenu());
    }

    // Private instance methods.

    /** Get the FAM menu contents */
    private List<MenuEntry> getSelectionMenu() {
        final List<MenuEntry> menu = new ArrayList<>();
        menu.add(getTintEntry(R.string.CreateRestrictedUserTitle, R.drawable.ic_create_black_24dp));
        return menu;
    }

}