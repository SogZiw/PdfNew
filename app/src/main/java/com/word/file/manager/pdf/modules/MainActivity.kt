package com.word.file.manager.pdf.modules

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.databinding.ActivityMainBinding
import com.word.file.manager.pdf.modules.fragments.BookmarkFragment
import com.word.file.manager.pdf.modules.fragments.HomeFragment
import com.word.file.manager.pdf.modules.fragments.RecentFragment
import com.word.file.manager.pdf.modules.permissions.StoragePermissionActivity
import com.word.file.manager.pdf.modules.permissions.hasStorageAccessPermission
import kotlinx.coroutines.launch

class MainActivity : StoragePermissionActivity<ActivityMainBinding>() {

    override fun setViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        initViewPager()
        lifecycleScope.launch {
            app.documentRepository.requestStorageAccess.collect {
                checkStoragePermission(com.word.file.manager.pdf.base.data.DocumentOpenType)
            }
        }
        if (hasStorageAccessPermission()) {
            app.documentRepository.refreshFiles(activity)
        } else {
            app.documentRepository.updatePermissionGuide(true)
        }
        binding.btnAdd.setOnClickListener { }
    }

    override fun onStorageAccessGranted(type: DocumentActionType?) {
        app.documentRepository.refreshFiles(activity)
    }

    override fun onStorageAccessDenied() {
        app.documentRepository.updatePermissionGuide(true)
    }

    private fun initViewPager() {
        val fragments = listOf(
            HomeFragment(),
            RecentFragment(),
            BookmarkFragment(),
            Fragment(),
        )
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = fragments.size
        binding.viewPager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        binding.tabHome.setOnClickListener { changeTab(0) }
        binding.tabRecently.setOnClickListener { changeTab(1) }
        binding.tabHistory.setOnClickListener { changeTab(2) }
        binding.tabSet.setOnClickListener { changeTab(3) }
        changeTab(0)
    }

    private fun changeTab(index: Int) {
        binding.tabHome.isSelected = index == 0
        binding.tabRecently.isSelected = index == 1
        binding.tabHistory.isSelected = index == 2
        binding.tabSet.isSelected = index == 3
        binding.viewPager.setCurrentItem(index, false)
    }

    override fun onClickBack() {
        moveTaskToBack(true)
    }

}