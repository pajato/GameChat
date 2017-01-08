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
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.MenuEntry;
import com.pajato.android.gamechat.common.adapter.MenuItemEntry;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.exp.ExpFragmentType;
import com.pajato.android.gamechat.main.PaneManager;

import java.util.Locale;

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

    // Protected instance variables.

    /** The fragment active state; set when entering onResume and cleared in onPause. */
    protected boolean mActive;

    /** The persisted layout view for this fragment. */
    protected View mLayout;
    /** The persistent layout's corresponding resource ID. */
    protected int mLayoutId;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseFragment() {}

    // Public instance methods.

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

        // The layout does not exist.  Create and persist it, and initialize the fragment layout.
        mLayout = inflater.inflate(mLayoutId, container, false);
        // All chat and game fragments will use the options menu.
        setHasOptionsMenu(true);
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

    /** Handle an options menu choice. */
    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_game_icon:
                // Show the game panel.
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                if(viewPager != null) {
                    viewPager.setCurrentItem(PaneManager.GAME_INDEX);
                }
                break;
            case R.id.search:
                // TODO: Handle a search in the groups panel by fast scrolling to chat.
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
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
    protected MenuEntry getEntry(final int titleId, final int iconId, final ExpFragmentType type) {
        int ordinal = type.ordinal();
        return new MenuEntry(new MenuItemEntry(MENU_ITEM_NO_TINT_TYPE, titleId, iconId, ordinal));
    }

    /** Return a menu entry for with given title and icon resource items. */
    protected MenuEntry getNoTintEntry(final int titleId, final int iconId) {
        return new MenuEntry(new MenuItemEntry(MENU_ITEM_NO_TINT_TYPE, titleId, iconId));
    }

    /** Return a menu entry for with given title and icon resource items. */
    protected MenuEntry getTintEntry(final int titleId, final int iconId) {
        return new MenuEntry(new MenuItemEntry(MENU_ITEM_TINT_TYPE, titleId, iconId));
    }

    /** Provide a logger to show the given message and the given bundle. */
    protected abstract void logEvent(String message, Bundle bundle);

    /** Provide a logger to show the given message. */
    protected abstract void logEvent(String message);

    /** Delegate the setup to the subclasses. */
    protected abstract boolean onDispatch(Context context, Dispatcher dispatcher);

    /** Make the given menu item either visible or invisible. */
    protected void setItemState(final Menu menu, final int itemId, final boolean state) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) item.setVisible(state);
    }

    /** Set the Layout ID. Should be called by child classes in their onCreate methods. */
    protected void setLayoutId(int id) {
        this.mLayoutId = id;
    }

    /** Set the title in the toolbar based on the list type. */
    protected void setTitles() {
        // Ensure that there is an accessible toolbar at this point.  Abort if not, otherwise case
        // on the list type to apply the titles.
        Toolbar bar = mLayout != null ? (Toolbar) mLayout.findViewById(R.id.toolbar) : null;
        if (bar == null) return;

        bar.setTitle(getResources().getString(R.string.app_name));
    }

    /** Provide a way to handle volunteer solicitations for unimplemented functions. */
    protected void showFutureFeatureMessage(final int resourceId) {
        // Post a toast message.
        Context context = getContext();
        String prefix = context.getString(resourceId);
        String suffix = context.getString(R.string.FutureFeature);
        CharSequence text = String.format(Locale.getDefault(), "%s %s", prefix, suffix);
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
