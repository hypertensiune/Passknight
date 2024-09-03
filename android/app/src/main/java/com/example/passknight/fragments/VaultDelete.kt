package com.example.passknight.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.passknight.R
import com.example.passknight.databinding.FragmentVaultDeleteBinding
import com.example.passknight.services.BiometricsProvider
import com.example.passknight.services.Cryptography
import com.example.passknight.viewmodels.VaultDeleteViewModel

class VaultDelete : Fragment() {

    private lateinit var binding: FragmentVaultDeleteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vault_delete, container, false)

        val biometricsProvider = BiometricsProvider(
            requireContext(),
            requireActivity(),
            "Enter your lock screen lock to access",
            "Please unlock to proceed",
            BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        val viewModel = VaultDeleteViewModel(arguments?.getString("vault")!!, container!!.findNavController(), biometricsProvider)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        // Clear the viewModelStore so a new view model can be created
        // For more info see [VaultView::onCreateView]
        // Also remove the stretched master key from the encrypted shared preferences
        viewModel.clearFlag.observe(viewLifecycleOwner) {
            requireActivity().viewModelStore.clear()
            with(Cryptography.Utils.getEncryptedSharedPreferences(requireContext()).edit()) {
                remove("smk")
                commit()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            viewModel.navController.popBackStack()
        }

        return binding.root
    }
}