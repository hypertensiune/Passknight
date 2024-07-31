package com.example.passknight.viewmodels

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R
import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VaultUnlockViewModel(
    val vault: String,
    private val navController: NavController
): ViewModel() {

    var password: String = ""
    var errorText: MutableLiveData<String> = MutableLiveData("")

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        errorText.value = ""
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
            navController.navigate(R.id.vault_unlock_to_view, bundleOf("vault" to vault))
        }
    }
}