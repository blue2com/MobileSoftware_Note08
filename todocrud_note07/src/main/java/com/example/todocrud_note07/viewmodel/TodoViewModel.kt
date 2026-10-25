package com.example.todocrud_note07.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.todocrud_note07.model.Todo

class TodoViewModel : ViewModel() {
    private var nextId = 0
    var todoList = mutableStateListOf<Todo>()
        private set

    fun addTodo(title: String) {
        todoList.add(Todo(id = nextId++, title = title))
    }

    fun updateTodo(id: Int, title: String) {
        val index = todoList.indexOfFirst { it.id == id }
        if (index != -1) {
            todoList[index] = todoList[index].copy(title = title)
        }
    }

    fun deleteTodo(id: Int) {
        todoList.removeAll { it.id == id }
    }
}
