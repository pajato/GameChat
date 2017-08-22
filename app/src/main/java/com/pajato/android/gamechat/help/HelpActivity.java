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
package com.pajato.android.gamechat.help;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.ListAdapter;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.main.SupportManager;

import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

/**
 * Provide an activity to display and manage help content.
 */

public class HelpActivity extends Activity {

    /** The logcat TAG. */
    private static final String TAG = HelpActivity.class.getSimpleName();

    private String mBitmapPath;
    private String mLogCatPath;

    // Public instance methods.

    /** Handle a back press event by popping the back stack. */
    @Override public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    /** Process a click on a given view by posting a button click event. */
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.feedbackIcon:
            case R.id.feedbackTitle:
                SupportManager.instance.sendFeedback(this, "GameChat Feedback",
                        "Feedback: ", mBitmapPath, mLogCatPath);

                break;
            case R.id.header:
                // Handle click on a header. If it's either of the future-feature values, show msg.
                TextView textView = (view instanceof TextView) ? (TextView)view : null;
                if (textView == null)
                    return;
                if (textView.getText().equals(getString(R.string.HelpMostPopularTitle)) ||
                        textView.getText().equals(getString(R.string.HelpRecentTitle))) {
                    String prefix = String.format(getString(R.string.HelpFutureFeatureMessage),
                            textView.getText().toString());
                    String suffix = getString(R.string.HelpFutureFeature);
                    CharSequence text = String.format(Locale.getDefault(), "%s %s", prefix, suffix);
                    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                // Use the Event bus to post the click event.
                Object payload = view.getTag();
                if (payload == null || !(payload instanceof ListItem))
                    return;
                ListItem clickedItem = (ListItem) payload;
                String path = clickedItem.text;
                Fragment fragment = HelpContentFragment.newInstance(path);
                getFragmentManager().beginTransaction().add(android.R.id.content, fragment).
                        addToBackStack(HelpContentFragment.class.getSimpleName()).
                        commit();
                break;
        }
    }

    // Protected instance methods.

    /** Deal with the creation life-cycle callback by setting up the send form. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);
        ImageView image = (ImageView) findViewById(R.id.feedbackIcon);
        image.setImageResource(R.drawable.ic_feedback_black_24dp);
        // Save attachments in case user selects 'feedback'
        mBitmapPath = getIntent().getStringExtra("bitmapPath");
        mLogCatPath = getIntent().getStringExtra("logCatPath");
    }

    /** Deal with the foreground life-cycle callback by updating the help adapter. */
    @Override protected void onResume() {
        super.onResume();
        updateAdapterList();
    }

    // Private instance methods. */

    /** Return TRUE iff the adapter has been set up, FALSE otherwise. */
    private boolean updateAdapterList() {
        // Determine if the fragment has a view and that it has a list type.  Abort if not,
        // otherwise ensure that the list adapter exists, creating it if necessary.
        View view = findViewById(R.id.helpItemList);
        if (view == null)
            return false;
        RecyclerView recycler = (RecyclerView) view;
        RecyclerView.Adapter adapter = recycler.getAdapter();
        if (adapter == null) {
            // Initialize the recycler view.
            adapter = new ListAdapter();
            recycler.setAdapter(adapter);
            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(getBaseContext(), VERTICAL, false);
            recycler.setLayoutManager(layoutManager);
            recycler.setItemAnimator(new DefaultItemAnimator());
        }

        // Inject the list items into the recycler view making sure to scroll to the end of the
        // list when showing messages.
        ListAdapter listAdapter = (ListAdapter) adapter;
        listAdapter.clearItems();
        List<ListItem> items = HelpManager.instance.getList(this);
        int size = items != null ? items.size() : 0;
        Log.d(TAG, String.format(Locale.US, "Updating with %d items.", size));
        listAdapter.addItems(items);
        return true;
    }
}
