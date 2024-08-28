package com.example.passknight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        navHostFragment.navController.setGraph(graph, intent.extras)
    }

    override fun onPause() {
        super.onPause()
        Firestore.signOut()
    }
}