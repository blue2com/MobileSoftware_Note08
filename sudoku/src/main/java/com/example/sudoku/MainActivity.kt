package com.example.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.sudoku.tictactoe.TicTacToeScreen
import com.example.sudoku.ui.SudokuScreen
import com.example.sudoku.ui.home.HomeScreen
import com.example.sudoku.ui.navigation.Screen
import com.example.sudoku.ui.theme.MobileProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileProjectTheme {
                var currentScreen by remember { mutableStateOf(Screen.HOME) }

                BackHandler(enabled = currentScreen != Screen.HOME) {
                    currentScreen = Screen.HOME
                }

                when (currentScreen) {
                    Screen.HOME -> {
                        HomeScreen(
                            onNavigateToTicTacToe = { currentScreen = Screen.TIC_TAC_TOE },
                            onNavigateToSudoku = { currentScreen = Screen.SUDOKU }
                        )
                    }
                    Screen.TIC_TAC_TOE -> {
                        TicTacToeScreen(
                            onBackClick = { currentScreen = Screen.HOME }
                        )
                    }
                    Screen.SUDOKU -> {
                        SudokuScreen(
                            onBackClick = { currentScreen = Screen.HOME }
                        )
                    }
                }
            }
        }
    }
}
