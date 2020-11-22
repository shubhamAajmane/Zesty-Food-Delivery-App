package com.example.zesty.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectivityManager {

    fun checkConnectivity(context: Context): Boolean {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo

        var isConnected: Boolean = false

        if (activeNetwork?.isConnected != null) {

            if (activeNetwork?.isConnected)
                isConnected = activeNetwork.isConnected
        } else {
            isConnected = false
        }
        return isConnected
    }
}