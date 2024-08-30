package com.example.passknight

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.Dataset
import android.util.Base64
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators
import androidx.lifecycle.lifecycleScope
import com.example.passknight.services.BiometricsProvider
import com.example.passknight.services.Clipboard
import com.example.passknight.services.Cryptography
import com.example.passknight.services.Dialog
import com.example.passknight.services.Firestore
import com.example.passknight.services.Settings
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.Security
import javax.crypto.spec.PBEParameterSpec

class MainActivity : AppCompatActivity() {

    private lateinit var biometricsProvider: BiometricsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show a message and close the app if there is no internet connection
        if(!isOnline()) {
            Dialog("No internet connection! Connect to internet and try again.", "Ok", null, positiveAction = {
                finishAndRemoveTask()
            }).show(supportFragmentManager, "NO_INTERNET")
            return
        }

        biometricsProvider = BiometricsProvider(
            applicationContext,
            this,
            "Enter your lock screen lock to access",
            "Please unlock to proceed",
            Authenticators.DEVICE_CREDENTIAL or Authenticators.BIOMETRIC_WEAK
        )

        findViewById<MaterialButton>(R.id.unlock_btn).setOnClickListener {
            biometricsProvider.prompt({ onBiometricSuccess() })
        }

        biometricsProvider.prompt({ onBiometricSuccess() })

    }

    private fun onBiometricSuccess() {
        if(!Settings.firebaseInitialized) {
            Dialog("Looks like this is the first time the app is run. In order to use the app you have to fill in the firebase config options and restart the app.", "Ok", null, {
                startActivity(Intent(this, AppActivity::class.java))
            }, {}).show(supportFragmentManager, "DIALOG_FIREBASE")
        } else {
            startActivity(Intent(this, AppActivity::class.java))
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }
}