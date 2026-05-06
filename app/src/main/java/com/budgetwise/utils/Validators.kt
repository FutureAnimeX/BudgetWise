package com.budgetwise.utils

object Validators {
    fun validateUsername(username: String): String? {
        if (username.isBlank()) return "Username is required"
        if (username.length < 3) return "Username must be at least 3 characters"
        if (username.length > 30) return "Username must be 30 characters or fewer"
        return null
    }
    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Password is required"
        if (password.length < 8) return "Password must be at least 8 characters"
        if (!password.any { it.isUpperCase() }) return "Password must contain an uppercase letter"
        if (!password.any { it.isDigit() }) return "Password must contain a digit"
        return null
    }
    fun validateAmount(raw: String): String? {
        if (raw.isBlank()) return "Amount is required"
        val v = raw.toDoubleOrNull() ?: return "Amount must be a valid number"
        if (v <= 0) return "Amount must be greater than zero"
        return null
    }
    fun validateDescription(desc: String): String? {
        if (desc.isBlank()) return "Description is required"
        return null
    }
}
