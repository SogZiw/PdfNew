package com.word.file.manager.pdf.modules

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun setViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        initViewPager()
    }

    private fun initViewPager() {
        val fragments = listOf(Fragment(), Fragment(), Fragment(), Fragment())
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


}