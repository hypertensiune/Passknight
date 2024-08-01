package com.example.passknight.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.passknight.adapters.PasswordListAdapter

data class PasswordItem(
    val name: String,
    val website: String,
    val username: String,
    val password: String
) {
    companion object {
        fun from(map: Map<String, Any>) = PasswordItem(
            map["name"] as String,
            map["website"] as String,
            map["username"] as String,
            map["password"] as String
        )
    }
}

data class NoteItem(
    val name: String,
    val content: String
) {
    companion object {
        fun from(map: Map<String, Any>) = NoteItem(
            map["name"] as String,
            map["content"] as String
        )
    }
}

class Vault (
    private val name: String,
    data: Map<String, Any>?
) {
    var passwords: MutableLiveData<MutableList<PasswordItem>>
    var notes: MutableLiveData<MutableList<NoteItem>>
    var generatorHistory: MutableLiveData<MutableList<String>>

    init {
        if(data == null) {
            passwords = MutableLiveData(mutableListOf())
            notes = MutableLiveData(mutableListOf())
            generatorHistory = MutableLiveData(mutableListOf())
        } else {
            // Transform the array list of hashmap returned by firestore to an arraylist of PasswordItem or NoteItem
            val p = (data["passwords"] as List<Map<String, Any>>).map { PasswordItem.from(it) }.toMutableList()
            passwords = MutableLiveData(p)

            val n = (data["notes"] as List<Map<String, Any>>).map { NoteItem.from(it) }.toMutableList()
            notes = MutableLiveData(n)

            generatorHistory = MutableLiveData(data["history"] as MutableList<String>)
        }
    }
}
