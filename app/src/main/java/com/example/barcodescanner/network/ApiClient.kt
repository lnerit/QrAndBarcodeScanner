package com.example.barcodescanner.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    //"https://mcstsmas.unitech.ac.pg/public/"
    // "http://192.168.144.111/public/"
    private const val BASE_URL = "https://mcstsmas.unitech.ac.pg/public/"
    private var retrofit: Retrofit? = null

    fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}