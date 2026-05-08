package com.word.file.manager.pdf.modules.guide

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.databinding.ActivityGuideReasonBinding

class GuideReasonActivity : BaseActivity<ActivityGuideReasonBinding>() {

    override fun setViewBinding() = ActivityGuideReasonBinding.inflate(layoutInflater)

    override fun initView() {
        binding.toolbar.toolbarTitle.text = getString(R.string.guide_reason_title)
        binding.toolbar.actionBack.setOnClickListener { }
        bindReasonOptions()
        binding.guideReasonKeepButton.setOnClickListener { }
        binding.guideReasonUninstallButton.setOnClickListener { }
    }

    private fun bindReasonOptions() {
        setReasonOption(binding.guideReasonComplexRow, binding.guideReasonComplexCheck, selected = true)
        setReasonOption(binding.guideReasonAdsRow, binding.guideReasonAdsCheck)
        setReasonOption(binding.guideReasonQualityRow, binding.guideReasonQualityCheck)
        setReasonOption(binding.guideReasonNotificationRow, binding.guideReasonNotificationCheck)
        setReasonOption(binding.guideReasonToolsRow, binding.guideReasonToolsCheck)
        setReasonOption(binding.guideReasonSystemViewerRow, binding.guideReasonSystemViewerCheck)
    }

    private fun setReasonOption(
        row: View,
        checkView: AppCompatImageView,
        selected: Boolean = false,
    ) {
        row.isSelected = selected
        checkView.setImageResource(if (selected) R.drawable.ic_language_selected else R.drawable.ic_language_unselected)
        row.setOnClickListener {
            val nextSelected = row.isSelected.not()
            row.isSelected = nextSelected
            checkView.setImageResource(
                if (nextSelected) R.drawable.ic_language_selected else R.drawable.ic_language_unselected,
            )
        }
    }

    override fun onClickBack() = Unit
}
