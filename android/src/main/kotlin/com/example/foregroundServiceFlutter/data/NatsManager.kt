package com.example.foregroundServiceFlutter.data

import android.os.Build
import com.example.foregroundServiceFlutter.utils.NatsDataCollector
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.foregroundServiceFlutter.utils.Constants
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import java.io.BufferedInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import java.time.Duration
import javax.net.ssl.*

class NatsManager(private val dataCollector: NatsDataCollector) {

    private lateinit var natsConnection : Connection
    var connect = false

    var KEYSTORE_PATH = "keystore.jks"
    var TRUSTSTORE_PATH = "truststore.jks"
    var STORE_PASSWORD = "password"
    var KEY_PASSWORD = "password"
    var ALGORITHM = "X509"

    fun connect(natsUrl:String, userName: String, password: String) {
        Thread {
            //val sslContext = createSSLContext()
            //   .sslContext(sslContext)
            val options: Options = Options.Builder()
                    .server(natsUrl)
                    .userInfo(userName.toCharArray(),  password.toCharArray())
                    .build()
            try {
                natsConnection = Nats.connect(options)
                connect = true
                Log.e("nats", "connected")
                Log.e("url", natsUrl)
                dataCollector.setConnect(true)
            } catch (exp: Exception) {
                connect = false
                Log.e("nats", exp.message.toString())
                dataCollector.setConnect(false)
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun publish(topic: String, request: String){
        if(connect) {
            Log.e("topic", topic)
            Log.e("request", request)
            natsConnection.request(topic, request.toByteArray(StandardCharsets.UTF_8))

        }
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun subscribe(topic: String){
        Log.e("topic", topic)
        if (!connect) {
            Log.e("nats", "not connected")
            return
        }
        val dispatcher = natsConnection.createDispatcher { message ->
            val response = String(message.data, StandardCharsets.UTF_8)
            dataCollector.setResponse(response)
            Log.e("response",response)

        }
        dispatcher.subscribe(topic)

    }

    fun close(){
        natsConnection.close()
    }

    @Throws(Exception::class)
    fun loadKeystore(path: String?): KeyStore {
        val store: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        val `in` = BufferedInputStream(this.javaClass.classLoader?.getResourceAsStream(path))
        try {
            store.load(`in`, STORE_PASSWORD.toCharArray())
        } finally {
            if (`in` != null) {
                `in`.close()
            }
        }
        return store
    }

    @Throws(Exception::class)
    fun createTestKeyManagers(): Array<KeyManager?>? {
        val store: KeyStore = loadKeystore(KEYSTORE_PATH)
        val factory: KeyManagerFactory = KeyManagerFactory.getInstance(ALGORITHM)
        factory.init(store, KEY_PASSWORD.toCharArray())
        return factory.getKeyManagers()
    }

    @Throws(Exception::class)
    fun createTestTrustManagers(): Array<TrustManager?>? {
        val store: KeyStore = loadKeystore(TRUSTSTORE_PATH)
        val factory: TrustManagerFactory = TrustManagerFactory.getInstance(ALGORITHM)
        factory.init(store)
        return factory.getTrustManagers()
    }

    @Throws(Exception::class)
    fun createSSLContext(): SSLContext? {
        val ctx: SSLContext = SSLContext.getInstance(Options.DEFAULT_SSL_PROTOCOL)
        ctx.init(createTestKeyManagers(), createTestTrustManagers(), SecureRandom())
        return ctx
    }


}