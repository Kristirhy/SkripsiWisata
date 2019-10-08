package com.example.wisatajogja.Gcm

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import com.example.wisatajogja.Activity.ActivityNewsInfoDetails
import com.example.wisatajogja.Activity.ActivityPlaceDetails
import com.example.wisatajogja.Activity.ActivitySplashScreen
import com.example.wisatajogja.Data.Constant
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.gson.Gson
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener

class GcmIntentService : IntentService() {
    private val TAG = GcmIntentService::class.java.name
    private val imgloader = ImageLoader.getInstance()
    private var retry_count = 0

    fun GcmIntentService() {
        super(TAG)
        if (!imgloader.isInited) Tools.initImageLoader(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        showGcmNotif(intent!!)
    }

    private fun showGcmNotif(intent: Intent) {
        val gcm = GoogleCloudMessaging.getInstance(this)
        val messageType = gcm.getMessageType(intent)
        //Toast.makeText(this, "messageType : " + messageType, Toast.LENGTH_SHORT).show();
        if (!intent.extras!!.isEmpty) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE == messageType) {
                val gcmNotif = GcmNotif()
                gcmNotif.setTitle(intent.getStringExtra("title"))
                gcmNotif.setContent(intent.getStringExtra("content"))
                gcmNotif.setType(intent.getStringExtra("type"))

                // load data place if exist
                val place_str = intent.getStringExtra("place")
                val p = if (place_str != null) Gson().fromJson<Place>(
                    place_str,
                    Place::class.java!!
                ) else null
                gcmNotif.setPlace(p)

                // load data news_info if exist
                val news_str = intent.getStringExtra("news")
                val n = if (news_str != null) Gson().fromJson<NewsInfo>(
                    news_str,
                    NewsInfo::class.java!!
                ) else null
                gcmNotif.setNews(n)
                loadRetryImageFromUrl(gcmNotif, object : CallbackImageNotif {
                    override fun onSuccess(bitmap: Bitmap?) {
                        displayNotificationIntent(gcmNotif, bitmap)
                    }

                    override fun onFailed() {
                        displayNotificationIntent(gcmNotif, null)
                    }
                })
            }
        }
    }

    private fun displayNotificationIntent(gcmNotif: GcmNotif, bitmap: Bitmap?) {
        var intent = Intent(this, ActivitySplashScreen::class.java)
        // handle notification for open Place Details
        if (gcmNotif.getPlace() != null) {
            intent = ActivityPlaceDetails.navigateBase(this, gcmNotif.getPlace(), true)
        } else if (gcmNotif.getNews() != null) { // handle notification for open News Info Details
            DatabaseHandler(this).refreshTableNewsInfo()
            intent = ActivityNewsInfoDetails.navigateBase(this, gcmNotif.getNews(), true)
        }
        val pendingIntent =
            PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, 0)

        val builder = NotificationCompat.Builder(this)
        builder.setContentTitle(gcmNotif.getTitle())
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(gcmNotif.getContent()))
        builder.setContentText(gcmNotif.getContent())
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setDefaults(Notification.DEFAULT_LIGHTS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_HIGH)
        }
        if (bitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText(
                    gcmNotif.getContent()
                )
            )
        }
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val unique_id = System.currentTimeMillis().toInt()
        notificationManager.notify(unique_id, builder.build())
    }

    private fun loadRetryImageFromUrl(gcmNotif: GcmNotif, callback: CallbackImageNotif) {
        var url = ""
        if (gcmNotif.getPlace() != null) {
            url = Constant.getURLimgPlace(gcmNotif.getPlace().image)
        } else if (gcmNotif.getNews() != null) {
            url = Constant.getURLimgNews(gcmNotif.getNews().image)
        } else {
            callback.onFailed()
            return
        }
        loadImageFromUrl(url, object : CallbackImageNotif {
            override fun onSuccess(bitmap: Bitmap?) {
                callback.onSuccess(bitmap)
            }

            override fun onFailed() {
                Log.e("onFailed", "on Failed")
                if (retry_count <= Constant.LOAD_IMAGE_NOTIF_RETRY) {
                    retry_count++
                    Handler().postDelayed({ loadRetryImageFromUrl(gcmNotif, callback) }, 1000)
                } else {
                    callback.onFailed()
                }
            }
        })
    }

    private fun loadImageFromUrl(url: String, callback: CallbackImageNotif) {
        imgloader.loadImage(url, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                callback.onSuccess(loadedImage)
            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                callback.onFailed()
                super.onLoadingFailed(imageUri, view, failReason)
            }
        })
    }

    private interface CallbackImageNotif {
        fun onSuccess(bitmap: Bitmap?)
        fun onFailed()
    }
}
