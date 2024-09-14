package com.example.passknight.viewmodels

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R
import com.example.passknight.fragments.VaultCreateDirections
import com.example.passknight.fragments.VaultUnlockDirections
import com.example.passknight.services.Cryptography

import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VaultCreateViewModel(val navController: NavController) : ViewModel() {

    val nameError: MutableLiveData<String> = MutableLiveData("")
    var name: String = ""
        set(value) {
            nameError.value = ""
            field = value
        }

    val masterPasswordError: MutableLiveData<String> = MutableLiveData("")
    var masterPassword: String = ""
        set(value) {
            masterPasswordError.value = ""
            field = value
        }

    val confirmMasterPasswordError: MutableLiveData<String> = MutableLiveData("")
    var confirmMasterPassword: String = ""
        set(value) {
            confirmMasterPasswordError.value = ""
            field = value
        }

    val loadingScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val loadingMessage = "Creating vault.."
    val toastMessage: MutableLiveData<String> = MutableLiveData("")

    fun createVault(view: View) {
        // validate data before attempting to create a new vault
        if(validateData()) {
            // enable the progress bar while waiting for the result
            loadingScreen.value = true
            viewModelScope.launch(Dispatchers.Main) {
                val cryptoPair = Cryptography.Utils.create("$name@passknight.vault", masterPassword)
                val result = Firestore.createVault(name, cryptoPair.first, cryptoPair.second)
                if(result) {
                    // if creation was successful navigate to the newly created vault
                    loadingScreen.postValue(false)
                    // Don't allow back navigation from the vault view
                    // The user can get back to the vault list view only by pressing a button that signs out of firebase
                    // https://stackoverflow.com/questions/50514758/how-to-clear-navigation-stack-after-navigating-to-another-fragment-in-android
                    navController.navigate(VaultCreateDirections.vaultCreateToView(name, "$name@passknight.vault", masterPassword, cryptoPair.second))
                    //navController.navigate(R.id.vault_create_to_view)
                } else {
                    // otherwise clear the input fields and display an error message
                    loadingScreen.postValue(false)
                    toastMessage.postValue("Vault creation failed! Vault name is already used!")
                    name = ""
                    masterPassword = ""
                    confirmMasterPassword = ""
                }
            }
        }
    }

    /**
     * Validates the inputs required to create a new vault.
     * @return true if the data is valid for use, false otherwise
     */
    private fun validateData(): Boolean {
        var valid = true
        if(name.isEmpty()) {
            nameError.value = "Vault name is required"
            valid = false
        }
        if(masterPassword.length < 15) {
            masterPasswordError.value = "Password should be at least 15 characters"
            valid = false
        }
        if(masterPassword != confirmMasterPassword) {
            confirmMasterPasswordError.value = "Passwords did not match"
            valid = false
        }

        return valid
    }
}