package com.example.passknight.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passknight.R
import com.example.passknight.adapters.PasswordListAdapter
import com.example.passknight.databinding.FragmentTabPasswordsBinding
import com.example.passknight.models.PasswordItem
import com.example.passknight.models.Vault
import com.example.passknight.services.Settings
import com.example.passknight.viewmodels.VaultViewModel

class TabPasswords : Fragment() {

    private lateinit var binding: FragmentTabPasswordsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_passwords, container, false)

        val viewModel = ViewModelProvider(requireActivity())[VaultViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.passwordListRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.vault.observe(viewLifecycleOwner) {vault ->
            vault.passwords.observe(viewLifecycleOwner) {passwords ->

                val adapter = PasswordListAdapter(requireContext(), passwords,
                    onItemClick = {
                        if(Settings.fromAutofillService) {
                            setFragmentResult("key", bundleOf(
                                "password" to viewModel.cryptography.decrypt(it.password),
                                "username" to it.username
                            ))
                        } else {
                            viewModel.openPasswordItemForm(it)
                        }
                    },
                    onPasswordCopyClick = {
                        viewModel.copyUsername(it)
                    },
                    onUsernameCopyClick = {
                        viewModel.copyPassword(it)
                    }
                )

                binding.passwordListRecyclerView.adapter = adapter
            }
        }

        viewModel.clipboardMessage.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}