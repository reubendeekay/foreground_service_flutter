package com.example.foregroundServiceFlutter.utils
import android.content.SharedPreferences

class SharedPreferencesHelper(private val sharedPreferences: SharedPreferences) {

    fun saveString(key: String, value: String?) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            commit()
        }
    }

    fun getSavedString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun saveBoolean(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            commit()
        }
    }

    fun getSavedBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun saveInteger(key: String, value: Int) {
        with(sharedPreferences.edit()) {
            putInt(key, value)
            commit()
        }
    }

    fun getSavedInteger(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun saveLong(key: String, value: Long) {
        with(sharedPreferences.edit()) {
            putLong(key, value)
            commit()
        }
    }

    fun getSavedLong(key: String): Long {
        return sharedPreferences.getLong(key, 0)
    }

    fun saveFloat(key: String, value: Float) {
        with(sharedPreferences.edit()) {
            putFloat(key, value)
            commit()
        }
    }

    fun getFloat(key: String): Float {
        return sharedPreferences.getFloat(key, 0.0F)
    }
}
