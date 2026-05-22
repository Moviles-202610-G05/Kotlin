package com.example.foodgram.models.map

import com.google.firebase.firestore.GeoPoint

data class PointPosition(
    val geopoint: GeoPoint = GeoPoint(0.0, 0.0)
)
