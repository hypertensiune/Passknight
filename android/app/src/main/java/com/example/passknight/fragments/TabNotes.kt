package com.example.passknight.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passknight.R
import com.example.passknight.adapters.NoteListAdapter
import com.example.passknight.databinding.FragmentTabNotesBinding
import com.example.passknight.viewmodels.VaultViewModel

class TabNotes : Fragment() {

    private lateinit var binding: FragmentTabNotesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_notes, container, false)

        val viewModel = ViewModelProvider(requireActivity())[VaultViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.noteListRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.vault.observe(viewLifecycleOwner) {vault ->
            vault.notes.observe(viewLifecycleOwner) {notes ->
                val adapter = NoteListAdapter(requireContext(), notes) {
                    viewModel.openNoteItemForm(it)
                }
                binding.noteListRecyclerView.adapter = adapter
            }
        }

        return binding.root
    }
}