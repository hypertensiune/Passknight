package com.example.passknight.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.passknight.R
import com.example.passknight.ViewPagerAdapter
import com.example.passknight.databinding.FragmentVaultViewBinding
import com.example.passknight.viewmodels.VaultViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class VaultView : Fragment() {

    private lateinit var binding: FragmentVaultViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vault_view, container, false)

        // https://stackoverflow.com/questions/53184320/how-to-pass-custom-parameters-to-a-viewmodel-using-factory
        // Use custom factory to create the VaultViewModel with the required parameters
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VaultViewModel(container!!.findNavController()) as T
            }
        }

        val viewModel = ViewModelProvider(requireActivity(), factory)[VaultViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewPager.adapter = ViewPagerAdapter(requireActivity(), viewModel)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Passwords"
                1 -> tab.text = "Notes"
                2 -> tab.text = "Generator"
                else -> tab.text = "Passwords"
            }
        }.attach()

        return binding.root
    }
}