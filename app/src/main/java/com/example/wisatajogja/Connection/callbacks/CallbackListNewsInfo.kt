package com.example.wisatajogja.Connection.callbacks

import java.io.Serializable
import java.util.ArrayList

class CallbackListNewsInfo : Serializable {

    var status = ""
    var count = -1
    var count_total = -1
    var pages = -1
    var news_infos: List<NewsInfo> = ArrayList<NewsInfo>()
}