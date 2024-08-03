package com.example.passknight.adapters

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * @param data Data for the recycler view to display
 * @param onItemClick Function to execute when the view holder itme is clicked
 */
abstract class PkRecyclerViewAdapter<T, V : ViewDataBinding>(
    context: Context,
    private val data: List<T>?,
    private val onItemClick: (T) -> Unit
) : RecyclerView.Adapter<PkRecyclerViewAdapter.ViewHolder<T, V>>() {

    protected lateinit var binding: V

    class ViewHolder<T, V : ViewDataBinding>(
        private val binding: V,
        private val _bind: (T, V) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(element: T) = _bind(element, binding)
    }

    override fun getItemCount(): Int = data?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder<T, V>, position: Int) {
        data?.elementAt(position)?.let { holder.bind(it) }
    }

}