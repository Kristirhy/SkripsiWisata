package com.example.wisatajogja.Model

import java.io.Serializable
import java.util.ArrayList

class ApiClient : Serializable {

    var places: List<Place> = ArrayList()
    var place_category: List<PlaceCategory> = ArrayList()
    var images: List<Images> = ArrayList()

    fun ApiClient() {
    }

    fun ApiClient(
        places: List<Place>,
        place_category: List<PlaceCategory>,
        images: List<Images>
    ){
        this.places = places
        this.place_category = place_category
        this.images = images
    }
}