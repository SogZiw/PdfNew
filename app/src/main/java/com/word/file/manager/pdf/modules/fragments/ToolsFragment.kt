package com.word.file.manager.pdf.modules.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseFragment
import com.word.file.manager.pdf.databinding.FragmentToolsBinding
import com.word.file.manager.pdf.databinding.ViewToolActionCardBinding

class ToolsFragment : BaseFragment<FragmentToolsBinding>() {

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentToolsBinding {
        return FragmentToolsBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        bindToolAction(binding.actionMerge, R.drawable.ic_menu_merge_pdf, R.string.merge_pdf)
        bindToolAction(binding.actionSplit, R.drawable.ic_menu_pdf_split, R.string.split_pdf)
        bindToolAction(binding.actionLock, R.drawable.ic_menu_lock, R.string.lock)
        bindToolAction(binding.actionUnlock, R.drawable.ic_menu_unlock, R.string.unlock)
    }

    private fun bindToolAction(
        actionBinding: ViewToolActionCardBinding,
        iconRes: Int,
        titleRes: Int,
    ) {
        actionBinding.toolIcon.setImageResource(iconRes)
        actionBinding.toolTitle.setText(titleRes)
        actionBinding.root.setOnClickListener { }
    }
}
