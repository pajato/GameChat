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
package com.pajato.android.gamechat.common.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.PlayModeChangeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.adapter.PlayModeMenuEntry.MENU_TEXT_TYPE;

/**
 * Provide a recycler view adapter menu entries containing simple text for the player2 menu. To
 * avoid confusion, documentation for this class attempts to always prefix "item" with either
 * "adapter" or "menu" to make the context very clear and unambiguous.
 */
public class PlayModeMenuAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {

    // Private instance variables.

    /** A list of menu (group) headers or menu items. */
    private List<PlayModeMenuEntry> mList = new ArrayList<>();

    // Public instance methods.

    /** Add menu entries to the adapter's main list. */
    public void addEntries(final List<PlayModeMenuEntry> entries) {
        // Add all the items after clearing the current ones.
        mList.addAll(entries);
        notifyDataSetChanged();
    }

    /** Clear all current menu entries. */
    public void clearEntries() {
        mList.clear();
    }

    /** Populate the widgets for the item at the given position */
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        PlayModeMenuEntry menuEntry = mList.get(position);
        if (menuEntry != null && menuEntry.type == MENU_TEXT_TYPE)
            updateMenuTextHolder((MenuTextViewHolder) holder, menuEntry);
    }

    /** Post any item clicks to the app */
    @Override public void onClick(final View view) {
        AppEventManager.instance.post(new PlayModeChangeEvent(view));
    }

    /** Create the recycler view holder */
    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent, final int type) {
        if (type == MENU_TEXT_TYPE)
            return new MenuTextViewHolder(getView(parent, R.layout.item_simple_text));
        return null;
    }

    /** Obtain the number of entries in the item list. */
    @Override public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    /** Obtain the type for the item at the given position. */
    @Override public int getItemViewType(int position) {
        // Return the type for the item at the given position, -1 if there is no such item.
        return mList != null && mList.size() > position ? mList.get(position).type : -1;
    }

    /** Obtain a view by inflating the given resource id. */
    private View getView(final ViewGroup parent, final int resourceId) {
        // Inflate the entry view and set the click handlers for the fields.
        View result = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);
        View view = result.findViewById(R.id.playerModeTextItem);
        if (view != null)
            view.setOnClickListener(this);
        return result;
    }

    /** Update the given view holder using the data from the given menu entry */
    private void updateMenuTextHolder(final MenuTextViewHolder holder,final PlayModeMenuEntry entry) {
        holder.title.setText(entry.title);
        holder.title.setTag(entry);
    }

    /** Provide a class to include a menu text item (used for player2 control) */
    private class MenuTextViewHolder extends RecyclerView.ViewHolder {

        /** The text view showing the simple text */
        TextView title;


        /** Build a simple text menu item view holder from the given adapter item view. */
        MenuTextViewHolder(final View adapterItemView) {
            super(adapterItemView);
            title = (TextView) adapterItemView.findViewById(R.id.playerModeTextItem);
        }
    }

}