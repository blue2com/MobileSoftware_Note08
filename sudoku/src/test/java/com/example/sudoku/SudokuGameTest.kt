package com.example.sudoku

import com.example.sudoku.model.Difficulty
import com.example.sudoku.ui.SudokuViewModel
import com.example.sudoku.util.SudokuGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SudokuGameTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSudokuGeneratorCluesCount() {
        Difficulty.entries.forEach { difficulty ->
            val board = SudokuGenerator.generate(difficulty)
            assertEquals(81, board.size)

            val givenCount = board.count { it.isGiven }
            assertEquals("Check clues count for $difficulty", difficulty.cluesCount, givenCount)
        }
    }

    @Test
    fun testSudokuGeneratorSolutionValidity() {
        val board = SudokuGenerator.generate(Difficulty.EASY)

        // 모든 셀의 solutionValue가 1~9 범위인지 확인
        assertTrue(board.all { it.solutionValue in 1..9 })

        // 행/열/3x3 박스 내 정답 숫자가 유일한지 확인
        for (i in 0 until 9) {
            val rowValues = (0 until 9).map { c -> board[i * 9 + c].solutionValue }
            assertEquals(9, rowValues.distinct().size)

            val colValues = (0 until 9).map { r -> board[r * 9 + i].solutionValue }
            assertEquals(9, colValues.distinct().size)
        }
    }

    @Test
    fun testViewModelInputAndErrorDetection() {
        val viewModel = SudokuViewModel()
        viewModel.startNewGame(Difficulty.EASY)

        val initialBoard = viewModel.uiState.value.board
        // 비어있는(isGiven == false) 첫 번째 셀 찾기
        val emptyCell = initialBoard.firstOrNull { !it.isGiven }
        assertNotNull("비어있는 셀이 존재해야 함", emptyCell)

        emptyCell?.let { cell ->
            // 셀 선택
            viewModel.selectCell(cell.row, cell.col)

            // 1) 정답과 다른 잘못된 숫자 입력 -> isError = true
            val wrongNumber = if (cell.solutionValue == 1) 2 else 1
            viewModel.inputNumber(wrongNumber)

            val boardAfterWrong = viewModel.uiState.value.board
            val wrongCell = boardAfterWrong[cell.row * 9 + cell.col]
            assertEquals(wrongNumber, wrongCell.value)
            assertTrue("틀린 숫자는 isError == true 이어야 함", wrongCell.isError)

            // 2) 올바른 숫자 입력 -> isError = false
            viewModel.inputNumber(cell.solutionValue)

            val boardAfterCorrect = viewModel.uiState.value.board
            val correctCell = boardAfterCorrect[cell.row * 9 + cell.col]
            assertEquals(cell.solutionValue, correctCell.value)
            assertFalse("맞는 숫자는 isError == false 이어야 함", correctCell.isError)

            // 3) 셀 지우기
            viewModel.clearCell()
            val boardAfterClear = viewModel.uiState.value.board
            val clearedCell = boardAfterClear[cell.row * 9 + cell.col]
            assertEquals(0, clearedCell.value)
            assertFalse(clearedCell.isError)
        }
    }
}
