package com.example.passknight

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.service.autofill.Dataset
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT
import android.view.autofill.AutofillValue
import android.widget.RelativeLayout
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgs
import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.passknight.fragments.VaultUnlockDirections
import com.example.passknight.services.Clipboard
import com.example.passknight.services.Cryptography
import com.example.passknight.services.Dialog
import com.example.passknight.services.Firestore
import com.example.passknight.services.Settings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Identifier
import java.util.concurrent.TimeUnit

class AppActivity : AppCompatActivity() {

    class LockWorker(val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
        override fun doWork(): Result {
            Firestore.signOut()
            with(Cryptography.Utils.getEncryptedSharedPreferences(context).edit()) {
                remove("smk")
                commit()
            }
            return Result.success()
        }
    }

    private lateinit var loadingScreen: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        loadingScreen = findViewById(R.id.appLoadingScreen)

        lifecycleScope.launch {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val inflater = navHostFragment.navController.navInflater
            val graph = inflater.inflate(R.navigation.navigation)

            val (args, destination) = getStartDestination()

            graph.setStartDestination(destination)
            navHostFragment.navController.setGraph(graph, args)
        }

        Settings.fromAutofillService = intent.getBooleanExtra("AutofillService", false)
        if(Settings.fromAutofillService) {
            supportFragmentManager.setFragmentResultListener("key", this) { requestKey, bundle ->

                // https://developer.android.com/identity/autofill/autofill-services#auth
                val usernameAutofillId = intent.getParcelableExtra<AutofillId>("usernameAutofillId")!!
                val passwordAutofillId = intent.getParcelableExtra<AutofillId>("passwordAutofillId")!!

                val notUsed = RemoteViews(packageName, R.layout.autofill_list_item)

                val replyIntent = Intent().apply {
                    val responseDataset = Dataset.Builder()
                        .setValue(usernameAutofillId, AutofillValue.forText(bundle.getString("username")), notUsed)
                        .setValue(passwordAutofillId, AutofillValue.forText(bundle.getString("password")), notUsed)
                        .build()

                    putExtra(EXTRA_AUTHENTICATION_RESULT, responseDataset)
                }

                setResult(RESULT_OK, replyIntent)
                finish()
            }
        }
    }

    private suspend fun getStartDestination(): Pair<Bundle, Int> {
        val navArgs = Bundle()
        var startDestination = -1

        if(!Settings.firebaseInitialized) {
            startDestination = R.id.settings
        }
        else {
            if(Firebase.auth.currentUser == null) {
                startDestination = R.id.vault_list
            } else {
                // If the user auth was persisted proceed to obtain the stretched master key from
                // the encrypted shared preferences and the protected symmetric key from firestore
                // required to initialize the Cryptography provider used by the vault

                loadingScreen.visibility = View.VISIBLE

                val stretchedMasterKey = Cryptography.Utils.getEncryptedSharedPreferences(baseContext).getString("smk", "") as String

                println(stretchedMasterKey)

                // If there was no value in shared preferences ignore the persistence and
                // require the user to login again
                if(stretchedMasterKey.isEmpty()) {
                    startDestination = R.id.vault_list
                } else {
                    startDestination = R.id.vault_view
                    navArgs.putString("psk", Firestore.getVaultPsk()!!)
                    navArgs.putString("smk", stretchedMasterKey)
                }

                loadingScreen.visibility = View.GONE
            }
        }

        return Pair(navArgs, startDestination)
    }

    override fun onStop() {
        super.onStop()
        val delay = (Settings.get("lockTimeout") as String).toLong()
        if(delay.toInt() != -1) {
            val workRequest = OneTimeWorkRequestBuilder<LockWorker>().setInitialDelay(delay, TimeUnit.MINUTES).build()
            WorkManager.getInstance(baseContext).enqueueUniqueWork("LockVault", ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}