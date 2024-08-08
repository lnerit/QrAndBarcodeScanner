package com.example.barcodescanner.feature

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.barcodescanner.di.rotationHelper
import com.example.barcodescanner.network.ApiClient
import com.example.barcodescanner.network.ApiService
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = ApiClient.getRetrofitInstance().create(ApiService::class.java)
        val call = apiService.PostBarcode(
            code = "23301913",
            eventId = "c8347c02-cec5-4e8c-8c40-b4dcf03f12d5",
            roomNumber = "MCS202",
            deviceId = "20fd-7e41-442b-8b88-d780-93e4-cd97-e081"
        )

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("MainActivity", "Response:"+data?.get("success"))
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("MainActivity", "Error: ${t.message}")
            }
        })

        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        rotationHelper.lockCurrentOrientationIfNeeded(this)
    }
}