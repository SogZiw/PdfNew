package com.word.file.manager.pdf.modules

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileCategory
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.utils.buildInfoText
import com.word.file.manager.pdf.base.utils.getFileCategory
import com.word.file.manager.pdf.base.utils.markFileAsRecent
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.ActivityOfficeViewBinding
import kotlinx.coroutines.launch
import java.io.File

class OfficeViewActivity : BaseActivity<ActivityOfficeViewBinding>() {

    override fun setViewBinding(): ActivityOfficeViewBinding = ActivityOfficeViewBinding.inflate(LayoutInflater.from(this))

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        val fileItem = intent?.getParcelableExtra<FileItem>(EXTRA_FILE_ITEM)
        if (fileItem == null) {
            showMessageToast(getString(R.string.common_error_message))
            finish()
            return
        }
        binding.toolbar.actionBack.setOnClickListener { onClickBack() }
        binding.toolbar.toolbarTitle.text = fileItem.fileName
        val fileCategory = fileItem.getFileCategory()
        val viewerUrl = when (fileCategory) {
            FileCategory.Word -> "file:///android_asset/word/viewer.html"
            FileCategory.Excel -> "file:///android_asset/excel/viewer.html"
            FileCategory.Ppt -> "file:///android_asset/ppt/viewer.html"
            else -> null
        }
        if (viewerUrl == null) {
            showMessageToast(getString(R.string.common_error_message))
            finish()
            return
        }
        binding.webView.apply {
            settings.apply {
                allowFileAccess = true
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                useWideViewPort = true
                loadWithOverviewMode = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
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
        binding.loadingContainer.visibility = android.view.View.VISIBLE
        val targetFile = Uri.fromFile(File(fileItem.filePath)).toString()
        binding.webView.loadUrl("$viewerUrl?file=$targetFile")
        lifecycleScope.launch {
            markFileAsRecent(fileItem)
        }
    }
}
