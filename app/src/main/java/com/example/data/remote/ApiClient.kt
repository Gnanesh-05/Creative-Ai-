package com.example.data.remote

import android.content.Context
import com.example.data.local.UserPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    @Volatile
    private var instance: ApiService? = null

    private var currentBaseUrl: String = ApiConfig.DEV_BASE_URL

    fun setBaseUrl(url: String) {
        currentBaseUrl = if (url.endsWith("/")) url else "$url/"
        instance = null // Reset retrofit instance on URL change
    }

    fun getApiService(context: Context): ApiService {
        return instance ?: synchronized(this) {
            instance ?: buildRetrofit(context.applicationContext).create(ApiService::class.java).also {
                instance = it
            }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val userPreferences = UserPreferences(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(ApiConfig.TIMEOUT_CONNECT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.TIMEOUT_READ_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.TIMEOUT_WRITE_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(userPreferences))
            .addInterceptor(RetryInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
