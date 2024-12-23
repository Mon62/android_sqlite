package com.example.filemanagement

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.filemanagement.databinding.ActivityFileContentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val path = intent.getStringExtra("path") ?: run {
            showError("Invalid file path")
            return
        }

        loadFileContent(File(path))
    }

    private fun loadFileContent(file: File) {
        binding.progressBar.visibility = View.VISIBLE
        binding.textViewContent.visibility = View.GONE
        binding.textViewError.visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    if (!file.exists() || !file.isFile) {
                        throw IllegalStateException("Invalid file")
                    }
                    file.readText()
                }
                showContent(content)
            } catch (e: Exception) {
                showError(e.message ?: "Error reading file")
            }
        }
    }

    private fun showContent(content: String) {
        binding.progressBar.visibility = View.GONE
        binding.textViewContent.visibility = View.VISIBLE
        binding.textViewContent.text = content
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.textViewError.visibility = View.VISIBLE
        binding.textViewError.text = message
    }
}