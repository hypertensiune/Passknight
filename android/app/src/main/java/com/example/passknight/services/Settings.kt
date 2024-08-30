package com.example.passknight.services

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions


object Settings {

    /**
     * True if the options required by [FirebaseOptions] used with [FirebaseApp.initializeApp] were
     * provided in settings. Otherwise, false
     */
    var firebaseInitialized: Boolean = false

    var fromAutofillService: Boolean = false

    private lateinit var settings: Map<String, *>

    fun init(settings: Map<String, *>) {
        this.settings = settings
    }

    fun get(key: String): Any? = settings[key]
}