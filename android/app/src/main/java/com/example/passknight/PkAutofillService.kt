package com.example.passknight

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.app.slice.Slice
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.CancellationSignal
import android.provider.ContactsContract.Data
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.util.Log
import android.util.Size
import android.view.View
import android.view.View.AUTOFILL_TYPE_TEXT
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import android.widget.RemoteViews.RemoteView
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi
import kotlin.random.Random

class PkAutofillService : AutofillService() {

    val list = ArrayList<AutofillId?>()

    private var passwordAutofillId: AutofillId? = null
    private var usernameAutofillId: AutofillId? = null
    private var genericAutofillId: AutofillId? = null

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) { }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        // Get the structure from the request
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure

        //Log.d("Passknight", "Fill request ${structure.activityComponent.packageName}")

        // Traverse the structure to find the fields that need to be autofilled
        traverseStructure(structure)

        val presentation = RemoteViews(packageName, R.layout.autofill_list_item)

        // Start a new activity to unlock a vault and choose a password item to fill
        val intent = Intent(this, AppActivity::class.java).apply {
            putExtra("AutofillService", true)
            putExtra("usernameAutofillId", usernameAutofillId)
            putExtra("passwordAutofillId", passwordAutofillId)
        }
        val intentSender = PendingIntent.getActivity(this, 1001, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE).intentSender

        val fillResponse: FillResponse.Builder = FillResponse.Builder()

        // If the genericAutofillId is null that means that no fields were found that can be autofilled
        if(usernameAutofillId == null && passwordAutofillId == null && genericAutofillId == null) {
            callback.onFailure("No fields")
            return
        }

        val presentationSpec = InlinePresentationSpec.Builder(Size(500, 110), Size(700, 110)).build()

        val pendingIntent = PendingIntent.getActivity(this, Random.nextInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val slice = InlineSuggestionUi.newContentBuilder(pendingIntent).setTitle("Passknight").setSubtitle("Unlock vault").build().slice

        val inlinePresentation = InlinePresentation(slice, presentationSpec, false)

        Log.i("Passknight", "Autofill ${usernameAutofillId.toString()} ${passwordAutofillId.toString()}")
        val dataset = Dataset.Builder().apply {
            usernameAutofillId?.let { setValue(usernameAutofillId!!, AutofillValue.forText(null), presentation, inlinePresentation) }
            passwordAutofillId?.let { setValue(passwordAutofillId!!, AutofillValue.forText(null), presentation, inlinePresentation) }
            setAuthentication(intentSender)
        }.build()

        fillResponse.addDataset(dataset)

        // If there are no errors, call onSuccess() and pass the response
        callback.onSuccess(fillResponse.build())
    }

    /**
     *  Traverse a structure to retrieve the autofill data required by the autofill service
     *  (find the username and password fields to autofill).
     *
     *  The autofill service requires 2 fields, one for the username and one for the password to be
     *  able to autofill both credentials. (It can also work with only one field and fill that accordingly)
     *
     *  For more details see [parseNode].
     *
     *  https://developer.android.com/identity/autofill/autofill-services#kotlin
     */
    private fun traverseStructure(structure: AssistStructure) {
        val windowNodes: List<AssistStructure.WindowNode> =
            structure.run {
                (0 until windowNodeCount).map { getWindowNodeAt(it) }
            }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            traverseNode(viewNode)
        }
    }

    private fun traverseNode(viewNode: AssistStructure.ViewNode?) {
        parseNode(viewNode)

        val children: List<AssistStructure.ViewNode>? =
            viewNode?.run {
                (0 until childCount).map { getChildAt(it) }
            }

        children?.forEach { childNode: AssistStructure.ViewNode ->
            traverseNode(childNode)
        }
    }

    /**
     * If a node has **autofillHints** use those to determine what type of field it is.
     * If it doesn't have any **autofillHints** check the node's **hint** and **text**
     * properties (look for keywords that might determine its type).
     */
    private fun parseNode(viewNode: AssistStructure.ViewNode?) {
        // Return if the current viewNode is not marked as importantForAutofill (as we don't want to
        // autofill search boxes, for example)
        if(viewNode == null || viewNode.importantForAutofill == View.IMPORTANT_FOR_AUTOFILL_NO || viewNode.importantForAutofill == View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS) {
            return
        }

        if (viewNode.autofillHints?.isNotEmpty() == true) {
            if(viewNode.autofillHints!!.any { it == View.AUTOFILL_HINT_PASSWORD }) {
                // If a focused view node is found use that, otherwise use the first field found
                if(viewNode.isFocused || passwordAutofillId == null) {
                    passwordAutofillId = viewNode.autofillId
                }
            } else { //if(viewNode.autofillHints!!.any { it == View.AUTOFILL_HINT_NAME || it == View.AUTOFILL_HINT_USERNAME || it == View.AUTOFILL_HINT_EMAIL_ADDRESS }) {
                // Same here, look for the focused field..
                if(viewNode.isFocused || usernameAutofillId == null) {
                    usernameAutofillId = viewNode.autofillId
                }
            }
        } else {
            viewNode.hint?.let {
                if(usernameCheckIfContains(it.lowercase())) {
                    usernameAutofillId = viewNode.autofillId
                    //Log.d("Passknight", "Hint: ${viewNode.hint} ${viewNode.importantForAutofill}")
                }
                if(passwordCheckIfContains(it.lowercase())) {
                    passwordAutofillId = viewNode.autofillId
                    //Log.d("Passknight", "Hint: ${viewNode.hint} ${viewNode.importantForAutofill}")
                }
            }

            viewNode.text.let {
                if(usernameAutofillId == null && usernameCheckIfContains(it.toString().lowercase())) {
                    usernameAutofillId = viewNode.autofillId
                    //Log.d("Passknight", "Text: ${viewNode.text} ${viewNode.importantForAutofill}")
                }
                if(passwordAutofillId == null && passwordCheckIfContains(it.toString().lowercase())) {
                    passwordAutofillId = viewNode.autofillId
                    //Log.d("Passknight", "Text: ${viewNode.text} ${viewNode.importantForAutofill}")
                }
            }
        }
    }

    private fun passwordCheckIfContains(string: String): Boolean {
        val possibleValues = listOf("password")
        return possibleValues.any { string.contains(it) }
    }

    private fun usernameCheckIfContains(string: String): Boolean {
        val possibleValues = listOf("username", "name", "email")
        return possibleValues.any { string.contains(it) }
    }
}