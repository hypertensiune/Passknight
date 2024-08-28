package com.example.passknight

import android.app.Application
import android.content.Intent
import androidx.preference.PreferenceManager
import com.example.passknight.services.Settings
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class PkApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("pkcryptonative")

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        Settings.init(preferences.all)

        val apiKey = preferences.getString("apikey", "")!!
        val projectId = preferences.getString("projectId", "")!!
        val storageBucket = preferences.getString("storageBucket", "")!!

        Settings.firebaseInitialized = apiKey.isNotEmpty() && projectId.isNotEmpty() && storageBucket.isNotEmpty()

        FirebaseApp.initializeApp(applicationContext, FirebaseOptions.Builder()
            .setApiKey(apiKey)
            .setApplicationId(applicationContext.packageName)
            .setProjectId(projectId)
            .setStorageBucket(storageBucket)
            .build())
    }
}