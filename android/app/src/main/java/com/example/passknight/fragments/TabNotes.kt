package com.example.passknight.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.passknight.R
import com.example.passknight.databinding.FragmentTabNotesBinding
import com.example.passknight.viewmodels.VaultViewModel

class TabNotes(private val viewModel: VaultViewModel) : Fragment() {

    private lateinit var binding: FragmentTabNotesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_notes, container, false)

        binding.viewModel = viewModel

        return binding.root
    }
}