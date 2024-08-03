package com.example.passknight.services

import android.content.Context
import android.util.Log
import com.example.passknight.models.NoteItem
import com.example.passknight.models.PasswordItem
import com.example.passknight.models.Vault
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import java.net.HttpURLConnection
import java.net.URL

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

    /**
     * @return A list of the all the vaults in the firestore database
     */
    suspend fun getVaults(): List<String>? {
        val res = Firebase.firestore.collection("vaults").document("ids").get().await()
        return res.data?.keys?.toList()
    }

    suspend fun getData(vault: String): MutableMap<String, Any>? {
        try {
            val res = Firebase.firestore.collection("vaults").document(currentUnlockedVaultID).get().await()
            Log.d("Passknight", "DOC: ${res.data}")
            return res.data
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * @return true if unlocking the vault was successful, false if there was a problem unlocking the vault
     */
    suspend fun unlockVault(vault: String, password: String): Boolean {
        try {
            val result = Firebase.auth.signInWithEmailAndPassword("$vault@passknight.vault", password).await()
            currentUnlockedVaultName = vault
            currentUnlockedVaultID = result.user?.uid ?: ""
            return true
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun createVault(vault: String, password: String): Boolean {
        try {
            val result = Firebase.auth.createUserWithEmailAndPassword("${vault.lowercase()}@passknight.vault", password).await()

            val uid = result.user!!.uid
            Firebase.firestore.collection("vaults").document(uid).set(hashMapOf(
                "salt" to "salt",
                "passwords" to emptyList<PasswordItem>(),
                "notes" to emptyList<NoteItem>(),
                "history" to emptyList<String>()
            )).await()

            Firebase.firestore.collection("vaults").document("ids").set(hashMapOf(
                vault.lowercase() to uid
            ), SetOptions.merge()).await()

            return true

        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun <T> addItemToVault(item: T): Boolean {
        try {
            if(item is PasswordItem) {
                Firebase.firestore.collection("vaults").document(currentUnlockedVaultID).update("passwords", FieldValue.arrayUnion(item)).await()
            } else {
                Firebase.firestore.collection("vaults").document(currentUnlockedVaultID).update("notes", FieldValue.arrayUnion(item)).await()
            }

            Log.d("Passknight", "add successful")
            return true
        } catch (e: FirebaseException) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun <T> editItemInVault(oldItem: T, newItem: T): Boolean {
        try {
            if(oldItem is PasswordItem) {
                // First add the new item (modified) and only then remove the old one
                // Do the operations in this order to avoid the following situation: removal of the old
                // item is successful but adding the new item fails (because of internet connection loss, for example).
                // If this happens the item is completely lost
                // In this case if there is a problem with the second operation there will just be the
                // 2 verions of the same item in the database (and the old one can be manually removed)
                Firebase.firestore.collection("vaults").document(currentUnlockedVaultID).update("passwords", FieldValue.arrayUnion(newItem)).await()
                Firebase.firestore.collection("vaults").document(currentUnlockedVaultID).update("passwords", FieldValue.arrayRemove(oldItem)).await()
            } else {
                Firebase.firestore.collection("vaults").document(currentUnlockedVaultID).update("notes", FieldValue.arrayUnion(newItem)).await()
                Firebase.firestore.collection("vaults").document(currentUnlockedVaultID).update("notes", FieldValue.arrayRemove(oldItem)).await()
            }
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