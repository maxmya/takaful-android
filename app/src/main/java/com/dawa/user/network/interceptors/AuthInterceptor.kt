package com.dawa.user.network.interceptors

import com.dawa.user.handlers.PreferenceManagerService
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestBuilder =
            original
                .newBuilder()
                .addHeader("Authorization", "Bearer " + PreferenceManagerService.retrieveToken())
                .method(original.method, original.body)

        val request = requestBuilder.build()

        return chain.proceed(request)
    }

}