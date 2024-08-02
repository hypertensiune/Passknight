package com.example.passknight.viewmodels

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R
import com.example.passknight.models.NoteItem
import com.example.passknight.models.PasswordItem
import com.example.passknight.models.Vault
import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable

class VaultViewModel(private val navController: NavController) : ViewModel() {

    val vault: MutableLiveData<Vault> = MutableLiveData(Vault("android", null))

    val passwordItem = PasswordItem.empty()

    val loadingScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val loadingMessage = "Adding new password item.."
    val toastMessage: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val data = Firestore.getData("android")
            vault.postValue(Vault("vaultName", data))
        }
    }

    fun openPasswordItemForm(view: View) {
        navController.navigate(R.id.vault_view_to_password_form)
    }

    fun addNewPasswordItem(view: View) {
        // enable the progress bar while waiting for the result
        loadingScreen.value = true
        viewModelScope.launch(Dispatchers.Main) {
            val result = Firestore.addItemToVault(passwordItem)
            if(result) {
                // If firebase successfully added the new item add it in the vault as well to display
                // Do this to avoid fetching all the data from firebase again after adding
                vault.value?.addItem(passwordItem)

                // Navigate back to the passwords tab
                navController.popBackStack()
            } else {
                // Disable the loading screen and display a message
                // notifying the user there was an error
                loadingScreen.postValue(false)
                toastMessage.postValue("There was an error adding the new password item to firebase!")

                // Clear all data
                passwordItem.name = ""
                passwordItem.website = ""
                passwordItem.username = ""
                passwordItem.password = ""
            }
        }
    }

    fun openNoteItemForm(view: View) {

    }
}