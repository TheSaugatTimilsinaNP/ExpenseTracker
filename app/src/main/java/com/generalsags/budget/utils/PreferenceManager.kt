package com.generalsags.budget.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("budget_tracker_prefs", Context.MODE_PRIVATE)

    fun saveLanguage(languageCode: String) {
        sharedPreferences.edit().putString("language_code", languageCode).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString("language_code", "en") ?: "en"
    }
}
