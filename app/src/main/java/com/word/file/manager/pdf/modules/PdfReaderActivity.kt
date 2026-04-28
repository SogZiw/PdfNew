package com.word.file.manager.pdf.modules

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.utils.isPdfPasswordValid
import com.word.file.manager.pdf.base.utils.isPdfPasswordRequired
import com.word.file.manager.pdf.base.utils.markFileAsRecent
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.ActivityPdfReaderBinding
import com.word.file.manager.pdf.databinding.DialogPdfPasswordBinding
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
        if (isPdfPasswordRequired(fileItem.filePath)) {
            promptPassword(fileItem)
        } else {
            showPdfContent(fileItem)
        }
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

    private fun promptPassword(fileItem: FileItem) {
        val dialogBinding = DialogPdfPasswordBinding.inflate(
            LayoutInflater.from(this),
            window.decorView as ViewGroup,
            false,
        )
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.btnConfirm.setOnClickListener {
            val password = dialogBinding.etPassword.text?.toString().orEmpty()
            if (password.isBlank() || !isPdfPasswordValid(fileItem.filePath, password)) {
                showMessageToast(getString(R.string.pdf_password_incorrect))
                dialogBinding.etPassword.setText("")
                return@setOnClickListener
            }
            dialog.dismiss()
            showPdfContent(fileItem, password)
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }

    private fun showPdfContent(fileItem: FileItem, password: String? = null) {
        binding.pdfView.fromFile(File(fileItem.filePath))
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .enableAnnotationRendering(true)
            .password(password)
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
