package com.word.file.manager.pdf.modules.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseFragment
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.data.FileTabFilter
import com.word.file.manager.pdf.base.utils.buildInfoText
import com.word.file.manager.pdf.base.utils.getFileCategory
import com.word.file.manager.pdf.base.utils.matchesFilter
import com.word.file.manager.pdf.base.utils.openFileBySystem
import com.word.file.manager.pdf.databinding.FragmentDocumentBinding
import com.word.file.manager.pdf.databinding.ItemFileInfoWithMenuBinding
import com.word.file.manager.pdf.modules.permissions.hasStorageAccessPermission
import kotlinx.coroutines.Dispatchers
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
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter
        when (source) {
            DocumentSource.Home -> {
                app.mainViewModel.onScanResultLiveData.observe(viewLifecycleOwner) {
                    submitList(it)
                }
                submitList(app.mainViewModel.allFiles)
            }

            DocumentSource.Recent -> {
                app.mainViewModel.onRecentUpdateLiveData.observe(viewLifecycleOwner) {
                    submitList(it)
                }
            }

            DocumentSource.Bookmark -> {
                app.mainViewModel.onBookmarkUpdateLiveData.observe(viewLifecycleOwner) {
                    submitList(it)
                }
            }
        }
    }

    private fun openItem(item: FileItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbItem = app.database.fileItemDao().getFileByPath(item.filePath) ?: item
            dbItem.recentViewTime = System.currentTimeMillis()
            app.database.fileItemDao().upsert(dbItem)
        }
        activity.openFileBySystem(item)
    }

    private fun submitList(sourceList: List<FileItem>) {
        val result = sourceList.filter { it.matchesFilter(fileFilter) }
        adapter.submitList(result)
        binding.emptyView.isVisible = hasStorageAccessPermission() && result.isEmpty()
    }

    private class DocumentListAdapter(
        private val onItemClicked: (FileItem) -> Unit,
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
            holder.itemView.setOnClickListener {
                onItemClicked(item)
            }
        }

        override fun getItemCount(): Int = items.size

        class DocumentViewHolder(val binding: ItemFileInfoWithMenuBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
