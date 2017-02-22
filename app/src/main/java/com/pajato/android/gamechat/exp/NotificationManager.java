/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.pajato.android.gamechat.exp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.InviteEvent;
import com.pajato.android.gamechat.event.TagClickEvent;

import static com.pajato.android.gamechat.event.InviteEvent.ItemType.group;
import static com.pajato.android.gamechat.event.InviteEvent.ItemType.room;
import static com.pajato.android.gamechat.exp.NotificationManager.NotifyType.chat;
import static com.pajato.android.gamechat.exp.NotificationManager.NotifyType.experience;

/**
 * Manages the presentation of UI messages, currently via a Snackbar message.
 *
 * @author Bryan Scott
 * @author Paul Michael Reilly
 */
public enum NotificationManager {
    instance;

    public enum NotifyType {chat, experience}

    // Public instance methods.

    /** Create a Snackbar notification indicating a group has been created */
    public void notifyGroupCreate(@NonNull final Fragment fragment,  @NonNull String groupKey,
                                  @NonNull String groupName) {
        Context context = fragment.getContext();
        String text = String.format(context.getString(R.string.ItemCreatedMessage), groupName);
        final String sendInvites = context.getString(R.string.InviteFriendMessage);
        showSnackbar(fragment, text, sendInvites, new SnackbarGroupActionHandler(groupKey), chat);
    }

    /** Create a Snackbar notification indicating a room has been created */
    public void notifyRoomCreate(@NonNull final Fragment fragment, @NonNull String roomKey,
                                 @NonNull String roomName) {
        Context context = fragment.getContext();
        String text = String.format(context.getString(R.string.ItemCreatedMessage), roomName);
        final String sendInvites = fragment.getContext().getString(R.string.InviteFriendMessage);
        showSnackbar(fragment, text, sendInvites, new SnackbarRoomActionHandler(roomKey), chat);
    }

    /** Show a snackbar notification for game-complete */
    public void notifyGameDone(@NonNull final Fragment fragment, final String text) {
        final String playAgain = fragment.getContext().getString(R.string.PlayAgain);
        showSnackbar(fragment, text, playAgain, new SnackbarActionHandler(fragment), experience);
    }

    /** Create and show a Snackbar notification for game-complete, based on the given parameters. */
    public void notifyNoAction(@NonNull final Fragment fragment, final String text) {
        // The game is ended so generate a notification that could start a new game.
        final String playAgain = fragment.getContext().getString(R.string.PlayAgain);
        showSnackbar(fragment, text, playAgain, new SnackbarActionHandler(fragment), experience);
    }

    /** Put up a snackbar for the given experience fragment and resource string. */
    public void notifyNoAction(final Fragment fragment, final int resId, final NotifyType type) {
        String message = fragment.getContext().getString(resId);
        showSnackbar(fragment, message, null, null, type);
    }

    /** Put up a snackbar for the given experience fragment and resource string. */
    public void notify(final Fragment fragment, final int resId) {
        String message = fragment.getContext().getString(resId);
        notifyNoAction(fragment, message);
    }

    /** Show a snackbar message. If actionText is null, use a short duration with no action. */
    private void showSnackbar(@NonNull final Fragment fragment, @NonNull final String message,
                              final String actionText, final View.OnClickListener listener,
                              NotifyType type) {
        // Ensure that the fragment is attached and has a view.  Abort if it does not.
        if (fragment.getView() == null)
            return;
        Snackbar snackbar;
        if (actionText == null) {
            snackbar = Snackbar.make(fragment.getView(), message, Snackbar.LENGTH_SHORT);
        } else {
            snackbar = Snackbar.make(fragment.getView(), message, Snackbar.LENGTH_LONG);
            if (listener != null)
                snackbar.setAction(actionText, listener);
        }

        // Use a primary color background with white text for the snackbar and hide the FAB button
        // while the snackbar is presenting.
        int color = ContextCompat.getColor(fragment.getContext(), R.color.colorPrimaryDark);
        snackbar.getView().setBackgroundColor(color);
        snackbar.addCallback(new SnackbarChangeHandler(fragment, type))
                .setActionTextColor(ColorStateList.valueOf(Color.WHITE))
                .show();
    }

    // Inner classes.

    /** Provide a handler to show/hide the FAB for snackbar messaging for game fragments. */
    private class SnackbarChangeHandler extends Snackbar.Callback {

        // Instance variables.

        /** The calling fragment. */
        Fragment mFragment;

        /** Whether this notification is from an experience or chat */
        NotifyType mType;

        // Constructors

        /** Build an instance with a given fragment. */
        SnackbarChangeHandler(final Fragment fragment, NotifyType type) {
            mFragment = fragment;
            mType = type;
        }

        @Override public void onDismissed(final Snackbar snackbar, final int event) {
            if (mType == chat)
                FabManager.chat.show(mFragment);
            else
                FabManager.game.show(mFragment);
        }

        @Override public void onShown(final android.support.design.widget.Snackbar snackbar) {
            if (mType == chat)
                FabManager.chat.hide(mFragment);
            else
                FabManager.game.hide(mFragment);
        }
    }

    /** Handle a snackbar action click specifically to send an invitation to a group */
    private class SnackbarRoomActionHandler implements View.OnClickListener {
        // Instance variables.

        /** The room key to which to send an invitation */
        String mRoomKey;

        // Constructor

        /** Build an instance with a given room key */
        SnackbarRoomActionHandler(final String key) {
            mRoomKey = key;
        }

        /** Handle an action click from the snackbar by posting the event to the app */
        @Override public void onClick(final View view) {
            view.setTag(mRoomKey);
            AppEventManager.instance.post(new InviteEvent(mRoomKey, room));
        }
    }

    /** Handle a snackbar action click specifically to send an invitation to a group */
    private class SnackbarGroupActionHandler implements View.OnClickListener {
        // Instance variables.

        /** The group key to which to send an invitation */
        String mGroupKey;

        // Constructor

        /** Build an instance with a given group key */
        SnackbarGroupActionHandler(final String key) {
            mGroupKey = key;
        }

        /** Handle an action click from the snackbar by posting the event to the app */
        @Override public void onClick(final View view) {
            view.setTag(mGroupKey);
            AppEventManager.instance.post(new InviteEvent(mGroupKey, group));
        }
    }

    /** Handle a snackbar action click. */
    private class SnackbarActionHandler implements View.OnClickListener {

        // Instance variables.

        /** The tag value to post with the event to the app. */
        String mClassName;

        // Constructor.

        /** Build an instance with a given tag value. */
        SnackbarActionHandler(final Fragment fragment) {
            mClassName = fragment.getClass().getSimpleName();
        }

        /** Handle an action click from the snackbar by posting the tag to the app. */
        @Override public void onClick(final View view) {
            // Post the saved tag (the originating fragment's tag) to the app.
            view.setTag(mClassName);
            AppEventManager.instance.post(new TagClickEvent(view));
        }

    }
}
