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

package com.pajato.android.gamechat.common.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.TagClickEvent;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.common.adapter.MenuEntry.MENU_HEADER_TYPE;
import static com.pajato.android.gamechat.common.adapter.MenuEntry.MENU_ITEM_TYPE;

/**
 * Provide a recycler view adapter to show zero or more menu entries.  A menu entry is either a
 * header (group label) or a title/icon pair, commonly known as a menu item.  To avoid confusion,
 * documentation for this class attempts to always prefix "item" with either "adapter" or "menu" to
 * make the context very clear and unambiguous.
 *
 * @author Paul Michael Reilly
 */
public class MenuAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {

    // Private instance variables.

    /** A list of menu (group) headers or menu items. */
    private List<MenuEntry> mList = new ArrayList<>();

    // Public instance methods.

    /** Add menu entries to the adapter's main list. */
    public void addEntries(final List<MenuEntry> entries) {
        // Add all the items after clearing the current ones.
        mList.addAll(entries);
        notifyDataSetChanged();
    }

    /** Clear all current menu entries. */
    public void clearEntries() {
        mList.clear();
    }

    /** Populate the widgets for the item at the given position. */
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        MenuEntry menuEntry = mList.get(position);
        if (menuEntry != null) {
            switch (menuEntry.type) {
                case MENU_ITEM_TYPE:
                    // Update the menu item holder by setting up the title and icon.
                    updateMenuItemHolder((MenuItemViewHolder) holder, menuEntry);
                    break;
            }
        }
    }

    /** Post any item clicks to the app. */
    @Override public void onClick(final View view) {
        AppEventManager.instance.post(new TagClickEvent(view));
    }

    /** Create the recycler view holder using the given adapter adapter item type. */
    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent, final int type) {
        switch (type) {
            case MENU_ITEM_TYPE:
                // Normal case: deal with a standard menu item.
                return new MenuItemViewHolder(getView(parent, R.layout.item_menu));
            case MENU_HEADER_TYPE:
                // Rare case: deal with menu header (group label).
                return new MenuHeaderViewHolder(getView(parent, R.layout.item_header));
            default:
                break;
        }

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

    // Private instance methods.

    /** Obtain a view by inflating the given resource id. */
    private View getView(final ViewGroup parent, final int resourceId) {
        // Inflate the entry view and set the click handlers for the fields.
        View result = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);
        View view = result.findViewById(R.id.menuItemTitle);
        if (view != null) view.setOnClickListener(this);
        view = result.findViewById(R.id.menuItemIcon);
        if (view != null) view.setOnClickListener(this);

        return result;
    }

    /** Return true iff the given entry has a URL for the icon that the holder can load. */
    private boolean loadUrl(final MenuItemViewHolder holder, final MenuEntry entry) {
        if (entry.url == null) return false;

        // There is a url to use.  Parse it to see if it is valid.  Abort if not valid.
        Uri imageUri = Uri.parse(entry.url);
        if (imageUri == null) return false;

        // There is an valid image to load.  Use Glide to do the heavy lifting.
        Context context = holder.icon.getContext();
        holder.icon.setImageURI(imageUri);
        Glide.with(context)
            .load(entry.url)
            .into(holder.icon);
        return true;
    }

    /** Update the given view holder using the data from the given entry. */
    private void updateMenuItemHolder(final MenuItemViewHolder holder, final MenuEntry entry) {
        // Set the text on the holder and put the entry on the item layout, title and icon view
        // tags.
        Context context = holder.title.getContext();
        String text = context.getString(entry.titleResId);
        holder.title.setText(Html.fromHtml(text));
        holder.itemView.setTag(entry.fragmentTypeIndex);
        holder.title.setTag(entry.fragmentTypeIndex);
        holder.icon.setTag(entry.fragmentTypeIndex);

        // Set the icon on the holder.
        if (loadUrl(holder, entry)) return;

        // The url is not viable (reason is unclear).  Time to try the icon resource id.
        if (entry.iconResId > 0) {
            holder.icon.setImageResource(entry.iconResId);
            return;
        }

        holder.icon.setImageResource(R.drawable.ic_account_circle_black_24dp);
    }

    // Inner classes.

    /** Provide a class to include a menu item (title and icon) in the list. */
    private class MenuItemViewHolder extends RecyclerView.ViewHolder {
        Button title;
        ImageView icon;

        /** Build a menu item view holder from the given adapter item view. */
        MenuItemViewHolder(final View adapterItemView) {
            super(adapterItemView);
            title = (Button) adapterItemView.findViewById(R.id.menuItemTitle);
            icon = (ImageView) adapterItemView.findViewById(R.id.menuItemIcon);
        }
    }

    /** Provide a class to include a menu (group) header in the list. */
    private class MenuHeaderViewHolder extends RecyclerView.ViewHolder {

        /** The text view showing the date or contact header. */
        TextView title;

        /** Build a menu header (group) view holder from the given adapter item view. */
        MenuHeaderViewHolder(final View adapterItemView) {
            super(adapterItemView);
            title = (TextView) adapterItemView.findViewById(R.id.header);
        }
    }

}
