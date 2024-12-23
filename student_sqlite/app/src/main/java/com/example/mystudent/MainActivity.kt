package com.example.mystudent

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
  private lateinit var studentList: MutableList<StudentModel>
  private lateinit var adapter: StudentAdapter
  private lateinit var dbHelper: DatabaseHelper

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    dbHelper = DatabaseHelper(this)
    studentList = dbHelper.getAllStudents()

    // If database is empty, add sample data
    if (studentList.isEmpty()) {
      addSampleData()
      studentList = dbHelper.getAllStudents()
    }

    val recyclerView: RecyclerView = findViewById(R.id.recycler_view_students)
    recyclerView.layoutManager = LinearLayoutManager(this)

    adapter = StudentAdapter(studentList) { view, position ->
      adapter.contextMenuPosition = position
      registerForContextMenu(view)
      view.showContextMenu()
    }
    recyclerView.adapter = adapter
  }

  private fun addSampleData() {
    val sampleStudents = listOf(
      StudentModel("Nguyễn Văn An", "SV001"),
      StudentModel("Trần Thị Bảo", "SV002"),
      StudentModel("Lê Hoàng Cường", "SV003")
      // Add more sample students as needed
    )

    for (student in sampleStudents) {
      dbHelper.insertStudent(student)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_add_new -> {
        val intent = Intent(this, AddStudentActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_ADD)
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onCreateContextMenu(
    menu: ContextMenu,
    v: View,
    menuInfo: ContextMenu.ContextMenuInfo?
  ) {
    super.onCreateContextMenu(menu, v, menuInfo)
    menuInflater.inflate(R.menu.context_menu, menu)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == RESULT_OK && data != null) {
      when (requestCode) {
        REQUEST_CODE_ADD -> {
          val newStudent = data.getParcelableExtra<StudentModel>("student")
          if (newStudent != null) {
            dbHelper.insertStudent(newStudent)
            studentList.add(newStudent)
            adapter.notifyItemInserted(studentList.size - 1)
          }
        }
        REQUEST_CODE_EDIT -> {
          val editedStudent = data.getParcelableExtra<StudentModel>("student")
          val position = data.getIntExtra("position", -1)
          val oldStudentId = studentList[position].studentId

          if (position != -1 && editedStudent != null) {
            dbHelper.updateStudent(editedStudent, oldStudentId)
            studentList[position] = editedStudent
            adapter.notifyItemChanged(position)
          }
        }
      }
    }
  }

  override fun onContextItemSelected(item: MenuItem): Boolean {
    val position = adapter.contextMenuPosition
    when (item.itemId) {
      R.id.menu_edit -> {
        val intent = Intent(this, EditStudentActivity::class.java)
        intent.putExtra("student", studentList[position])
        intent.putExtra("position", position)
        startActivityForResult(intent, REQUEST_CODE_EDIT)
      }
      R.id.menu_remove -> {
        val studentId = studentList[position].studentId
        dbHelper.deleteStudent(studentId)
        studentList.removeAt(position)
        adapter.notifyItemRemoved(position)
      }
    }
    return super.onContextItemSelected(item)
  }

  companion object {
    const val REQUEST_CODE_ADD = 1001
    const val REQUEST_CODE_EDIT = 1002
  }
}