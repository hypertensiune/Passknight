package com.example.passknight.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.passknight.R
import com.example.passknight.databinding.FragmentPasswordItemFormBinding
import com.example.passknight.services.Dialog
import com.example.passknight.viewmodels.VaultViewModel

class PasswordItemForm : Fragment() {

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


        binding.itemMenuButton.setOnClickListener { showPopupMenu(it, viewModel) }

        return binding.root
    }

    private fun showPopupMenu(view: View, viewModel: VaultViewModel) {
        val popupMenu = PopupMenu(requireContext(), view, 0, androidx.appcompat.R.attr.popupMenuStyle, R.style.PopupMenu)
        popupMenu.menuInflater.inflate(R.menu.item_form_menu, popupMenu.menu)

        // setForceShowIcon requires API level 29 (Q)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }

        popupMenu.setOnMenuItemClickListener {
            if(it.itemId == R.id.menu_delete) {
                Dialog("Are you sure you want to delete this item? Action is not reversible.", "Yes", "No", {
                    viewModel.deleteItem(VaultViewModel.ITEM_PASSWORD)
                }, {}).show(childFragmentManager, "DELETE_DIALOG")
            }

            true
        }

        popupMenu.show()
    }
}