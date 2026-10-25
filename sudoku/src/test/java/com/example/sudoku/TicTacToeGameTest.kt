package com.example.sudoku

import com.example.sudoku.tictactoe.GameMode
import com.example.sudoku.tictactoe.Player
import com.example.sudoku.tictactoe.TicTacToeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TicTacToeGameTest {

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
    fun testPvpWinConditionRow() {
        val viewModel = TicTacToeViewModel()
        viewModel.setGameMode(GameMode.PVP)

        // X: (0,0), O: (1,0)
        viewModel.onCellClick(0, 0)
        viewModel.onCellClick(1, 0)

        // X: (0,1), O: (1,1)
        viewModel.onCellClick(0, 1)
        viewModel.onCellClick(1, 1)

        // X: (0,2) -> X 가로 1줄 승리!
        viewModel.onCellClick(0, 2)

        val state = viewModel.uiState.value
        assertEquals(Player.X, state.winner)
        assertEquals(1, state.xWins)
        assertNotNull(state.winningLine)
        assertEquals(listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)), state.winningLine)
    }

    @Test
    fun testVsAiMode() {
        val viewModel = TicTacToeViewModel()
        viewModel.setGameMode(GameMode.VS_AI)

        // X가 (0,0)에 놓으면 AI(O)가 응답하여 한 수를 놓아야 함
        viewModel.onCellClick(0, 0)

        // 딜레이된 AI 코루틴 작업 완료 대기
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        // X(0,0) + AI(1개) = 총 2개의 기호가 보드에 존재해야 함
        val filledCount = state.board.flatten().count { it != null }
        assertEquals(2, filledCount)
        assertEquals(Player.X, state.currentPlayer) // AI가 둔 후 다시 X 차례
    }

    @Test
    fun testResetRoundAndScores() {
        val viewModel = TicTacToeViewModel()
        viewModel.setGameMode(GameMode.PVP)

        // X 승리 만들기
        viewModel.onCellClick(0, 0)
        viewModel.onCellClick(1, 0)
        viewModel.onCellClick(0, 1)
        viewModel.onCellClick(1, 1)
        viewModel.onCellClick(0, 2)

        assertEquals(1, viewModel.uiState.value.xWins)

        // 라운드 리셋 후 스코어 유지 확인
        viewModel.resetRound()
        assertNull(viewModel.uiState.value.winner)
        assertEquals(1, viewModel.uiState.value.xWins)

        // 스코어 리셋 후 0으로 초기화 확인
        viewModel.resetScores()
        assertEquals(0, viewModel.uiState.value.xWins)
    }
}
