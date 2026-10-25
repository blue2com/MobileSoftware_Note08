package com.example.todocrud_note07.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todocrud_note07.screen.AddEditTodoScreen
import com.example.todocrud_note07.screen.TodoListScreen
import com.example.todocrud_note07.viewmodel.TodoViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: TodoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            TodoListScreen(viewModel, navController)
        }
        composable("addEdit?id={id}") { backStackEntry ->
            AddEditTodoScreen(viewModel, navController, backStackEntry)
        }
        composable("addEdit") { backStackEntry ->
            AddEditTodoScreen(viewModel, navController, backStackEntry)
        }
    }
}
