package com.example.mobilestorage.model

import com.google.firebase.Timestamp

data class Departure (
    var id: String,
    var departureDate: Timestamp,
    var addressOfDeparture: String,
    var mapOfProductsCount: MutableMap<String, Long>
        )