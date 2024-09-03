package com.example.passknight.models

abstract class Item {

    abstract var name: String

    abstract var created: String
    abstract var updated: String

    abstract fun clear()
    abstract fun encrypt(encryptionProvider: (String) -> String)
    abstract fun decrypt(decryptionProvider: (String) -> String)
}