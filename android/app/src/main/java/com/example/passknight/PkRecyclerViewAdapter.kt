package com.example.passknight

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.ViewDataBindingKtx
import androidx.recyclerview.widget.RecyclerView

abstract class PkRecyclerViewAdapter<T, V : ViewDataBinding>(
    context: Context,
    private val data: List<T>?,
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<PkRecyclerViewAdapter.ViewHolder<T, V>>() {

    protected lateinit var binding: V

    class ViewHolder<T, V : ViewDataBinding>(
        private val binding: V,
        private val onItemClick: (Any) -> Unit,
        private val _bind: (T, V) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(element: T) = _bind(element, binding)
    }

    override fun getItemCount(): Int = data?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder<T, V>, position: Int) {
        data?.elementAt(position)?.let { holder.bind(it) }
    }

}