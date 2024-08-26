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
import com.example.passknight.fragments.VaultViewDirections
import com.example.passknight.services.Cryptography
import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VaultUnlockViewModel(
    val vault: String,
    private val navController: NavController
): ViewModel() {

    val errorText: MutableLiveData<String> = MutableLiveData("")
    var password: String = ""
        set(value) {
            errorText.value = ""
            field = value
        }

    val loadingScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val loadingMessage = "Unlocking vault.."

    fun onUnlockClick(view: View) {
        if(password.isEmpty()) {
            errorText.value = "Master password required"
            return
        }

        loadingScreen.value = true
        viewModelScope.launch(Dispatchers.Main) {
            val masterPasswordHash = Cryptography.Utils.getMasterPasswordHash("$vault@passknight.vault", password)
            val result = Firestore.unlockVault(vault, masterPasswordHash)
            if(result == null) {
                loadingScreen.postValue(false)
                errorText.postValue("Invalid master password")
                return@launch
            }

            // Don't allow back navigation from the vault view
            // The user can get back to the vault list view only by pressing a button that signs out of firebase
            // https://stackoverflow.com/questions/50514758/how-to-clear-navigation-stack-after-navigating-to-another-fragment-in-android
            navController.navigate(VaultUnlockDirections.vaultUnlockToView("$vault@passknight.vault", password, result))
            //navController.navigate(R.id.vault_unlock_to_view)
        }
    }
}