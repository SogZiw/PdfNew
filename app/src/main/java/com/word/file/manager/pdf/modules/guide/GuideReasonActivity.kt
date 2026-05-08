package com.word.file.manager.pdf.modules.guide

import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.databinding.ActivityGuideReasonBinding
import com.word.file.manager.pdf.hasGoSettings
import com.word.file.manager.pdf.modules.MainActivity
import com.word.file.manager.pdf.modules.permissions.createAppDetailsSettingsIntent

class GuideReasonActivity : BaseActivity<ActivityGuideReasonBinding>() {

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasGoSettings = false
        goMainPage()
    }

    override fun setViewBinding() = ActivityGuideReasonBinding.inflate(layoutInflater)

    override fun initView() {
        binding.toolbar.toolbarTitle.text = getString(R.string.guide_reason_title)
        binding.toolbar.actionBack.setOnClickListener { onClickBack() }
        bindReasonOptions()
        binding.guideReasonKeepButton.setOnClickListener {
            goMainPage()
        }
        binding.guideReasonUninstallButton.setOnClickListener {
            hasGoSettings = true
            settingsLauncher.launch(createAppDetailsSettingsIntent(activity))
        }
        AdCenter.scanNative.renderNative(
            activity = activity,
            host = binding.exContainer,
            style = NativeAdStyle.Media,
            eventName = "ad_uninstall_nat2",
            allowed = { UserBlockHelper.canShowExtra() },
        )
        EventCenter.logEvent("uninstall_page2_show")
    }

    private fun bindReasonOptions() {
        setReasonOption(binding.guideReasonComplexRow, binding.guideReasonComplexCheck)
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

    private fun goMainPage() {
        startActivity(Intent(activity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    override fun onClickBack() {
        goMainPage()
    }

    override fun onDestroy() {
        hasGoSettings = false
        super.onDestroy()
    }
}
