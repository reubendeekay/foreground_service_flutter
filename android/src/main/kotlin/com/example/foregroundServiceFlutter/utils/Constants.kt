package com.example.foregroundServiceFlutter.utils

import io.flutter.BuildConfig


object Constants {
    const val prefTokenKey="token"
    const val prefDriverIdKey="userId"
    const val prefCarTypeKey="carType"
    const val prefDriverStatusKey="onlineStatus"
    const val prefActiveTripKey="activeTrip"
    const val prefCarColorKey="carColor"
    const val prefNatsUrlKey="natsUrl"
    const val prefUsernameKey="username"
    const val prefPasswordKey="password"
    const val prefSubjectKey="subject"

    const val driverStatusTopic = "location.service.driver"
    const val onlineSecondsTopic = "da-ride.user.heartbeat"
    const val notificationsTopic="notifications."
    const val defaultUserTopic = "user.topic."

    const val TRIP_RECEIVER = "Trip Receiver"


}