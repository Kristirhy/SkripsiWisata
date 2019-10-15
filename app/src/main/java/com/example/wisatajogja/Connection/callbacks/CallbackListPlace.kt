package com.example.wisatajogja.Connection.callbacks

import com.example.wisatajogja.Model.Place
import java.io.Serializable
import java.util.ArrayList

class CallbackListPlace : Serializable {

    var status = ""
    var count = -1
    var count_total = -1
    var pages = -1
    var places: List<Place> = ArrayList<Place>()
}