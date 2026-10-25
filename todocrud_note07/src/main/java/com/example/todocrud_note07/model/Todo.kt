package com.example.todocrud_note07.model

data class Todo(
    val id: Int,        // 고유 ID
    val title: String,  // 제목
    val isDone: Boolean = false // 완료 여부
)
