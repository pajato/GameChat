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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.MenuAdapter;
import com.pajato.android.gamechat.common.adapter.MenuEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.pajato.android.gamechat.common.FabManager.State.opened;


/** Provide a singleton to manage the rooms panel fab button. */
public enum FabManager {
    chat(R.id.chatFab, R.id.chatFam, R.id.chatDimmer),
    game(R.id.gameFab, R.id.gameFam, R.id.gameDimmer);

    /** Provide FAB state constants. */
    public enum State {opened, closed}

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = FabManager.class.getSimpleName();

    // Private instance variables.

    /** The fab resource identifier. */
    private int mFabId;

    /** The fab menu resource identifier. */
    private int mFabMenuId;

    /** The resource id used to access the dimmer view used to blur the content. */
    private int mFabDimmerId;

    /** The FAB image resource id. */
    private int mImageResourceId;

    /** The cache of menus (FAM) supported by the this manager. */
    private Map<String, List<MenuEntry>> mMenuMap = new HashMap<>();

    /** The resource id used to access the main fragment. */
    private String mTag;

    // Sole Constructor.

    /** Build the instance with the given resource ids. */
    FabManager(final int fabId, final int fabMenuId, final int fabDimmerId) {
        mFabId = fabId;
        mFabMenuId = fabMenuId;
        mFabDimmerId = fabDimmerId;
        mImageResourceId = R.drawable.ic_add_white_24dp;
    }

    // Public instance methods

    /** Dismiss the menu associated with the given FAB button. */
    public void dismissMenu(@NonNull final Fragment fragment) {
        // Determine if the chat fragment is accessible.  If not, abort.
        View layout = getFragmentLayout(fragment);
        if (layout != null) dismissMenu(layout);
    }

    /** Ensure that the FAb is not being shown. */
    public void hide(@NonNull final Fragment fragment) {
        View layout = getFragmentLayout(fragment);
        if (layout == null) return;
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setVisibility(View.GONE);
    }

    /** Initialize the fab state. */
    public void init(final Fragment fragment) {
        // Ensure that the layout exists. Abort if it does not.  Set the FAB state to closed if it
        // does.
        View layout = getFragmentLayout(fragment);
        if (layout == null) return;
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setTag(R.integer.fabStateKey, opened);
        dismissMenu(layout);
    }

    /** Set the floating action menu (FAM) with a given name and menu. */
    public void setMenu(final String name, final List<MenuEntry> menu) {
        // Cache the menu, if one is provided.
        if (menu != null && name != null) mMenuMap.put(name, menu);
    }

    /** Set the current FAM using the cached item with the given name. */
    public void setMenu(final Fragment fragment, final String name) {
        // Ensure that the game fragment layout is accessible.  Abort if not (error is logged).
        View layout = getFragmentLayout(fragment);
        if (layout == null) return;

        // Add the menu to initialize the recycler view.
        Context context = fragment.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.MenuList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Ensure that the recycler view has an adapter installed.  Install one if not.  Set up the
        // adapter on the recycler view with the given menu.
        MenuAdapter adapter = (MenuAdapter) recyclerView.getAdapter();
        if (adapter == null) {
            adapter = new MenuAdapter();
            recyclerView.setAdapter(adapter);
        }
        adapter.clearEntries();
        adapter.addEntries(mMenuMap.get(name));
    }

    /** Set the FAB state. */
    public void setVisibility(@NonNull final Fragment fragment, final int state) {
        // Obtain the layout view owning the FAB.  If it is not accessible, just return since an
        // error message will have been generated.  If it is accessible, apply the given visibility
        // state.
        View layout = getFragmentLayout(fragment);
        if (layout == null) return;
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setVisibility(state);
    }

    /** Set the FAB state. */
    public void setImage(final int resourceId) {
        // Set the image resource id to be used going forward.
        mImageResourceId = resourceId;
    }

    /** Initialize the tag used to find the main fragment that contains the fab button. */
    public void setTag(final String fragmentTag) {
        // Use the fragment tag string to find the main fragment and then in turn, the FAB.
        mTag = fragmentTag;
    }

    /** Ensure that the FAb is being shown. */
    public void show(@NonNull final Fragment fragment) {
        View layout = getFragmentLayout(fragment);
        if (layout == null) return;
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setVisibility(View.VISIBLE);
    }

    /** Toggle the state of the FAB button using a given fragment to obtain the layout view. */
    public void toggle(@NonNull final Fragment fragment) {
        // Determine if the fragment layout exists.  Continue if it does.  Return if it does not.
        // An error message with stack trace will have been generated if the view cannot be
        // accessed.
        View layout = getFragmentLayout(fragment);
        if (layout == null) return;

        // The layout view is valid.  Use it to toggle the fab state.
        View dimmerView = layout.findViewById(mFabDimmerId);
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        Object payload = fab.getTag(R.integer.fabStateKey);
        if (payload instanceof State) {
            // It does.  Toggle it by casing on the value to show and hide the relevant views.
            State value = (State) payload;
            String format = "Toggle the FAB/FAM state: {%s}.";
            Log.d(TAG, String.format(Locale.US, format, value));
            switch (value) {
                case opened:
                    // The FAB is showing 'X' and it's menu is visible.  Set the icon to '+', close
                    // the menu and undim the frame.
                    dismissMenu(layout);
                    dimmerView.setVisibility(View.GONE);
                    break;
                case closed:
                    // The FAB is showing '+' and the menu is not visible.  Set the icon to X and
                    // open the menu.
                    fab.setImageResource(R.drawable.ic_clear_white_24dp);
                    fab.setTag(R.integer.fabStateKey, opened);
                    dimmerView.setVisibility(View.VISIBLE);
                    View menu = layout.findViewById(mFabMenuId);
                    menu.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    // Private instance methods.

    /** Dismiss the menu associated with the given layout view. */
    private void dismissMenu(@NonNull final View layout) {
        // The fragment is accessible and the layout has been established.  Dismiss the FAM.
        View dimmerView = layout.findViewById(mFabDimmerId);
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setTag(R.integer.fabStateKey, State.closed);
        fab.setImageResource(mImageResourceId);
        View menu = layout.findViewById(mFabMenuId);
        menu.setVisibility(View.GONE);
        dimmerView.setVisibility(View.GONE);
    }

    /** Return the main envelope game fragment's layout view, if one exists, otherwise null. */
    private View getFragmentLayout(@NonNull final Fragment fragment) {
        // Determine if the given fragment has an associated activity.  Abort if not and log an
        // error.
        FragmentActivity activity = fragment.getActivity();
        String format = "The fragment {%s} does not have an activity attached!";
        if (activity == null) return logError(format, fragment);

        // Determine if the attached fragment has a view.  Abort if not and log an error.
        Fragment envelopeFragment = activity.getSupportFragmentManager().findFragmentByTag(mTag);
        format = "The envelope fragment {%s} does not have a layout view!";
        if (envelopeFragment == null || envelopeFragment.getView() == null)
            return logError(format, envelopeFragment);

        // There is a layout view to return.
        return envelopeFragment.getView();
    }

    /** Log an error and return a null view. */
    private View logError(final String format, final Fragment fragment) {
        // The activity is not accessible.  This is probably caused by a software error, in this
        // case, it is likely that an app event subscribed handler has invoked this call
        // inappropriately.  To that end, a stack trace is being appended to the error message
        // being logged.
        Throwable stack = new Throwable();
        Log.e(TAG, String.format(Locale.US, format, fragment), stack);
        return null;
    }

}
