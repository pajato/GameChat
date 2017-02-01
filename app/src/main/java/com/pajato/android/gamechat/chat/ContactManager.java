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

package com.pajato.android.gamechat.chat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Data;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pajato.android.gamechat.common.adapter.ContactItem;
import com.pajato.android.gamechat.common.adapter.ListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.Contacts.CONTENT_URI;
import static android.provider.ContactsContract.Contacts.HAS_PHONE_NUMBER;
import static android.provider.ContactsContract.Contacts.Photo.CONTENT_DIRECTORY;

/**
 * Provides the interface to the device contact list.
 *
 * @author Paul Michael Reilly
 */
public enum ContactManager {
    instance;

    // Public enums.

    // Private class constants.
    public static final int REQUEST_CONTACTS = 1;

    /** The logcat tag. */
    private static final String TAG = ContactManager.class.getSimpleName();

    // Private instance variables.

    /** The contact cache, a map associating a name and a chat list item. */
    private Map<String, ListItem> mContactMap = new HashMap<>();

    // Public instance methods.

    /** Return a list of device contacts formatted as list items. */
    public List<ListItem> getDeviceContactList() {
        // Convert each contact to a list item.
        List<ListItem> result = new ArrayList<>();
        for (String name : mContactMap.keySet()) {
            result.add(mContactMap.get(name));
        }

        return result;
    }

    /** Get permissions to access contacts. Not used, but could be added to MainActivity init */
    public void init(@NonNull final AppCompatActivity context) {
        getPermission(context);
    }

    /** Check for and if necessary ask user for permission to access contacts */
    public void getPermission(@NonNull final Activity context) {
        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // we can proceed
            fetchContacts(context);
            return;
        }

        // ask the user for permission (assume no explanation is needed)
        ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS);
    }

    /** Handle response from request for permission to access contacts. Return true on success. */
    public boolean onRequestContactsResult(Activity context, String permissions[],
                                             int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchContacts(context);
            return true;
        } else if (grantResults.length <= 0) {
            // How to disable the functionality that depends on this permission.
            Log.e(TAG, "grantResults length is invalid; cannot access contacts");
            return false;
        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            Log.w(TAG, "Permission denied for contacts access");
            return false;
        }
    }

    // Private instance methods.

    /** Fetch the contacts having already determined that access is granted. */
    private void fetchContacts(@NonNull final Activity context) {
        Log.d(TAG, "Starting to populate the contacts cache.");
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(CONTENT_URI, null, null, null, null);
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            // Determine if this row is for a contact not seen yet.
            String name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
            if (!mContactMap.containsKey(name)) {
                // The contact has not yet been see.  Cache it now, using the first email and/or
                // phone number encountered.
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String email = getEmail(resolver, id);
                String value = cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER));
                String phone = Integer.parseInt(value) > 0 ? getPhone(resolver, id) : null;
                String url = getPhotoUrl(resolver, id);
                if ((email != null && email.length() > 0) || phone != null && phone.length() > 0) {
                    ListItem item = new ListItem(new ContactItem(name, email, phone, url));
                    mContactMap.put(name, item);
                }
            }
        }
        cursor.close();
        Log.d(TAG, "Finished populating the contacts cache.");
    }

    /** Return the first email from a contact, null if there is no email entries. */
    private String getEmail(@NonNull final ContentResolver resolver, @NonNull final String id) {
        // Query the email entries in the contact with the given id.
        String result = null;
        String queryText = Email.CONTACT_ID + " = " + id;
        Cursor emails = resolver.query(Email.CONTENT_URI, null, queryText, null, null);
        if (emails == null) return null;

        // ...
        if (emails.moveToNext()) result = emails.getString(emails.getColumnIndex(Email.DATA));
        emails.close();

        return result;
    }

    /** Return the first phone number from a contact, null if there are no phone numbers. */
    private String getPhone(@NonNull final ContentResolver resolver, @NonNull final String id) {
        // Query the phone entries in the contact with the given id.
        String result = null;
        String queryText = Phone.CONTACT_ID + " = ?";
        String[] queryData = new String[] {id};
        Cursor cursor = resolver.query(Phone.CONTENT_URI, null, queryText, queryData, null);
        if (cursor == null) return null;

        // ...
        if (cursor.moveToNext()) result = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
        cursor.close();

        return result;
    }

    /** Return the contact photo URL, null if no photo URL is available. */
    private String getPhotoUrl(@NonNull final ContentResolver resolver, @NonNull final String id) {
        // Query the phone entries in the contact with the given id.
        String result = null;
        String queryText = Data.CONTACT_ID + " = " + id + " AND " + Data.MIMETYPE + "='"
            + Photo.CONTENT_ITEM_TYPE + "'";
        Cursor cursor = resolver.query(Data.CONTENT_URI, null, queryText, null, null);
        if (cursor == null) return null;

        // ...
        if (cursor.moveToNext()) {
            Uri person = ContentUris.withAppendedId(CONTENT_URI, Long.parseLong(id));
            result = Uri.withAppendedPath(person, CONTENT_DIRECTORY).toString();
        }
        cursor.close();

        return result;
    }

}
