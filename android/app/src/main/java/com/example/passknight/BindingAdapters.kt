package com.example.passknight

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception
import java.net.URL
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
