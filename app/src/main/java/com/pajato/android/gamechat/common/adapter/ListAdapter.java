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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.model.Group;
import com.pajato.android.gamechat.chat.model.Room;
import com.pajato.android.gamechat.common.adapter.ListItem.ItemType;
import com.pajato.android.gamechat.database.AccountManager;
import com.pajato.android.gamechat.database.GroupManager;
import com.pajato.android.gamechat.database.RoomManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.ClickEvent;
import com.pajato.android.gamechat.main.CompatUtils;
import com.pajato.android.gamechat.main.NavigationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pajato.android.gamechat.chat.model.Room.RoomType.ME;

/**
 * Provide a recycler view adapter to handle showing a list of rooms with messages or experiences
 * to view based on how recently messages in those room were generated.
 *
 * @author Paul Michael Reilly
 */
public class ListAdapter extends RecyclerView.Adapter<ViewHolder>
    implements View.OnClickListener {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = ListAdapter.class.getSimpleName();

    /** Click listener for selection widgets */
    private SelectorClickListener selectorListener = new SelectorClickListener();

    /** Click listener for the end icon */
    private IconCLickListener endIconListener = new IconCLickListener();

    /** A format string for displaying unhandled cases. */
    private static final String UNHANDLED_FORMAT = "Unhandled item entry type: {%s}.";

    /** The list displayed by the owning list view. */
    private List<ListItem> mList = new ArrayList<>();

    // Public instance methods.

    /** Add items to the adapter's main list. */
    public void addItems(final List<ListItem> items) {
        if (items == null)
            return;
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
            case resourceHeader:
            case roomsHeader:
                return new HeaderViewHolder(getView(parent, R.layout.item_header));
            case chatGroup:
            case expGroup:
                return new ItemListViewHolder(getView(parent, R.layout.item_with_tint));
            case contact:
                return new ContactViewHolder(getView(parent, R.layout.item_contact));
            case expList:
                return new ItemListViewHolder(getView(parent, R.layout.item_without_tint));
            case expRoom:
            case chatRoom:
                return new ItemListViewHolder(getView(parent, R.layout.item_with_tint));
            case groupList:
                return new ItemListViewHolder(getView(parent, R.layout.item_select_for_invites));
            case helpArticle:
                return new HelpListViewHolder(getView(parent, R.layout.item_help_article));
            case helpHeader:
                return new HeaderViewHolder(getView(parent, R.layout.item_help_header));
            case member:
                return new ContactViewHolder(getView(parent, R.layout.item_contact));
            case message:
                return new ItemListViewHolder(getView(parent, R.layout.item_message));
            case selectableMember:
                return new ItemListViewHolder(getView(parent, R.layout.item_join_member));
            case selectableRoom:
                return new ItemListViewHolder(getView(parent, R.layout.item_join_room));
            case selectUser:
                return new ItemListViewHolder(getView(parent, R.layout.item_select_user));
            case protectedUserList:
                return new ItemListViewHolder(getView(parent, R.layout.item_protected_user));
            case inviteCommonRoom:
            case inviteRoom:
                return new ItemListViewHolder(getView(parent, R.layout.item_select_invites_room));
            case inviteGroup:
                return new ItemListViewHolder(getView(parent, R.layout.item_select_for_invites));
            case newItem:
                return new NewItemViewHolder(getView(parent, R.layout.item_create_new));
            default:
                Log.e(TAG, String.format(Locale.US, UNHANDLED_FORMAT, viewType));
                return null;
        }
    }

    /** Populate the widgets for the item at the given position. */
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        ListItem item = mList.get(position);
        if (item != null) {
            switch (item.type) {
                case contact:
                case member:
                    updateContactHolder((ContactViewHolder) holder, item);
                    break;
                case date:
                case resourceHeader:
                case roomsHeader:
                case helpHeader:
                    updateHeaderItemHolder((HeaderViewHolder) holder, item);
                    break;
                case chatGroup:
                case chatRoom:
                case expGroup:
                case expList:
                case expRoom:
                case groupList:
                case message:
                case inviteRoom:
                case inviteCommonRoom:
                case inviteGroup:
                case protectedUserList:
                case selectUser:
                case selectableMember:
                case selectableRoom:
                     // The group item has to update the group title, the number of new messages,
                    // and the list of rooms with messages (possibly old).
                    updateHolder((ItemListViewHolder) holder, item);
                    break;
                case newItem:
                    updateNewItemHolder((NewItemViewHolder)holder, item);
                    break;
                case helpArticle:
                    updateHelpItemHolder((HelpListViewHolder) holder, item);
                    break;
                default:
                    Log.e(TAG, String.format(Locale.US, UNHANDLED_FORMAT, item.type));
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

    /** Update the end icon in the given holder based on the specified item */
    private void setEndIcon(final ItemListViewHolder holder, final ListItem item) {
        switch (item.type) {
            case chatGroup:
            case expGroup:
                // Set an end icon ONLY if the group is not the 'me' group (user cannot leave
                // or delete the 'me' group). If the group is owned by this account, set the
                // delete icon, otherwise set the 'exit group' icon.
                Group group = GroupManager.instance.getGroupProfile(item.groupKey);
                if (group == null) {
                    String format = "Found null group profile for group %s";
                    Log.e(TAG, String.format(format, item.groupKey));
                    break;
                }
                // No icon for 'me' group
                if (group.key.equals(AccountManager.instance.getMeGroupKey()))
                    break;
                // Set leave or delete icon
                holder.endIcon.setTag(item);
                if (group.owner.equals(AccountManager.instance.getCurrentAccountId()))
                    holder.endIcon.setImageResource(R.drawable.ic_delete_forever_black_24dp);
                else
                    holder.endIcon.setImageResource(R.drawable.ic_exit_to_app_black_24dp);
                break;
            case protectedUserList:
                // Set end icons - in this case there are two
                holder.endIcon.setTag(item);
                holder.veryEndIcon.setTag(item);
                holder.endIcon.setImageResource(R.drawable.ic_call_made_black_24dp);
                holder.veryEndIcon.setImageResource(R.drawable.ic_delete_forever_black_24dp);
                break;
            case chatRoom:
            case expRoom:
                Room room = RoomManager.instance.getRoomProfile(item.roomKey);
                if (room == null) {
                    String format = "Found null room profile for room %s";
                    Log.e(TAG, String.format(format, item.roomKey));
                    break;
                }
                // No icon for 'me' room
                if (room.type == ME)
                    break;
                // Set leave or delete icon
                holder.endIcon.setTag(item);
                if (room.owner.equals(AccountManager.instance.getCurrentAccountId()))
                    holder.endIcon.setImageResource(R.drawable.ic_delete_forever_black_24dp);
                else
                    holder.endIcon.setImageResource(R.drawable.ic_exit_to_app_black_24dp);
                break;
            case expList:
                Room expRoom = RoomManager.instance.getRoomProfile(item.roomKey);
                if (expRoom == null) {
                    String format = "Found null room profile for room %s";
                    Log.e(TAG, String.format(format, item.roomKey));
                    break;
                }
                if (expRoom.owner.equals(AccountManager.instance.getCurrentAccountId())) {
                    holder.endIcon.setTag(item);
                    holder.endIcon.setImageResource(R.drawable.ic_delete_forever_black_24dp);
                }
                break;
            default:
                // ignore other types
                break;
        }
    }

    /** Update the chat icon in the given holder based on the given item type. */
    private void setIcon(final ItemListViewHolder holder, final ListItem item) {
        Context context = holder.icon.getContext();
        switch (item.type) {
            case expGroup:
            case chatGroup:
                holder.icon.setImageResource(R.drawable.vd_group_black_24px);
                break;
            case expRoom:
            case chatRoom:
                holder.icon.setImageResource(R.drawable.ic_checkers_black_24dp);
                break;
            case expList:
                holder.icon.setImageResource(item.iconResId);
                break;
            case groupList:
                holder.icon.setImageResource(R.drawable.vd_group_black_24px);
                break;
            case helpArticle:
                holder.icon.setImageResource(R.drawable.ic_content_newspaper_black_24dp);
                break;
            case protectedUserList:
                if (item.iconUrl == null) {
                    holder.icon.setImageResource(R.drawable.ic_account_circle_black_24dp);
                } else {
                    Uri imageUri = Uri.parse(item.iconUrl);
                    if (imageUri != null) {
                        // There is an image to load.  Use Glide to do the heavy lifting.
                        holder.icon.setImageURI(imageUri);
                        Glide.with(context)
                                .load(item.iconUrl)
                                .transform(new NavigationManager.CircleTransform(context))
                                .into(holder.icon);
                    } else
                        holder.icon.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
                break;
            case message:
            case selectUser:
            case selectableMember:
                // For a message, ensure that both the holder and the item have an icon value,
                // and load the icon or default if not found at the specified URL.
                if (holder.icon == null || item.iconUrl == null) {
                    holder.icon.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    return;
                }
                Uri imageUri = Uri.parse(item.iconUrl);
                if (imageUri != null) {
                    // There is an image to load.  Use Glide to do the heavy lifting.
                    holder.icon.setImageURI(imageUri);
                    Glide.with(context)
                        .load(item.iconUrl)
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
    private void updateNewItemHolder(NewItemViewHolder holder, final ListItem item) {
        holder.name.setText(holder.name.getContext().getResources().
                getString(item.nameResourceId));
        holder.name.setTag(item);
        holder.icon.setTag(item);
        holder.itemView.setTag(item);
    }

    /** Update the section title for the given header view holder. */
    private void updateHeaderItemHolder(HeaderViewHolder holder, ListItem item) {
        int id = item.nameResourceId;
        holder.itemView.setTag(item);
        String name = holder.itemView.getContext().getResources().getString(id);
        holder.title.setText(name);
        holder.title.setTag(item);
    }

    /** Update the given view holder using the data from the given item. */
    private void updateHelpItemHolder(HelpListViewHolder holder, ListItem item) {
        holder.title.setText(item.name);
        holder.title.setTag(item);
        holder.icon.setImageResource(R.drawable.ic_content_newspaper_black_24dp);
        holder.icon.setTag(item);
        holder.itemView.setTag(item);
    }

    /** Update the given view holder using the data from the given item. */
    private void updateHolder(ItemListViewHolder holder, final ListItem item) {
        // Set the title and list text view content based on the given item.  Provide the item in
        // the view holder tag field.
        holder.name.setText(item.name);
        if (holder.text != null) {
            if (item.text == null || item.text.length() == 0)
                holder.text.setVisibility(View.GONE);
            else if (item.mGroupKeyList != null) {
                List<String> groupNames = new ArrayList<>();
                for (String aGroupKey : item.mGroupKeyList) {
                    Group g = GroupManager.instance.getGroupProfile(aGroupKey);
                    if (g != null)
                        groupNames.add(g.name);
                }
                String groupList = TextUtils.join(", ", groupNames);
                holder.text.setText(groupList);
            }
            else
                holder.text.setText(CompatUtils.fromHtml(item.text));

        }
        setIcon(holder, item);
        setEndIcon(holder, item);
        holder.itemView.setTag(item);

        // Set the new message count field, if necessary.
        if (item.count > 0) {
            String text = String.format(Locale.getDefault(), "%d new", item.count);
            holder.count.setText(text);
            holder.count.setVisibility(View.VISIBLE);
        } else {
            if (holder.count != null) holder.count.setVisibility(View.GONE);
        }

        // Set the selector button checked state using the item selection state, enable or
        // disable the button if the item has something to do with the common room, and attach the
        // item as a payload to the button tag.
        if (holder.button == null || !(holder.button instanceof CompoundButton))
            return;
        CompoundButton settableButton = (CompoundButton) holder.button;
        settableButton.setChecked(item.selected);
        holder.button.setEnabled(getEnableState(item));
        holder.button.setTag(item);
    }

    /** Use the item passed to update the specified contact view holder */
    private void updateContactHolder(ContactViewHolder holder, final ListItem item) {
        holder.name.setText(item.name);
        holder.email.setText(item.email);
        Context context = holder.icon.getContext();
        if (item.iconUrl == null) {
            holder.icon.setImageResource(R.drawable.ic_account_circle_black_48dp);
        } else {
            Uri imageUri = Uri.parse(item.iconUrl);
            if (imageUri != null) {
                // There is an image to load.  Use Glide to do the heavy lifting.
                holder.icon.setImageURI(imageUri);
                Glide.with(context)
                        .load(item.iconUrl)
                        .transform(new NavigationManager.CircleTransform(context))
                        .into(holder.icon);
            } else
                holder.icon.setImageResource(R.drawable.ic_account_circle_black_48dp);
        }
    }

    /** Return TRUE iff the given item's selector button should be enabled. */
    private boolean getEnableState(@NonNull final ListItem item) {
        switch (item.type) {
            case inviteCommonRoom:
                return false;
            default:
                return true;
        }
    }

    // Inner classes.

    /** Provide a handler for clicks on the selector button. */
    private class SelectorClickListener implements View.OnClickListener {
        public void onClick(View view) {
            // Post the click event to the app.
            AppEventManager.instance.post(new ClickEvent(view));
        }
    }

    /** Provide a handler for clicks on the end icon */
    private class IconCLickListener implements View.OnClickListener {
        public void onClick(View view) {
            // Post the click event to the app
            AppEventManager.instance.post(new ClickEvent(view));
        }
    }

    /** Provide a view holder for a chat list item. */
    private class ItemListViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView count;
        TextView text;
        ImageView icon;
        ImageView endIcon;
        ImageView veryEndIcon;
        Button button;

        /** Build an instance given the item view. */
        ItemListViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.Name);
            count = (TextView) itemView.findViewById(R.id.Count);
            text = (TextView) itemView.findViewById(R.id.Text);
            icon = (ImageView) itemView.findViewById(R.id.ListItemIcon);
            endIcon = (ImageView) itemView.findViewById(R.id.endIcon);
            veryEndIcon = (ImageView) itemView.findViewById(R.id.veryEndIcon);
            if (endIcon != null) {
                endIcon.setOnClickListener(endIconListener);
            }
            if (veryEndIcon != null) {
                veryEndIcon.setOnClickListener(endIconListener);
            }
            setSelectorButton(itemView);
            if (button != null) {
                button.setOnClickListener(selectorListener);
            }
        }

        /** Get a selector button dealing with the pathological case of a single radio button. */
        private void setSelectorButton(@NonNull final View itemView) {
            // Determine if this item view is the ugly case of a single radio button.  If not,
            // abort.  If so replace the radio button with a check box alternative.
            button = (Button) itemView.findViewById(R.id.selector);
            if (button == null || mList.size() > 1 || !(button instanceof RadioButton))
                return;
            button.setVisibility(View.GONE);
            button = (Button) itemView.findViewById(R.id.altSelector);
        }
    }

    /** Provide a view holder for 'new' item */
    private class NewItemViewHolder extends RecyclerView.ViewHolder {

        // Private instance variables.

        TextView name;
        ImageView icon;

        /** Build an instance give the item view. */
        NewItemViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.NewName);
            icon = (ImageView) itemView.findViewById(R.id.NewListItemIcon);
        }
    }

    /** Provide a view holder for a help list item */
    private class HelpListViewHolder extends RecyclerView.ViewHolder {

        // Private instance variables.

        TextView title;
        ImageView icon;

        /** Build an instance given the item view. */
        HelpListViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            icon = (ImageView) itemView.findViewById(R.id.ListItemIcon);
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
