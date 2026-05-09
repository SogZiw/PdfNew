package com.word.file.manager.pdf.modules.guide

import android.content.Intent
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_IMPRESSION
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.databinding.ActivityGuideFirstBinding
import com.word.file.manager.pdf.modules.MainActivity

class GuideFirstActivity : BaseActivity<ActivityGuideFirstBinding>() {

    override fun setViewBinding() = ActivityGuideFirstBinding.inflate(layoutInflater)

    override fun initView() {
        binding.toolbar.toolbarTitle.text = getString(R.string.guide_first_title)
        binding.toolbar.actionBack.setOnClickListener { onUserBack() }
        binding.guideFirstPdfRetryButton.setOnClickListener { goMainPage() }
        binding.guideFirstFeatureExploreButton.setOnClickListener { goMainPage() }
        binding.guideFirstKeepButton.setOnClickListener { goMainPage() }
        binding.guideFirstContinueUninstallButton.setOnClickListener {
            EventCenter.logEvent(APP_AD_IMPRESSION, mapOf(AD_POS_ID to "ad_uninstall_int"))
            AdCenter.scanInterstitial.showFullScreen(activity, eventName = "ad_uninstall_int", allowed = {
                UserBlockHelper.canShowExtra()
            }, closed = {
                startActivity(Intent(activity, GuideReasonActivity::class.java))
                finish()
            })
        }
        AdCenter.scanInterstitial.preload()
        AdCenter.scanNative.preload()
        AdCenter.mainNative.renderNative(activity, binding.exContainer, NativeAdStyle.COMMON_MEDIA, eventName = "ad_uninstall_nat1", allowed = {
            UserBlockHelper.canShowExtra()
        })
        EventCenter.logEvent("uninstall_page1_show")
    }

    private fun goMainPage() {
        startActivity(Intent(activity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    override fun onUserBack() {
        goMainPage()
    }
}
