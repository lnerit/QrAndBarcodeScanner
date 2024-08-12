package com.example.barcodescanner.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import com.google.gson.JsonObject as JsonDataSet
interface  ApiService {
    @POST("PostBarcode")
     fun PostBarcode(
        @Query("Code") code: String,
        @Query("EventId") eventId: String,
        @Query("RoomNumber") roomNumber: String,
        @Query("DeviceId") deviceId: String
    ): Call<JsonDataSet>

    @GET("ScanBarcodeMobile")
    fun ScanBarcodeMobile(
        @Query("DeviceId") deviceId: String
    ): Call<JsonDataSet>
}