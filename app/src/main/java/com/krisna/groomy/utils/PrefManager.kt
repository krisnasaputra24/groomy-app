package com.krisna.groomy.utils

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("groomy_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "access_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
