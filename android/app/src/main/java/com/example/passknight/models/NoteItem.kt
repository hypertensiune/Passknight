package com.example.passknight.models

data class NoteItem(
    var name: String,
    var content: String
) : Item {
    companion object {
        fun empty(): NoteItem = NoteItem("", "")
    }

    override fun clear() {
        name = ""
        content = ""
    }

    override fun encrypt(encryptionProvider: (String) -> String) {
        content = encryptionProvider(content)
    }

    override fun decrypt(decryptionProvider: (String) -> String) {
        content = decryptionProvider(content)
    }
}