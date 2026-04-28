package com.word.file.manager.pdf.modules.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.databinding.DialogFileActionsBinding
import com.word.file.manager.pdf.databinding.ViewDialogActionItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileActionsDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_FILE_ITEM = "arg_file_item"
        private const val ARG_IS_COLLECTED = "arg_is_collected"

        fun newInstance(
            fileItem: FileItem,
            isCollected: Boolean = false,
        ): FileActionsDialogFragment {
            return FileActionsDialogFragment().apply {
                arguments = bundleOf(
                    ARG_FILE_ITEM to fileItem,
                    ARG_IS_COLLECTED to isCollected,
                )
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
        val isCollected = arguments?.getBoolean(ARG_IS_COLLECTED, false) ?: false
        bindHeader(fileItem, isCollected)
        bindActionRows()
    }

    private fun readFileItem(): FileItem? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_FILE_ITEM, FileItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_FILE_ITEM)
        }
    }

    private fun bindHeader(fileItem: FileItem, isCollected: Boolean) {
        binding.textFileName.text = fileItem.fileName
        binding.textFilePath.text = fileItem.filePath
        renderCollectState(isCollected)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val storedItem = app.database.fileItemDao().getFileByPath(fileItem.filePath)
            val actualCollected = storedItem?.isFavorite ?: isCollected
            withContext(Dispatchers.Main) {
                renderCollectState(actualCollected)
            }
        }
    }

    private fun renderCollectState(isCollected: Boolean) {
        binding.btnCollect.setImageResource(
            if (isCollected) R.drawable.ic_menu_collected else R.drawable.ic_menu_add_collect,
        )
    }

    private fun bindActionRows() {
        bindAction(binding.actionRename, R.drawable.ic_menu_rename, R.string.rename)
        bindAction(binding.actionOpen, R.drawable.ic_menu_open, R.string.open)
        bindAction(binding.actionShare, R.drawable.ic_menu_share, R.string.share)
        bindAction(binding.actionSplit, R.drawable.ic_menu_pdf_split, R.string.split_pdf)
        bindAction(binding.actionMerge, R.drawable.ic_menu_merge_pdf, R.string.merge_pdf)
        bindAction(binding.actionPrint, R.drawable.ic_menu_print_pdf, R.string.print)
        bindAction(binding.actionDelete, R.drawable.ic_menu_delete, R.string.delete, showDivider = false)
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
