package com.example.backend.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(private val maxRetries: Int = ApiConfig.MAX_RETRIES) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount <= maxRetries) {
            try {
                response?.close()
                response = chain.proceed(request)
                
                // If response is successful or 4xx client error (not retryable), return immediately
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }
            } catch (e: IOException) {
                exception = e
            }
            tryCount++
            if (tryCount <= maxRetries) {
                try {
                    Thread.sleep(1000L * tryCount)
                } catch (ignored: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }

        if (response != null) {
            return response
        }
        throw exception ?: IOException("Network execution failed after $maxRetries retries")
    }
}
