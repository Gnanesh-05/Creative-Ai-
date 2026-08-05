package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import retrofit2.Response

object NetworkUtils {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    suspend fun <T, R> safeApiCall(
        apiCall: suspend () -> Response<ApiResponse<T>>,
        transform: (T) -> R
    ): Resource<R> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Resource.Success(transform(body.data))
                } else if (body != null && !body.success) {
                    Resource.Error(body.message ?: "Server operation returned unsuccessful status")
                } else {
                    Resource.Error("Empty response body received from server")
                }
            } else {
                val errorCode = response.code()
                val errorMsg = when (errorCode) {
                    401 -> "Unauthorized session. Please re-login."
                    403 -> "Access forbidden for requested resource."
                    404 -> "Requested endpoint not found."
                    422 -> "Validation error in request payload."
                    500 -> "Internal server error. Please try again later."
                    else -> "HTTP $errorCode: ${response.message()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage ?: "Connection failed"}", e)
        }
    }
}
