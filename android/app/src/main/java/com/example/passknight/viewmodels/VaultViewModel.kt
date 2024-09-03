package com.example.passknight.viewmodels

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds.Note
import android.service.autofill.AutofillService
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.os.persistableBundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.passknight.R
import com.example.passknight.Utils
import com.example.passknight.fragments.VaultViewDirections
import com.example.passknight.models.Generator
import com.example.passknight.models.Item
import com.example.passknight.models.NoteItem
import com.example.passknight.models.PasswordItem
import com.example.passknight.models.Vault
import com.example.passknight.services.Clipboard
import com.example.passknight.services.Cryptography
import com.example.passknight.services.Dialog
import com.example.passknight.services.Firestore
import com.example.passknight.services.Settings
import com.google.android.material.color.utilities.DislikeAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class VaultViewModel(
    val navController: NavController,
    private val clipboard: Clipboard,
    val cryptography: Cryptography
) : ViewModel() {

    companion object {
        const val ITEM_PASSWORD = 0
        const val ITEM_NOTE = 1
    }

    val vault: MutableLiveData<Vault> = MutableLiveData(Vault(null, null, null, null, null))

    val search: MutableLiveData<String> = MutableLiveData("")

    private var originalPasswordItem: PasswordItem? = null
    var passwordItem = PasswordItem.empty()
        set(value) {
            field = value
            attachItemNameObserver()
        }

    private var originalNoteItem: NoteItem? = null
    var noteItem = NoteItem.empty()
        set(value) {
            field = value
            attachItemNameObserver()
        }

    var itemEditing = false

    val formScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val formMessage: MutableLiveData<String> = MutableLiveData("")
    val formInputError: MutableLiveData<String> = MutableLiveData("")

    val toastMessage: MutableLiveData<String> = MutableLiveData("")
    val clipboardMessage: MutableLiveData<String> = MutableLiveData("")

    val generator = Generator()

    private var job: Job? = null
    private var namejob: Job? = null

    init {
        if(!Settings.fromAutofillService) {
            viewModelScope.launch(Dispatchers.Main) {
                generator.generatedPassword.asFlow().collect {
                    if(it.isNotEmpty()) {
                        // If a job was launched cancel it and launch a new one
                        // with a 1500 ms delay so we don't spam firestore
                        // with too many request to update the passwords history
                        // if it updates too ofter (moving the length slider around continuously)
                        job?.cancel()
                        job = viewModelScope.launch {
                            delay(1500)
                            vault.value?.addHistoryItem(it)
                            Firestore.updateHistoryItems(vault.value?.generatorHistory?.value!!)
                        }
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val firestoreVault = Firestore.getVault()
            if(firestoreVault != null) {
                vault.postValue(firestoreVault)
            } else {
                toastMessage.postValue("Error when fetching the vault from firebase!")
            }
        }

        attachItemNameObserver()
    }

    private fun attachItemNameObserver() {
        namejob?.cancel()
        namejob = viewModelScope.launch(Dispatchers.Main) {
            var prevpass = ""
            var prevnote = ""
            passwordItem.nameLive.asFlow().collect {
                if(it != prevpass) { formInputError.value = ""; prevpass = it }
            }
            noteItem.nameLive.asFlow().collect {
                if(it != prevnote) { formInputError.value = ""; prevnote = it }
            }
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

    /**
     * Open the password item form for adding new item
     */
    fun openPasswordItemForm() {
        itemEditing = false
        passwordItem = PasswordItem.empty()
        navController.navigate(R.id.vault_view_to_password_form)
    }

    /**
     * Open the password item form for editing
     * @param password The password to edit
     */
    fun openPasswordItemForm(password: PasswordItem) {
        itemEditing = true
        passwordItem = password.copy()
        passwordItem.decrypt(cryptography::decrypt)
        originalPasswordItem = password
        navController.navigate(R.id.vault_view_to_password_form)
    }

    /**
     * Open the note item form for adding new item
     */
    fun openNoteItemForm() {
        itemEditing = false
        noteItem = NoteItem.empty()
        navController.navigate(R.id.vault_view_to_note_form)
    }

    /**
     * Open the note item form for editing
     * @param note The note to edit
     */
    fun openNoteItemForm(note: NoteItem) {
        itemEditing = true
        noteItem = note.copy()
        noteItem.decrypt(cryptography::decrypt)
        originalNoteItem = note
        navController.navigate(R.id.vault_view_to_note_form)
    }

    fun openHistory() {
        navController.navigate(R.id.vault_view_to_history)
    }

    private fun checkItemNameIsValid(item: Item): Boolean {
        item.name = item.name.filterNot { it.isWhitespace() }
        if(item.name.isEmpty()) {
            formInputError.value = "Name must to be empty"
            return false
        }

        if(item is PasswordItem && vault.value?.passwords?.value?.any { it.name == item.name } == true) {
            formInputError.value = "There is already an item with this name"
            return false
        }

        if(item is NoteItem && vault.value?.notes?.value?.any { it.name == item.name } == true) {
            formInputError.value = "There is already an item with this name"
            return false
        }

        return true
    }

    private fun noChanges(original: Item?, item: Item): Boolean {
        if(original is PasswordItem && item is PasswordItem) {
            return original.name == item.name && original.website == item.website && cryptography.decrypt(original.password) == item.password
        } else if(original is NoteItem && item is NoteItem) {
            return original.name == item.name && cryptography.decrypt(original.content) == item.content
        }
        return true
    }

    fun addNewItem(itemFlag: Int) {
        val item = getItem(itemFlag)

        if(!checkItemNameIsValid(item)) {
            return
        }

        // enable the progress bar while waiting for the result
        formScreen.value = true
        formMessage.value = "Creating new item.."

        viewModelScope.launch(Dispatchers.Main) {
            item.encrypt(cryptography::encrypt)

            val result = Firestore.addItemToVault(item)
            formScreen.postValue(false)

            if(result) {
                // If firebase successfully added the new item add it in the vault as well to display
                // Do this to avoid fetching all the data from firebase again after adding
                vault.value?.addItem(item)

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

        if(noChanges(original, item)) {
            navController.popBackStack()
            return
        }

        if(original?.name != item.name && !checkItemNameIsValid(item)) {
            return
        }

        item.updated = Utils.currentTimestamp()
        item.encrypt(cryptography::encrypt)

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

    fun deleteItem(itemFlag: Int) {
        val originalItem = getOriginalItem(itemFlag)

        formScreen.value = true
        formMessage.value = "Deleting item item.."

        viewModelScope.launch(Dispatchers.Main) {
            val result = Firestore.deleteItemInVault(originalItem)
            formScreen.postValue(false)

            if(result) {
                vault.value?.deleteItem(originalItem)
                navController.popBackStack()
                toastMessage.postValue("Item deleted!")
            } else {
                toastMessage.postValue("There was an error deleting the item in firebase!")
            }
        }
    }

    /**
     * Copies the username to the clipboard.
     */
    fun copyUsername(username: String) {
        viewModelScope.launch {
            clipboard.copy("Username", username, false) {
                clipboardMessage.value = "Username copied to clipboard"
            }
        }
    }

    /**
     * Copies the password to the clipboard.
     */
    fun copyPassword(password: String) {
        viewModelScope.launch {
            clipboard.copy("Password", password, true) {
                clipboardMessage.value = "Password copied to clipboard"
            }
        }
    }

    fun fillGeneratorPassword() {
        val pass = generator.generatePassword()
        passwordItem.password = pass
    }

    suspend fun clearHistory() {
        vault.value?.clearHistory()
        Firestore.updateHistoryItems(emptyList())
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

    fun timestampStringToDate(string: String) = Utils.timestampStringToDate(string)
}