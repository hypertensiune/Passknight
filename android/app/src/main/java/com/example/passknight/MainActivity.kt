package com.example.passknight

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.Dataset
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.biometric.BiometricManager.Authenticators
import com.example.passknight.services.BiometricsProvider
import com.example.passknight.services.Firestore
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var biometricsProvider: BiometricsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MaterialButton>(R.id.unlock_btn).setOnClickListener {
            // For autofill service testing purposes
            // https://developer.android.com/identity/autofill/autofill-services#auth
            if(intent.getBooleanExtra("AutofillService", false)) {

                val usernameAutofillId = intent.getParcelableExtra<AutofillId>("usernameAutofillId")!!
                val passwordAutofillId = intent.getParcelableExtra<AutofillId>("passwordAutofillId")!!

                val notUsed = RemoteViews(packageName, R.layout.autofill_list_item)

                val replyIntent = Intent().apply {
                    val responseDataset = Dataset.Builder()
                        .setValue(usernameAutofillId, AutofillValue.forText("Username"), notUsed)
                        .setValue(passwordAutofillId, AutofillValue.forText("1234"), notUsed)
                        .build()

                    putExtra(EXTRA_AUTHENTICATION_RESULT, responseDataset)
                }

                setResult(RESULT_OK, replyIntent)
                finish()
            } else {
                biometricsProvider.prompt { onBiometricSuccess() }
                startActivity(Intent(this, AppActivity::class.java))
            }
        }

        biometricsProvider = BiometricsProvider(
            applicationContext,
            this,
            "Enter your lock screen lock to access",
            "Please unlock to proceed",
            Authenticators.DEVICE_CREDENTIAL or Authenticators.BIOMETRIC_WEAK
        )

        biometricsProvider.prompt { onBiometricSuccess() }

    }

    private fun onBiometricSuccess() {
        startActivity(Intent(this, AppActivity::class.java))
    }
}