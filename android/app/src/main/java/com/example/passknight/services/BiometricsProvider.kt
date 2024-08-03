package com.example.passknight.services

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import android.content.Context
import androidx.fragment.app.FragmentActivity

class BiometricsProvider(
    private val context: Context,
    private val activity: FragmentActivity,
    title: String,
    subtitle: String,
    authenticators: Int,
) {

    private var executor: Executor = ContextCompat.getMainExecutor(context)
    private lateinit var biometricPrompt: BiometricPrompt
    private var promptInfo: BiometricPrompt.PromptInfo

    init {
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()
    }

    class AuthenticationListener(private val context: Context, private val onSuccessCallback: () -> Unit) : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Toast.makeText(context, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
            onSuccessCallback()
        }
    }

    fun prompt(onSuccessCallback: () -> Unit) {
        biometricPrompt = BiometricPrompt(activity, executor, AuthenticationListener(context, onSuccessCallback))
        biometricPrompt.authenticate(promptInfo)
    }

}