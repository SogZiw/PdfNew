package com.word.file.manager.pdf.modules.tools

import android.content.Intent
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.EXTRA_RESULT_TEXT
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.utils.isUsablePdfForTool
import com.word.file.manager.pdf.base.utils.mergePdfDocuments
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.ActivityPdfMergeBinding
import com.word.file.manager.pdf.databinding.DialogPdfWorkingBinding
import com.word.file.manager.pdf.modules.PdfCreateResultActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PdfMergeActivity : BaseActivity<ActivityPdfMergeBinding>() {

    private lateinit var fileAdapter: PdfToolFileAdapter
    private var mergeInProgress = false

    override fun setViewBinding(): ActivityPdfMergeBinding = ActivityPdfMergeBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        binding.toolbar.actionBack.setOnClickListener { onClickBack() }
        binding.toolbar.toolbarTitle.text = getString(R.string.merge_pdf)
        fileAdapter = PdfToolFileAdapter(
            pickMode = PdfToolFileAdapter.PickMode.Multiple,
            onSelectionChanged = { updateMergeButton(it) },
            onSelectionLimitReached = { showMessageToast(getString(R.string.choose_limit_no_more)) },
        )
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.adapter = fileAdapter
        binding.btnMerge.setOnClickListener { mergeSelectedFiles() }
        observePdfFiles()
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

    private fun updateMergeButton(selectedItems: List<FileItem>) {
        binding.btnMerge.isEnabled = selectedItems.size >= 2
        binding.btnMerge.text = if (selectedItems.isEmpty()) {
            getString(R.string.merge)
        } else {
            "${getString(R.string.merge)}(${selectedItems.size})"
        }
    }

    private fun mergeSelectedFiles() {
        if (mergeInProgress) return
        val selectedItems = fileAdapter.getSelectedItems()
        if (selectedItems.size < 2) return
        lifecycleScope.launch {
            mergeInProgress = true
            binding.btnMerge.isEnabled = false
            val dialog = showWorkingDialog(getString(R.string.merging))
            val outputTask = async { mergePdfDocuments(selectedItems) }
            delay(MIN_WORKING_DURATION_MS)
            val outputFile = outputTask.await()
            dialog.dismiss()
            if (outputFile == null) {
                mergeInProgress = false
                updateMergeButton(fileAdapter.getSelectedItems())
                showMessageToast(getString(R.string.common_error_message))
                return@launch
            }
            val outputItem = app.documentRepository.registerToolOutputPdf(outputFile)
            startActivity(Intent(this@PdfMergeActivity, PdfCreateResultActivity::class.java).apply {
                putExtra(EXTRA_FILE_ITEM, outputItem)
                putExtra(EXTRA_RESULT_TEXT, getString(R.string.merge_successful))
            })
            finish()
        }
    }

    private fun showWorkingDialog(message: String) = MaterialAlertDialogBuilder(this)
        .setView(
            DialogPdfWorkingBinding.inflate(layoutInflater).apply {
                textProgress.text = "$message..."
            }.root,
        )
        .setCancelable(false)
        .create()
        .also { it.show() }

    private companion object {
        const val MIN_WORKING_DURATION_MS = 3_000L
    }
}
