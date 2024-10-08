package com.example.passknight.viewmodels

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R
import com.example.passknight.services.Firestore
import com.example.passknight.services.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VaultListViewModel(private val navController: NavController): ViewModel() {

    val vaults: MutableLiveData<List<String>> = MutableLiveData<List<String>>()

    val loadingScreen: MutableLiveData<Boolean> = MutableLiveData(true)
    val loadingMessage = "Fetching vaults from firebase.."

    private val TAG = "Passknight"

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getVaults()
        }
    }

    // https://stackoverflow.com/questions/47941537/notify-observer-when-item-is-added-to-list-of-livedata
    private suspend fun getVaults() {
        vaults.postValue(Firestore.getVaultNames())
        loadingScreen.postValue(false)
    }

    fun onVaultItemClick(vault: String) {
        Log.d(TAG, "clicked $vault")
        navController.navigate(R.id.vault_list_to_unlock, bundleOf("vault" to vault))
    }

    fun onVaultCreateClick(view: View) {
        navController.navigate(R.id.vault_list_tc_create)
    }

    fun openSettings() {
        navController.navigate(R.id.vault_list_to_settings)
    }
}