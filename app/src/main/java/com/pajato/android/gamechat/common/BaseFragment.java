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

package com.pajato.android.gamechat.common;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.ListAdapter;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.adapter.MenuItemEntry;
import com.pajato.android.gamechat.database.ExperienceManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.JoinManager;
import com.pajato.android.gamechat.database.MessageManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.AppEventManager;

import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.pajato.android.gamechat.common.FragmentType.messageList;
import static com.pajato.android.gamechat.common.adapter.MenuEntry.MENU_ITEM_NO_TINT_TYPE;
import static com.pajato.android.gamechat.common.adapter.MenuEntry.MENU_ITEM_TINT_TYPE;

/**
 * Provide a base class to support common artifacts shared between chat and game fragments, and to
 * support fragment lifecycle debugging.  All lifecycle events except for onViewCreate() are handled
 * by providing logcat tracing information.  The fragment manager is displayed in order to help
 * catch an elusive connected check failure.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseFragment extends Fragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseFragment.class.getSimpleName();

    // Public instance variables.

    /** The fragment type. */
    public FragmentType type;

    // Protected instance variables.

    /** The fragment active state; set when entering onResume and cleared in onPause. */
    protected boolean mActive;

    /** An ad view to be conditionally shown at the top of the view. */
    protected AdView mAdView;

    /** The item information passed from the parent fragment. */
    protected ListItem mItem;

    /** The persisted layout view for this fragment. */
    protected View mLayout;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseFragment() {}

    // Public instance methods.

    /** Return the toolbar being used by this fragment. */
    public Toolbar getToolbar() {
        return mLayout != null ? (Toolbar) mLayout.findViewById(R.id.toolbar) : null;
    }

    /** Return the, possibly null, toolbar type being used by this fragment. */
    public ToolbarManager.ToolbarType getToolbarType() {
        return type != null ? type.toolbarType : null;
    }

    @Override public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        logEvent("onActivityCreated", bundle);
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        logEvent("onAttach");
    }

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        logEvent("onCreate", bundle);
    }

    /** Handle the onCreateView lifecycle event. */
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                       final Bundle savedInstanceState) {
        // Determine if the layout exists and reuse it if so.
        logEvent("onCreateView", savedInstanceState);
        if (mLayout != null) return mLayout;

        // The layout does not exist.  Create and save it by initializing the fragment layout.
        mLayout = inflater.inflate(type.layoutResId, container, false);
        return mLayout;
    }

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        super.onDestroy();
        logEvent("onDestroy");
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        logEvent("onDestroyView");
    }

    @Override public void onDetach() {
        super.onDetach();
        logEvent("onDetach");
    }

    /** Log the lifecycle event, stop showing ads and turn off the app event bus. */
    @Override public void onPause() {
        super.onPause();
        logEvent("onPause");
        AppEventManager.instance.unregister(this);
        mActive = false;
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        // Log the event, handle ads and apply any queued adapter updates.  Only one try is
        // attempted.
        super.onResume();
        logEvent("onResume");
        AppEventManager.instance.register(this);
        mActive = true;
    }

    /** Provide a means to setup the fragment once it has been created. */
    public void onSetup(Context context, Dispatcher dispatcher) {
        if (!onDispatch(context, dispatcher)) {
            // The dispatch failed. Log it, toast it or some such.
            Log.d(TAG, "onDispatch failed ...");
        }
    }

    /** Log the lifecycle event. */
    @Override public void onStart() {
        super.onStart();
        logEvent("onStart");
    }

    /** Log the lifecycle event. */
    @Override public void onStop() {
        super.onStop();
        logEvent("onStop");
    }

    /** Log the lifecycle event. */
    @Override public void onViewStateRestored(Bundle bundle) {
        super.onViewStateRestored(bundle);
        logEvent("onViewStateRestored", bundle);
    }

    // Protected instance methods.

    /** Return a menu entry for a given title and icon id, and a given fragment type. */
    protected MenuEntry getEntry(final int titleId, final int iconId, final FragmentType type) {
        int ordinal = type.ordinal();
        return new MenuEntry(new MenuItemEntry(MENU_ITEM_NO_TINT_TYPE, titleId, iconId, ordinal));
    }

    /** Return -1 or the integer valud corresponding to the given string. */
    protected int getInt(final String value) {
        try {
            return value != null ? Integer.parseInt(value) : -1;
        } catch (NumberFormatException exc) {
            return -1;
        }
    }

    /** Return a menu entry for with given title and icon resource items. */
    protected MenuEntry getNoTintEntry(final int titleId, final int iconId) {
        return new MenuEntry(new MenuItemEntry(MENU_ITEM_NO_TINT_TYPE, titleId, iconId));
    }

    /** Return a menu entry for with given title and icon resource items. */
    protected MenuEntry getTintEntry(final int titleId, final int iconId) {
        return new MenuEntry(new MenuItemEntry(MENU_ITEM_TINT_TYPE, titleId, iconId));
    }

    /** Initialize the ad view by building and loading an ad request. */
    protected void initAdView(@NonNull final View layout) {
        mAdView = (AdView) layout.findViewById(R.id.adView);
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    /** Provide a logger to show the given message and the given bundle. */
    protected abstract void logEvent(String message, Bundle bundle);

    /** Provide a logger to show the given message. */
    protected abstract void logEvent(String message);

    /** Delegate the setup to the subclasses. */
    protected abstract boolean onDispatch(Context context, Dispatcher dispatcher);

    /** Provide a way to handle volunteer solicitations for unimplemented functions. */
    protected void showFutureFeatureMessage(final int resourceId) {
        // Post a toast message.
        Context context = getContext();
        String prefix = context.getString(resourceId);
        String suffix = context.getString(R.string.FutureFeature);
        CharSequence text = String.format(Locale.getDefault(), "%s %s", prefix, suffix);
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /** Return TRUE iff the list can be considered up to date. */
    protected boolean updateAdapterList() {
        // Determine if the fragment has a view and that it has a list type.  Abort if not,
        // otherwise ensure that the list adapter exists, creating it if necessary.
        View view = mLayout != null ? mLayout.findViewById(R.id.ItemList) : null;
        if (view == null)
            return false;
        RecyclerView recycler = (RecyclerView) view;
        RecyclerView.Adapter adapter = recycler.getAdapter();
        if (adapter == null) {
            // Initialize the recycler view.
            adapter = new ListAdapter();
            recycler.setAdapter(adapter);
            Context context = mLayout.getContext();
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
            recycler.setLayoutManager(layoutManager);
            recycler.setItemAnimator(new DefaultItemAnimator());
        }

        // Inject the list items into the recycler view making sure to scroll to the end of the
        // list when showing messages.
        ListAdapter listAdapter = (ListAdapter) adapter;
        listAdapter.clearItems();
        List<ListItem> items = getList(type, mItem);
        int size = items != null ? items.size() : 0;
        Log.d(TAG, String.format(Locale.US, "Updating with %d items.", size));
        listAdapter.addItems(items);
        if (type == messageList)
            recycler.scrollToPosition(listAdapter.getItemCount() - 1);
        return true;
    }

    /** Return null or a list to be displayed by a list adapter for a given fragment type. */
    private List<ListItem> getList(@NonNull final FragmentType type, final ListItem item) {
        switch (type) {
            case chatGroupList: // Get the data to be shown in a list of groups.
                return GroupManager.instance.getListItemData();
            case chatRoomList:  // Get the data to be show in a list of rooms.
                return RoomManager.instance.getListItemData(item.groupKey);
            case expGroupList:  // Get the groups with experiences.
                return ExperienceManager.instance.getListItemData();
            case experienceList:  // Get the groups with experiences.
                return ExperienceManager.instance.getListItemData(item);
            case joinRoom:      // Get the candidate list of rooms and members.
                return JoinManager.instance.getListItemData(item);
            case messageList:   // Get the data to be shown in a room.
                return MessageManager.instance.getListItemData(item);
            case selectExpGroupsRooms:
            case selectChatGroupsRooms: // Get the group and room selections.
                return InvitationManager.instance.getListItemData();
            case selectRoom:    // Get all the visible rooms for the current players.
                return PlayModeManager.instance.getListItemData(type);
            case selectUser:    // Get all the visible Users for the current account holder.
                return PlayModeManager.instance.getListItemData(type);
            default:
                return null;
        }
    }

}
