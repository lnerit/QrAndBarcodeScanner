package com.example.barcodescanner

import androidx.multidex.MultiDexApplication
import com.example.barcodescanner.di.settings
import com.example.barcodescanner.feature.tabs.settings.device.DeviceIdProvider
import com.example.barcodescanner.usecase.Logger
import io.reactivex.plugins.RxJavaPlugins

class App : MultiDexApplication() {

    override fun onCreate() {
        handleUnhandledRxJavaErrors()
        applyTheme()
        super.onCreate()
        DeviceIdProvider.init(this)  // Initialize with application context
    }

    private fun applyTheme() {
        settings.reapplyTheme()
    }

    private fun handleUnhandledRxJavaErrors() {
        RxJavaPlugins.setErrorHandler { error ->
            Logger.log(error)
        }
    }
}