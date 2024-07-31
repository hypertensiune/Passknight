package com.example.passknight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.passknight.services.Firestore

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
    }

    override fun onPause() {
        super.onPause()
        Firestore.signOut()
    }
}