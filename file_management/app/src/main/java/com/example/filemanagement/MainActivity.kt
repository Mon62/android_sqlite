package com.example.filemanagement

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filemanagement.databinding.ActivityMainBinding
import java.io.File
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fileAdapter: FileAdapter
    private var currentPath: File = Environment.getExternalStorageDirectory()
    private var isInSelectionMode = false
    private val fileOperationsManager = FileOperationsManager()
    private val batchOperations = BatchFileOperations()
    private val fileModifier = FileModifier()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        checkAndRequestPermissions()
        setupToolbar()
    }

    private fun setupRecyclerView() {
        binding.fileRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            fileAdapter = FileAdapter(
                files = listOf(),
                onItemClick = { file -> handleFileClick(file) },
                onItemLongClick = { file -> 
                    startSelectionMode(file)
                    true
                }
            )
            adapter = fileAdapter
        }
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        updateToolbarTitle()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).also {
                startActivityForResult(it, PERMISSION_REQUEST_CODE)
            }
        } else {
            loadFileList(currentPath)
        }
    }

    private fun loadFileList(directory: File) {
        try {
            val files = directory.listFiles()?.toList()?.sortedWith(compareBy(
                { !it.isDirectory }, // Directories first
                { it.name.lowercase() } // Then alphabetically
            )) ?: listOf()

            fileAdapter = FileAdapter(files) { file -> handleFileClick(file) }
            binding.fileRecyclerView.adapter = fileAdapter
            updateToolbarTitle()
            
            binding.emptyView.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            showError("Error loading files: ${e.message}")
        }
    }

    private fun handleFileClick(file: File) {
        when {
            file.isDirectory -> navigateToDirectory(file)
            isTextFile(file) -> openTextFile(file)
            else -> showUnsupportedFileTypeMessage()
        }
    }

    private fun navigateToDirectory(directory: File) {
        currentPath = directory
        loadFileList(directory)
    }

    private fun isTextFile(file: File): Boolean =
        file.extension.lowercase() in listOf("txt", "log", "json", "xml", "csv")

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateToolbarTitle() {
        title = currentPath.name.ifEmpty { "Storage" }
    }

    override fun onBackPressed() {
        if (currentPath == Environment.getExternalStorageDirectory()) {
            super.onBackPressed()
        } else {
            currentPath.parentFile?.let { navigateToDirectory(it) }
        }
    }

    private fun startSelectionMode(file: File) {
        isInSelectionMode = true
        fileAdapter.toggleSelection(file)
        invalidateOptionsMenu()
    }

    private fun handleFileOperations(operation: FileOperation) = lifecycleScope.launch {
        val selectedFiles = fileAdapter.getSelectedItems()
        binding.progressBar.visibility = View.VISIBLE
        
        val result = when (operation) {
            FileOperation.DELETE -> fileOperationsManager.deleteFiles(selectedFiles)
            FileOperation.COPY -> fileOperationsManager.copyFiles(selectedFiles, currentPath)
            FileOperation.MOVE -> fileOperationsManager.moveFiles(selectedFiles, currentPath)
        }

        handleOperationResult(result)
        binding.progressBar.visibility = View.GONE
        exitSelectionMode()
    }

    private fun handleOperationResult(result: FileOperationsManager.OperationResult) {
        when (result) {
            is FileOperationsManager.OperationResult.Success -> {
                showMessage(result.message)
                loadFileList(currentPath)
            }
            is FileOperationsManager.OperationResult.Error -> {
                showError(result.message)
            }
            is FileOperationsManager.OperationResult.Progress -> {
                updateProgress(result.progress, result.total)
            }
        }
    }

    private fun updateProgress(progress: Int, total: Int) {
        binding.progressText.text = "$progress/$total"
    }

    private fun deleteFiles(files: Set<File>) = lifecycleScope.launch(Dispatchers.IO) {
        try {
            files.forEach { it.deleteRecursively() }
            withContext(Dispatchers.Main) {
                loadFileList(currentPath)
                showMessage("Files deleted successfully")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showError("Error deleting files: ${e.message}")
            }
        }
    }

    private fun copyFiles(files: Set<File>) {
        // Implementation for copy operation
    }

    private fun moveFiles(files: Set<File>) {
        // Implementation for move operation
    }

    private fun exitSelectionMode() {
        isInSelectionMode = false
        fileAdapter.clearSelections()
        invalidateOptionsMenu()
    }

    private fun handleBatchOperation(operation: suspend (List<File>) -> Flow<BatchFileOperations.OperationResult>) {
        val selectedFiles = fileAdapter.getSelectedItems().toList()
        if (selectedFiles.isEmpty()) {
            showError("No files selected")
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            operation(selectedFiles).collect { result ->
                when (result) {
                    is BatchFileOperations.OperationResult.Progress -> {
                        updateProgress(result.current, result.total, result.currentFile)
                    }
                    is BatchFileOperations.OperationResult.Success -> {
                        showMessage("Operation completed successfully")
                        loadFileList(currentPath)
                    }
                    is BatchFileOperations.OperationResult.Error -> {
                        showError("Error processing ${result.file.name}: ${result.message}")
                    }
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateProgress(current: Int, total: Int, fileName: String) {
        binding.progressText.text = "Processing $fileName ($current/$total)"
        binding.progressBar.progress = (current * 100 / total)
    }

    private fun showBatchOperationsDialog() {
        val operations = arrayOf("Find and Replace", "Append Text", "Prepend Text")
        AlertDialog.Builder(this)
            .setTitle("Select Operation")
            .setItems(operations) { _, which ->
                when (which) {
                    0 -> showFindReplaceDialog()
                    1 -> showAppendDialog()
                    2 -> showPrependDialog()
                }
            }
            .show()
    }

    private fun showFindReplaceDialog() {
        ModificationDialog.showFindReplaceDialog(this) { findText, replaceText ->
            handleBatchModification("Finding and replacing text") { files ->
                fileModifier.modifyFiles(files, fileModifier.findAndReplace(findText, replaceText))
            }
        }
    }

    private fun showAppendDialog() {
        ModificationDialog.showTextInputDialog(
            context = this,
            title = "Append Text",
            hint = "Text to append"
        ) { text ->
            handleBatchModification("Appending text") { files ->
                fileModifier.modifyFiles(files, fileModifier.appendText(text))
            }
        }
    }

    private fun showPrependDialog() {
        ModificationDialog.showTextInputDialog(
            context = this,
            title = "Prepend Text",
            hint = "Text to prepend"
        ) { text ->
            handleBatchModification("Prepending text") { files ->
                fileModifier.modifyFiles(files, fileModifier.prependText(text))
            }
        }
    }

    private fun handleBatchModification(
        operationName: String,
        modification: suspend (List<File>) -> Flow<FileModifier.ModificationResult>
    ) = lifecycleScope.launch {
        val selectedFiles = fileAdapter.getSelectedItems().toList()
        if (selectedFiles.isEmpty()) {
            showError("No files selected")
            return@launch
        }

        try {
            binding.progressBar.visibility = View.VISIBLE
            modification(selectedFiles).collect { result ->
                when (result) {
                    is FileModifier.ModificationResult.Progress -> {
                        updateProgress(result.current, result.total, result.fileName)
                    }
                    is FileModifier.ModificationResult.Success -> {
                        showMessage("$operationName completed")
                        loadFileList(currentPath)
                    }
                    is FileModifier.ModificationResult.Error -> {
                        showError("Error modifying ${result.file.name}: ${result.message}")
                    }
                }
            }
        } finally {
            binding.progressBar.visibility = View.GONE
            exitSelectionMode()
        }
    }

    enum class FileOperation {
        DELETE, COPY, MOVE
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}

