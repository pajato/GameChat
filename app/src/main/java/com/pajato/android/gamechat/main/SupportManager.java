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

package com.pajato.android.gamechat.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * Provide a singleton to handle support operations like providing feedback and filing bug reports,
 * etc.
 *
 * @author Paul Michael Reilly
 */
enum SupportManager {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = SupportManager.class.getSimpleName();

    // Public instance methods

    /** Send email on a given subject to the support address. */
    public void sendFeedback(final Activity activity, final String subject) {
        // Send the message using the given subject and an empty list of attachments.
        sendFeedback(activity, subject, "Message: ", new ArrayList<String>());
    }

    /** Send email to the support address with a subject and zero or more attachments. */
    public void sendFeedback(final Activity activity, final String subject,
                             final List<String> attachments) {
        sendFeedback(activity, subject, "Message: ", attachments);
    }

    /** Send email with a given subject and body text to the support address. */
    public void sendFeedback(final Activity activity, final String subject, final String body,
                             final List<String> attachments) {
        // Set up the intent with the main extras.
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL  , new String[] {"support@pajato.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT   , body != null ? body : "no message");

        // Add the attachment extras.
        ArrayList<Uri> uriList = new ArrayList<>();
        for (String path : attachments) {
            Uri uri = getUriForFile(activity, "com.pajato.fileprovider", new File(path));
            uriList.add(uri);
            Log.d(TAG, String.format(Locale.US, "Attaching file with URI {%s}.", uri));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

        // Start the mailer activity of choice.
        try {
            activity.startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, "No email clients are installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
