package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    IT("it", "Italiano", "🇮🇹"),
    EN("en", "English", "🇬🇧");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: IT
        }
    }
}
