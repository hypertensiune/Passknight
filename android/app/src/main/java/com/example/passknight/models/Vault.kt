package com.example.passknight.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class PasswordItem(
    val name: String,
    val website: String,
    val username: String,
    val password: String
)

data class NoteItem(
    val name: String,
    val content: String
)

class Vault(
    passwords: List<PasswordItem>,
    notes: List<NoteItem>,
    generatorHistory: List<String>
) {
    val passwords: MutableLiveData<List<PasswordItem>> = MutableLiveData(passwords)
    val notes: MutableLiveData<List<NoteItem>> = MutableLiveData(notes)
    val generatorHistory: MutableLiveData<List<String>> = MutableLiveData(generatorHistory)
}
