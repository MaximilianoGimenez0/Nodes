package com.maxigimenez.nodes.core.util

import android.util.Patterns

object AuthValidator {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isNotBlank(value: String): Boolean {
        return value.isNotBlank()
    }
}
