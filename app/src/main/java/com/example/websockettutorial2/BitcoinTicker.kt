package com.example.websockettutorial2
import  com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BitcoinTicker(val price: String?)
