package com.example.passknight.viewmodels

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R

import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VaultCreateViewModel(private val navController: NavController) : ViewModel() {

    var name: String = ""
    val nameError: MutableLiveData<String> = MutableLiveData("")

    var masterPassword: String = ""
    val masterPasswordError: MutableLiveData<String> = MutableLiveData("")

    var confirmMasterPassword: String = ""
    val confirmMasterPasswordError: MutableLiveData<String> = MutableLiveData("")

    val creating: MutableLiveData<Boolean> = MutableLiveData(false)
    val toastMessage: MutableLiveData<String> = MutableLiveData("")

    fun onNameChange(s: CharSequence, start: Int, before: Int, count: Int) {
        nameError.value = ""
    }

    fun onPasswordChange(s: CharSequence, start: Int, before: Int, count: Int) {
        masterPasswordError.value = ""
    }

    fun onConfirmPasswordChange(s: CharSequence, start: Int, before: Int, count: Int) {
        confirmMasterPasswordError.value = ""
    }

    fun createVault(view: View) {
        // validate data before attempting to create a new vault
        if(validateData()) {
            // enable the progress bar while waiting for the result
            creating.value = true
            viewModelScope.launch(Dispatchers.Main) {
                val result = Firestore.createVault(name, masterPassword)
                Log.d("Passknight", "Finised creating $result")
                if(result) {
                    // if creation was successful navigate to the newly created vault
                    creating.postValue(false)
                    navController.navigate(R.id.vault_create_to_view)
                } else {
                    // otherwise clear the input fields and display an error message
                    creating.postValue(false)
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
        if(masterPassword.length < 6) {
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