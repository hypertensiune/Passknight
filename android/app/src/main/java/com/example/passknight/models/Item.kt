package com.example.passknight.models

interface Item {
    fun clear()
    fun encrypt(encryptionProvider: (String) -> String)
    fun decrypt(decryptionProvider: (String) -> String)
}