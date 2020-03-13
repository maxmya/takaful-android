package com.takaful.user.network.retrofit

import com.takaful.user.App
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    private const val BASE_URL = "http://167.71.52.220:8080/backend/"

    private val okHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor { chain ->

                val original = chain.request()

                val requestBuilder =
                    original.newBuilder().addHeader("Authorization", "Bearer " + App.TOKEN)
                        .method(original.method(), original.body())

                val request = requestBuilder.build()

                chain.proceed(request)

            }.addInterceptor(HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)).build()


    val INSTANCE: TakafulApiService by lazy {

        val retrofit =
            Retrofit
                .Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build()

        retrofit.create(TakafulApiService::class.java)
    }

}