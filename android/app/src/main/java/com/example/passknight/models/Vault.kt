package com.example.passknight.models

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.passknight.adapters.PasswordListAdapter

interface Item {
    fun clear()
}

data class PasswordItem(
    var name: String,
    var website: String,
    var username: String,
    var password: String
) : Item {
    companion object {
        fun from(map: Map<String, Any>) = PasswordItem(
            map["name"] as String,
            map["website"] as String,
            map["username"] as String,
            map["password"] as String
        )

        fun empty(): PasswordItem = PasswordItem("", "", "", "")
    }

    override fun clear() {
        name = ""
        website = ""
        username = ""
        password = ""
    }
}

data class NoteItem(
    var name: String,
    var content: String
) : Item {
    companion object {
        fun from(map: Map<String, Any>) = NoteItem(
            map["name"] as String,
            map["content"] as String
        )

        fun empty(): NoteItem = NoteItem("", "")
    }

    override fun clear() {
        name = ""
        content = ""
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

    fun <T> editItem(oldItem: T, newItem: T) {
        if(oldItem is PasswordItem) {
            editPasswordItem(oldItem as PasswordItem, newItem as PasswordItem)
        } else {
            editNoteItem(oldItem as NoteItem, newItem as NoteItem)
        }
    }

    private fun editPasswordItem(oldItem: PasswordItem, newItem: PasswordItem) {
        val p = passwords.value
        val i = p?.indexOf(oldItem)
        if(i == null || i < 0) {
            return
        }

        p.removeAt(i)
        p.add(i, newItem)

        passwords.value = p
    }

    private fun editNoteItem(oldItem: NoteItem, newItem: NoteItem) {
        val n = notes.value
        val i = n?.indexOf(oldItem)
        if(i == null || i < 0) {
            return
        }

        n.removeAt(i)
        n.add(i, newItem)

        notes.value = n
    }
}