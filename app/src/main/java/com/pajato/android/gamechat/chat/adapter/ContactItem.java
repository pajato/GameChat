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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pajato.android.gamechat.account.AccountManager;
import com.pajato.android.gamechat.chat.model.Message;
import com.pajato.android.gamechat.database.DatabaseManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provide a POJO to encapsulate a contact item to be added to a recycler view.
 *
 * @author Paul Michael Reilly
 */
public class ContactItem {

    // Private class constants.

    // Public instance variables.

    /** The email address, possibly null. */
    public String email;

    /** The phone number, possibly null. */
    public String phone;

    /** The contact's icon URL, possibly null. */
    String url;

    /** The contact's display name. */
    public String name;

    // Public constructors.

    /** Build an instance for the given group. */
    public ContactItem(final String name, final String email, final String phone, final String url) {
        // Update the group and room keys, the message text and url fields, and set the count to 0
        // to flag that it is not relevant for a message item.  Set the name field to the poster's
        // display name concatenated with the creation date.
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.url = url;
    }

}
