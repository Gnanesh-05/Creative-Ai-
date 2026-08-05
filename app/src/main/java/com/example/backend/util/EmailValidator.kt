package com.example.backend.util

import android.util.Patterns

data class EmailValidationResult(
    val input: String,
    val isValidFormat: Boolean,
    val isDisposable: Boolean,
    val suggestedCorrection: String? = null,
    val reason: String? = null
) {
    val isValid: Boolean get() = isValidFormat && !isDisposable
}

object EmailValidator {

    private val RFC_EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

    private val DISPOSABLE_DOMAINS = setOf(
        "tempmail.com", "temp-mail.org", "mailinator.com", "10minutemail.com",
        "guerrillamail.com", "yopmail.com", "trashmail.com", "sharklasers.com",
        "getnada.com", "dispostable.com", "throwawaymail.com", "maildrop.cc",
        "inboxkitten.com", "crazymailing.com", "nada.ltd", "tempmailo.com"
    )

    private val DOMAIN_TYPO_MAP = mapOf(
        "gmai.com" to "gmail.com",
        "gamil.com" to "gmail.com",
        "gmial.com" to "gmail.com",
        "gmai.co" to "gmail.com",
        "hotmai.com" to "hotmail.com",
        "hotmial.com" to "hotmail.com",
        "outloo.com" to "outlook.com",
        "yaho.com" to "yahoo.com",
        "yahoo.co" to "yahoo.com",
        "iclou.com" to "icloud.com",
        "icloud.co" to "icloud.com"
    )

    fun isValidFormat(email: String): Boolean {
        if (email.isBlank()) return false
        return RFC_EMAIL_REGEX.matches(email.trim()) || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun isDisposable(email: String): Boolean {
        val domain = email.trim().substringAfter("@", "").lowercase()
        return DISPOSABLE_DOMAINS.contains(domain)
    }

    fun getSuggestedCorrection(email: String): String? {
        val trimmed = email.trim()
        val parts = trimmed.split("@")
        if (parts.size != 2) return null

        val username = parts[0]
        val domain = parts[1].lowercase()

        val correctedDomain = DOMAIN_TYPO_MAP[domain]
        return if (correctedDomain != null) {
            "$username@$correctedDomain"
        } else {
            null
        }
    }

    fun validateEmailRealtime(email: String): EmailValidationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return EmailValidationResult(
                input = trimmed,
                isValidFormat = false,
                isDisposable = false,
                reason = "Email cannot be empty"
            )
        }

        val validFormat = isValidFormat(trimmed)
        if (!validFormat) {
            return EmailValidationResult(
                input = trimmed,
                isValidFormat = false,
                isDisposable = false,
                reason = "Invalid email format (e.g. user@example.com required)"
            )
        }

        val disposable = isDisposable(trimmed)
        if (disposable) {
            return EmailValidationResult(
                input = trimmed,
                isValidFormat = true,
                isDisposable = true,
                reason = "Disposable/temporary email domains are not allowed"
            )
        }

        val suggestion = getSuggestedCorrection(trimmed)

        return EmailValidationResult(
            input = trimmed,
            isValidFormat = true,
            isDisposable = false,
            suggestedCorrection = suggestion,
            reason = if (suggestion != null) "Did you mean $suggestion?" else null
        )
    }
}
