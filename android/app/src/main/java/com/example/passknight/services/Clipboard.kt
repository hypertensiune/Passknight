package com.example.passknight.services

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.os.persistableBundleOf
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.passknight.viewmodels.VaultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class Clipboard(private val context: Context) {

    /**
     * Worker for clearing the clipboard.
     *
     * Might not work properly. Seems like access to clipboard reading and writing is not
     * permitted from a background worker.
     */
    class ClearWorker(private val context: Context, workerParams: WorkerParameters) : androidx.work.Worker(context, workerParams) {
        override fun doWork(): Result {
            val overwriteCount = (Settings.get("clipboardIterations") as String).toInt()

            val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            runBlocking {
                launch {
                    for(i in 1..overwriteCount) {
                        delay(10)
                        manager.setPrimaryClip(ClipData.newPlainText(i.toString(), "\u200E".repeat(i)))
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        manager.clearPrimaryClip()
                    } else {
                        manager.setPrimaryClip(ClipData.newPlainText("", ""))
                    }
                }
            }

            return Result.success()
        }
    }

    private val manager: ClipboardManager? = ContextCompat.getSystemService(context, ClipboardManager::class.java)

    /**
     * Copies a text to the clipboard which is deleted after specified ms.
     * Android doesn't provide a way to completely clear the clipboard. It can clear only the primary
     * clip data (that is prompted to paste).
     *
     * As a workaround, besides clearing the primary clip data, fill the clip board history
     * with empty entries. This will overwrite everything in the clipboard history (based on the keyboard you used
     * the pinned items may also be lost).
     *
     * Note: This workaround may not work on all keyboards. Tested on Gboard (it works) and Swiftkey (doesn't work)
     */
     fun copy(label: String, text: String, isSensitive: Boolean, callback: () -> Unit = {}) {
        manager?.let {
            it.setPrimaryClip(ClipData.newPlainText(label, text).apply {
                description.extras = persistableBundleOf("android.content.extra.IS_SENSITIVE" to isSensitive)
            })

            callback()

            val clipboardTimeout = (Settings.get("clipboardTimeout") as String).toLong()

            if(clipboardTimeout.toInt() == -1) {
                return@let
            }

            val workRequest = OneTimeWorkRequestBuilder<ClearWorker>().setInitialDelay(clipboardTimeout, TimeUnit.MILLISECONDS).build()
            WorkManager.getInstance(context).enqueueUniqueWork("ClearClipboard", ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}