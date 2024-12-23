package com.example.filemanagement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.filemanagement.databinding.ItemFileBinding
import java.io.File

class FileAdapter(
    private val files: List<File>,
    private val onItemClick: (File) -> Unit,
    private val onItemLongClick: (File) -> Boolean
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {
    private val selectedItems = mutableSetOf<File>()

    class FileViewHolder(
        private val binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File, isSelected: Boolean, onItemClick: (File) -> Unit, onItemLongClick: (File) -> Boolean) {
            binding.apply {
                root.isSelected = isSelected
                root.setOnLongClickListener { onItemLongClick(file) }
                fileNameTextView.text = file.name
                fileTypeTextView.text = getFileType(file)
                root.setOnClickListener { onItemClick(file) }
            }
        }

        private fun getFileType(file: File): String = when {
            file.isDirectory -> "Folder"
            file.extension.isNotEmpty() -> "${file.extension.uppercase()} File"
            else -> "File"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file, selectedItems.contains(file), onItemClick, onItemLongClick)
    }

    override fun getItemCount() = files.size

    fun toggleSelection(file: File) {
        if (selectedItems.contains(file)) {
            selectedItems.remove(file)
        } else {
            selectedItems.add(file)
        }
        notifyItemChanged(files.indexOf(file))
    }

    fun getSelectedItems(): Set<File> = selectedItems.toSet()

    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
}