package com.example.mystudent

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EditStudentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add) // Dùng lại layout của AddStudentActivity

        val edtName: EditText = findViewById(R.id.name)
        val edtMSSV: EditText = findViewById(R.id.mssv)
        val btnSave: Button = findViewById(R.id.btnSave)

        // Nhận thông tin sinh viên từ Intent
        val student = intent.getParcelableExtra<StudentModel>("student") as StudentModel
        val position = intent.getIntExtra("position", -1)

        // Điền thông tin sinh viên vào các EditText
        edtName.setText(student.studentName)
        edtMSSV.setText(student.studentId)

        btnSave.setOnClickListener {
            val updatedName = edtName.text.toString().trim()
            val updatedMSSV = edtMSSV.text.toString().trim()

            if (updatedName.isNotEmpty() && updatedMSSV.isNotEmpty()) {
                val updatedStudent = StudentModel(updatedName, updatedMSSV)

                // Trả kết quả về MainActivity
                intent.putExtra("student", updatedStudent)
                intent.putExtra("position", position)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}