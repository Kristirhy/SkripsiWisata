package com.example.wisatajogja.Utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import com.example.wisatajogja.Activity.ActivitySplashScreen
import com.example.wisatajogja.Data.SharedPref
import com.example.wisatajogja.Activity.MainActivity
import com.example.wisatajogja.Data.AppConfig
import com.example.wisatajogja.Data.Constant
import com.example.wisatajogja.Data.ThisApplication
import com.example.wisatajogja.Model.DeviceInfo
import com.example.wisatajogja.Model.NewsInfo
import com.example.wisatajogja.Model.Place
import com.example.wisatajogja.R
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class Tools {

    fun needRequestPermission(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    fun isLolipopOrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun systemBarLolipop(act: Activity) {
        if (isLolipopOrHigher()) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Tools.colorDarker(SharedPref(act).getThemeColorInt())
        }
    }

    fun cekConnection(context: Context): Boolean {
        val conn = ConnectionDetector(context)
        return if (conn.isConnectingToInternet()) {
            true
        } else {
            false
        }
    }

    fun initImageLoader(context: Context) {
        val options = DisplayImageOptions.Builder()
            .cacheInMemory(AppConfig.IMAGE_CACHE)
            .cacheOnDisk(AppConfig.IMAGE_CACHE)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build()

        val config = ImageLoaderConfiguration.Builder(context)
            .defaultDisplayImageOptions(options)
            .threadPoolSize(3)
            .memoryCache(WeakMemoryCache())
            .build()

        ImageLoader.getInstance().init(config)
    }

    fun getGridOption(): DisplayImageOptions {

        return DisplayImageOptions.Builder()
            .cacheInMemory(AppConfig.IMAGE_CACHE)
            .cacheOnDisk(AppConfig.IMAGE_CACHE)
            .build()
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE + ""
    }

    fun getGridSpanCount(activity: Activity): Int {
        val display = activity.windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val cellWidth = activity.resources.getDimension(R.dimen.item_place_width)
        return Math.round(screenWidth / cellWidth)
    }

    fun configStaticMap(act: Activity, googleMap: GoogleMap, place: Place): GoogleMap {
        // set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(false)
        // Enable / Disable my location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(false)
        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(false)
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(false)
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(false)
        // enable traffic layer
        googleMap.isTrafficEnabled()
        googleMap.setTrafficEnabled(false)
        googleMap.getUiSettings().setScrollGesturesEnabled(false)
        googleMap.getUiSettings().setMapToolbarEnabled(false)

        val inflater = act.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val marker_view = inflater.inflate(R.layout.maps_marker, null)
        (marker_view.findViewById(R.id.marker_bg) as ImageView).setColorFilter(
            act.resources.getColor(
                R.color.marker_secondary
            )
        )

        val cameraPosition = CameraPosition.Builder().target(place.getPosition()).zoom(12).build()
        val markerOptions = MarkerOptions().position(place.getPosition())
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                Tools.createBitmapFromView(
                    act,
                    marker_view
                )
            )
        )
        googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        return googleMap
    }

    fun configActivityMaps(googleMap: GoogleMap): GoogleMap {
        // set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(true)

        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(true)
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true)
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true)

        googleMap.getUiSettings().setScrollGesturesEnabled(true)
        googleMap.getUiSettings().setMapToolbarEnabled(true)

        return googleMap
    }

    fun rateAction(activity: Activity) {
        val uri = Uri.parse("market://details?id=" + activity.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.packageName)
                )
            )
        }

    }

    private fun getPlayStoreUrl(act: Activity): String {
        return "http://play.google.com/store/apps/details?id=" + act.packageName
    }

    fun aboutAction(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.dialog_about_title))
        builder.setMessage(activity.getString(R.string.about_text))
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    fun dialNumber(ctx: Context, phone: String) {
        try {
            val i = Intent(Intent.ACTION_DIAL)
            i.data = Uri.parse("tel:$phone")
            ctx.startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(ctx, "Cannot dial number", Toast.LENGTH_SHORT)
        }
    }

    fun directUrl(ctx: Context, website: String) {
        var url = website
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            url = "http://$url"
        }
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        ctx.startActivity(i)
    }

    fun methodShare(act: Activity, p: Place) {

        // string to share
        val shareBody = ("View good place \'" + p.name + "\'"
                + "\n" + "located at : " + p.address + "\n\n"
                + "Using app : " + getPlayStoreUrl(act))

        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"

        sharingIntent.putExtra(
            android.content.Intent.EXTRA_SUBJECT,
            act.getString(R.string.app_name)
        )
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        act.startActivity(Intent.createChooser(sharingIntent, "Share Using"))
    }

    fun methodShareNews(act: Activity, n: NewsInfo) {

        // string to share
        val shareBody = n.title + "\n\n" + getPlayStoreUrl(act)

        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"

        sharingIntent.putExtra(
            android.content.Intent.EXTRA_SUBJECT,
            act.getString(R.string.app_name)
        )
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        act.startActivity(Intent.createChooser(sharingIntent, "Share Using"))
    }

    fun createBitmapFromView(act: Activity, view: View): Bitmap {
        val displayMetrics = DisplayMetrics()
        act.windowManager.defaultDisplay.getMetrics(displayMetrics)

        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    fun setActionBarColor(ctx: Context, actionbar: ActionBar) {
        val colordrw = ColorDrawable(SharedPref(ctx).getThemeColorInt())
        actionbar.setBackgroundDrawable(colordrw)
    }

    fun colorDarker(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.8f // value component
        return Color.HSVToColor(hsv)
    }

    fun colorBrighter(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] /= 0.8f // value component
        return Color.HSVToColor(hsv)
    }

    fun restartApplication(activity: Activity) {
        activity.finish()
        MainActivity.getInstance().finish()
        val i = Intent(activity, ActivitySplashScreen::class.java)
        activity.startActivity(i)
    }

    private fun calculateDistance(from: LatLng, to: LatLng): Float {
        val start = Location("")
        start.latitude = from.latitude
        start.longitude = from.longitude

        val end = Location("")
        end.latitude = to.latitude
        end.longitude = to.longitude

        val distInMeters = start.distanceTo(end)
        var resultDist = 0f
        if (AppConfig.DISTANCE_METRIC_CODE.equals("KILOMETER")) {
            resultDist = distInMeters / 1000
        } else {
            resultDist = (distInMeters * 0.000621371192).toFloat()
        }
        return resultDist
    }

    fun filterItemsWithDistance(act: Activity, items: List<Place>): List<Place> {
        if (AppConfig.SORT_BY_DISTANCE) { // checking for distance sorting
            val curLoc = Tools.getCurLocation(act)
            if (curLoc != null) {
                return Tools.getSortedDistanceList(items, curLoc)
            }
        }
        return items
    }

    fun itemsWithDistance(ctx: Context, items: List<Place>): List<Place> {
        if (AppConfig.SORT_BY_DISTANCE) { // checking for distance sorting
            val curLoc = Tools.getCurLocation(ctx)
            if (curLoc != null) {
                return Tools.getDistanceList(items, curLoc)
            }
        }
        return items
    }

    fun getDistanceList(places: List<Place>, curLoc: LatLng): List<Place> {
        if (places.size > 0) {
            for (p in places) {
                p.distance = calculateDistance(curLoc, p.getPosition())
            }
        }
        return places
    }

    fun getSortedDistanceList(places: List<Place>, curLoc: LatLng): List<Place> {
        val result = ArrayList<Place>()
        if (places.size > 0) {
            for (i in places.indices) {
                val p = places[i]
                p.distance = calculateDistance(curLoc, p.getPosition())
                result.add(p)
            }
            Collections.sort(result,
                Comparator<Any> { p1, p2 -> java.lang.Float.compare(p1.distance, p2.distance) })
        } else {
            return places
        }
        return result
    }

    fun getCurLocation(ctx: Context): LatLng? {
        if (PermissionUtil.isLocationGranted(ctx)) {
            val manager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                var loc = ThisApplication.getInstance().getLocation()
                if (loc == null) {
                    loc = getLastKnownLocation(ctx)
                    ThisApplication.getInstance().setLocation(loc)
                }
                if (loc != null) {
                    return LatLng(loc!!.getLatitude(), loc!!.getLongitude())
                }
            }
        }
        return null
    }

    fun getLastKnownLocation(ctx: Context): Location? {
        val mLocationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = Tools.requestLocationUpdate(mLocationManager)
        val providers = mLocationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = mLocationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        mLocationManager.removeUpdates(locationListener)
        return bestLocation
    }

    private fun requestLocationUpdate(manager: LocationManager): LocationListener {
        // Define a listener that responds to location updates
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {}

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        // Register the listener with the Location Manager to receive location updates
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        return locationListener
    }

    fun getFormatedDistance(distance: Float): String {
        val df = DecimalFormat()
        df.maximumFractionDigits = 1
        return df.format(distance.toDouble()) + " " + AppConfig.DISTANCE_METRIC_STR
    }

    // request gcmRegId to google
    fun obtainGcmRegId(context: Context, callbackRegId: CallbackRegId) {
        Log.d(Constant.LOG_TAG, "obtainGcmRegId")
        Thread(Runnable {
            val max_count = 3
            var loop_idx = 0
            var gcmRegId = ""
            while (max_count > loop_idx && gcmRegId == "") {
                try {
                    val googleCloudMessaging = GoogleCloudMessaging.getInstance(context)
                    gcmRegId = googleCloudMessaging.register(Constant.PROJECT_API_NUMBER)
                    SharedPref(context).setGCMRegId(gcmRegId)
                    Log.d(Constant.LOG_TAG, "gcmRegId : $gcmRegId")
                } catch (e: IOException) {
                    Log.d(Constant.LOG_TAG, "obtainGcmRegId : Failed")
                }

                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                }

                loop_idx++
            }
            if (!gcmRegId.isEmpty()) {
                callbackRegId.onSuccess(getDeviceInfo(context))
            } else {
                callbackRegId.onError()
            }
        }).start()
    }

    fun getDeviceInfo(context: Context): DeviceInfo {
        var phoneID = Build.SERIAL
        try {
            phoneID = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
        }

        val deviceInfo = DeviceInfo()
        deviceInfo.setDevice(Tools.getDeviceName())
        deviceInfo.setEmail(phoneID)
        deviceInfo.setVersion(Tools.getAndroidVersion())
        deviceInfo.setRegid(SharedPref(context).getGCMRegId())
        deviceInfo.setDate_create(System.currentTimeMillis())

        return deviceInfo
    }

    fun getFormattedDateSimple(dateTime: Long?): String {
        val newFormat = SimpleDateFormat("MMM dd, yyyy")
        return newFormat.format(Date(dateTime!!))
    }

    fun getFormattedDate(dateTime: Long?): String {
        val newFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm")
        return newFormat.format(Date(dateTime!!))
    }


    interface CallbackRegId {
        fun onSuccess(result: DeviceInfo)

        fun onError()
    }

    fun dpToPx(c: Context, dp: Int): Int {
        val r = c.resources
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.displayMetrics
            )
        )
    }

}
