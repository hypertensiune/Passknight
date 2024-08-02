package com.example.passknight.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.passknight.R
import com.example.passknight.databinding.FragmentPasswordItemFormBinding
import com.example.passknight.viewmodels.VaultViewModel

class PasswordForm : Fragment() {

    private lateinit var binding: FragmentPasswordItemFormBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_password_item_form, container, false)

        val viewModel = ViewModelProvider(requireActivity())[VaultViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}