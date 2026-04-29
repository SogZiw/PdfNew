package com.word.file.manager.pdf.modules.tools

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.utils.buildInfoText
import com.word.file.manager.pdf.databinding.ItemPdfToolFileBinding

class PdfToolFileAdapter(
    private val pickMode: PickMode,
    private val onSelectionChanged: (List<FileItem>) -> Unit = {},
    private val onFilePicked: (FileItem) -> Unit = {},
    private val onSelectionLimitReached: () -> Unit = {},
) : RecyclerView.Adapter<PdfToolFileAdapter.PdfToolFileViewHolder>() {

    private val items = mutableListOf<FileItem>()
    private val selectedPaths = mutableListOf<String>()

    enum class PickMode {
        Single,
        Multiple,
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(data: List<FileItem>) {
        items.clear()
        items.addAll(data)
        selectedPaths.retainAll(items.map { it.absolutePath }.toSet())
        notifyDataSetChanged()
        onSelectionChanged(getSelectedItems())
    }

    fun getSelectedItems(): List<FileItem> {
        return selectedPaths.mapNotNull { path -> items.firstOrNull { it.absolutePath == path } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfToolFileViewHolder {
        return PdfToolFileViewHolder(
            ItemPdfToolFileBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: PdfToolFileViewHolder, position: Int) {
        val item = items[position]
        val selectedOrder = selectedPaths.indexOf(item.absolutePath)
        val selected = selectedOrder >= 0
        holder.binding.itemName.text = item.documentTitle
        holder.binding.itemDesc.text = item.buildInfoText(holder.itemView.context)
        holder.binding.itemOrder.isVisible = selected
        holder.binding.itemOrder.text = if (selected) "${selectedOrder + 1}" else ""
        holder.binding.root.isSelected = selected
        holder.itemView.setOnClickListener {
            when (pickMode) {
                PickMode.Single -> onFilePicked(item)
                PickMode.Multiple -> toggleSelection(holder.bindingAdapterPosition)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun toggleSelection(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        val item = items[position]
        if (selectedPaths.remove(item.absolutePath)) {
            notifyDataSetChanged()
            onSelectionChanged(getSelectedItems())
            return
        }
        if (selectedPaths.size >= MAX_SELECTION_COUNT) {
            onSelectionLimitReached()
            return
        }
        selectedPaths.add(item.absolutePath)
        notifyDataSetChanged()
        onSelectionChanged(getSelectedItems())
    }

    override fun getItemCount(): Int = items.size

    class PdfToolFileViewHolder(val binding: ItemPdfToolFileBinding) : RecyclerView.ViewHolder(binding.root)

    private companion object {
        const val MAX_SELECTION_COUNT = 9
    }
}
