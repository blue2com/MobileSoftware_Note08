package com.example.todocrud_note07.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.todocrud_note07.viewmodel.TodoViewModel

@Composable
fun AddEditTodoScreen(
    viewModel: TodoViewModel,
    navController: NavController,
    backStackEntry: NavBackStackEntry
) {
    val todoId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
    val existingTodo = viewModel.todoList.find { it.id == todoId }

    var text by remember { mutableStateOf(existingTodo?.title ?: "") }
    
    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = text,
            onValueChange = { text = it }, 
            label = { Text("할 일") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (todoId != null) {
                viewModel.updateTodo(todoId, text)
            } else {
                viewModel.addTodo(text)
            }
            navController.popBackStack()
        }) {
            Text("저장")
        }
        if (todoId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                viewModel.deleteTodo(todoId)
                navController.popBackStack()
            }) {
                Text("삭제")
            }
        }
    }
}
