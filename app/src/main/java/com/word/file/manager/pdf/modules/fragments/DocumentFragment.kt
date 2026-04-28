package com.word.file.manager.pdf.modules.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseFragment
import com.word.file.manager.pdf.base.data.FileCategory
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.data.FileTabFilter
import com.word.file.manager.pdf.base.utils.buildInfoText
import com.word.file.manager.pdf.base.utils.getFileCategory
import com.word.file.manager.pdf.base.utils.matchesFilter
import com.word.file.manager.pdf.databinding.FragmentDocumentBinding
import com.word.file.manager.pdf.databinding.ItemFileInfoWithMenuBinding
import com.word.file.manager.pdf.modules.OfficePreviewActivity
import com.word.file.manager.pdf.modules.PdfReaderActivity
import com.word.file.manager.pdf.modules.dialogs.FileActionsDialogFragment
import com.word.file.manager.pdf.modules.permissions.hasStorageAccessPermission
import kotlinx.coroutines.launch

class DocumentFragment : BaseFragment<FragmentDocumentBinding>() {

    private lateinit var fileFilter: FileTabFilter
    private lateinit var source: DocumentSource
    private lateinit var adapter: DocumentListAdapter

    companion object {
        private const val ARG_FILTER = "arg_filter"
        private const val ARG_SOURCE = "arg_source"

        fun newInstance(filter: FileTabFilter, source: DocumentSource): DocumentFragment {
            return DocumentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FILTER, filter.name)
                    putString(ARG_SOURCE, source.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileFilter = FileTabFilter.valueOf(requireArguments().getString(ARG_FILTER, FileTabFilter.All.name))
        source = DocumentSource.valueOf(requireArguments().getString(ARG_SOURCE, DocumentSource.Home.name))
    }

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDocumentBinding {
        return FragmentDocumentBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        adapter = DocumentListAdapter(
            onItemClicked = { item ->
                openItem(item)
            },
            onMoreClicked = { item ->
                showFileActions(item)
            },
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter
        when (source) {
            DocumentSource.Home -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        app.documentRepository.allFiles.collect {
                            submitList(it)
                        }
                    }
                }
            }

            DocumentSource.Recent -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        app.documentRepository.recentFiles.collect {
                            submitList(it)
                        }
                    }
                }
            }

            DocumentSource.Bookmark -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        app.documentRepository.bookmarkFiles.collect {
                            submitList(it)
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.documentRepository.showPermissionGuide.collect {
                    val currentItems = when (source) {
                        DocumentSource.Home -> app.documentRepository.allFiles.value
                        DocumentSource.Recent -> app.documentRepository.recentFiles.value
                        DocumentSource.Bookmark -> app.documentRepository.bookmarkFiles.value
                    }
                    submitList(currentItems)
                }
            }
        }
    }

    private fun openItem(item: FileItem) {
        val targetClass = when (item.getFileCategory()) {
            FileCategory.Pdf -> PdfReaderActivity::class.java
            FileCategory.Word,
            FileCategory.Excel,
            FileCategory.Ppt -> OfficePreviewActivity::class.java

            null -> return
        }
        activity.startActivity(Intent(activity, targetClass).apply {
            putExtra(EXTRA_FILE_ITEM, item)
        })
    }

    private fun showFileActions(item: FileItem) {
        FileActionsDialogFragment.newInstance(
            fileItem = item,
            isCollected = item.isFavorite,
        ).show(childFragmentManager, "file_actions_dialog")
    }

    private fun submitList(sourceList: List<FileItem>) {
        val result = sourceList.filter { it.matchesFilter(fileFilter) }
        adapter.submitList(result)
        binding.emptyView.isVisible = hasStorageAccessPermission() && result.isEmpty()
    }

    private class DocumentListAdapter(
        private val onItemClicked: (FileItem) -> Unit,
        private val onMoreClicked: (FileItem) -> Unit,
    ) : RecyclerView.Adapter<DocumentListAdapter.DocumentViewHolder>() {

        private val items = mutableListOf<FileItem>()

        @SuppressLint("NotifyDataSetChanged")
        fun submitList(data: List<FileItem>) {
            items.clear()
            items.addAll(data)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
            return DocumentViewHolder(
                ItemFileInfoWithMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            )
        }

        override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
            val item = items[position]
            val fileCategory = item.getFileCategory() ?: return
            holder.binding.itemImage.setImageResource(fileCategory.iconRes)
            holder.binding.itemFileName.text = item.fileName
            holder.binding.itemFileDesc.text = item.buildInfoText(holder.itemView.context)
            holder.binding.itemMore.setOnClickListener {
                onMoreClicked(item)
            }
            holder.itemView.setOnClickListener {
                onItemClicked(item)
            }
        }

        override fun getItemCount(): Int = items.size

        class DocumentViewHolder(val binding: ItemFileInfoWithMenuBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
