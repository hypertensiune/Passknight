package com.example.passknight.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.passknight.R
import com.example.passknight.databinding.FragmentNoteItemFormBinding
import com.example.passknight.services.Dialog
import com.example.passknight.viewmodels.VaultViewModel

class NoteItemForm : Fragment() {

    private lateinit var binding: FragmentNoteItemFormBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_note_item_form, container, false)

        val viewModel = ViewModelProvider(requireActivity())[VaultViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            viewModel.navController.popBackStack()
        }

        if(viewModel.itemEditing) {
            binding.toolbar.inflateMenu(R.menu.item_form_menu)
            binding.toolbar.setOnMenuItemClickListener {
                if(it.itemId == R.id.menu_delete) {
                    Dialog("Are you sure you want to delete this item? Action is not reversible.", "Yes", "No", {
                        viewModel.deleteItem(VaultViewModel.ITEM_NOTE)
                    }, {}).show(childFragmentManager, "DELETE_DIALOG")
                }

                true
            }
        }

        return binding.root
    }
}