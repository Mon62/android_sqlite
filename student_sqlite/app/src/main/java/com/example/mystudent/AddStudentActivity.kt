package com.example.mystudent

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddStudentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add)

        val edtName: EditText = findViewById(R.id.name)
        val edtMSSV: EditText = findViewById(R.id.mssv)
        val btnSave: Button = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val mssv = edtMSSV.text.toString().trim()

            if (name.isNotEmpty() && mssv.isNotEmpty()) {
                val student = StudentModel(name, mssv)

                // Trả kết quả về MainActivity
                intent.putExtra("student", student)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}