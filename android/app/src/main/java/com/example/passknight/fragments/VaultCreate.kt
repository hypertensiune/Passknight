package com.example.passknight.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.passknight.R
import com.example.passknight.databinding.FragmentVaultCreateBinding
import com.example.passknight.viewmodels.VaultCreateViewModel

class VaultCreate : Fragment() {

    private lateinit var binding: FragmentVaultCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vault_create, container, false)

        val viewModel = VaultCreateViewModel(container!!.findNavController())

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            viewModel.navController.popBackStack()
        }

        return binding.root
    }
}