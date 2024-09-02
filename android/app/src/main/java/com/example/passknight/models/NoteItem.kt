package com.example.passknight.models

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.MutableLiveData

class NoteItem(name: String, content: String) : Item() {
    companion object {
        fun from(item: NoteItem) = NoteItem(item.name, item.content)
        fun empty(): NoteItem = NoteItem("", "")
    }


    val nameLive: MutableLiveData<String> = MutableLiveData(name)
    override var name: String
        get() = nameLive.value!!
        set(value) { nameLive.value = value }

    val contentLive: MutableLiveData<String> = MutableLiveData(content)
    var content: String
        get() = contentLive.value!!
        set(value) { contentLive.value = value }

    fun copy(): NoteItem {
        return NoteItem(name, content)
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