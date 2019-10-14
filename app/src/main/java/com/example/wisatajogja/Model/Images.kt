package com.example.wisatajogja.Model

import java.io.Serializable

class Images : Serializable {
    var place_id: Int = 0
    lateinit var name: String

    fun Images() {
    }

    fun Images(place_id: Int, name: String){
        this.place_id = place_id
        this.name = name
    }

    fun getImageUrl(): String {
        return name
    }
}
