package com.example.passknight.viewmodels

import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R
import com.example.passknight.models.Item
import com.example.passknight.models.NoteItem
import com.example.passknight.models.PasswordItem
import com.example.passknight.models.Vault
import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VaultViewModel(private val navController: NavController) : ViewModel() {

    companion object {
        const val ITEM_PASSWORD = 0
        const val ITEM_NOTE = 1
    }

    val vault: MutableLiveData<Vault> = MutableLiveData(Vault("android", null))

    var passwordItem = PasswordItem.empty()
    private var originalPasswordItem: PasswordItem? = null
    var noteItem = NoteItem.empty()
    private var originalNoteItem: NoteItem? = null

    var itemEditing = false

    val loadingScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val loadingMessage: MutableLiveData<String> = MutableLiveData("")
    val toastMessage: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val data = Firestore.getData("android")
            vault.postValue(Vault("vaultName", data))
        }
    }

    /**
     * Returns either the [passwordItem] or [noteItem] depending on the passed flag
     * @param itemFlag Should be [ITEM_PASSWORD] or [ITEM_NOTE]
     */
    private fun getItem(itemFlag: Int): Item = if (itemFlag == ITEM_PASSWORD) passwordItem else noteItem

    /**
     * Returns either the [originalPasswordItem] or [originalNoteItem] depending on the passed flag
     * @param itemFlag Should be [ITEM_PASSWORD] or [ITEM_NOTE]
     */
    private fun getOriginalItem(itemFlag: Int): Item? = if (itemFlag == ITEM_PASSWORD) originalPasswordItem else originalNoteItem

    fun openPasswordItemForm(view: View) {
        itemEditing = false
        passwordItem = PasswordItem.empty()
        navController.navigate(R.id.vault_view_to_password_form)
    }

    /**
     * Open the password form for editing
     * @param password The password to edit
     */
    fun openPasswordItemForm(password: PasswordItem) {
        itemEditing = true
        originalPasswordItem = password
        passwordItem = password.copy()
        navController.navigate(R.id.vault_view_to_password_form)
    }

    fun openNoteItemForm(view: View) {
        itemEditing = false
        noteItem = NoteItem.empty()
        navController.navigate(R.id.vault_view_to_note_form)
    }

    /**
     * Open the note form for editing
     * @param note The note to edit
     */
    fun openNoteItemForm(note: NoteItem) {
        itemEditing = true
        originalNoteItem = note
        noteItem = note.copy()
        navController.navigate(R.id.vault_view_to_note_form)
    }

    fun addNewItem(itemFlag: Int) {
        // enable the progress bar while waiting for the result
        loadingScreen.value = true
        loadingMessage.value = "Adding new item.."

        viewModelScope.launch(Dispatchers.Main) {
            val result = Firestore.addItemToVault(getItem(itemFlag))
            loadingScreen.postValue(false)

            if(result) {
                // If firebase successfully added the new item add it in the vault as well to display
                // Do this to avoid fetching all the data from firebase again after adding
                vault.value?.addItem(getItem(itemFlag))

                // Navigate back to the passwords tab
                navController.popBackStack()
            } else {
                toastMessage.postValue("There was an error adding the new item to firebase!")
                getItem(itemFlag).clear()
            }
        }
    }

    fun editItem(itemFlag: Int) {
        val original = getOriginalItem(itemFlag)
        val item = getItem(itemFlag)

        // Don't do anything if there was no changes to the item
        if(item == original) {
            toastMessage.postValue("No changes were made!")
            navController.popBackStack()
            return
        }

        loadingScreen.value = true
        loadingMessage.value = "Editing item.."

        viewModelScope.launch(Dispatchers.Main) {
            val result = Firestore.editItemInVault(original, item)
            loadingScreen.postValue(false)

            if(result) {
                vault.value?.editItem(original, item)
                navController.popBackStack()
            } else {
                toastMessage.postValue("There was an error updating the item in firebase!")
            }
        }
    }
}