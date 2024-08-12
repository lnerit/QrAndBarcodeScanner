package com.example.barcodescanner.feature

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.barcodescanner.di.rotationHelper
import com.example.barcodescanner.feature.tabs.settings.device.DeviceIdProvider
import com.example.barcodescanner.network.ApiClient
import com.example.barcodescanner.network.ApiService
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        rotationHelper.lockCurrentOrientationIfNeeded(this)
    }
}