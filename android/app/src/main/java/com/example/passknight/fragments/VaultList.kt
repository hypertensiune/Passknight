package com.example.passknight.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passknight.R
import com.example.passknight.VaultListAdapter
import com.example.passknight.databinding.FragmentVaultListBinding
import com.example.passknight.viewmodels.VaultListViewModel
import com.google.api.Distribution.BucketOptions.Linear
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class VaultList : Fragment() {

    private lateinit var binding: FragmentVaultListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vault_list, container, false)

        // https://stackoverflow.com/a/59275182
        // Navigation.findNavController(binding.root) wouldn't work as the view isn't created at this point
        val viewModel = VaultListViewModel(container!!.findNavController())

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vaultListRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.vaults.observe(viewLifecycleOwner) {
            val adapter = VaultListAdapter(requireContext(), viewModel.vaults.value?.toList()) {
                viewModel.onVaultItemClick(it as String)
            }
            binding.vaultListRecyclerView.adapter = adapter
        }

        return binding.root
    }
}