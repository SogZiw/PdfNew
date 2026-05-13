package com.word.file.manager.pdf.modules.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.FileCategory
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.utils.getPdfPageCount
import com.word.file.manager.pdf.base.utils.getFileCategory
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid13
import com.word.file.manager.pdf.base.utils.isUsablePdfForTool
import com.word.file.manager.pdf.base.utils.printPdf
import com.word.file.manager.pdf.base.utils.shareFile
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.DialogFileActionsBinding
import com.word.file.manager.pdf.databinding.DialogFileRenameBinding
import com.word.file.manager.pdf.databinding.ViewDialogActionItemBinding
import com.word.file.manager.pdf.modules.OfficePreviewActivity
import com.word.file.manager.pdf.modules.PdfReaderActivity
import com.word.file.manager.pdf.modules.tools.PdfSplitPagesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileActionsDialogFragment : BottomSheetDialogFragment() {

    private var currentFileItem: FileItem? = null
    private var currentCollectedState: Boolean = false
    private var headerCollectStateResolved: Boolean = false

    companion object {
        private const val ARG_FILE_ITEM = "arg_file_item"

        fun newInstance(fileItem: FileItem): FileActionsDialogFragment {
            return FileActionsDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FILE_ITEM, fileItem)
                }
            }
        }
    }

    private var _binding: DialogFileActionsBinding? = null
    private val binding: DialogFileActionsBinding
        get() = checkNotNull(_binding)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogFileActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fileItem = readFileItem() ?: return
        currentFileItem = fileItem
        bindHeader(fileItem)
        bindActionRows()
        binding.btnCollect.setOnClickListener {
            toggleCollectedState()
        }
    }

    private fun readFileItem(): FileItem? {
        return if (isAtLeastAndroid13()) {
            arguments?.getParcelable(ARG_FILE_ITEM, FileItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_FILE_ITEM)
        }
    }

    private fun bindHeader(fileItem: FileItem) {
        binding.textFileName.text = fileItem.documentTitle
        binding.textFilePath.text = fileItem.absolutePath
        binding.imageFileCover.setImageResource(fileItem.getFileCategory()?.iconRes ?: R.drawable.ic_file_pdf)
        headerCollectStateResolved = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val storedItem = app.database.fileItemDao().getFileByPath(fileItem.absolutePath)
            val actualCollected = storedItem?.collectedFlag ?: fileItem.collectedFlag
            withContext(Dispatchers.Main) {
                if (headerCollectStateResolved) return@withContext
                renderCollectState(actualCollected)
                headerCollectStateResolved = true
            }
        }
    }

    private fun renderCollectState(isCollected: Boolean) {
        currentCollectedState = isCollected
        binding.btnCollect.setImageResource(
            if (isCollected) R.drawable.ic_menu_collected else R.drawable.ic_menu_add_collect,
        )
    }

    private fun toggleCollectedState() {
        val fileItem = currentFileItem ?: return
        headerCollectStateResolved = true
        viewLifecycleOwner.lifecycleScope.launch {
            val collected = app.documentRepository.toggleFavorite(fileItem.copy(collectedFlag = currentCollectedState))
            currentFileItem = fileItem.copy(collectedFlag = collected)
            renderCollectState(collected)
        }
    }

    private fun bindActionRows() {
        bindAction(binding.actionRename, R.drawable.ic_menu_rename, R.string.rename)
        bindAction(binding.actionOpen, R.drawable.ic_menu_open, R.string.open)
        bindAction(binding.actionShare, R.drawable.ic_menu_share, R.string.share)
        bindAction(binding.actionSplit, R.drawable.ic_menu_pdf_split, R.string.split_pdf)
        bindAction(binding.actionPrint, R.drawable.ic_menu_print_pdf, R.string.print)
        bindAction(binding.actionDelete, R.drawable.ic_menu_delete, R.string.delete, showDivider = false)
        binding.actionRename.root.setOnClickListener { showRenameDialog() }
        binding.actionOpen.root.setOnClickListener { openFile() }
        binding.actionShare.root.setOnClickListener { shareCurrentFile() }
        binding.actionSplit.root.setOnClickListener { openSplitPagePicker() }
        binding.actionPrint.root.setOnClickListener { printCurrentFile() }
        binding.actionDelete.root.setOnClickListener { deleteCurrentFile() }
        setActionEnabled(binding.actionSplit, currentFileItem?.isUsablePdfForTool() == true)
        setActionEnabled(binding.actionPrint, currentFileItem?.getFileCategory() == FileCategory.Pdf)
    }

    private fun bindAction(
        itemBinding: ViewDialogActionItemBinding,
        iconRes: Int,
        textRes: Int,
        showDivider: Boolean = true,
    ) {
        itemBinding.actionIcon.setImageResource(iconRes)
        itemBinding.actionText.setText(textRes)
        itemBinding.actionDivider.visibility = if (showDivider) View.VISIBLE else View.GONE
    }

    private fun setActionEnabled(itemBinding: ViewDialogActionItemBinding, enabled: Boolean) {
        itemBinding.root.isEnabled = enabled
        itemBinding.actionText.alpha = if (enabled) 1f else 0.4f
        itemBinding.actionIcon.alpha = if (enabled) 1f else 0.4f
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun showRenameDialog() {
        val fileItem = currentFileItem ?: return
        val renameBinding = DialogFileRenameBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        renameBinding.etName.setText(fileItem.documentTitle.substringBeforeLast(".", fileItem.documentTitle))
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(renameBinding.root)
            .setCancelable(true)
            .create()
        renameBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        renameBinding.btnConfirm.setOnClickListener {
            val rawName = renameBinding.etName.text?.toString().orEmpty()
            if (rawName.isBlank()) {
                requireContext().showMessageToast(getString(R.string.file_name_required))
                return@setOnClickListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                val renamed = app.documentRepository.renameDocument(fileItem, rawName)
                if (renamed == null) {
                    requireContext().showMessageToast(getString(R.string.common_error_message))
                    return@launch
                }
                currentFileItem = renamed
                bindHeader(renamed)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun openFile() {
        val fileItem = currentFileItem ?: return
        dismiss()
        val targetClass = when (fileItem.getFileCategory()) {
            FileCategory.Pdf -> PdfReaderActivity::class.java
            FileCategory.Word,
            FileCategory.Excel,
            FileCategory.Ppt -> OfficePreviewActivity::class.java

            null -> return
        }
        startActivity(Intent(requireContext(), targetClass).apply {
            putExtra(EXTRA_FILE_ITEM, fileItem)
        })
    }

    private fun shareCurrentFile() {
        val fileItem = currentFileItem ?: return
        requireContext().shareFile(fileItem)
    }

    private fun openSplitPagePicker() {
        val fileItem = currentFileItem ?: return
        if (!fileItem.isUsablePdfForTool()) return
        viewLifecycleOwner.lifecycleScope.launch {
            val pageCount = withContext(Dispatchers.IO) { getPdfPageCount(fileItem) }
            if (pageCount <= 1) {
                requireContext().showMessageToast(getString(R.string.can_not_split))
                return@launch
            }
            dismiss()
            startActivity(Intent(requireContext(), PdfSplitPagesActivity::class.java).apply {
                putExtra(EXTRA_FILE_ITEM, fileItem)
            })
        }
    }

    private fun printCurrentFile() {
        val fileItem = currentFileItem ?: return
        if (fileItem.getFileCategory() != FileCategory.Pdf) return
        if (fileItem.encryptedFlag) {
            requireContext().showMessageToast(getString(R.string.cannot_print_encrypted_pdf))
            return
        }
        dismiss()
        val printContext = (requireActivity() as? BaseActivity<*>)?.printContext ?: requireContext()
        printContext.printPdf(fileItem)
    }

    private fun deleteCurrentFile() {
        val fileItem = currentFileItem ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val deleted = app.documentRepository.deleteDocument(fileItem)
            if (!deleted) {
                requireContext().showMessageToast(getString(R.string.common_error_message))
                return@launch
            }
            dismiss()
        }
    }
}
