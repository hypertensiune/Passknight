package com.example.passknight.services

import android.content.Context
import android.util.Log
import com.example.passknight.models.NoteItem
import com.example.passknight.models.PasswordItem
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
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

    private var vaults: MutableMap<String, Any>? = null

    /**
     * @return A list of the all the vaults in the firestore database
     */
    suspend fun getVaults(): List<String>? {
        val res = Firebase.firestore.collection("vaults").document("ids").get().await()
        vaults = res.data
        return res.data?.keys?.toList()
    }

    suspend fun getData(vault: String) {
        try {
            val vaultId = vaults?.get(vault) as String
            val res = Firebase.firestore.collection("vaults").document(vaultId).get().await()
            Log.d("Passknight", "DOC: ${res.data}")
        } catch (e: FirebaseException) {
            e.printStackTrace()
        }
    }

    /**
     * @return true if unlocking the vault was successful, false if there was a problem unlocking the vault
     */
    suspend fun unlockVault(vault: String, password: String): Boolean {
        try {
            Firebase.auth.signInWithEmailAndPassword("$vault@passknight.vault", password).await()
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

    fun signOut(): Unit {
        Firebase.auth.signOut()
    }
}