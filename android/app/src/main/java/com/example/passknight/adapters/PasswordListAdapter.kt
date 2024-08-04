package com.example.passknight.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.passknight.databinding.PasswordListItemBinding
import com.example.passknight.databinding.VaultListItemBinding
import com.example.passknight.models.PasswordItem

class PasswordListAdapter(
    context: Context,
    passwords: List<PasswordItem>?,
    private val onItemClick: (PasswordItem) -> Unit,
    private val onUsernameCopyClick: (String) -> Unit,
    private val onPasswordCopyClick: (String) -> Unit
) : PkRecyclerViewAdapter<PasswordItem, PasswordListItemBinding>(context, passwords, onItemClick) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<PasswordItem, PasswordListItemBinding> {
        binding = PasswordListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding) { item, binding ->
            binding.name = item.name
            binding.username = item.username
            binding.website = item.website

            binding.copyPasswordBtn.setOnClickListener {
                onPasswordCopyClick(item.password)
            }

            binding.copyUsernameBtn.setOnClickListener {
                onUsernameCopyClick(item.username)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

}