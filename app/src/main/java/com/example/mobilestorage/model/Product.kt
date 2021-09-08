package com.example.mobilestorage.model

import com.google.firebase.Timestamp

data class Product (
    var id: String,
    var name: String,
    var provider: String,
    var price: Long,
    var amount: Long,
    var unit: String,
    var total: Long,
    var arrivalDate: Timestamp
)