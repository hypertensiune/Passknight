package com.example.passknight

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment
import com.example.passknight.services.Dialog
import com.example.passknight.services.Firestore
import com.example.passknight.services.Settings

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.navigation)

        if(Settings.firebaseInitialized) {
            graph.setStartDestination(R.id.vault_list)
        } else {
            graph.setStartDestination(R.id.settings)
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

        navHostFragment.navController.setGraph(graph, intent.extras)
    }

    override fun onPause() {
        super.onPause()
        Firestore.signOut()
    }
}