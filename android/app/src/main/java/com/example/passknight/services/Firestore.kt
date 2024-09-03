package com.example.passknight.services

import com.example.passknight.models.NoteItem
import com.example.passknight.models.PasswordItem
import com.example.passknight.models.Vault
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Wrapper object around the Firebase firestore API.
 * Used for fetching, uploading, updating and deleting collections and documents.
 */
object Firestore {

    const val CREATION_OK = 0x01
    const val CREATION_FAIL = 0x01
    const val UNLOCK_OK = 0x10
    const val UNLOCK_FAIL = 0x11

    private var currentUnlockedVaultName = ""
    private var currentUnlockedVaultID = ""



    // Also removes all whitespaces from the vault name
    private fun email(vault: String) = "${vault.lowercase().filterNot { it.isWhitespace() }}@passknight.vault"

    /**
     * @return A list of the all the vaults in the firestore database
     */
    suspend fun getVaultNames(): List<String>? {
        try {
            val res = Firebase.firestore.collection("vaults").document("ids").get().await()
            return res.data?.keys?.toList()
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Creates a new vault. If creation is successful this will be the currently unlocked vault
     * @return The protected symmetric key associated with this vault if creation was successful, otherwise null
     */
    suspend fun createVault(vault: String, password: String, psk: String): Boolean {
        try {
            val result = Firebase.auth.createUserWithEmailAndPassword(email(vault), password).await()
            val uid = result.user!!.uid

            Firebase.firestore.collection(uid).document("psk").set(hashMapOf("psk" to psk))
            Firebase.firestore.collection(uid).document("passwords").set(emptyMap<String, Any>())
            Firebase.firestore.collection(uid).document("notes").set(emptyMap<String, Any>())
            Firebase.firestore.collection(uid).document("history").set(hashMapOf("history" to emptyList<String>()))

            Firebase.firestore.collection("vaults").document("ids").set(hashMapOf(
                vault.lowercase() to uid
            ), SetOptions.merge()).await()

            currentUnlockedVaultName = vault
            currentUnlockedVaultID = uid

            return true

        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * @return The protected symmetric key associated with this vault if unlocking was successful, otherwise null
     */
    suspend fun unlockVault(vault: String, password: String): String? {
        try {
            val result = Firebase.auth.signInWithEmailAndPassword(email(vault), password).await()

            currentUnlockedVaultName = vault
            currentUnlockedVaultID = result.user?.uid!!

            return Firebase.firestore.collection(currentUnlockedVaultID).document("psk").get().await().data?.get("psk")!! as String
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun getVaultPsk(): String? {
        if(Firebase.auth.currentUser == null) {
            return null
        }

        currentUnlockedVaultID = Firebase.auth.currentUser!!.uid
        currentUnlockedVaultName = Firebase.auth.currentUser!!.email!!.split("@")[0]

        return Firebase.firestore.collection(currentUnlockedVaultID).document("psk").get().await().data?.get("psk")!! as String
    }

    /**
     * Gets the currently unlocked vault data content
     *
     * @return A pair containing the name of the vault and its content
     */
    suspend fun getVault(): Vault? {
        try {
            val psk = Firebase.firestore.collection(currentUnlockedVaultID).document("psk").get().await()
            val passwords = Firebase.firestore.collection(currentUnlockedVaultID).document("passwords").get().await()
            val notes = Firebase.firestore.collection(currentUnlockedVaultID).document("notes").get().await()
            val history = Firebase.firestore.collection(currentUnlockedVaultID).document("history").get().await()

            return Vault(currentUnlockedVaultName, psk.data, passwords.data, notes.data, history.data)
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun <T> addItemToVault(item: T): Boolean {
        try {
            if(item is PasswordItem) {
                Firebase.firestore.collection(currentUnlockedVaultID).document("passwords").update(item.name, mapOf(
                    "website" to item.website,
                    "username" to item.username,
                    "password" to item.password,
                    "created" to item.created,
                    "updated" to item.updated
                )).await()
            } else if(item is NoteItem) {
                Firebase.firestore.collection(currentUnlockedVaultID).document("notes").update(item.name, mapOf(
                    "content" to item.content,
                    "created" to item.created,
                    "updated" to item.updated
                )).await()
            }
            return true
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun <T> editItemInVault(oldItem: T, newItem: T): Boolean {
        try {
            // First add the new item (modified) and only then remove the old one
            // Do the operations in this order to avoid the following situation: removal of the old
            // item is successful but adding the new item fails (because of internet connection loss, for example).
            // If this happens the item is completely lost
            // In this case if there is a problem with the second operation there will just be the
            // 2 verions of the same item in the database (and the old one can be manually removed)

            addItemToVault(newItem)

            if((oldItem is PasswordItem && newItem is PasswordItem && oldItem.name != newItem.name) ||
                (oldItem is NoteItem && newItem is NoteItem && oldItem.name != newItem.name)) {
                deleteItemInVault(oldItem)
            }

            return true
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun <T> deleteItemInVault(item: T): Boolean {
        try {
            if(item is PasswordItem) {
                Firebase.firestore.collection(currentUnlockedVaultID).document("passwords").update(item.name, FieldValue.delete()).await()
            } else if(item is NoteItem) {
                Firebase.firestore.collection(currentUnlockedVaultID).document("notes").update(item.name, FieldValue.delete()).await()
            }
            return true
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun updateHistoryItems(items: List<String>): Boolean {
        try {
            Firebase.firestore.collection(currentUnlockedVaultID).document("history").update("history", items).await()
            return true
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun deleteVault(): Boolean {
        // Return if there is no user signed in
        if(Firebase.auth.currentUser == null) {
            return false
        }

        try {
            Firebase.auth.currentUser!!.delete()

            Firebase.firestore.collection("vaults").document("ids").update(currentUnlockedVaultName, FieldValue.delete()).await()
            Firebase.firestore.collection(currentUnlockedVaultID).document("psk").delete()
            Firebase.firestore.collection(currentUnlockedVaultID).document("passwords").delete()
            Firebase.firestore.collection(currentUnlockedVaultID).document("notes").delete()
            Firebase.firestore.collection(currentUnlockedVaultID).document("history").delete()

            currentUnlockedVaultName = ""
            currentUnlockedVaultID = ""

            return true
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    fun signOut() {
        currentUnlockedVaultName = ""
        currentUnlockedVaultID = ""
        Firebase.auth.signOut()
    }
}