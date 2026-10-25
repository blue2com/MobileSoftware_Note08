package com.example.note08.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.note08.data.local.TodoEntity
import com.example.note08.ui.viewmodel.TodoViewModel

@Composable
fun AddEditTodoScreen(
    viewModel: TodoViewModel,
    navController: NavController,
    backStackEntry: NavBackStackEntry
) {
    val todoId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
    val todoList by viewModel.todoList.collectAsState()
    val existingTodo = todoList.find { it.id == todoId }
    var text by remember { mutableStateOf(existingTodo?.title ?: "") }
    var tag by remember { mutableStateOf(existingTodo?.tag ?: "") }
    var memo by remember { mutableStateOf(existingTodo?.memo ?: "") }

    Scaffold(
        topBar = { TopBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = { text = it },
                label = { Text("할 일") })
            
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = tag,
                onValueChange = { tag = it },
                label = { Text("태그") })
            
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = memo,
                onValueChange = { memo = it },
                label = { Text("메모") })
                
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val newOrUpdatedTodo = TodoEntity(
                    id = todoId ?: 0,
                    title = text,
                    tag = tag,
                    memo = memo
                )

                Button(onClick = {
                    if (todoId != null) {
                        viewModel.updateTodo(newOrUpdatedTodo)
                    } else {
                        viewModel.addTodo(newOrUpdatedTodo)
                    }
                    navController.popBackStack()
                }) {
                    if (todoId != null) {
                        Text("수정")
                    } else {
                        Text("추가")
                    }
                }

                if (todoId != null) {
                    Button(onClick = {
                        viewModel.deleteTodo(newOrUpdatedTodo)
                        navController.popBackStack()
                    }) {
                        Text("삭제")
                    }
                }
            }
        }
    }
}