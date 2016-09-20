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

package com.pajato.android.gamechat.game;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.main.PaneManager;

import java.util.Locale;

/**
 * Provide a base class to support fragment lifecycle debugging.  All fragment lifecycle events are
 * handled by providing logcat tracing information.
 *
 * @author Paul Michael Reilly
 */
public abstract class BaseGameFragment extends Fragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = BaseGameFragment.class.getSimpleName();

    /** The lifecycle event format string with no bundle. */
    private static final String FORMAT_NO_BUNDLE =
        "Event: %s; Fragment: %s; Fragment Manager: %s.";

    /** The lifecycle event format string with a bundle provided. */
    private static final String FORMAT_WITH_BUNDLE =
        "Event: %s; Fragment: %s; Fragment Manager: %s; Bundle: %s.";

    // Protected instance variables.

    /** The persisted layout view for this fragment. */
    protected View mLayout;

    /** The current turn indicator: True = Player 1, False = Player 2. */
    protected boolean mTurn;

    // Public constructors.

    /** Provide a default, no args constructor. */
    public BaseGameFragment() {}

    // Public instance methods.

    /** Return the current turn indicator, TRUE -> Player 1; FALSE -> Player 2. */
    public boolean getTurn() {
        return mTurn;
    }

    /** Obtain a layout file from the subclass. */
    abstract public int getLayout();

    @Override public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        logEvent("onActivityCreated", bundle);
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        logEvent("onAttach");
        AppEventManager.instance.register(this);
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
        mLayout = inflater.inflate(getLayout(), container, false);
        onInitialize();
        return mLayout;
    }

    /** Log the lifecycle event and kill the ads. */
    @Override public void onDestroy() {
        logEvent("onDestroy");
        super.onDestroy();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        logEvent("onDestroyView");
    }

    @Override public void onDetach() {
        super.onDetach();
        logEvent("onDetach");
        AppEventManager.instance.unregister(this);
    }

    /** Initialize the fragment. */
    public void onInitialize() {
        // All chat and game fragments will use the options menu.
        setHasOptionsMenu(true);
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
    }

    /** Log the lifecycle event and resume showing ads. */
    @Override public void onResume() {
        super.onResume();
        logEvent("onResume");
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

    /** Log a lifecycle event that has no bundle. */
    protected void logEvent(final String event) {
        String manager = getFragmentManager().toString();
        String format = FORMAT_NO_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, event, this, manager));
    }

    /** Log a lifecycle event that has a bundle. */
    protected void logEvent(final String event, final Bundle bundle) {
        String manager = getFragmentManager().toString();
        String format = FORMAT_WITH_BUNDLE;
        Log.v(TAG, String.format(Locale.US, format, event, this, manager, bundle));
    }

}
