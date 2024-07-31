package com.example.passknight

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("app:errorText")
fun setInputErrorText(view: TextInputLayout, text: String) {
    if(text.isEmpty()) {
        view.error = null
    } else {
        view.error = text
    }
}