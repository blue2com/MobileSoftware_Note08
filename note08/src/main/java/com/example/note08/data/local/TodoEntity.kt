package com.example.note08.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val tag: String = "",
    val memo: String = "",
    val description: String = "",
    val isDone: Boolean = false
)