package com.nova.healthconnect.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()

        val newRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .header("X-Client-Platform", "Android-Health-Connect")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("X-Client-Platform", "Android-Health-Connect")
                .build()
        }

        return chain.proceed(newRequest)
    }
}
