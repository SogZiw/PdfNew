package com.word.file.manager.pdf.modules.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseFragment
import com.word.file.manager.pdf.base.data.FileTabFilter
import com.word.file.manager.pdf.databinding.FragmentHomeBinding
import com.word.file.manager.pdf.modules.permissions.hasStorageAccessPermission

class BookmarkFragment : BaseFragment<FragmentHomeBinding>() {

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.appName.text = getString(R.string.bookmark)
        binding.btnGoSet.isVisible = false
        binding.viewPager.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            private val pages = listOf(
                DocumentFragment.newInstance(FileTabFilter.All, DocumentSource.Bookmark),
                DocumentFragment.newInstance(FileTabFilter.Pdf, DocumentSource.Bookmark),
                DocumentFragment.newInstance(FileTabFilter.Word, DocumentSource.Bookmark),
                DocumentFragment.newInstance(FileTabFilter.Ppt, DocumentSource.Bookmark),
                DocumentFragment.newInstance(FileTabFilter.Excel, DocumentSource.Bookmark),
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
            app.mainViewModel.requestStorageLiveData.postValue(true)
        }
        app.mainViewModel.changePermissionVisible.observe(viewLifecycleOwner) {
            binding.viewPermission.root.isVisible = it
        }
        binding.viewPermission.root.isVisible = !hasStorageAccessPermission()
        app.mainViewModel.collectBookmarkFiles()
        changeSelector(0)
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
