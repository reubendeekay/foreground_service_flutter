package com.example.foregroundServiceFlutter.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.foregroundServiceFlutter.ForegroundServiceFlutterPlugin
import com.example.foregroundServiceFlutter.data.NatsManager
import com.example.foregroundServiceFlutter.R
import com.example.foregroundServiceFlutter.data.model.DriverStatusModel
import com.example.foregroundServiceFlutter.data.model.OnlineSeconds
import com.example.foregroundServiceFlutter.utils.Constants.TRIP_RECEIVER
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

import com.google.gson.Gson


//import org.koin.android.ext.android.inject

class ForegroundService: Service(), NatsDataCollector {
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var locationCallback: LocationCallback
    private var currentLatLng: LatLng? = null
    private lateinit var natsManager: NatsManager
    private var isConnected = false
//    private val sharedPreferencesHelper: SharedPreferencesHelper by inject()


    companion object {

        fun startService(context: Context) {
            val message = "You are online"
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)

        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }

    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("noData",Context.MODE_PRIVATE)

        natsManager = NatsManager(this)
        val tripUsername =sharedPreferences.getString(Constants.prefUsernameKey,"nothing")
        val tripPassword = sharedPreferences.getString(Constants.prefPasswordKey,"nothing")
        val natsUrl = sharedPreferences.getString(Constants.prefNatsUrlKey,"nothing")

        natsManager.connect(natsUrl.toString(), tripUsername.toString(), tripPassword.toString())


        setUpLocationListener()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, ForegroundServiceFlutterPlugin::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, "locationS")
            .setContentTitle("DARIDE DRIVER")
            .setContentText("You are online")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        val name = "Running Location Notification"
        val descriptionText = "LocChannel"
        val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
        val natsChannel = android.app.NotificationChannel("locationS", name, importance)
        natsChannel.description = descriptionText
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(natsChannel)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification.build(), FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification.build())
        }
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationListener() {
        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().setInterval(10000).setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {

                    currentLatLng = LatLng(location.latitude, location.longitude)
                    currentLatLng?.let{
//                        val status = if(sharedPreferencesHelper.getSavedBoolean(Constants.prefIsDriverOnline)) {
//                            Constants.driverAvailable
//                        } else {
//                            Constants.driverUnavailable
//                        }

                        if (isConnected) {

//                                updateDriverStatus(status)
                            updateDriverStatus(bearing = location.bearing.toDouble())

                        }

                    }
                }
            }
        }
        fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
        )
    }

//    private fun updateDriverStatus(status: Int) {
        private fun updateDriverStatus(bearing: Double = 0.0) {

        currentLatLng?.let {
            val token =sharedPreferences.getString(Constants.prefTokenKey,"nothing")
            val userId = sharedPreferences.getLong(Constants.prefDriverIdKey,0)
            val carType = sharedPreferences.getLong(Constants.prefCarTypeKey,1)
            val onlineStatus = sharedPreferences.getInt(Constants.prefDriverStatusKey,1)
val activeTrip = sharedPreferences.getString(Constants.prefActiveTripKey,"0")
val carColor = sharedPreferences.getString(Constants.prefCarColorKey,"")


            val driverStatus = DriverStatusModel(
                token = token.toString(),
                driverStatus = onlineStatus,
                driverLat = it.latitude,
                driverLong = it.longitude,
                userId = userId,
                bearing = if (bearing==0.0) 0.1 else bearing,
                carType = carType,
                activeTrip = if (activeTrip=="0") null else activeTrip,
                carColor = carColor


            )

            val onlineSeconds=OnlineSeconds(
                token = token.toString(),
                identifier = "DRIVER-$userId",
                product = "daride",
                unit = "ONLINE_SECONDS",
                value= 5

            )

            val gson = Gson()
            val onlineSecondsJson = gson.toJson(onlineSeconds)
            Log.e("onlineSeconds",Constants.onlineSecondsTopic)
            natsManager.publish(Constants.onlineSecondsTopic, onlineSecondsJson)

            val driverStatusJson = gson.toJson(driverStatus)

            natsManager.publish(Constants.driverStatusTopic, driverStatusJson)
            if(activeTrip != "0") {
                natsManager.publish(Constants.notificationsTopic + activeTrip, driverStatusJson)
            }

        }
    }



    override fun setConnect(isConnected: Boolean) {
        this.isConnected = isConnected

    }

    override fun setResponse(response: String) {
        val intent = Intent()
        intent.apply {
            putExtra("data", response)
            action = TRIP_RECEIVER
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        //Check subscription and handle if it contains ride_request as action
        Log.e("response",response)

        if (response.contains("ride_request")) {
            //Launch Flutter app
            getAppLaunched(this, intent)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        //Check if natsManager  are initialized
        if (this::natsManager.isInitialized){
            natsManager.close()
        }
        Log.e("service","service destroyed")
    }

    @SuppressLint("ServiceCast")
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val appProcesses = activityManager.runningAppProcesses
        if (appProcesses != null) {
            for (appProcess in appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName == context.packageName
                ) {
                    return true
                }
            }
        }

        return false
    }

    private fun getAppLaunched(context: Context, intent: Intent): Boolean {

        if (isAppInForeground(context)) {
            println("App is in the foreground")
            return false
        }
        println("App is in the background")


        // Bring the app to the foreground by launching the main activity
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(launchIntent)


        return true
    }


}
