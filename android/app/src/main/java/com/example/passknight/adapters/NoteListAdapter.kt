package com.example.passknight.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.passknight.databinding.NoteListItemBinding
import com.example.passknight.databinding.PasswordListItemBinding
import com.example.passknight.models.NoteItem

class NoteListAdapter(
    context: Context,
    passwords: List<NoteItem>?,
    private val onItemClick: (Any) -> Unit
) : PkRecyclerViewAdapter<NoteItem, NoteListItemBinding>(context, passwords, onItemClick) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<NoteItem, NoteListItemBinding> {
        binding = NoteListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding, onItemClick) { item, binding ->
            binding.name = item.name
        }
    }
}