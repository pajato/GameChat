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

import static android.support.design.widget.FloatingActionButton.SIZE_MINI;
import static android.support.design.widget.FloatingActionButton.SIZE_NORMAL;
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

    /** The name of the default menu. */
    private String mDefaultMenuName;

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
        // Determine if the chat fragment is accessible.  If so, dismiss the FAM.
        View layout = getFragmentLayout(fragment);
        if (layout != null) dismissMenu(fragment, layout);
    }

    /** Initialize the FAB. Use small FAB size for game fragments; all others use normal size. */
    public void init(@NonNull final BaseFragment fragment) {
        // Ensure that the layout and the recycler views exist. Abort quietly if they do not.
        View layout = getFragmentLayout(fragment);
        RecyclerView recyclerView;
        recyclerView = layout != null ? (RecyclerView) layout.findViewById(R.id.MenuList) : null;
        if (layout == null || recyclerView == null)
            return;

        // Set up the recycler view by establishing a layout manager, item animator and adapter.
        Context context = fragment.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context, VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new MenuAdapter());

        // Set the FAB state to closed by assuming an open FAM and dismissing it.
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setTag(R.integer.fabStateKey, opened);
        fab.setVisibility(View.VISIBLE);
        switch (fragment.type) {
            case chess:
            case checkers:
            case tictactoe:
                fab.setSize(SIZE_MINI);
                setImage(R.drawable.ic_refresh_white_24dp);
                break;
            default:
                fab.setSize(SIZE_NORMAL);
                break;
        }
        dismissMenu(fragment, layout);
    }

    /** Initialize to use the given fragment and FAM. */
    public void init(@NonNull final BaseFragment fragment, final String name) {
        this.init(fragment);
        mDefaultMenuName = name;
    }

    /** Set the named floating action menu (FAM) making it the default. */
    public void setMenu(@NonNull final String name, @NonNull final List<MenuEntry> menu) {
        // Test for a reasonable (non-empty) name.  Abort if not.
        if (name.length() == 0) return;

        // Cache the menu and make it the default.
        mMenuMap.put(name, menu);
        mDefaultMenuName = name;
    }

    /** Set the FAB state. */
    public void setVisibility(@NonNull final Fragment fragment, final int state) {
        // Obtain the layout view owning the FAB.  If it is not accessible, just return since an
        // error message will have been generated.  If it is accessible, apply the given visibility
        // state.
        View layout = getFragmentLayout(fragment);
        if (layout == null)
            return;
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

    /** Ensure that the FAB is being shown. */
    public void show(@NonNull final Fragment fragment) {
        if (fragment instanceof BaseFragment && !((BaseFragment) fragment).mActive)
            return;
        View layout = getFragmentLayout(fragment);
        if (layout == null)
            return;
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setVisibility(View.VISIBLE);
    }

    /** Toggle the state of the FAB button for the given fragment, use the given menu once. */
    public void toggle(@NonNull final Fragment fragment) {
        toggle(fragment, mDefaultMenuName);
    }

    /** Toggle the state of the FAB/FAM for the given fragment using the given menu. */
    public void toggle(@NonNull final Fragment fragment, @NonNull final String name) {
        // Determine if the fragment layout exists.  Continue if it does.  Return if it does not.
        // An error message with stack trace will have been generated if the view cannot be
        // accessed.
        FloatingActionButton fab;
        View layout = getFragmentLayout(fragment);
        fab = layout != null ? (FloatingActionButton) layout.findViewById(mFabId) : null;
        Object payload = fab != null ? fab.getTag(R.integer.fabStateKey) : null;
        if (fab == null || !(payload instanceof State))
            return;

        // The layout view is valid and the payload is of type State.  Toggle the fab state by
        // casing on the value to show and hide the relevant views.
        View dimmerView = layout.findViewById(mFabDimmerId);
        State value = (State) payload;
        String format = "Toggle the FAB/FAM state: {%s}.";
        Log.d(TAG, String.format(Locale.US, format, value));
        switch (value) {
            case opened:
                // The FAB is showing 'X' and it's menu is visible.  Set the icon to '+', close
                // the menu and un-dim the frame.
                dismissMenu(fragment, layout);
                break;
            case closed:
                // The FAB is showing '+' and the menu is not visible.  Set the icon to X and
                // open the named menu (if name is set) or the last one used.
                setMenu(fragment, name);
                fab.setImageResource(R.drawable.ic_clear_white_24dp);
                fab.setTag(R.integer.fabStateKey, opened);
                dimmerView.setVisibility(View.VISIBLE);
                View menu = layout.findViewById(mFabMenuId);
                menu.setVisibility(View.VISIBLE);
                break;
        }
    }

    // Private instance methods.

    /** Dismiss the menu associated with the given layout view. */
    private void dismissMenu(@NonNull final Fragment fragment, @NonNull final View layout) {
        // Dismiss the FAM and ensure that the default menu has been setup.
        View menu = layout.findViewById(mFabMenuId);
        menu.setVisibility(View.GONE);
        if (mDefaultMenuName != null) setMenu(fragment, mDefaultMenuName);

        // Restore the FAB to the closed state and dismiss the dimmer view.
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(mFabId);
        fab.setTag(R.integer.fabStateKey, State.closed);
        fab.setImageResource(mImageResourceId);
        View dimmerView = layout.findViewById(mFabDimmerId);
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

    /** Set the current FAM using the cached item with the given name. */
    private void setMenu(final Fragment fragment, final String name) {
        // Ensure that the game fragment layout is accessible and that there is an adapter on the
        // recycler view.  Abort quietly if not.
        View layout = getFragmentLayout(fragment);
        RecyclerView recyclerView;
        recyclerView = layout != null ? (RecyclerView) layout.findViewById(R.id.MenuList) : null;
        MenuAdapter adapter = recyclerView != null ? (MenuAdapter) recyclerView.getAdapter() : null;
        if (layout == null || recyclerView == null || adapter == null) return;

        // Add the menu to initialize the recycler view's data items.
        adapter.clearEntries();
        adapter.addEntries(mMenuMap.get(name));
    }
}
