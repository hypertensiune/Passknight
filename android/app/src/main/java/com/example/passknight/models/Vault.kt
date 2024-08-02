package com.example.passknight.models

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.passknight.adapters.PasswordListAdapter

data class PasswordItem(
    var name: String,
    var website: String,
    var username: String,
    var password: String
) {
    companion object {
        fun from(map: Map<String, Any>) = PasswordItem(
            map["name"] as String,
            map["website"] as String,
            map["username"] as String,
            map["password"] as String
        )

        fun empty(): PasswordItem = PasswordItem("", "", "", "")
    }
}

data class NoteItem(
    var name: String,
    var content: String
) {
    companion object {
        fun from(map: Map<String, Any>) = NoteItem(
            map["name"] as String,
            map["content"] as String
        )

        fun empty(): NoteItem = NoteItem("", "")
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

    fun <T> addItem(item: T) {
        if(item is PasswordItem) {
            val p = passwords.value
            p?.add(item)
            passwords.value = p
        } else if(item is NoteItem) {
            val n = notes.value
            n?.add(item)
            notes.value = n
        }
    }
}
