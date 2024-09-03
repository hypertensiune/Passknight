package com.example.passknight.models

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.MutableLiveData
import com.example.passknight.Utils
import java.time.Instant

class NoteItem(
    name: String,
    content: String,
    override var created: String,
    override var updated: String,
) : Item() {
    companion object {

        fun from(name: String, data: Map<String, String>) = NoteItem(
            name,
            data["content"] as String,
            data["created"] as String,
            data["updated"] as String
        )

        fun empty(): NoteItem = NoteItem("", "", Utils.currentTimestamp(), Utils.currentTimestamp())
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
        return NoteItem(name, content, created, updated)
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