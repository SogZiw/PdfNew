package com.word.file.manager.pdf.modules.tools

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.EXTRA_RESULT_TEXT
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.base.utils.splitPdfDocument
import com.word.file.manager.pdf.databinding.ActivityPdfSplitPagesBinding
import com.word.file.manager.pdf.databinding.DialogPdfWorkingBinding
import com.word.file.manager.pdf.databinding.ItemPdfPagePreviewBinding
import com.word.file.manager.pdf.modules.PdfCreateResultActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

class PdfSplitPagesActivity : BaseActivity<ActivityPdfSplitPagesBinding>() {

    private var fileItem: FileItem? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private val thumbnailCache = LinkedHashMap<Int, Bitmap>()
    private val renderMutex = Mutex()
    private var renderJob: Job? = null
    private lateinit var pageAdapter: PdfPageAdapter
    private var splitInProgress = false

    override fun setViewBinding(): ActivityPdfSplitPagesBinding {
        return ActivityPdfSplitPagesBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        fileItem = readTargetFile()
        val targetFile = fileItem
        if (targetFile == null) {
            showMessageToast(getString(R.string.common_error_message))
            finish()
            return
        }
        binding.toolbar.actionBack.setOnClickListener { onClickBack() }
        binding.toolbar.toolbarTitle.text = targetFile.documentTitle
        lifecycleScope.launch {
            val pageCount = withContext(Dispatchers.IO) { prepareRenderer(targetFile) }
            if (pageCount <= 1) {
                showMessageToast(getString(R.string.common_error_message))
                finish()
                return@launch
            }
            pageAdapter = PdfPageAdapter(
                pageCount = pageCount,
                onSelectionChanged = { selectedPages -> updateSplitButton(selectedPages) },
            )
            binding.recyclerView.itemAnimator = null
            binding.recyclerView.adapter = pageAdapter
        }
        binding.btnSplit.setOnClickListener { splitSelectedPages(targetFile) }
    }

    private fun readTargetFile(): FileItem? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_FILE_ITEM, FileItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_FILE_ITEM)
        }
    }

    private fun prepareRenderer(fileItem: FileItem): Int {
        return runCatching {
            fileDescriptor = ParcelFileDescriptor.open(File(fileItem.absolutePath), ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(checkNotNull(fileDescriptor))
            checkNotNull(pdfRenderer).pageCount
        }.getOrElse {
            closeRenderer()
            0
        }
    }

    private fun updateSplitButton(selectedPages: List<Int>) {
        binding.btnSplit.isEnabled = selectedPages.isNotEmpty()
        binding.btnSplit.text = if (selectedPages.isEmpty()) {
            getString(R.string.split)
        } else {
            "${getString(R.string.split)}(${selectedPages.size})"
        }
    }

    private fun splitSelectedPages(fileItem: FileItem) {
        if (splitInProgress) return
        val selectedPages = pageAdapter.getSelectedPages()
        if (selectedPages.isEmpty()) return
        lifecycleScope.launch {
            splitInProgress = true
            binding.btnSplit.isEnabled = false
            val dialog = showWorkingDialog(getString(R.string.splitting))
            val outputTask = async { splitPdfDocument(fileItem, selectedPages) }
            delay(MIN_WORKING_DURATION_MS)
            val outputFile = outputTask.await()
            dialog.dismiss()
            if (outputFile == null) {
                splitInProgress = false
                updateSplitButton(pageAdapter.getSelectedPages())
                showMessageToast(getString(R.string.common_error_message))
                return@launch
            }
            val outputItem = app.documentRepository.registerToolOutputPdf(outputFile)
            startActivity(Intent(this@PdfSplitPagesActivity, PdfCreateResultActivity::class.java).apply {
                putExtra(EXTRA_FILE_ITEM, outputItem)
                putExtra(EXTRA_RESULT_TEXT, getString(R.string.split_successful))
            })
            setResult(RESULT_OK)
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

    private suspend fun renderPageThumbnail(position: Int): Bitmap? {
        return withContext(Dispatchers.IO) {
            if (position < 0) return@withContext null
            ensureActive()
            val renderer = pdfRenderer ?: return@withContext null
            thumbnailCache[position]?.let { return@withContext it }
            runCatching {
                renderer.openPage(position).use { page ->
                    val bitmap = createBitmap(max(1, page.width / 2), max(1, page.height / 2))
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    thumbnailCache[position] = bitmap
                    bitmap
                }
            }.getOrNull()
        }
    }

    override fun onDestroy() {
        renderJob?.cancel()
        renderJob = null
        CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, _ -> }).launch {
            delay(RENDERER_CLOSE_DELAY_MS)
            renderMutex.withLock {
                closeRenderer()
            }
        }
        super.onDestroy()
    }

    private fun closeRenderer() {
        thumbnailCache.values.forEach { it.recycle() }
        thumbnailCache.clear()
        runCatching { pdfRenderer?.close() }
        runCatching { fileDescriptor?.close() }
        pdfRenderer = null
        fileDescriptor = null
    }

    inner class PdfPageAdapter(
        private val pageCount: Int,
        private val onSelectionChanged: (List<Int>) -> Unit,
    ) : RecyclerView.Adapter<PdfPageAdapter.PdfPageViewHolder>() {

        private val selectedPages = mutableListOf<Int>()

        fun getSelectedPages(): List<Int> = selectedPages.toList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder {
            return PdfPageViewHolder(
                ItemPdfPagePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
            val selectedOrder = selectedPages.indexOf(position)
            val selected = selectedOrder >= 0
            holder.binding.textPage.text = "${position + 1}"
            holder.binding.textOrder.isVisible = selected
            holder.binding.textOrder.text = if (selected) "${selectedOrder + 1}" else ""
            holder.binding.root.isSelected = selected
            holder.binding.imagePage.setImageBitmap(thumbnailCache[position])
            holder.itemView.setOnClickListener {
                togglePage(holder.bindingAdapterPosition)
            }
        }

        override fun onViewAttachedToWindow(holder: PdfPageViewHolder) {
            super.onViewAttachedToWindow(holder)
            val itemCache = thumbnailCache[holder.layoutPosition]
            if (itemCache == null) {
                renderJob = lifecycleScope.launch(Dispatchers.Main) {
                    renderMutex.withLock {
                        val bitmap = renderPageThumbnail(holder.layoutPosition)
                        holder.binding.imagePage.setImageBitmap(bitmap)
                    }
                }
            } else {
                holder.binding.imagePage.setImageBitmap(itemCache)
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun togglePage(position: Int) {
            if (position == RecyclerView.NO_POSITION) return
            if (selectedPages.remove(position).not()) {
                selectedPages.add(position)
            }
            notifyDataSetChanged()
            onSelectionChanged(getSelectedPages())
        }

        override fun getItemCount(): Int = pageCount

        inner class PdfPageViewHolder(
            val binding: ItemPdfPagePreviewBinding,
        ) : RecyclerView.ViewHolder(binding.root)
    }

    private companion object {
        const val RENDERER_CLOSE_DELAY_MS = 1_000L
        const val MIN_WORKING_DURATION_MS = 3_000L
    }
}
