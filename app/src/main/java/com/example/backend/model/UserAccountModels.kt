package com.example.backend.model

/**
 * Data models matching the User Account & Device Schema
 */
data class UserAccount(
    val authUid: String = "",
    val email: String = "",
    val isVerified: Boolean = false,
    val displayName: String? = null,
    val phoneNumber: String? = null
)

data class VerificationSession(
    val userUid: String = "",
    val sentAt: Long = System.currentTimeMillis()
)

data class AppSetting(
    val key: String = "",
    val value: String = ""
)

data class AccessLog(
    val userUid: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceMetadata(
    val userUid: String = "",
    val deviceId: String = "",
    val lastLoginAt: Long = System.currentTimeMillis()
)
