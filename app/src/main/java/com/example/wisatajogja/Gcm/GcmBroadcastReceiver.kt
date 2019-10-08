package com.example.wisatajogja.Gcm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator
import androidx.legacy.content.WakefulBroadcastReceiver
import com.example.wisatajogja.Data.AppConfig
import com.example.wisatajogja.Data.SharedPref
import com.nostra13.universalimageloader.core.ImageLoader

class GcmBroadcastReceiver : WakefulBroadcastReceiver() {
    private val VIBRATION_TIME = 500 // in millisecond
    private var sharedPref: SharedPref? = null
    private val imgloader = ImageLoader.getInstance()

    override fun onReceive(p0: Context?, p1: Intent?) {
        sharedPref = SharedPref(p0!!)

        sharedPref!!.setRefreshPlaces(true)
        if (imgloader.isInited && AppConfig.REFRESH_IMG_NOTIF) {
            imgloader.clearDiskCache()
            imgloader.clearMemoryCache()
        }

        if (sharedPref!!.getNotification()) {
            playRingtoneVibrate(p0)
            startWakefulService(p0, p1!!.setComponent(ComponentName (p0.getPackageName(), GcmIntentService::class.java!!.getName())))
        }
    }

    private fun playRingtoneVibrate(context: Context) {
        try {
            // play vibration
            if (sharedPref!!.getVibration()) {
                (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(
                    VIBRATION_TIME.toLong()
                )
            }
            RingtoneManager.getRingtone(context, Uri.parse(sharedPref!!.getRingtone())).play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}