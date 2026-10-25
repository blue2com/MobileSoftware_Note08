package com.example.note08.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.note08.ui.screens.AddEditTodoScreen
import com.example.note08.ui.screens.SettingsScreen
import com.example.note08.ui.screens.TodoListScreen
import com.example.note08.ui.viewmodel.SettingsViewModel
import com.example.note08.ui.viewmodel.TodoViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val todoViewModel: TodoViewModel = hiltViewModel()
    
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            TodoListScreen(todoViewModel, navController)
        }
        
        composable("addEdit?id={id}") { backStackEntry ->
            AddEditTodoScreen(todoViewModel, navController, backStackEntry)
        }
        
        composable("addEdit") { backStackEntry ->
            AddEditTodoScreen(todoViewModel, navController, backStackEntry)
        }
        
        composable("settings") {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(vm = settingsViewModel, onBack = { navController.popBackStack() })
        }
    }
}