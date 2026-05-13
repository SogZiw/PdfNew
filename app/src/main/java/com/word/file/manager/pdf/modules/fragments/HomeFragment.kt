package com.word.file.manager.pdf.modules.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseFragment
import com.word.file.manager.pdf.base.data.FileTabFilter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.databinding.FragmentHomeBinding
import com.word.file.manager.pdf.modules.SettingsActivity
import com.word.file.manager.pdf.modules.permissions.hasStorageAccessPermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnGoSet.isVisible = true
        binding.btnGoSet.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.viewPager.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            private val pages = listOf(
                DocumentFragment.newInstance(FileTabFilter.All, DocumentSource.Home),
                DocumentFragment.newInstance(FileTabFilter.Pdf, DocumentSource.Home),
                DocumentFragment.newInstance(FileTabFilter.Word, DocumentSource.Home),
                DocumentFragment.newInstance(FileTabFilter.Ppt, DocumentSource.Home),
                DocumentFragment.newInstance(FileTabFilter.Excel, DocumentSource.Home),
            )

            override fun getItemCount(): Int = pages.size

            override fun createFragment(position: Int): Fragment = pages[position]
        }
        binding.viewPager.isUserInputEnabled = false
        binding.layoutSelector.btnAll.setOnClickListener { changeSelector(0) }
        binding.layoutSelector.btnPdf.setOnClickListener { changeSelector(1) }
        binding.layoutSelector.btnWord.setOnClickListener { changeSelector(2) }
        binding.layoutSelector.btnPpt.setOnClickListener { changeSelector(3) }
        binding.layoutSelector.btnExcel.setOnClickListener { changeSelector(4) }
        binding.viewPermission.btnAllow.setOnClickListener {
            app.documentRepository.requestStoragePermission()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.documentRepository.showPermissionGuide.collect {
                    binding.viewPermission.root.isVisible = it
                }
            }
        }
        binding.viewPermission.root.isVisible = !hasStorageAccessPermission()
        changeSelector(0)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(250L)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                AdCenter.mainNative.renderNative(activity, binding.exContainer, NativeAdStyle.NO_ACTION_MEDIA, eventName = "ad_main_nat", allowed = {
                    RemoteLogicConfig.fetchPromotionConfig().dashboardNat && UserBlockHelper.canShowExtra()
                })
            }
        }
    }

    private fun changeSelector(index: Int) {
        binding.layoutSelector.btnAll.isSelected = index == 0
        binding.layoutSelector.btnPdf.isSelected = index == 1
        binding.layoutSelector.btnWord.isSelected = index == 2
        binding.layoutSelector.btnPpt.isSelected = index == 3
        binding.layoutSelector.btnExcel.isSelected = index == 4
        binding.viewPager.setCurrentItem(index, false)
    }

}
