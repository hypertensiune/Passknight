package com.example.passknight.viewmodels

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R
import com.example.passknight.fragments.VaultCreateDirections
import com.example.passknight.fragments.VaultUnlockDirections
import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VaultUnlockViewModel(
    val vault: String,
    private val navController: NavController
): ViewModel() {

    var errorText: MutableLiveData<String> = MutableLiveData("")
    var password: String = ""
        set(value) {
            errorText.value = ""
            field = value
        }

    fun onUnlockClick(view: View) {
        if(password.isEmpty()) {
            errorText.value = "Master password required"
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            val result = Firestore.unlockVault(vault, password)
            if(!result) {
                errorText.postValue("Invalid master password")
                return@launch
            }

            // Don't allow back navigation from the vault view
            // The user can get back to the vault list view only by pressing a button that signs out of firebase
            // https://stackoverflow.com/questions/50514758/how-to-clear-navigation-stack-after-navigating-to-another-fragment-in-android
            navController.navigate(VaultUnlockDirections.vaultUnlockToView())
            //navController.navigate(R.id.vault_unlock_to_view)
        }
    }
}