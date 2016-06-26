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
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Provide a base class to support fragment lifecycle debugging.  All lifecycle events except for
 * onViewCreate() are handled by providing logcat tracing information.  The fragment manager is
 * displayed in order to help catch an elusive connected check failure.
 *
 * @author Paul Michael Reilly
 */
public class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();
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
        String format = "onDestroy: Destroying fragment {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onDestroy();
    }

    @Override public void onDestroyView() {
        String format = "onDestroyView: Destroying fragment {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onDestroyView();
    }

    @Override public void onDetach() {
        String format = "onDetach: Detaching fragment {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onDetach();
    }

    @Override public void onPause() {
        String format = "onPause: Fragment {%s} is no longer visible and running. Fragment manager: "
            + "{%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onPause();
    }

    @Override public void onResume() {
        String format = "onResume: Fragment {%s} is visible and running. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onResume();
    }

    @Override public void onStart() {
        String format = "onStart: Make the fragment {%s} visible. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onStart();
    }

    @Override public void onStop() {
        String format = "onStop: Fragment {%s} is no longer visible. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, getFragmentManager()));
        super.onStop();
    }

    @Override public void onViewStateRestored(Bundle bundle) {
        String format = "onViewStateRestored: The saved state has been restored to fragment {%s} "
            + "using bundle {%s}. Fragment manager: {%s}.";
        Log.v(TAG, String.format(format, this, bundle, getFragmentManager()));
        super.onViewStateRestored(bundle);
    }

}
