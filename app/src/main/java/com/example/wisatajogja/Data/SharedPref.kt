package com.example.wisatajogja.Data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import com.example.wisatajogja.R

class SharedPref {


    private val MAX_OPEN_COUNTER = 15

    private val GCM_PREF_KEY = "app.thecity.data.GCM_PREF_KEY"
    private val SERVER_FLAG_KEY = "app.thecity.data.SERVER_FLAG_KEY"
    private val THEME_COLOR_KEY = "app.thecity.data.THEME_COLOR_KEY"
    private val LAST_PLACE_PAGE = "LAST_PLACE_PAGE_KEY"

    // need refresh
    val REFRESH_PLACES = "app.thecity.data.REFRESH_PLACES"
    internal var context: Context? = null
    private var sharedPreferences: SharedPreferences? = null

    constructor(){}
    constructor(context: Context){
        this.context = context
        sharedPreferences =
            context.getSharedPreferences("ProjectKristi", Context.MODE_PRIVATE)
    }

    fun setGCMRegId(gcmRegId: String) {
        sharedPreferences!!.edit().putString(GCM_PREF_KEY, gcmRegId).apply()
    }

    fun getGCMRegId(): String? {
        return sharedPreferences!!.getString(GCM_PREF_KEY, null)
    }

    fun isGcmRegIdEmpty(): Boolean {
        return TextUtils.isEmpty(getGCMRegId())
    }

    fun setRegisteredOnServer(registered: Boolean) {
        sharedPreferences!!.edit().putBoolean(SERVER_FLAG_KEY, registered).apply()
    }

    fun isRegisteredOnServer(): Boolean {
        return sharedPreferences!!.getBoolean(SERVER_FLAG_KEY, false)
    }

    fun isNeedRegisterGcm(): Boolean {
        return isGcmRegIdEmpty() || !isRegisteredOnServer()
    }

    /**
     * For notifications flag
     */
    fun getNotification(): Boolean {
        return sharedPreferences!!.getBoolean(context!!.getString(R.string.pref_key_notif), true)
    }

    fun getRingtone(): String? {
        return sharedPreferences!!.getString(
            context!!.getString(R.string.pref_key_ringtone),
            "content://settings/system/notification_sound"
        )
    }

    fun getVibration(): Boolean {
        return sharedPreferences!!.getBoolean(context!!.getString(R.string.pref_key_vibrate), true)
    }

    fun isRefreshPlaces(): Boolean {
        return sharedPreferences!!.getBoolean(REFRESH_PLACES, false)
    }

    fun setRefreshPlaces(need_refresh: Boolean) {
        sharedPreferences!!.edit().putBoolean(REFRESH_PLACES, need_refresh).apply()
    }


    /**
     * For theme color
     */
    fun setThemeColor(color: String) {
        sharedPreferences!!.edit().putString(THEME_COLOR_KEY, color).apply()
    }

    fun getThemeColor(): String? {
        return sharedPreferences!!.getString(THEME_COLOR_KEY, "")
    }

    fun getThemeColorInt(): Int {
        return if (getThemeColor() == "") {
            context!!.resources.getColor(R.color.colorPrimary)
        } else Color.parseColor(getThemeColor())
    }


    /**
     * To save last state request
     */
    fun setLastPlacePage(page: Int) {
        sharedPreferences!!.edit().putInt(LAST_PLACE_PAGE, page).apply()
    }

    fun getLastPlacePage(): Int {
        return sharedPreferences!!.getInt(LAST_PLACE_PAGE, 1)
    }


    /**
     * To save dialog permission state
     */
    fun setNeverAskAgain(key: String, value: Boolean) {
        sharedPreferences!!.edit().putBoolean(key, value).apply()
    }

    fun getNeverAskAgain(key: String): Boolean {
        return sharedPreferences!!.getBoolean(key, false)
    }

    // when app open N-times it will update gcm RegID at server
    fun isOpenAppCounterReach(): Boolean {
        val counter = sharedPreferences!!.getInt("OPEN_COUNTER_KEY", MAX_OPEN_COUNTER) + 1
        setOpenAppCounter(counter)
        Log.e("COUNTER", "" + counter)
        return counter >= MAX_OPEN_COUNTER
    }

    fun setOpenAppCounter(`val`: Int) {
        sharedPreferences!!.edit().putInt("OPEN_COUNTER_KEY", `val`).apply()
    }

}
