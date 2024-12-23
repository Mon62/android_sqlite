package com.example.filemanagement

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.filemanagement.databinding.ActivityTextFileBinding
import kotlinx.coroutines.*
import java.io.File
import com.example.filemanagement.operations.FileOperation
import com.example.filemanagement.operations.FileOperationManager
import com.example.filemanagement.manager.FileModificationManager

class TextFileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextFileBinding
    private lateinit var currentFile: File
    private var isEditMode = false
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val operationManager = FileOperationManager()
    private val modificationManager = FileModificationManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupActionBar()
        loadFile()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
        }
    }

    private fun loadFile() {
        intent.getStringExtra("FILE_PATH")?.let { path ->
            currentFile = File(path)
            title = currentFile.name
            readFileContent()
        }
    }

    private fun readFileContent() = scope.launch {
        try {
            val content = withContext(Dispatchers.IO) {
                currentFile.readText()
            }
            binding.textFileContent.setText(content)
        } catch (e: Exception) {
            showError("Cannot read file: ${e.message}")
        }
    }

    private fun saveFile() = lifecycleScope.launch {
        try {
            val content = binding.textFileContent.text.toString()
            modificationManager.modifyFile(currentFile, content).collect { result ->
                when (result) {
                    is FileModificationManager.OperationResult.Progress -> {
                        binding.progressBar.progress = result.percent
                    }
                    is FileModificationManager.OperationResult.Success -> {
                        showMessage("File saved successfully")
                        toggleEditMode(false)
                    }
                    is FileModificationManager.OperationResult.Error -> {
                        showError("Save failed: ${result.message}")
                    }
                }
            }
        } catch (e: Exception) {
            showError("Save failed: ${e.message}")
        }
    }

    private fun handleUndo() = lifecycleScope.launch {
        modificationManager.undo().collect { result ->
            when (result) {
                is FileModificationManager.OperationResult.Success -> {
                    readFileContent()
                    showMessage("Undo successful")
                }
                is FileModificationManager.OperationResult.Error -> {
                    showError(result.message)
                }
                else -> Unit
            }
        }
    }

    private fun toggleEditMode(enabled: Boolean) {
        isEditMode = enabled
        binding.textFileContent.isEnabled = enabled
        invalidateOptionsMenu()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.text_file_menu, menu)
        menu.findItem(R.id.action_edit)?.isVisible = !isEditMode
        menu.findItem(R.id.action_save)?.isVisible = isEditMode
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_edit -> {
            toggleEditMode(true)
            true
        }
        R.id.action_save -> {
            saveFile()
            true
        }
        R.id.action_undo -> {
            handleUndo()
            true
        }
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}