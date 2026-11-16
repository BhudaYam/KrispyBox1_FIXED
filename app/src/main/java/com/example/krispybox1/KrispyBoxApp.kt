package com.example.krispybox1

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class KrispyBoxApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences(
            "krispybox_prefs",
            MODE_PRIVATE
        )

        val darkMode = prefs.getBoolean("dark_mode", true)

        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
