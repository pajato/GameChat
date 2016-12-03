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

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.main.NavigationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.chat.adapter.ChatListItem.CONTACT_HEADER_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.CONTACT_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.DATE_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.GROUP_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.MEMBER_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.MESSAGE_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.ROOMS_HEADER_ITEM_TYPE;
import static com.pajato.android.gamechat.chat.adapter.ChatListItem.ROOM_ITEM_TYPE;

/**
 * Provide a recycler view adapter to handle showing a list of rooms with messages to view based on
 * how recently messages in those room were generated.
 *
 * @author Paul Michael Reilly
 */
public class ChatListAdapter extends RecyclerView.Adapter<ViewHolder>
    implements View.OnClickListener {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ChatListAdapter.class.getSimpleName();

    /** A format string for displaying unhandled cases. */
    private static final String UNHANDLED_FORMAT = "Unhandled item entry type: {%s}.";

    /** The list displayed by the owning list view. */
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

    /** Manage the recycler view holder. */
    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent, final int entryType) {
        switch (entryType) {
            case CONTACT_HEADER_ITEM_TYPE:
                return new HeaderViewHolder(getView(parent, R.layout.item_header));
            case CONTACT_ITEM_TYPE:
                return new ContactViewHolder(getView(parent, R.layout.item_contact));
            case DATE_ITEM_TYPE:
                return new HeaderViewHolder(getView(parent, R.layout.item_header));
            case GROUP_ITEM_TYPE:
                return new ChatListViewHolder(getView(parent, R.layout.item_group));
            case ROOM_ITEM_TYPE:
                return new ChatListViewHolder(getView(parent, R.layout.item_room));
            case ROOMS_HEADER_ITEM_TYPE:
                return new HeaderViewHolder(getView(parent, R.layout.item_header));
            case MEMBER_ITEM_TYPE:
                return new ChatListViewHolder(getView(parent, R.layout.item_room));
            case MESSAGE_ITEM_TYPE:
                return new ChatListViewHolder(getView(parent, R.layout.item_message));
            default:
                Log.d(TAG, String.format(Locale.US, UNHANDLED_FORMAT, entryType));
                break;
        }

        return null;
    }

    /** Populate the widgets for the item at the given position. */
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        ChatListItem item = mList.get(position);
        if (item != null) {
            switch (item.type) {
                case DATE_ITEM_TYPE:
                case ROOMS_HEADER_ITEM_TYPE:
                    // The header item types simply update the section title.
                    int id = item.nameResourceId;
                    String name = holder.itemView.getContext().getResources().getString(id);
                    ((HeaderViewHolder) holder).title.setText(name);
                    break;
                case GROUP_ITEM_TYPE:
                case MEMBER_ITEM_TYPE:
                case MESSAGE_ITEM_TYPE:
                case ROOM_ITEM_TYPE:
                    // The group item has to update the group title, the number of new messages,
                    // and the list of rooms with messages (possibly old).
                    updateChatHolder((ChatListViewHolder) holder, item);
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
        return mList != null && mList.size() > position ? mList.get(position).type : -1;
    }

    // Private instance methods.

    /** Obtain a view by inflating the given resource id. */
    private View getView(final ViewGroup parent, final int resourceId) {
        View result = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);
        result.setOnClickListener(this);

        return result;
    }

    /** Update the chat icon in the given holder based on the given item type. */
    private void setChatIcon(final ChatListViewHolder holder, final ChatListItem item) {
        // Ensure that both the holder and the item have an icon.
        if (holder.icon == null || item.url == null) return;

        // The icon and url both exist.  Case on the item type.
        Context context = holder.icon.getContext();
        switch (item.type) {
            case MESSAGE_ITEM_TYPE:
                // For a message, load the icon, if there is one.
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
    private void updateChatHolder(ChatListViewHolder holder, final ChatListItem item) {
        // Set the title and list text view content based on the given item.  Provide the item in
        // the view holder tag field.
        holder.name.setText(item.name);
        holder.text.setText(Html.fromHtml(item.text));
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
    }

    // Inner classes.

    /** Provide a view holder for a chat list item. */
    private class ChatListViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView count;
        TextView text;
        ImageView icon;

        /** Build an instance given the item view. */
        ChatListViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.chatName);
            count = (TextView) itemView.findViewById(R.id.newCount);
            text = (TextView) itemView.findViewById(R.id.chatText);
            icon = (ImageView) itemView.findViewById(R.id.chatIcon);
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

}
