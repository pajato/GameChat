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

package com.pajato.android.gamechat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.ChatListManager;
import com.pajato.android.gamechat.chat.adapter.ChatListAdapter;
import com.pajato.android.gamechat.chat.adapter.ChatListItem;

import java.util.List;

import static android.support.v7.widget.LinearLayoutCompat.VERTICAL;

/**
 * Provide a base class to support fragment lifecycle debugging.  All lifecycle events except for
 * onViewCreate() are handled by providing logcat tracing information.  The fragment manager is
 * displayed in order to help catch an elusive connected check failure.
 *
 * @author Paul Michael Reilly
 */
public class BaseFragment extends Fragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseFragment.class.getSimpleName();

    // Protected instance variables.

    /** Show an ad at the top of the view. */
    protected AdView mAdView;

    /** The item information passed from the parent fragment. */
    protected ChatListItem mItem;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseFragment() {}

    // Public instance methods.

    public void onActivityCreated(Bundle bundle) {
        String format = "onActivityCreated: The activity associated with fragment {%s} has been "
            + "created using bundle {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, bundle, getFragmentManager()));
        super.onActivityCreated(bundle);
    }

    @Override public void onAttach(Context context) {
        String format = "onAttach: Attaching fragment {%s} to activity with context {%s}. Fragment "
            + "manager: {%s}.";
        Log.v(TAG, String.format(format, this, context, getFragmentManager()));
        super.onAttach(context);
    }

    @Override public void onCreate(Bundle bundle) {
        String format = "onCreate: Creating fragment {%s} with bundle {%s}. Fragment manager: "
            + "{%s}.";
        Log.v(TAG, String.format(format, this, bundle, getFragmentManager()));
        super.onCreate(bundle);
    }

    @Override public void onDestroy() {
        // Log the lifecycle event to help during development.
        String format = "onDestroy: Destroying fragment {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));

        // Kill the ad view.
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override public void onDestroyView() {
        // Log the lifecycle event to help during development.
        String format = "onDestroyView: Destroying fragment {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onDestroyView();
    }

    @Override public void onDetach() {
        // Log the lifecycle event to help during development.
        String format = "onDetach: Detaching fragment {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onDetach();
    }

    @Override public void onPause() {
        // Log the lifecycle event to help during development.
        String format = "onPause: Fragment {%s} is no longer visible and running. Fragment manager: "
            + "{%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));

        // Pause the ad control.
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override public void onResume() {
        // Log the lifecycle event to help during development.
        String format = "onResume: Fragment {%s} is visible and running. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));

        // Resume the ad view.
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
    }

    @Override public void onStart() {
        // Log the lifecycle event to help during development.
        String format = "onStart: Make the fragment {%s} visible. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onStart();
    }

    @Override public void onStop() {
        // Log the lifecycle event to help during development.
        String format = "onStop: Fragment {%s} is no longer visible. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onStop();
    }

    @Override public void onViewStateRestored(Bundle bundle) {
        // Log the lifecycle event to help during development.
        String format = "onViewStateRestored: The saved state has been restored to fragment {%s} "
            + "using bundle {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, bundle, getFragmentManager()));
        super.onViewStateRestored(bundle);
    }

    /** Set the item defining this fragment (passed from the parent (spawning) fragment. */
    public void setItem(final ChatListItem item) {
        mItem = item;
    }

    // Protected instance methods.

    /** Initialize the fragment's list. */
    protected void initList(@NonNull final View layout, final List<ChatListItem> items,
                            final boolean stackFromEnd) {
        // Initialize the recycler view.
        Context context = layout.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.chatList);
        if (stackFromEnd) layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up the adapter on the recycler view with a set of default messages.
        ChatListAdapter adapter = new ChatListAdapter();
        adapter.addItems(items);
        recyclerView.setAdapter(adapter);
    }

    /** Set the title in the toolbar using the group name. */
    protected void setTitles(final String groupKey, final String roomKey) {
        // Ensure that the action bar exists.
        String title;
        String subtitle = null;
        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            // The action bar does exist, as expected.  Set the title and subtitle accordingly.
            if (groupKey == null && roomKey == null) {
                title = getResources().getString(R.string.app_name);
            } else if (groupKey != null && roomKey == null) {
                title = ChatListManager.instance.getGroupName(groupKey);
            } else if (groupKey == null) {
                title = ChatListManager.instance.getRoomName(roomKey);
            } else {
                title = ChatListManager.instance.getRoomName(roomKey);
                subtitle = ChatListManager.instance.getGroupName(groupKey);
            }

            // Apply the title and subtitle to the action bar.
            bar.setTitle(title);
            bar.setSubtitle(subtitle);
            return;
        }

        // The action bar does not exist!  Log the error.
        Log.e(TAG, "The action bar is not accessible in order to set the titles!");
    }

    /** Initialize the ad view by building and loading an ad request. */
    protected void initAdView(@NonNull final View layout) {
        mAdView = (AdView) layout.findViewById(R.id.adView);
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    /** Deal with the options menu by hiding the back button. */
    protected void setOptionsMenu(final Menu menu, final MenuInflater inflater, final int[] visible,
                                  final int[] gone) {
        // Ensure that the menu options has been inflated and make the specified items visible and
        // gone.
        if (!menu.hasVisibleItems()) inflater.inflate(R.menu.chat_menu_base, menu);
        if (visible != null) for (int itemId : visible) setItemState(menu, itemId, true);
        if (gone != null) for (int itemId : gone) setItemState(menu, itemId, false);
    }

    // Private instance methods.

    /** Make the given menu item either visible or invisible. */
    private void setItemState(final Menu menu, final int itemId, final boolean state) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) item.setVisible(state);
    }

}
