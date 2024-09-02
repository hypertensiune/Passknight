package com.example.passknight.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.passknight.R
import com.example.passknight.ViewPagerAdapter
import com.example.passknight.databinding.FragmentVaultViewBinding
import com.example.passknight.services.Clipboard
import com.example.passknight.services.Cryptography
import com.example.passknight.viewmodels.VaultViewModel
import com.google.android.material.tabs.TabLayoutMediator

class VaultView : Fragment() {

    private lateinit var binding: FragmentVaultViewBinding

    private val args: VaultViewArgs by navArgs<VaultViewArgs>()

    private var searchbarOpenend = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Clear the view model store to allow creation of a new VaultViewModel.
        // If it is not cleared the ViewModelProvider will get the previous instance of the vm and
        // so the previous vault that was unlocked will be displayed.
        // This issue appears after locking a vault and unlocking another
        //
        // By clearing it here a new problem is created. After navigating to an item form and
        // coming back (by manually navigating back or by creating a new item) this fragment along
        // with the view model will be created again (along with it the vault has to be refetched
        // from firebase.
        //
        // Moved it down to popupMenu itemClickListener to handle the vault lock case
        // and the VaultDelete fragment and view model will now handle the clearing when deleting
        // a vault
        //requireActivity().viewModelStore.clear()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vault_view, container, false)

        val cryptography = if(args.password.isNotEmpty() && args.email.isNotEmpty())
            Cryptography(requireContext(), args.email, args.password, args.psk)
        else
            Cryptography(requireContext(), args.smk, args.psk)

        // https://stackoverflow.com/questions/53184320/how-to-pass-custom-parameters-to-a-viewmodel-using-factory
        // Use custom factory to create the VaultViewModel with the required parameters
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VaultViewModel(
                    container!!.findNavController(),
                    Clipboard(requireContext()),
                    cryptography
                ) as T
            }
        }

        val viewModel = ViewModelProvider(requireActivity(), factory)[VaultViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewPager.adapter = ViewPagerAdapter(requireActivity(), viewModel)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Passwords"
                1 -> tab.text = "Notes"
                2 -> tab.text = "Generator"
                else -> tab.text = "Passwords"
            }
        }.attach()

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        binding.menuButton.setOnClickListener { showPopupMenu(it, viewModel) }

        binding.searchButton.setOnClickListener { view ->
            // https://stackoverflow.com/questions/42563815/animate-a-view-from-0dp-width-to-match-parent
            if(!searchbarOpenend) {
                val animator = ValueAnimator.ofInt(0, (binding.searchBar.parent as View).measuredWidth + (24 * requireContext().resources.displayMetrics.density).toInt())
                animator.duration = 200
                animator.interpolator = DecelerateInterpolator()
                animator.addUpdateListener {
                    binding.searchBar.layoutParams.width = animator.animatedValue as Int
                    binding.searchBar.requestLayout()
                }

                animator.start()
                view.visibility = View.GONE

                searchbarOpenend = true
            }
        }

        binding.searchBar.setEndIconOnClickListener {
            if(searchbarOpenend) {
                binding.searchBar.layoutParams.width = 0
                binding.searchBar.editText?.text?.clear()
                binding.searchButton.visibility = View.VISIBLE

                searchbarOpenend = false
            }
        }

        return binding.root
    }

    private fun showPopupMenu(view: View, viewModel: VaultViewModel) {
        val popupMenu = PopupMenu(requireContext(), view, 0, androidx.appcompat.R.attr.popupMenuStyle, R.style.PopupMenu)
        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)

        // setForceShowIcon requires API level 29 (Q)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }

        popupMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.menu_lock -> {
                    viewModel.lockVault()

                    with(Cryptography.Utils.getEncryptedSharedPreferences(requireContext()).edit()) {
                        remove("smk")
                        commit()
                    }

                    requireActivity().viewModelStore.clear()
                }
                R.id.menu_delete -> viewModel.deleteVault()
                R.id.menu_settings -> viewModel.navController.navigate(R.id.vault_view_to_settings)

            }

            true
        }

        popupMenu.show()
    }
}