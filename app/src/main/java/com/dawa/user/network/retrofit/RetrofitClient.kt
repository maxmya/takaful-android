package com.dawa.user.network.retrofit

import com.dawa.user.network.interceptors.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    //    private const val BASE_URL = "http://142.93.166.121:8080/"
    private const val BASE_URL = "http://192.168.1.17:8080/"


    private val okHttpClient =
        OkHttpClient.Builder().retryOnConnectionFailure(true).addInterceptor(AuthInterceptor())
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()


    val INSTANCE: DawaAPIService by lazy {

        val retrofit = Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL)
            .client(okHttpClient).build()

        retrofit.create(DawaAPIService::class.java)
    }

}