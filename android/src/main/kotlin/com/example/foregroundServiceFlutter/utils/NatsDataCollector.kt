package com.example.foregroundServiceFlutter.utils

interface NatsDataCollector {
    fun setConnect(isConnected: Boolean)
    fun setResponse(response: String)
}
