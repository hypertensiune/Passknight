package com.example.passknight.models

import androidx.lifecycle.MutableLiveData

class PasswordItem(name: String, website: String, username: String, pass: String) : Item() {

    companion object {
        fun from(name: String, data: Map<String, String>) = PasswordItem(
            name,
            data["website"] as String,
            data["username"] as String,
            data["password"] as String
        )

        fun empty(): PasswordItem = PasswordItem("", "", "", "")
    }

    val nameLive: MutableLiveData<String> = MutableLiveData(name)
    override var name: String
        get() = nameLive.value!!
        set(value) { nameLive.value = value }

    val websiteLive: MutableLiveData<String> = MutableLiveData(website)
    var website: String
        get() = websiteLive.value!!
        set(value) { websiteLive.value = value }

    val usernameLive: MutableLiveData<String> = MutableLiveData(username)
    var username: String
        get() = usernameLive.value!!
        set(value) { usernameLive.value = value }

    val passwordLive: MutableLiveData<String> = MutableLiveData(pass)
    var password: String
        get() = passwordLive.value!!
        set(value) { passwordLive.value = value }

    fun copy(): PasswordItem {
        return PasswordItem(name, website, username, password)
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