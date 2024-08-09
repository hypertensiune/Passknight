package com.example.passknight

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception
import java.net.URL
import java.util.regex.Pattern
import kotlin.concurrent.thread

@BindingAdapter("app:errorText")
fun setInputErrorText(view: TextInputLayout, text: String) {
    if(text.isEmpty()) {
        view.error = null
    } else {
        view.error = text
    }
}

@BindingAdapter("app:websiteIcon")
fun setWebsiteIcon(view: ImageView, website: String?) {
    /**
     * Used to load an image from a website directly into the image view
     * https://stackoverflow.com/questions/68960646/load-image-from-url-without-third-party
     */
    if(view.drawable == null) {
        val url = "https://icon.horse/icon/$website"
        val uiHandler = Handler(Looper.getMainLooper())

        // Get the result from the URL on another thread to not block the main thread
        thread(start = true) {
            var bitmap: Bitmap? = null
            try {
                val conn = URL(url).openConnection()
                conn.connect()

                val inputStream = conn.getInputStream()
                bitmap = BitmapFactory.decodeStream(inputStream)

                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if(bitmap != null) {
                // UI updates have to be made from the UI thread
                uiHandler.post {
                    view.setImageBitmap(bitmap)
                }
            }
        }
    }
}

// https://stackoverflow.com/questions/62026687/how-to-create-binding-adapter-for-material-slider-view/62118131#62118131
// https://stackoverflow.com/a/62118131
@InverseBindingAdapter(attribute = "android:value")
fun getSliderValue(slider: Slider) = slider.value

@BindingAdapter("android:valueAttrChanged")
fun setSliderListeners(slider: Slider, attrChange: InverseBindingListener) {
    slider.addOnChangeListener { _, _, _ ->
        attrChange.onChange()
    }
}

/**
 * Breaks the password into small pieces composed of only lowercase, uppercase, number or symbols
 * and colors each one differently. (NOT WORKING)
 *
 * https://stackoverflow.com/questions/6094315/single-textview-with-multiple-colored-text
 */
@BindingAdapter("app:generatedPassword")
fun setGeneratedPassword(textView: TextView, password: String) {

    textView.text = password

//    Even if a new thread is created this is making the UI too slow.
//    Until I find a better way of doing this don't split the password.
//
//    val match = Pattern.compile("(\\d+)|([a-zA-Z]+)|([!@#\$%^&*]+)").matcher(password)
//
//    val spans = mutableListOf<SpannableString>()
//
//    val uiHandler = Handler(Looper.getMainLooper())
//    thread(start = true) {
//        while(match.find()) {
//            with(match.group()) {
//                val span = SpannableString(this)
//
//                if(this.isDigitsOnly()) {
//                    // #6F9DF1
//                    span.setSpan(ForegroundColorSpan(Color.rgb(111, 157, 241)), 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                } else if(this.all { it.isLetter() }) {
//                    // #C9C9C9
//                    span.setSpan(ForegroundColorSpan(Color.rgb(201, 201, 201)), 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                } else {
//                    // #FF8D85
//                    span.setSpan(ForegroundColorSpan(Color.rgb(255, 141, 133)), 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                }
//
//                spans.add(span)
//            }
//        }
//
//        uiHandler.post {
//            spans.forEach { textView.append(it) }
//        }
//    }
}
