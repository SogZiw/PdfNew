package com.word.file.manager.pdf.modules.permissions

import android.content.Intent
import android.graphics.Paint
import androidx.activity.result.contract.ActivityResultContracts
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_CHANCE
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.EXTRA_FROM_SET
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid13
import com.word.file.manager.pdf.base.utils.startButtonAnimation
import com.word.file.manager.pdf.databinding.ActivityExtraGuideBinding
import com.word.file.manager.pdf.hasGoSettings
import com.word.file.manager.pdf.modules.IntroduceActivity
import com.word.file.manager.pdf.modules.LanguageActivity
import com.word.file.manager.pdf.modules.MainActivity

class ExtraGuideActivity : BaseActivity<ActivityExtraGuideBinding>() {

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasGoSettings = false
        if (hasOverlayPermission()) {
            EventCenter.logEvent("win_permission_success")
            goNextPage()
        }
    }

    override fun setViewBinding() = ActivityExtraGuideBinding.inflate(layoutInflater)

    override fun initView() {
        binding.actionSkip.paintFlags = binding.actionSkip.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.actionSkip.setOnClickListener {
            EventCenter.logEvent(APP_AD_CHANCE, mapOf(AD_POS_ID to "ad_back_int"))
            AdCenter.backInterstitial.showFullScreen(activity, eventName = "ad_back_int", allowed = {
                RemoteLogicConfig.fetchPromotionConfig().overlaySkipInt && UserBlockHelper.canShowExtra()
            }, closed = {
                goNextPage()
            })
        }
        binding.extraGuideContinueButton.setOnClickListener {
            EventCenter.logEvent("page_win_click")
            permissionLauncher.launch(createOverlayPermissionPageIntent(activity))
        }
        AdCenter.backInterstitial.preload()
        binding.extraGuideContinueButton.startButtonAnimation()
        EventCenter.logEvent("page_win_show")
    }

    private fun goNextPage() {
        val actionType = readLaunchActionType()
        if (!LocalPrefs.hasSeenIntroduce) {
            if (RemoteLogicConfig.fetchFeatureConfig().firstShow.pageLang) {
                startActivity(Intent(activity, LanguageActivity::class.java).apply {
                    putExtra(EXTRA_FROM_SET, false)
                    putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
                })
                finish()
                return
            }
            if (RemoteLogicConfig.fetchFeatureConfig().firstShow.pageIntro) {
                startActivity(Intent(activity, IntroduceActivity::class.java).apply {
                    putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
                })
                finish()
                return
            }
            startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
            })
            finish()
        } else {
            startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
            })
            finish()
        }
    }

    private fun readLaunchActionType(): DocumentActionType {
        return if (isAtLeastAndroid13()) {
            intent?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE, DocumentActionType::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE)
        } ?: DocumentOpenType
    }


    override fun onUserBack() = Unit

}
