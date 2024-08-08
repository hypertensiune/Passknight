package com.example.passknight.models

import androidx.lifecycle.MutableLiveData

class Generator {

    data class Settings(
        var lowercase: Boolean,
        var uppercase: Boolean,
        var numbers: Boolean,
        var symbols: Boolean,
        var length: Int
    )

    var settings: Settings = Settings(
        lowercase = true,
        uppercase = true,
        numbers = true,
        symbols = true,
        15
    )

}