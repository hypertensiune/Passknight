package com.example.passknight.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passknight.R
import com.example.passknight.adapters.HistoryListAdapter
import com.example.passknight.adapters.PasswordListAdapter
import com.example.passknight.databinding.FragmentGeneratorHistoryBinding
import com.example.passknight.databinding.FragmentTabGeneratorBinding
import com.example.passknight.services.Dialog
import com.example.passknight.viewmodels.VaultViewModel
import kotlinx.coroutines.launch

class GeneratorHistory : Fragment() {

    private lateinit var binding: FragmentGeneratorHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_generator_history, container, false)

        val viewModel = ViewModelProvider(requireActivity())[VaultViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = HistoryListAdapter(requireContext(), viewModel.vault.value?.generatorHistory?.value) {
            viewModel.copyPassword(it)
        }
        binding.historyRecyclerView.adapter = adapter

        binding.toolbar.setNavigationOnClickListener {
            viewModel.navController.popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener {
            if(it.itemId == R.id.menu_clear) {
                Dialog("Are you sure you want to clear the password history?", "Yes", "No", {
                    lifecycleScope.launch {
                        viewModel.clearHistory()
                        viewModel.navController.popBackStack()
                    }
                }, {}).show(childFragmentManager, "DELETE_DIALOG")
            }
            true
        }

        return binding.root
    }
}