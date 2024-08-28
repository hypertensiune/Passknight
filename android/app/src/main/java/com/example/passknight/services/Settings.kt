package com.example.passknight.services

object Settings {

    var firebaseInitialized: Boolean = false

    private lateinit var settings: Map<String, *>

    fun init(settings: Map<String, *>) {
        this.settings = settings
    }

    fun get(key: String): Any? = settings[key]
}