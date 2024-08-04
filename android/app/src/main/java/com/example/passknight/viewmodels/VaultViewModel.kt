package com.example.passknight.viewmodels

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.service.autofill.AutofillService
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R
import com.example.passknight.fragments.VaultViewDirections
import com.example.passknight.models.Item
import com.example.passknight.models.NoteItem
import com.example.passknight.models.PasswordItem
import com.example.passknight.models.Vault
import com.example.passknight.services.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VaultViewModel(
    private val navController: NavController,
    private val clipboardManager: ClipboardManager?
) : ViewModel() {

    companion object {
        const val ITEM_PASSWORD = 0
        const val ITEM_NOTE = 1
        const val CLIPBOARD_TIMEOUT: Long = 5000
    }

    val vault: MutableLiveData<Vault> = MutableLiveData(Vault(null, null))

    var passwordItem = PasswordItem.empty()
    private var originalPasswordItem: PasswordItem? = null
    var noteItem = NoteItem.empty()
    private var originalNoteItem: NoteItem? = null

    var itemEditing = false

    val formScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val formMessage: MutableLiveData<String> = MutableLiveData("")

    val toastMessage: MutableLiveData<String> = MutableLiveData("")
    val clipboardMessage: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val vaultData = Firestore.getVault()
            vault.postValue(Vault(vaultData?.first, vaultData?.second))
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
        formScreen.value = true
        formMessage.value = "Adding new item.."

        viewModelScope.launch(Dispatchers.Main) {
            val result = Firestore.addItemToVault(getItem(itemFlag))
            formScreen.postValue(false)

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

        formScreen.value = true
        formMessage.value = "Editing item.."

        viewModelScope.launch(Dispatchers.Main) {
            val result = Firestore.editItemInVault(original, item)
            formScreen.postValue(false)

            if(result) {
                vault.value?.editItem(original, item)
                navController.popBackStack()
            } else {
                toastMessage.postValue("There was an error updating the item in firebase!")
            }
        }
    }

    /**
     * Copies the username to the clipboard.
     * Clipboard is cleared after [CLIPBOARD_TIMEOUT] ms
     */
    fun copyUsername(username: String) {
        clipboardManager?.let {
            it.setPrimaryClip(ClipData.newPlainText("Username", username))
            clipboardMessage.value = "Username copied to clipboard"

            viewModelScope.launch(Dispatchers.IO) {
                delay(CLIPBOARD_TIMEOUT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    clipboardManager.clearPrimaryClip()
                } else {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            }
        }
    }

    /**
     * Copies the password to the clipboard.
     * Clipboard is cleared after [CLIPBOARD_TIMEOUT] ms
     */
    fun copyPassword(password: String) {
        clipboardManager?.let {
            it.setPrimaryClip(ClipData.newPlainText("Password", password))
            clipboardMessage.value = "Password copied to clipboard"

            viewModelScope.launch(Dispatchers.IO) {
                delay(CLIPBOARD_TIMEOUT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    clipboardManager.clearPrimaryClip()
                } else {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            }
        }
    }

    fun deleteVault() {
        navController.navigate(R.id.vault_view_to_vault_delete, bundleOf("vault" to vault.value?.name))
    }

    fun lockVault() {
        Firestore.signOut()

        // Don't allow back navigation
        // https://stackoverflow.com/questions/50514758/how-to-clear-navigation-stack-after-navigating-to-another-fragment-in-android
        navController.navigate(VaultViewDirections.vaultViewToVaultList())
    }
}