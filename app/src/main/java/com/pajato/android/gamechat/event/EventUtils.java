/*
 * Copyright (C) 2016 Pajato Technologies, Inc.
 *
 * This file is part of Pajato GameChat.

 * GameChat is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GameChat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.

 * You should have received a copy of the GNU General Public License along with GameChat. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.pajato.android.gamechat.event;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

/**
 * Provides a set of utility methods for EventBus interactions.
 *
 * @author Paul Michael Reilly
 */
public class EventUtils {

    // Public class methods.

    /** Post a click event for a given view. */
    public static void post(final Context context, final View view) {
        int value = view.getTag() != null ? getIntegerTag(view) : view.getId();
        String className = view.getClass().getSimpleName();
        EventBus.getDefault().post(new ClickEvent(context, value, view, null, className));
    }

    /** Post a click event for a given item. */
    public static void post(final Context context, final MenuItem item) {
        int value = item.getItemId();
        String className = item.getClass().getSimpleName();
        EventBus.getDefault().post(new ClickEvent(context, value, null, item, className));
    }

    /** Return the integer value of the tag in the given view, -1 if the value is not an integer. */
    private static int getIntegerTag(final View view) {
        // Handle a string tag.
        Object o = view.getTag();
        if (o instanceof String) {
            String s = (String) o;
            return Integer.valueOf(s);
        }

        // Handle an integer tag.
        if (o instanceof Integer) {
            return (Integer) o;
        }

        // Default to -1.
        return -1;
    }

}
