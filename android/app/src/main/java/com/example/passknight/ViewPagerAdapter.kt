package com.example.passknight

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.passknight.fragments.TabGenerator
import com.example.passknight.fragments.TabNotes
import com.example.passknight.fragments.TabPasswords
import com.example.passknight.viewmodels.VaultViewModel

class ViewPagerAdapter(
    activity: FragmentActivity,
    private val viewModel: VaultViewModel
) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> TabPasswords()
            1 -> TabNotes()
            2 -> TabGenerator()
            else -> TabPasswords()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

}