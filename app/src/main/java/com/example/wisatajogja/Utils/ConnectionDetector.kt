package com.example.wisatajogja.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

@Suppress("DEPRECATION")
class ConnectionDetector {

    private var ctx: Context? = null

    fun ConnectionDetector(context: Context){
        this.ctx = context
    }

    /**
     * Checking for all possible internet providers
     */
    fun isConnectingToInternet(): Boolean {
        val connectivity =
            ctx!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            if (info != null)
                for (i in info.indices)
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }

        }
        return false
    }
}