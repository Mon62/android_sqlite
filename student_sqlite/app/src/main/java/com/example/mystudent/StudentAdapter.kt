package com.example.mystudent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
  private val studentList: MutableList<StudentModel>,
  private val onItemClick: (View, Int) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

  var contextMenuPosition: Int = -1

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_student_item, parent, false)
    return StudentViewHolder(view)
  }

  override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
    val student = studentList[position]
    holder.name.text = student.studentName
    holder.studentId.text = student.studentId
    holder.itemView.setOnClickListener {
      onItemClick(it, position)
    }
  }

  override fun getItemCount() = studentList.size

  inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.text_student_name)
    val studentId: TextView = itemView.findViewById(R.id.text_student_id)
  }
}
