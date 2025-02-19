package com.example.foregroundServiceFlutter.utils

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>

    class LocationException(message: String): Exception()
}