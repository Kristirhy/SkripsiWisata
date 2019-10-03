package com.example.wisatajogja.Data

class Constant {

    // Edit WEB_URL with your url. Make sure you have backslash('/') in the end url
    var WEB_URL = "ww"

    // for map zoom
    val city_lat = -6.9174639
    val city_lng = 107.6191228

    // for gcm
    private val PROJECT_API_NUMBER = "107798XXXXXXXXX"


    /**
     * ------------------- DON'T EDIT THIS ---------------------------------------------------------
     */

    // image file url
    fun getURLimgPlace(file_name: String): String {
        return WEB_URL + "uploads/place/" + file_name
    }

    fun getURLimgNews(file_name: String): String {
        return WEB_URL + "uploads/news/" + file_name
    }

    // this limit value used for give pagination (request and display) to decrease payload
    val LIMIT_PLACE_REQUEST = 40
    val LIMIT_LOADMORE = 40

    val LIMIT_NEWS_REQUEST = 40

    // retry load image notification
    var LOAD_IMAGE_NOTIF_RETRY = 3

    // for search logs Tag
    val LOG_TAG = "CITY_LOG"

    // Google analytics event category
    enum class Event {
        FAVORITES,
        THEME,
        NOTIFICATION,
        REFRESH
    }

}
