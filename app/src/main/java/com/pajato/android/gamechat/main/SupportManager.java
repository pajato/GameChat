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
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.pajato.android.gamechat.BuildConfig;
import com.pajato.android.gamechat.event.AppEventManager;
import com.pajato.android.gamechat.event.MenuItemEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public enum SupportManager {
    instance;

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = SupportManager.class.getSimpleName();

    // Public instance methods

    /** Send email with a given subject and body text to the support address. */
    public void sendFeedback(final Activity activity, final String subject, final String body,
                             final String bitmapPath, final String logCatPath) {
        // Set up the intent with the main extras.
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@pajato.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body != null ? body : "no message");
        ArrayList<Uri> uriList = new ArrayList<>();
        uriList.add(getUriForFile(activity, "com.pajato.fileprovider", new File(bitmapPath)));
        uriList.add(getUriForFile(activity, "com.pajato.fileprovider", new File(logCatPath)));
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        // Start the mailer activity of choice.
        try {
            activity.startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, "No email clients are installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Send email with a given subject and body text to the support address. */
    public void sendFeedback(final Activity activity, final String subject, final String body,
                             final List<String> attachments) {
        // Set up the intent with the main extras.
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@pajato.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body != null ? body : "no message");

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

    /** Return the file where logcat data has been placed, null if no data is available. */
    public String getLogcatPath(final Activity activity) {
        // Capture the current state of the logcat file.
        File dir = new File(activity.getFilesDir(), "logcat");
        if (!dir.exists() && !dir.mkdirs()) return null;

        File outputFile = new File(dir, "logcat.txt");
        try {
            Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException exc) {
            Log.e(TAG, exc.getMessage(), exc);
            return null;
        }

        Log.d(TAG, String.format("File size is %d.", outputFile.length()));
        Log.d(TAG, String.format("File path is {%s}.", outputFile.getPath()));

        return outputFile.getPath();
    }

    /** Return null if the given bitmap cannot be saved or the file path it has been saved to. */
    public String getBitmapPath(final Bitmap bitmap, final Activity activity) {
        // Create the image file on internal storage.  Abort if the subdirectories cannot be
        // created.
        FileOutputStream outputStream;
        File dir = new File(activity.getFilesDir(), "images");
        if (!dir.exists() && !dir.mkdirs()) return null;

        // Flush the bitmap to the image file as a stream and return the result.
        File imageFile = new File(dir, "screenshot.png");
        Log.d(TAG, String.format(Locale.US, "Image file path is {%s}", imageFile.getPath()));
        try {
            outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException exc) {
            Log.e(TAG, exc.getMessage(), exc);
            return null;
        }
        return imageFile.getPath();
    }

    /** Return "about" information: a string describing the app and it's version information. */
    public String getAbout() {
        final String format = "GameChat %s-%d Bug Report";
        final String name = BuildConfig.VERSION_NAME;
        final int code = BuildConfig.VERSION_CODE;
        return String.format(Locale.US, format, name, code);
    }

    /** Handle a bug report by performing a screen capture, grabbing logcat and sending email. */
    public void handleBugReport(final Activity activity, final MenuItemEvent event) {
        // Capture the screen (with any luck, sans menu.), send the message and cancel event
        // propagation.
        View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        List<String> attachments = new ArrayList<>();
        String path = SupportManager.instance.getBitmapPath(rootView.getDrawingCache(), activity);
        if (path != null) attachments.add(path);
        path = SupportManager.instance.getLogcatPath(activity);
        if (path != null) attachments.add(path);
        SupportManager.instance.sendFeedback(activity, SupportManager.instance.getAbout(),
                "Extra information: ", attachments);
        AppEventManager.instance.cancel(event);
    }


}
