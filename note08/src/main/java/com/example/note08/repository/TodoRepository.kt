package com.example.note08.repository

import com.example.note08.data.local.TodoDao
import com.example.note08.data.local.TodoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(private val dao: TodoDao) {
    val allTodos = dao.getAll()

    suspend fun insert(todo: TodoEntity) = dao.insert(todo)
    suspend fun update(todo: TodoEntity) = dao.update(todo)
    suspend fun delete(todo: TodoEntity) = dao.delete(todo)
}