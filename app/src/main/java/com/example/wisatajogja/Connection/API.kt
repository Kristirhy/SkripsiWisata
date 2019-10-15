package com.example.wisatajogja.Connection

import com.example.wisatajogja.Connection.callbacks.CallbackDevice
import com.example.wisatajogja.Connection.callbacks.CallbackListNewsInfo
import com.example.wisatajogja.Connection.callbacks.CallbackListPlace
import com.example.wisatajogja.Connection.callbacks.CallbackPlaceDetails
import com.example.wisatajogja.Model.DeviceInfo
import retrofit2.Call
import retrofit2.http.*

interface API {

    val CACHE = "Cache-Control: max-age=0"
    val AGENT = "User-Agent: Place"

    /* Place API transaction ------------------------------- */

    @Headers(CACHE, AGENT)
    @GET("app/services/listPlaces")
    abstract fun getPlacesByPage(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("draft") draft: Int
    ): Call<CallbackListPlace>

    @Headers(CACHE, AGENT)
    @GET("app/services/getPlaceDetails")
    abstract fun getPlaceDetails(
        @Query("place_id") place_id: Int
    ): Call<CallbackPlaceDetails>

    /* News Info API transaction ------------------------------- */

    @Headers(CACHE, AGENT)
    @GET("app/services/listNewsInfo")
    abstract fun getNewsInfoByPage(
        @Query("page") page: Int,
        @Query("count") count: Int
    ): Call<CallbackListNewsInfo>

    @Headers(CACHE, AGENT)
    @POST("app/services/insertGcm")
    abstract fun registerDevice(
        @Body deviceInfo: DeviceInfo
    ): Call<CallbackDevice>
}