package com.example.passknight

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.passknight.databinding.VaultListItemBinding

class VaultListAdapter(
    context: Context,
    private val vaults: List<String>?,
    private val onItemClick: (Any) -> Unit
): PkRecyclerViewAdapter<String, VaultListItemBinding>(context, vaults, onItemClick) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<String, VaultListItemBinding> {
        binding = VaultListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding, onItemClick) { element, binding ->
            binding.vault = element
            binding.root.setOnClickListener {
                onItemClick(element)
            }
        }
    }

}