package com.word.file.manager.pdf.modules.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseFragment
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.PdfLockType
import com.word.file.manager.pdf.base.data.PdfMergeType
import com.word.file.manager.pdf.base.data.PdfSplitType
import com.word.file.manager.pdf.base.data.PdfUnlockType
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.databinding.FragmentToolsBinding
import com.word.file.manager.pdf.databinding.ViewToolActionCardBinding
import com.word.file.manager.pdf.modules.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ToolsFragment : BaseFragment<FragmentToolsBinding>() {

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentToolsBinding {
        return FragmentToolsBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        bindToolAction(binding.actionMerge, R.drawable.ic_tools_merge_pdf, R.string.merge_pdf, PdfMergeType)
        bindToolAction(binding.actionSplit, R.drawable.ic_tools_split_pdf, R.string.split_pdf, PdfSplitType)
        bindToolAction(binding.actionLock, R.drawable.ic_tools_lock_pdf, R.string.lock_pdf, PdfLockType)
        bindToolAction(binding.actionUnlock, R.drawable.ic_tools_unlock_pdf, R.string.unlock_pdf, PdfUnlockType)
    }

    private fun bindToolAction(
        actionBinding: ViewToolActionCardBinding,
        iconRes: Int,
        titleRes: Int,
        actionType: DocumentActionType? = null,
    ) {
        actionBinding.toolIcon.setImageResource(iconRes)
        actionBinding.toolTitle.setText(titleRes)
        actionBinding.root.setOnClickListener {
            EventCenter.logEvent("tools_click")
            actionType?.let { (requireActivity() as? MainActivity)?.openDocumentTool(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(250L)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                AdCenter.scanNative.renderNative(activity, binding.exContainer, NativeAdStyle.ANIM_MEDIA, eventName = "ad_tools_nat", allowed = {
                    RemoteLogicConfig.fetchPromotionConfig().dashboardNat && UserBlockHelper.canShowExtra()
                })
            }
        }
    }
}
