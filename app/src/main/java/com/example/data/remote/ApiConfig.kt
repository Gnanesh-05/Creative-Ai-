package com.example.data.remote

object ApiConfig {
    // Development Base URL (Local / Cloud instance)
    const val DEV_BASE_URL = "https://ais-dev-emmqli2gf7hf52b26s2qtv-728910100170.asia-southeast1.run.app/"
    
    // Production Base URL (HTTPS)
    const val PROD_BASE_URL = "https://ais-pre-emmqli2gf7hf52b26s2qtv-728910100170.asia-southeast1.run.app/"

    const val TIMEOUT_CONNECT_SECONDS = 15L
    const val TIMEOUT_READ_SECONDS = 30L
    const val TIMEOUT_WRITE_SECONDS = 30L
    const val MAX_RETRIES = 2
}
