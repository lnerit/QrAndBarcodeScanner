package com.example.barcodescanner.feature.tabs.settings.device

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings as Settings1

@SuppressLint("StaticFieldLeak")
object DeviceIdProvider {
    private lateinit var context: Context
    fun init(appContext: Context) {
        appContext.also { context = it }
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings1.Secure.getString(context.contentResolver, Settings1.Secure.ANDROID_ID)
    }
}