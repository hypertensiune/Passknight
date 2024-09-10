package com.example.passknight.services

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore


class EncryptedSharedPrefsUtil(val context: Context) {

    fun get(name: String): SharedPreferences {
        /**
         * When the app is reinstalled the encrypted shared preferences file persists, key store
         * changes and so becomes unusable and crashes.
         *
         * To avoid this, if there is an exception when creating the EncryptedSharedPreferences, clear
         * and delete the previous file and delete the previous master key
         *
         * https://stackoverflow.com/a/77237528
         */
        return try {
            createEncryptedSharedPreferences(name)
        } catch (e: Exception) {
            deleteSharedPreferences(name)
            deleteMasterKey()
            createEncryptedSharedPreferences(name)
        }
    }

    private fun getEncryptedSharedPrefsKey(context: Context): MasterKey {
        val spec = KeyGenParameterSpec
            .Builder(MasterKey.DEFAULT_MASTER_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(10, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
            .build()

        return MasterKey.Builder(context).setKeyGenParameterSpec(spec).build()
    }
    private fun createEncryptedSharedPreferences(name: String): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        name,
        getEncryptedSharedPrefsKey(context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun deleteSharedPreferences(name: String) {
        try {
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().apply()
            context.deleteSharedPreferences(name)
        } catch (e: Exception) {
            Log.e("Passknight", "Error deleting shared preferences")
        }
    }

    private fun deleteMasterKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            Log.e("Passknight", "Error deleting encrypted shared prefs master key")
        }
    }
}