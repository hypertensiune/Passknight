package com.example.passknight

import android.content.Intent
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

        biometricsProvider = BiometricsProvider(
            applicationContext,
            this,
            "Enter your lock screen lock to access",
            "Please unlock to proceed",
            Authenticators.DEVICE_CREDENTIAL or Authenticators.BIOMETRIC_WEAK
        )

        findViewById<MaterialButton>(R.id.unlock_btn).setOnClickListener {
            // For autofill service testing purposes
            // https://developer.android.com/identity/autofill/autofill-services#auth
//            if(intent.getBooleanExtra("AutofillService", false)) {
//
//                val usernameAutofillId = intent.getParcelableExtra<AutofillId>("usernameAutofillId")!!
//                val passwordAutofillId = intent.getParcelableExtra<AutofillId>("passwordAutofillId")!!
//
//                val notUsed = RemoteViews(packageName, R.layout.autofill_list_item)
//
//                val replyIntent = Intent().apply {
//                    val responseDataset = Dataset.Builder()
//                        .setValue(usernameAutofillId, AutofillValue.forText("Username"), notUsed)
//                        .setValue(passwordAutofillId, AutofillValue.forText("1234"), notUsed)
//                        .build()
//
//                    putExtra(EXTRA_AUTHENTICATION_RESULT, responseDataset)
//                }
//
//                setResult(RESULT_OK, replyIntent)
//                finish()
//            } else {
            biometricsProvider.prompt { onBiometricSuccess() }
        }

        biometricsProvider.prompt { onBiometricSuccess() }

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
}