package com.example.wisatajogja.Data

import android.app.Application
import android.location.Location
import android.util.Log
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.StandardExceptionParser
import com.google.android.gms.analytics.Tracker

class ThisApplication : Application() {

    private var mInstance: ThisApplication? = null
    private var tracker: Tracker? = null
    private var location: Location? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(Constant.LOG_TAG, "onCreate : ThisApplication")
        mInstance = this

        //init image loader
        Tools.initImageLoader(applicationContext)

        // activate analytics tracker
        getGoogleAnalyticsTracker()
    }

    @Synchronized
    fun getInstance(): ThisApplication? {
        return mInstance
    }


    /**
     * --------------------------------------------------------------------------------------------
     * For Google Analytics
     */
    @Synchronized
    fun getGoogleAnalyticsTracker(): Tracker? {
        if (tracker == null) {
            val analytics = GoogleAnalytics.getInstance(this)
            analytics.setDryRun(!AppConfig.ENABLE_ANALYTICS)
            tracker = analytics.newTracker(R.xml.app_tracker)
        }
        return tracker
    }

    fun trackScreenView(screenName: String) {
        val t = getGoogleAnalyticsTracker()
        // Set screen name.
        t!!.setScreenName(screenName)
        // Send a screen view.
        t!!.send(HitBuilders.ScreenViewBuilder().build())
        GoogleAnalytics.getInstance(this).dispatchLocalHits()
    }

    fun trackException(e: Exception?) {
        if (e != null) {
            val t = getGoogleAnalyticsTracker()
            t!!.send(
                HitBuilders.ExceptionBuilder()
                    .setDescription(
                        StandardExceptionParser(
                            this,
                            null
                        ).getDescription(Thread.currentThread().name, e)
                    )
                    .setFatal(false)
                    .build()
            )
        }
    }

    fun trackEvent(category: String, action: String, label: String) {
        val t = getGoogleAnalyticsTracker()
        // Build and send an Event.
        t!!.send(HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build())
    }

    /** ---------------------------------------- End of analytics ---------------------------------  */

    fun getLocation(): Location? {
        return location
    }

    fun setLocation(location: Location) {
        this.location = location
    }
}
