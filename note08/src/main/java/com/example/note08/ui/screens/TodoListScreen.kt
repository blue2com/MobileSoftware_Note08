package com.example.note08.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.note08.ui.viewmodel.TodoViewModel

@Composable
fun TodoListScreen(viewModel: TodoViewModel, navController: NavController) {
    val todos by viewModel.todoList.collectAsState()
    Scaffold(
        topBar = { TopBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addEdit") }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Todo"
                )
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(todos) { todo ->
                TodoItem(
                    todo = todo,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}