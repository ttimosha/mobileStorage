package com.example.mobilestorage.model

import com.google.firebase.Timestamp

data class Arrival (
    var id: String,
    var arrivalDate: Timestamp,
    var providerID: String,
    var providerName: String,
    var listOfProductsID: MutableList<String>
)