package com.word.file.manager.pdf.modules

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_CHANCE
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileCategory
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.utils.getFileCategory
import com.word.file.manager.pdf.base.utils.markFileAsRecent
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.ActivityOfficePreviewBinding
import kotlinx.coroutines.launch
import java.io.File

class OfficePreviewActivity : BaseActivity<ActivityOfficePreviewBinding>() {

    override fun setViewBinding(): ActivityOfficePreviewBinding = ActivityOfficePreviewBinding.inflate(LayoutInflater.from(this))

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        val fileItem = readTargetFile()
        if (fileItem == null) {
            showMessageToast(getString(R.string.common_error_message))
            finish()
            return
        }
        val fileCategory = fileItem.getFileCategory()
        val previewSource = resolvePreviewSource(fileCategory)
        if (previewSource == null) {
            showMessageToast(getString(R.string.common_error_message))
            finish()
            return
        }
        bindPreviewHeader(fileItem, previewSource)
        prepareWebPreview()
        dispatchPreview(previewSource, fileItem)
        rememberOpenAction(fileItem)
        AdCenter.backInterstitial.preload()
    }

    override fun onUserBack() {
        EventCenter.logEvent(APP_AD_CHANCE, mapOf(AD_POS_ID to "ad_back_int"))
        AdCenter.backInterstitial.showFullScreen(activity, eventName = "ad_back_int", allowed = {
            UserBlockHelper.canShowExtra()
        }, closed = {
            super.onUserBack()
        })
    }

    private fun readTargetFile(): FileItem? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_FILE_ITEM, FileItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_FILE_ITEM)
        }
    }

    private fun resolvePreviewSource(fileCategory: FileCategory?): PreviewSource? {
        return when (fileCategory) {
            FileCategory.Word -> PreviewSource("file:///android_asset/word/viewer.html", getString(R.string.word))
            FileCategory.Excel -> PreviewSource("file:///android_asset/excel/viewer.html", getString(R.string.excel))
            FileCategory.Ppt -> PreviewSource("file:///android_asset/ppt/viewer.html", getString(R.string.ppt))
            FileCategory.Pdf -> null
            null -> null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindPreviewHeader(fileItem: FileItem, previewSource: PreviewSource) {
        binding.toolbar.actionBack.setOnClickListener { onUserBack() }
        binding.toolbar.toolbarTitle.text = "${previewSource.badgeText}  ${fileItem.documentTitle}"
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun prepareWebPreview() {
        binding.webView.apply {
            settings.apply {
                allowFileAccess = true
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                useWideViewPort = true
                loadWithOverviewMode = true
                allowContentAccess = true
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.loadingContainer.visibility = android.view.View.GONE
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return true
                }
            }
        }
    }

    private fun dispatchPreview(previewSource: PreviewSource, fileItem: FileItem) {
        binding.loadingContainer.visibility = android.view.View.VISIBLE
        val targetFile = Uri.fromFile(File(fileItem.absolutePath)).toString()
        binding.webView.loadUrl("${previewSource.assetUrl}?file=$targetFile")
    }

    private fun rememberOpenAction(fileItem: FileItem) {
        lifecycleScope.launch {
            markFileAsRecent(fileItem)
        }
    }

    private data class PreviewSource(
        val assetUrl: String,
        val badgeText: String,
    )
}
