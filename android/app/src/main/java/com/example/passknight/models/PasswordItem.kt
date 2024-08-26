package com.example.passknight.models

data class PasswordItem(
    var name: String,
    var website: String,
    var username: String,
    var password: String
) : Item {
    companion object {
        fun from(name: String, data: Map<String, String>) = PasswordItem(
            name,
            data["website"] as String,
            data["username"] as String,
            data["password"] as String
        )

        fun empty(): PasswordItem = PasswordItem("", "", "", "")
    }

    override fun clear() {
        name = ""
        website = ""
        username = ""
        password = ""
    }

    override fun encrypt(encryptionProvider: (String) -> String) {
        password = encryptionProvider(password)
    }

    override fun decrypt(decryptionProvider: (String) -> String) {
        password = decryptionProvider(password)
    }
}