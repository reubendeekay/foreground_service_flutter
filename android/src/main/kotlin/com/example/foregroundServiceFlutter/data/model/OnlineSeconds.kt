package com.example.foregroundServiceFlutter.data.model


import com.google.gson.annotations.SerializedName

data class OnlineSeconds(
    @SerializedName("token")
    val token: String?,

    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("product")
    val product: String,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("value")
    val value: Long,

)