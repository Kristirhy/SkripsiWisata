package com.example.wisatajogja.Connection

import com.example.wisatajogja.BuildConfig
import com.example.wisatajogja.Data.Constant
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RestAdapter {

    fun createAPI(): API {

        val logging = HttpLoggingInterceptor()
        logging.setLevel(if (BuildConfig.DEBUG) Experimental.Level.BODY else Experimental.Level.NONE)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .cache(null)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.WEB_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(okHttpClient)
            .build()

        return retrofit.create(API::class.java!!)
    }

    /**
     * createShortAPI use only for GCM registration
     * use 2 second only to connect and register
     */
    fun createShortAPI(): API {

        val logging = HttpLoggingInterceptor()
        logging.setLevel(if (BuildConfig.DEBUG) Experimental.Level.BODY else Experimental.Level.NONE)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .cache(null)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.WEB_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(okHttpClient)
            .build()

        return retrofit.create(API::class.java!!)
    }
}
