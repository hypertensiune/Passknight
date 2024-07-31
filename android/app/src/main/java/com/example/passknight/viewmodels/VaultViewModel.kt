package com.example.passknight.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.services.Firestore
import kotlinx.coroutines.launch

class VaultViewModel(private val navController: NavController, vault: String) : ViewModel() {



    init {
        viewModelScope.launch {
            Firestore.getData(vault)
        }
    }
}