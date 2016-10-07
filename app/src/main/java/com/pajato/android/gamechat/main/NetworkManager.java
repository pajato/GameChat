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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Provide a singleton to monitor the network state. For now the state is always assumed to be
 * connected.  In the future the class should be enhanced to smooth out network disconnects.
 *
 * @author Paul Michael Reilly
 */
public enum NetworkManager {
    instance;

    // Private instance variables.

    private boolean online;

    // Public instance methods

    /** Return TRUE iff the network is connected. */
    public boolean isConnected() {
        // TODO: enhance this to smooth out short term fluctuations, like less than 30 seconds.
        return online;
    }

    /** Set the current network connection state. */
    public void init(final Context context) {
        ConnectivityManager manager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        online = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
