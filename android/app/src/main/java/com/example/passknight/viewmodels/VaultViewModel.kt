package com.example.passknight.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.models.NoteItem
import com.example.passknight.models.PasswordItem
import com.example.passknight.models.Vault
import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

class VaultViewModel(private val navController: NavController) : ViewModel() {

    val vault: MutableLiveData<Vault> = MutableLiveData(Vault("android", null))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val data = Firestore.getData("android")
            vault.postValue(Vault("vaultName", data))
        }
    }

    fun onNewPasswordItemClick(view: View) {
        val p = vault.value?.passwords?.value
        p?.add(PasswordItem("name", "website", "username", "1234"))
        vault.value?.passwords?.value = p
    }

    fun onNewNoteItemClick(view: View) {

    }
}