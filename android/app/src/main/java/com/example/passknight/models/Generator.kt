package com.example.passknight.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlin.random.Random

/**
 * Generate random passwords using custom settings.
 * https://github.com/bitwarden/clients/blob/main/libs/common/src/tools/generator/password/password-generation.service.ts
 */
class Generator() {

    var lowercase: Boolean = true
        set(value) {
            field = value
            generatePassword()
        }

    var uppercase: Boolean = true
        set(value) {
            field = value
            generatePassword()
        }

    var numbers: Boolean = true
        set(value) {
            field = value
            generatePassword()
        }

    var symbols: Boolean = true
        set(value) {
            field = value
            generatePassword()
        }

    var length: Float = 15f
        set(value) {
            field = value
            generatePassword()
        }

    val generatedPassword: MutableLiveData<String> = MutableLiveData("")

    object CharSets {
        const val lowercase = "abcdefghijklmnopqrstuvwxyz"
        const val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        const val numbers = "0123456789"
        const val symbols = "!@#\$%^&*"
    }

    init { generatePassword() }

    /**
     * @return A generated password using the current settings
     */
    fun generatePassword() {
        var positions = ""
        var characters = ""

        if(!lowercase && !uppercase && !symbols && !numbers) {
            generatedPassword.value = ""
            return
        }

        if(lowercase) {
            characters += CharSets.lowercase
            positions += 'l'
        }
        if(uppercase) {
            characters += CharSets.uppercase
            positions += 'u'
        }
        if(numbers) {
            characters += CharSets.numbers
            positions += 'n'
        }
        if(symbols) {
            characters += CharSets.symbols
            positions += 's'
        }

        positions += "a".repeat((length - positions.length).toInt())

        val shuffled = positions.toCharArray()
        shuffled.shuffle()

        var password = ""
        for(char: Char in shuffled) {

            val chars = when(char) {
                'l' -> CharSets.lowercase
                'u' -> CharSets.uppercase
                'n' -> CharSets.numbers
                's' -> CharSets.symbols
                else -> characters
            }

            val random = Random.nextInt(0, chars.length - 1)
            password += chars[random]
        }

        generatedPassword.value = password
    }
}