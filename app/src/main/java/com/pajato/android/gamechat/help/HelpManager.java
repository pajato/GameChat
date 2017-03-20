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
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.ListItem;
import com.pajato.android.gamechat.main.NavigationManager;
import com.pajato.android.gamechat.main.SupportManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.adapter.ListItem.ItemType.helpHeader;

/** Provide a singleton to manage the help content. */
public enum HelpManager {
    instance;

    /** The logcat TAG. */
    private static final String TAG = HelpManager.class.getSimpleName();

    /** Constructor */
    HelpManager() {}

    /** Launch the help activity by first insuring the nav drawer and/or menu are closed. */
    public void launchHelp(final FragmentActivity activity) {
        // Close whichever menu started this
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        if (toolbar.isOverflowMenuShowing())
            toolbar.hideOverflowMenu();
        if (NavigationManager.instance.isDrawerOpen(activity)) {
            final DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
            drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {}

                @Override
                public void onDrawerOpened(View drawerView) {}

                @Override
                public void onDrawerClosed(View drawerView) {
                    drawerLayout.removeDrawerListener(this);
                    // When the drawer is closed, take the screenshot
                    startHelpAndFeedback(activity);
                }

                @Override
                public void onDrawerStateChanged(int newState) {}
            });
            NavigationManager.instance.closeDrawerIfOpen(activity);
        } else
            startHelpAndFeedback(activity);
    }

    /** Perform a capture of the current screen, get logcat and send both to help activity. */
    private void startHelpAndFeedback(final Activity activity) {
        // Grab the logcat output to send to the help activity
        String logCatPath = SupportManager.instance.getLogcatPath(activity);
        // Capture the screen and to send to the help activity.
        View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(false); // clear any previous cached data
        rootView.destroyDrawingCache();
        rootView.setDrawingCacheEnabled(true); // start over
        String bitmapPath =
                SupportManager.instance.getBitmapPath(rootView.getDrawingCache(), activity);
        Intent intent = new Intent(activity, HelpActivity.class);
        intent.putExtra("bitmapPath", bitmapPath);
        intent.putExtra("logCatPath", logCatPath);
        activity.startActivity(intent);
    }

    /** Get list items for help activity display */
    public List<ListItem> getList(Activity activity) {
        List<ListItem> items = new ArrayList<>();
        items.add(new ListItem(helpHeader, R.string.HelpAllTitle));
        items.addAll(getAllArticles(activity));
        items.add(new ListItem(helpHeader, R.string.HelpMostPopularTitle));
        items.addAll(getMostPopularArticles(activity));
        items.add(new ListItem(helpHeader, R.string.HelpRecentTitle));
        items.addAll(getRecentArticles(activity));
        return items;
    }

    /** Get a list of all help articles */
    private List<ListItem> getAllArticles(Activity activity) {
        List<ListItem> articlesList = new ArrayList<>();
        // Read list of articles from JSON list
        try {
            String inputText = loadJSONFromAsset("article_list.json", activity);
            JSONObject inputJson = new JSONObject(inputText);
            JSONArray jsonArray = inputJson.getJSONArray("articles");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject article = (JSONObject) jsonArray.get(i);
                article.get("name");
                article.get("path");
                articlesList.add(new ListItem((String)article.get("name"), (String)article.get("path")));
            }
        } catch (JSONException e){
            Log.e(TAG, "Error reading JSON");
        }
        return articlesList;
    }

    /** Get a list of popular articles. Currently displays a 'future feature' message. */
    private List<ListItem> getMostPopularArticles(@SuppressWarnings("unused") Activity activity) {
        return new ArrayList<>();
    }

    /** Get a list of recent articles. Currently displays a 'future feature' message.  */
    private List<ListItem> getRecentArticles(@SuppressWarnings("unused") Activity activity) {
        return new ArrayList<>();
    }

    /** Read JSON data from specified file */
    private String loadJSONFromAsset(String fileName, Activity activity) {
        String json = null;
        InputStream inputStream = null;
        try {
            inputStream = activity.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            int readSize = inputStream.read(buffer);
            Log.i(TAG, "Read " + readSize + " from " + fileName);
            json = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return json;
    }
}
