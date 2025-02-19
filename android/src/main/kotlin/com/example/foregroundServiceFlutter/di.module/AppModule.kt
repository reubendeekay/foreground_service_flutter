package com.example.foregroundServiceFlutter.di.module

import android.content.SharedPreferences

import com.example.foregroundServiceFlutter.utils.SharedPreferencesHelper

import org.koin.dsl.module


val appModule = module {
    single { provideSharedPreferencesHelper(get()) }


}

private fun provideSharedPreferencesHelper(sharedPreferences: SharedPreferences) =
    SharedPreferencesHelper(sharedPreferences)