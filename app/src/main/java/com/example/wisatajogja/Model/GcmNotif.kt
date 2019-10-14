package com.example.wisatajogja.Model

import java.io.Serializable

class GcmNotif : Serializable {
    private var title: String? = null
    private var content:String? = null
    private var type:String? = null
    private var place: Place? = null
    private var news: NewsInfo? = null

    fun GcmNotif(){
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getContent(): String? {
        return content
    }

    fun setContent(content: String) {
        this.content = content
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String) {
        this.type = type
    }

    fun getPlace(): Place? {
        return place
    }

    fun setPlace(place: Place) {
        this.place = place
    }

    fun getNews(): NewsInfo? {
        return news
    }

    fun setNews(news: NewsInfo) {
        this.news = news
    }
}
