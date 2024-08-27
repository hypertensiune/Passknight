package com.example.passknight.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.passknight.databinding.HistoryItemBinding

class HistoryListAdapter(
    context: Context,
    private val history: List<String>?,
    private val onItemClick: (String) -> Unit
): PkRecyclerViewAdapter<String, HistoryItemBinding>(context, history, {}) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<String, HistoryItemBinding> {
        binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding) { item, binding ->
            binding.password = item
            binding.copyHistory.setOnClickListener {
                onItemClick(item)
            }
        }
    }

}