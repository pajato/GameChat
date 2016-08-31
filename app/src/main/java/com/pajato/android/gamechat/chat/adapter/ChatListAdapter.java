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

package com.pajato.android.gamechat.chat.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.chat.adapter.ChatListItem.DATE_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.GROUP_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.ROOM_ITEM_TYPE;

/**
 * Provide a recycler view adapter to handle showing a list of rooms with messages to view based on
 * how recently messages in those room were generated.
 *
 * @author Paul Michael Reilly
 */
public class ChatListAdapter extends RecyclerView.Adapter<ViewHolder>
    implements View.OnClickListener {

    private List<ChatListItem> mList = new ArrayList<>();

    // Public instance methods.

    /** Add items to the adapter's main list. */
    public void addItems(final List<ChatListItem> items) {
        // Add all the items after clearing the current ones.
        mList.addAll(items);
        notifyDataSetChanged();
    }

    /** Clear all current items. */
    public void clearItems() {
        mList.clear();
    }

    /** Manage the recycler view holder thingy. */
    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent, final int entryType) {
        switch (entryType) {
            case DATE_ITEM_TYPE:
                return new DateHeaderViewHolder(getView(parent, R.layout.item_date_header));
            case GROUP_ITEM_TYPE:
                return new ChatListViewHolder(getView(parent, R.layout.item_group_list));
            case ROOM_ITEM_TYPE:
                return new ChatListViewHolder(getView(parent, R.layout.item_room_list));
        }

        return null;
    }

    /** Populate the widgets for the item at the given position. */
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        ChatListItem item = mList.get(position);
        if (item != null) {
            switch (item.type) {
                case DATE_ITEM_TYPE:
                    // The date header simply updates the section title.
                    int id = item.nameResourceId;
                    String name = holder.itemView.getContext().getResources().getString(id);
                    ((DateHeaderViewHolder) holder).mTitle.setText(name);
                    break;
                case GROUP_ITEM_TYPE:
                    // The group item has to update the group title, the number of new messages,
                    // and the list of rooms with messages (possibly old).
                    updateChatHolder((ChatListViewHolder) holder, item);
                    break;
                case ROOM_ITEM_TYPE:
                    // The ...
                    updateChatHolder((ChatListViewHolder) holder, item);
                    break;
            }
        }
    }

    /** Post any item clicks to the app. */
    public void onClick(final View view) {
        String className = view.getClass().getName();
        ClickEvent event = new ClickEvent(view.getContext(), -1, view, null, className);
        EventBus.getDefault().post(event);
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
        View result = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);
        result.setOnClickListener(this);

        return result;
    }

    /** Update the given view holder using the data from the given item. */
    private void updateChatHolder(ChatListViewHolder holder, final ChatListItem item) {
        // Set the title and list text view content based on the given item.  Provide the item in
        // the view holder tag field.
        holder.title.setText(item.name);
        holder.list.setText(Html.fromHtml(item.text));
        holder.itemView.setTag(item);

        // Set the new message count field, if necessary.
        if (item.count > 0) {
            String text = String.format(Locale.getDefault(), "%d new", item.count);
            holder.count.setText(text);
            holder.count.setVisibility(View.VISIBLE);
        } else {
            holder.count.setVisibility(View.GONE);
        }
    }

    // Nested classes.

    /** ... */
    private class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        DateHeaderViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.titleTextView);
        }
    }

    /** ... */
    private class ChatListViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView count;
        TextView list;

        ChatListViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.titleTextView);
            count = (TextView) itemView.findViewById(R.id.newCount);
            list = (TextView) itemView.findViewById(R.id.list);
        }
    }

}
