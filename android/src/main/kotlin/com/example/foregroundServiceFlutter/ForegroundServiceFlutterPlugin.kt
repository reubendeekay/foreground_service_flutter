package com.example.foregroundServiceFlutter

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONObject
import com.example.foregroundServiceFlutter.utils.Constants
import com.example.foregroundServiceFlutter.utils.ForegroundService

class ForegroundServiceFlutterPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences

    private val LOCATION_NATS_CHANNEL = "com.reuben.nats/foreground_location"

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        sharedPreferences = context.getSharedPreferences("noData", Context.MODE_PRIVATE)
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, LOCATION_NATS_CHANNEL)
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        try {
            when (call.method) {
                "startService" -> {
                    // Access the arguments directly as a Map
                    val arguments = call.arguments as? Map<*, *>
                    if (arguments != null) {
                        val token = arguments["token"] as? String ?: ""
                        val userId = arguments["userId"] as? String ?: ""
                        val carType = arguments["carType"] as? String ?: ""
                        val onlineStatus = arguments["onlineStatus"] as? String ?: ""
                        val activeTrip = arguments["activeTrip"] as? String ?: ""
                        val carColor = arguments["carColor"] as? String ?: ""

                        val natsUrl = arguments["natsUrl"] as? String ?: ""
                        val username = arguments["username"] as? String ?: ""
                        val password = arguments["password"] as? String ?: ""
                        val subject = arguments["subject"] as? String ?: ""
    
                        // Save data to SharedPreferences
                        sharedPreferences.edit().apply {
                            putString(Constants.prefTokenKey, token)
                            putLong(Constants.prefDriverIdKey, userId.toLong())
                            putLong(Constants.prefCarTypeKey, carType.toLong())
                            putInt(Constants.prefDriverStatusKey, onlineStatus.toInt())
                            putString(Constants.prefActiveTripKey, activeTrip)
                            putString(Constants.prefCarColorKey, carColor)
                            putString(Constants.prefNatsUrlKey, natsUrl)
                            putString(Constants.prefUsernameKey, username)
                            putString(Constants.prefPasswordKey, password)
                            putString(Constants.prefSubjectKey, subject)
                            apply()
                           

                        }
    
                        // Start the foreground service
                        ForegroundService.startService(context)
                        result.success("Service started")
                    } else {
                        result.error("InvalidArguments", "Arguments are null or invalid", null)
                    }
                }
                "stopService" -> {
                    ForegroundService.stopService(context)
                    result.success("Service stopped")
                }
                else -> result.notImplemented()
            }
        } catch (e: Exception) {
            result.error("Error", e.message, null)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}