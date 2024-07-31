package com.example.passknight.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.passknight.R

import com.example.passknight.databinding.FragmentVaultUnlockBinding
import com.example.passknight.viewmodels.VaultUnlockViewModel

class VaultUnlock : Fragment() {

    private lateinit var binding: FragmentVaultUnlockBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vault_unlock, container, false)

        binding.viewModel =  VaultUnlockViewModel(arguments?.getString("vault")!!, container!!.findNavController())
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}