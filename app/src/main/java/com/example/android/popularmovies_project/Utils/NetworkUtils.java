package com.example.android.popularmovies_project.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class NetworkUtils {
    private NetworkUtils() {}

    public static boolean hasInternetConnection(Context context) {
        // first check if network is available or not
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        } else {
            // check for internet connection if network is available\
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("8.8.8.8", 53), 2500);
                socket.close();
                // has internet connection
                return true;
            } catch (IOException e) {
                // no internet connection
                return false;
            }
        }
    }
}
