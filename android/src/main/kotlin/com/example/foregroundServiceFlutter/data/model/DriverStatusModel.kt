package com.example.foregroundServiceFlutter.data.model


import com.google.gson.annotations.SerializedName

data class DriverStatusModel(
    @SerializedName("token")
    val token: String?,
    @SerializedName("actions")

    val actions: String = "driver_status",
    @SerializedName("status")
    val driverStatus: Int,
    @SerializedName("latitude")
    val driverLat: Double,
    @SerializedName("longitude")
    val driverLong: Double,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("bearing")
    val bearing: Double ,
    @SerializedName("car_type")
    val carType: Long = 1,
    @SerializedName("active_trip")
    val activeTrip: String?,
    @SerializedName("car_color")
    val carColor: String?
)