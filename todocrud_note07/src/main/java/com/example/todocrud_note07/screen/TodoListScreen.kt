package com.example.todocrud_note07.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.todocrud_note07.viewmodel.TodoViewModel

@Composable
fun TodoListScreen(viewModel: TodoViewModel, navController: NavController) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addEdit") }) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(viewModel.todoList) { todo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("addEdit?id=${todo.id}")
                        }
                        .padding(16.dp)
                ) {
                    Text(todo.title)
                }
            }
        }
    }
}
