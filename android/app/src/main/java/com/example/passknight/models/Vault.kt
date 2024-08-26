package com.example.passknight.models

import androidx.lifecycle.MutableLiveData

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