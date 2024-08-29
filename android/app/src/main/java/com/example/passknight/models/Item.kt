package com.example.passknight.models

interface Item {

    var name: String

    fun clear()
    fun encrypt(encryptionProvider: (String) -> String)
    fun decrypt(decryptionProvider: (String) -> String)
}