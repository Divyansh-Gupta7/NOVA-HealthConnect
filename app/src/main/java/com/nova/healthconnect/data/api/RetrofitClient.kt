package com.nova.healthconnect.data.api

import android.content.Context
import android.content.SharedPreferences
import com.nova.healthconnect.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val PREFS_NAME = "nova_network_prefs"
    private const val KEY_BASE_URL = "base_url"

    private var apiService: NovaApiService? = null
    private var currentBaseUrl: String? = null
    private var tokenProvider: (() -> String?) = { null }

    fun initialize(context: Context, tokenProvider: () -> String?) {
        this.tokenProvider = tokenProvider
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUrl = prefs.getString(KEY_BASE_URL, BuildConfig.DEFAULT_BACKEND_URL)
            ?: BuildConfig.DEFAULT_BACKEND_URL
        updateBaseUrl(savedUrl)
    }

    fun getBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BASE_URL, BuildConfig.DEFAULT_BACKEND_URL)
            ?: BuildConfig.DEFAULT_BACKEND_URL
    }

    fun saveBaseUrl(context: Context, newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BASE_URL, formatted).apply()
        updateBaseUrl(formatted)
    }

    private fun updateBaseUrl(baseUrl: String) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(NovaApiService::class.java)
        currentBaseUrl = baseUrl
    }

    fun getService(): NovaApiService {
        return apiService ?: run {
            updateBaseUrl(BuildConfig.DEFAULT_BACKEND_URL)
            apiService!!
        }
    }
}
