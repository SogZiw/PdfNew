package com.word.file.manager.pdf.modules

import android.os.Build
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.utils.markFileAsRecent
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.ActivityPdfReaderBinding
import kotlinx.coroutines.launch
import java.io.File

class PdfReaderActivity : BaseActivity<ActivityPdfReaderBinding>() {

    override fun setViewBinding(): ActivityPdfReaderBinding = ActivityPdfReaderBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        val fileItem = readTargetFile()
        if (fileItem == null) {
            showMessageToast(getString(R.string.common_error_message))
            finish()
            return
        }
        setupHeader(fileItem)
        showPdfContent(fileItem)
        rememberOpenAction(fileItem)
    }

    private fun readTargetFile(): FileItem? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_FILE_ITEM, FileItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_FILE_ITEM)
        }
    }

    private fun setupHeader(fileItem: FileItem) {
        binding.toolbar.actionBack.setOnClickListener { onClickBack() }
        binding.toolbar.toolbarTitle.text = fileItem.fileName
    }

    private fun rememberOpenAction(fileItem: FileItem) {
        lifecycleScope.launch {
            markFileAsRecent(fileItem)
        }
    }

    private fun showPdfContent(fileItem: FileItem) {
        binding.pdfView.fromFile(File(fileItem.filePath))
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .enableAnnotationRendering(true)
            .scrollHandle(DefaultScrollHandle(this))
            .enableAntialiasing(true)
            .spacing(3)
            .autoSpacing(false)
            .pageFitPolicy(FitPolicy.WIDTH)
            .fitEachPage(false)
            .pageSnap(false)
            .pageFling(false)
            .nightMode(false)
            .onLoad { pageCount ->
                binding.textPageState.text = getString(R.string.pdf_page_state, 1, pageCount)
            }
            .onPageChange { page, pageCount ->
                binding.textPageState.text = getString(R.string.pdf_page_state, page + 1, pageCount)
            }
            .onError {
                showMessageToast(it.message ?: getString(R.string.common_error_message))
            }
            .load()
    }
}
