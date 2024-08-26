package com.example.passknight.services

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Dialog(
    private val message: String,
    private val positiveButtonText: String,
    private val negativeButtonText: String,
    private val positiveAction: () -> Unit,
    private val negativeAction: () -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction.
            val builder = MaterialAlertDialogBuilder(it)
            builder.setMessage(message)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    positiveAction()
                }
                .setNegativeButton(negativeButtonText) { _, _ ->
                    negativeAction()
                }
            // Create the AlertDialog object and return it.
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}