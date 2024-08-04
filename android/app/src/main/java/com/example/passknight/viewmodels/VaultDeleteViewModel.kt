package com.example.passknight.viewmodels

import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.AppActivity
import com.example.passknight.fragments.VaultDeleteDirections
import com.example.passknight.fragments.VaultUnlockDirections
import com.example.passknight.services.BiometricsProvider
import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This view model requires the vault name because in the process of deletion
 * the master password is asked again and used with [Firestore.unlockVault] to authenticate
 *
 * @param vault The vault's name to delete
 */
class VaultDeleteViewModel(
    val vault: String,
    private val navController: NavController,
    private val biometricsProvider: BiometricsProvider
) : ViewModel() {

    val masterPasswordError: MutableLiveData<String> = MutableLiveData("")
    var masterPassword: String = ""
        set(value) {
            masterPasswordError.value = ""
            field = value
        }

    val loadingScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val toastMessage: MutableLiveData<String> = MutableLiveData("")

    /**
     * Try to delete the [vault]
     */
    fun deleteVault() {
        if(masterPassword.isEmpty()) {
            return
        }

        loadingScreen.value = true

        viewModelScope.launch(Dispatchers.Main) {
            val result = Firestore.unlockVault(vault, masterPassword)
            if(!result) {
                loadingScreen.postValue(false)
                masterPasswordError.postValue("Invalid master password!")
                return@launch
            }

            // Ask for one more confirmation with the biometric prompt before finally deleting the vault
            biometricsProvider.prompt {
                viewModelScope.launch(Dispatchers.Main) {
                    val res = Firestore.deleteVault()
                    if(!res) {
                        toastMessage.postValue("There was an error deleting this vault!")
                    }

                    // Don't allow back navigation from the vault view
                    // https://stackoverflow.com/questions/50514758/how-to-clear-navigation-stack-after-navigating-to-another-fragment-in-android
                    navController.navigate(VaultDeleteDirections.vaultDeleteToVaultList())
                }
            }
        }
    }

    fun cancel() {
        navController.popBackStack()
    }
}