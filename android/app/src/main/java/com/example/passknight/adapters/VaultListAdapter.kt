package com.example.passknight.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
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

        return ViewHolder(binding) { element, binding ->
            binding.vault = element

            binding.root.setOnClickListener {
                onItemClick(element)
            }
        }
    }

}