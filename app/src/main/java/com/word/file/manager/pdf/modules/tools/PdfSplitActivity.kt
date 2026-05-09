package com.word.file.manager.pdf.modules.tools

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_CHANCE
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.utils.getPdfPageCount
import com.word.file.manager.pdf.base.utils.isUsablePdfForTool
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.ActivityPdfSplitBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfSplitActivity : BaseActivity<ActivityPdfSplitBinding>() {

    private lateinit var fileAdapter: PdfToolFileAdapter
    private val splitPagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            finish()
        }
    }

    override fun setViewBinding(): ActivityPdfSplitBinding = ActivityPdfSplitBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        binding.toolbar.actionBack.setOnClickListener { onUserBack() }
        binding.toolbar.toolbarTitle.text = getString(R.string.split_pdf)
        fileAdapter = PdfToolFileAdapter(
            pickMode = PdfToolFileAdapter.PickMode.Single,
            onFilePicked = { openPagePicker(it) },
        )
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.adapter = fileAdapter
        observePdfFiles()
        AdCenter.backInterstitial.preload()
        AdCenter.scanInterstitial.preload()
    }

    override fun onUserBack() {
        EventCenter.logEvent(APP_AD_CHANCE, mapOf(AD_POS_ID to "ad_back_int"))
        AdCenter.backInterstitial.showFullScreen(activity, eventName = "ad_back_int", allowed = {
            UserBlockHelper.canShowExtra()
        }, closed = {
            super.onUserBack()
        })
    }

    private fun observePdfFiles() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.documentRepository.allFiles.collect { files ->
                    val pdfFiles = files.filter { it.isUsablePdfForTool() }
                    fileAdapter.submitList(pdfFiles)
                    binding.emptyView.isVisible = pdfFiles.isEmpty()
                }
            }
        }
    }

    private fun openPagePicker(fileItem: FileItem) {
        lifecycleScope.launch {
            val pageCount = withContext(Dispatchers.IO) { getPdfPageCount(fileItem) }
            if (pageCount <= 1) {
                showMessageToast(getString(R.string.can_not_split))
                return@launch
            }
            splitPagesLauncher.launch(Intent(this@PdfSplitActivity, PdfSplitPagesActivity::class.java).apply {
                putExtra(EXTRA_FILE_ITEM, fileItem)
            })
        }
    }
}
