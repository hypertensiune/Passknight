package com.example.passknight.models

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.passknight.adapters.PasswordListAdapter

interface Item {
    fun clear()
    fun encrypt(encryptionProvider: (String) -> String)
    fun decrypt(decryptionProvider: (String) -> String)
}

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

class Vault (
    val name: String?,
    psk: Map<String, Any>?,
    passwords: Map<String, Any>?,
    notes: Map<String, Any>?,
    history: Map<String, Any>?
) {
    val passwords: MutableLiveData<MutableList<PasswordItem>>
    val notes: MutableLiveData<MutableList<NoteItem>>
    val generatorHistory: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    init {
        if(psk == null || passwords == null || notes == null) {
            this.passwords = MutableLiveData(mutableListOf())
            this.notes = MutableLiveData(mutableListOf())
        } else {
            // Transform the array list of hashmap returned by firestore to an arraylist of PasswordItem or NoteItem
            val p = passwords!!.map { PasswordItem.from(it.key, it.value as Map<String, String>) }.toMutableList()
            this.passwords = MutableLiveData(p)

            val n = notes!!.map { NoteItem(it.key, it.value as String) }.toMutableList()
            this.notes = MutableLiveData(n)

//            this.generatorHistory = MutableLiveData(history)
        }
    }

    fun addHistoryItem(item: String) {
        val h = generatorHistory.value
        h?.add(item)
        generatorHistory.value = h
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

    fun <T> deleteItem(item: T) {
        if(item is PasswordItem) {
            val p = passwords.value
            p?.remove(item)
            passwords.value = p
        } else if(item is NoteItem) {
            val n = notes.value
            n?.remove(item)
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