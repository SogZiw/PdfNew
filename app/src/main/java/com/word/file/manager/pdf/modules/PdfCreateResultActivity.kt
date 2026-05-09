package com.word.file.manager.pdf.modules

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_CHANCE
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.EXTRA_RESULT_TEXT
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.base.utils.buildInfoText
import com.word.file.manager.pdf.databinding.ActivityPdfCreateResultBinding

class PdfCreateResultActivity : BaseActivity<ActivityPdfCreateResultBinding>() {

    override fun setViewBinding(): ActivityPdfCreateResultBinding = ActivityPdfCreateResultBinding.inflate(LayoutInflater.from(this))

    @SuppressLint("SetTextI18n")
    override fun initView() {
        val fileItem = intent?.getParcelableExtra<FileItem>(EXTRA_FILE_ITEM)
        if (fileItem == null) {
            finish()
            return
        }
        binding.toolbar.actionBack.setOnClickListener { onUserBack() }
        binding.toolbar.toolbarTitle.text = getString(R.string.create_pdf)
        binding.textResult.text = intent?.getStringExtra(EXTRA_RESULT_TEXT).orEmpty().ifBlank { getString(R.string.pdf_created) }
        binding.itemTitle.text = fileItem.documentTitle
        binding.itemDesc.text = "${fileItem.buildInfoText(this)}\n${fileItem.absolutePath}"
        binding.btnOpen.setOnClickListener {
            startActivity(Intent(this, PdfReaderActivity::class.java).apply {
                putExtra(EXTRA_FILE_ITEM, fileItem)
            })
            finish()
        }
        binding.btnHome.setOnClickListener {
            finish()
        }
        AdCenter.backInterstitial.preload()
        AdCenter.scanNative.renderNative(activity, binding.exContainer, style = NativeAdStyle.ANIM_MEDIA, eventName = "ad_scan_nat", allowed = {
            UserBlockHelper.canShowExtra()
        })
    }

    override fun onUserBack() {
        EventCenter.logEvent(APP_AD_CHANCE, mapOf(AD_POS_ID to "ad_back_int"))
        AdCenter.backInterstitial.showFullScreen(activity, eventName = "ad_back_int", allowed = {
            UserBlockHelper.canShowExtra()
        }, closed = {
            super.onUserBack()
        })
    }
}
