package com.example.sudoku.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudoku.model.Difficulty
import com.example.sudoku.model.SudokuCell
import com.example.sudoku.util.SudokuGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SudokuUiState(
    val difficulty: Difficulty = Difficulty.EASY,
    val board: List<SudokuCell> = emptyList(),
    val selectedCell: Pair<Int, Int>? = null,
    val isCompleted: Boolean = false,
    val timerSeconds: Int = 0
)

class SudokuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startNewGame(Difficulty.EASY)
    }

    fun startNewGame(difficulty: Difficulty = _uiState.value.difficulty) {
        val newBoard = SudokuGenerator.generate(difficulty)
        _uiState.value = SudokuUiState(
            difficulty = difficulty,
            board = newBoard,
            selectedCell = null,
            isCompleted = false,
            timerSeconds = 0
        )
        startTimer()
    }

    fun resetCurrentGame() {
        val currentBoard = _uiState.value.board.map { cell ->
            if (cell.isGiven) {
                cell
            } else {
                cell.copy(value = 0, isError = false)
            }
        }
        _uiState.value = _uiState.value.copy(
            board = currentBoard,
            selectedCell = null,
            isCompleted = false,
            timerSeconds = 0
        )
        startTimer()
    }

    fun selectCell(row: Int, col: Int) {
        if (_uiState.value.isCompleted) return
        _uiState.value = _uiState.value.copy(selectedCell = Pair(row, col))
    }

    fun inputNumber(number: Int) {
        val (row, col) = _uiState.value.selectedCell ?: return
        val currentBoard = _uiState.value.board.toMutableList()
        val index = row * 9 + col
        val targetCell = currentBoard[index]

        // 초기 고정 숫자는 수정 불가
        if (targetCell.isGiven || _uiState.value.isCompleted) return

        // 틀린 숫자는 isError = true
        val isError = (number != targetCell.solutionValue)

        currentBoard[index] = targetCell.copy(
            value = number,
            isError = isError
        )

        val completed = checkIsCompleted(currentBoard)
        if (completed) {
            timerJob?.cancel()
        }

        _uiState.value = _uiState.value.copy(
            board = currentBoard,
            isCompleted = completed
        )
    }

    fun clearCell() {
        val (row, col) = _uiState.value.selectedCell ?: return
        val currentBoard = _uiState.value.board.toMutableList()
        val index = row * 9 + col
        val targetCell = currentBoard[index]

        if (targetCell.isGiven || _uiState.value.isCompleted) return

        currentBoard[index] = targetCell.copy(
            value = 0,
            isError = false
        )

        _uiState.value = _uiState.value.copy(board = currentBoard)
    }

    private fun checkIsCompleted(board: List<SudokuCell>): Boolean {
        return board.all { cell ->
            cell.value != 0 && !cell.isError && cell.value == cell.solutionValue
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    timerSeconds = _uiState.value.timerSeconds + 1
                )
            }
        }
    }
}
