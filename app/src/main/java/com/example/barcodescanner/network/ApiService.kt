package com.example.barcodescanner.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.JsonObject as PostBarcode

interface  ApiService {
    @POST("PostBarcode")
     fun PostBarcode(
        @Query("Code") code: String,
        @Query("EventId") eventId: String,
        @Query("RoomNumber") roomNumber: String,
        @Query("DeviceId") deviceId: String
    ): Call<PostBarcode>
}