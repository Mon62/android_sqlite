package com.example.mystudent

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "StudentDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_STUDENTS = "students"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_STUDENT_ID = "student_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_STUDENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_STUDENT_ID TEXT NOT NULL UNIQUE
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        onCreate(db)
    }

    fun insertStudent(student: StudentModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, student.studentName)
            put(COLUMN_STUDENT_ID, student.studentId)
        }
        return db.insert(TABLE_STUDENTS, null, values)
    }

    fun getAllStudents(): MutableList<StudentModel> {
        val studentList = mutableListOf<StudentModel>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_NAME ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val studentId = getString(getColumnIndexOrThrow(COLUMN_STUDENT_ID))
                studentList.add(StudentModel(name, studentId))
            }
        }
        cursor.close()
        return studentList
    }

    fun updateStudent(student: StudentModel, oldStudentId: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, student.studentName)
            put(COLUMN_STUDENT_ID, student.studentId)
        }
        return db.update(
            TABLE_STUDENTS,
            values,
            "$COLUMN_STUDENT_ID = ?",
            arrayOf(oldStudentId)
        )
    }

    fun deleteStudent(studentId: String): Int {
        val db = this.writableDatabase
        return db.delete(
            TABLE_STUDENTS,
            "$COLUMN_STUDENT_ID = ?",
            arrayOf(studentId)
        )
    }
}