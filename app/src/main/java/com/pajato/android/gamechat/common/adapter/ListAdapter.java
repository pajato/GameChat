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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.adapter.ListItem.ItemType;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.main.CompatUtils;
import com.pajato.android.gamechat.main.NavigationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provide a recycler view adapter to handle showing a list of rooms with messages to view based on
 * how recently messages in those room were generated.
 *
 * @author Paul Michael Reilly
 */
public class ListAdapter extends RecyclerView.Adapter<ViewHolder>
    implements View.OnClickListener {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ListAdapter.class.getSimpleName();

    /** Click listener for selection check boxes */
    private CheckBoxClickListener checkboxListener = new CheckBoxClickListener();

    /** A format string for displaying unhandled cases. */
    private static final String UNHANDLED_FORMAT = "Unhandled item entry type: {%s}.";

    /** The list displayed by the owning list view. */
    private List<ListItem> mList = new ArrayList<>();

    // Public instance methods.

    /** Add items to the adapter's main list. */
    public void addItems(final List<ListItem> items) {
        // Add all the items after clearing the current ones.
        mList.addAll(items);
        notifyDataSetChanged();
    }

    /** Clear all current items. */
    public void clearItems() {
        mList.clear();
    }

    /** Get the items being adapted. */
    public List<ListItem> getItems() {return mList;}

    /** Manage the recycler view holder. */
    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType < 0 || viewType >= ItemType.values().length)
            return null;
        ItemType type = ItemType.values()[viewType];
        switch (type) {
            case contactHeader:
            case date:
            case roomsHeader:
                return new HeaderViewHolder(getView(parent, R.layout.item_header));
            case contact:
                return new ContactViewHolder(getView(parent, R.layout.item_contact));
            case group:
                return new ItemListViewHolder(getView(parent, R.layout.item_group));
            case room:
                return new ItemListViewHolder(getView(parent, R.layout.item_room));
            case message:
                return new ItemListViewHolder(getView(parent, R.layout.item_message));
            case selectableMember:
                return new ItemListViewHolder(getView(parent, R.layout.item_join_member));
            case selectableRoom:
                return new ItemListViewHolder(getView(parent, R.layout.item_join_room));
            case inviteCommonRoom:
            case inviteRoom:
                return new ItemListViewHolder(getView(parent, R.layout.item_select_invites_room));
            case inviteGroup:
                return new ItemListViewHolder(getView(parent, R.layout.item_select_for_invites));
            default:
                Log.d(TAG, String.format(Locale.US, UNHANDLED_FORMAT, viewType));
                return null;
        }
    }

    /** Populate the widgets for the item at the given position. */
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        ListItem item = mList.get(position);
        if (item != null) {
            switch (item.type) {
                case date:
                case roomsHeader:
                    // The header item types simply update the section title.
                    int id = item.nameResourceId;
                    String name = holder.itemView.getContext().getResources().getString(id);
                    ((HeaderViewHolder) holder).title.setText(name);
                    break;
                case group:
                case message:
                case room:
                case selectableMember:
                case selectableRoom:
                case inviteRoom:
                case inviteCommonRoom:
                case inviteGroup:
                    // The group item has to update the group title, the number of new messages,
                    // and the list of rooms with messages (possibly old).
                    updateChatHolder((ItemListViewHolder) holder, item);
                    break;
                default:
                    Log.d(TAG, String.format(Locale.US, UNHANDLED_FORMAT, item.type));
                    break;
            }
        }
    }

    /** Post any item clicks to the app. */
    public void onClick(final View view) {
        AppEventManager.instance.post(new ClickEvent(view));
    }

    /** Obtain the number of entries in the item list. */
    @Override public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    /** Obtain the type for the item at the given position. */
    @Override public int getItemViewType(int position) {
        // Return the type for the item at the given position, -1 if there is no such item.
        return mList != null && mList.size() > position ? mList.get(position).type.ordinal() : -1;
    }

    // Private instance methods.

    /** Obtain a view by inflating the given resource id. */
    private View getView(final ViewGroup parent, final int resourceId) {
        View result = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);
        result.setOnClickListener(this);

        return result;
    }

    /** Update the chat icon in the given holder based on the given item type. */
    private void setChatIcon(final ItemListViewHolder holder, final ListItem item) {
        Context context = holder.icon.getContext();
        switch (item.type) {
            case selectableMember:
            case message:
                // For a message, ensure that both the holder and the item have an icon value,
                // and load the icon or default if not found at the specified URL.
                if (holder.icon == null || item.url == null) return;
                Uri imageUri = Uri.parse(item.url);
                if (imageUri != null) {
                    // There is an image to load.  Use Glide to do the heavy lifting.
                    holder.icon.setImageURI(imageUri);
                    Glide.with(context)
                        .load(item.url)
                        .transform(new NavigationManager.CircleTransform(context))
                        .into(holder.icon);
                } else {
                    // There is no image.  Use an anonymous image.
                    holder.icon.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
                holder.icon.setVisibility(View.VISIBLE);
                break;
            default:
                // Ignore other types.
                break;
        }
    }

    /** Update the given view holder using the data from the given item. */
    private void updateChatHolder(ItemListViewHolder holder, final ListItem item) {
        // Set the title and list text view content based on the given item.  Provide the item in
        // the view holder tag field.
        holder.name.setText(item.name);
        if (item.text != null && !item.text.equals(""))
            holder.text.setText(CompatUtils.fromHtml(item.text));
        setChatIcon(holder, item);
        holder.itemView.setTag(item);

        // Set the new message count field, if necessary.
        if (item.count > 0) {
            String text = String.format(Locale.getDefault(), "%d new", item.count);
            holder.count.setText(text);
            holder.count.setVisibility(View.VISIBLE);
        } else {
            if (holder.count != null) holder.count.setVisibility(View.GONE);
        }

        // Set the check box using the item selection state.
        if (holder.checkBox == null) return;
        holder.checkBox.setChecked(item.selected);
        // common room selection is set based on group selection and never allowed separately
        if (item.type == ItemType.inviteCommonRoom)
            holder.checkBox.setEnabled(false);
        else
            holder.checkBox.setEnabled(item.enabled);
        holder.checkBox.setTag(item);
    }

    // Inner classes.

    private class CheckBoxClickListener implements View.OnClickListener {
        public void onClick(View v) {
            // Post the click event to the app
            AppEventManager.instance.post(new ClickEvent(v));
        }
    }

    /** Provide a view holder for a chat list item. */
    private class ItemListViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView count;
        TextView text;
        ImageView icon;
        CheckBox checkBox;

        /** Build an instance given the item view. */
        ItemListViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.chatName);
            count = (TextView) itemView.findViewById(R.id.newCount);
            text = (TextView) itemView.findViewById(R.id.chatText);
            icon = (ImageView) itemView.findViewById(R.id.chatIcon);
            checkBox = (CheckBox) itemView.findViewById(R.id.selectorCheck);
            if (checkBox != null) {
                checkBox.setOnClickListener(checkboxListener);
            }
        }
    }

    /** Provide a class to include a contact view in the list. */
    private class ContactViewHolder extends RecyclerView.ViewHolder {

        // Private instance variables.

        TextView name;
        TextView email;
        ImageView icon;

        /** Build a contact view holder for the given item. */
        ContactViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.contactName);
            email = (TextView) itemView.findViewById(R.id.contactEmail);
            icon = (ImageView) itemView.findViewById(R.id.contactIcon);
        }
    }

    /** Provide a class to include a header in the list. */
    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        // Private instance variables.

        /** The text view showing the date or contact header. */
        TextView title;

        /** ... */
        HeaderViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.header);
        }
    }

}
