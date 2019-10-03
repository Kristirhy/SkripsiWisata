package com.example.wisatajogja.Data

class AppConfig {

//    // flag for display ads
//    val ADS_MAIN_INTERSTITIAL = false
//    val ADS_PLACE_DETAILS_BANNER = false
//    val ADS_NEWS_DETAILS_BANNER = false

    // this flag if you want to hide menu news info
    var ENABLE_NEWS_INFO = true

    // flag for save image offline
    val IMAGE_CACHE = true

    // if you place data more than 200 items please set TRUE
    val LAZY_LOAD = false

    // flag for tracking analytics
    val ENABLE_ANALYTICS = true

    // clear image cache when receive push notifications
    val REFRESH_IMG_NOTIF = true


    // when user enable gps, places will sort by distance
    val SORT_BY_DISTANCE = true

    // distance metric, fill with KILOMETER or MILE only
    val DISTANCE_METRIC_CODE = "KILOMETER"

    // related to UI display string
    val DISTANCE_METRIC_STR = "Km"

    // flag for enable disable theme color chooser, in Setting
    val THEME_COLOR = true

}
