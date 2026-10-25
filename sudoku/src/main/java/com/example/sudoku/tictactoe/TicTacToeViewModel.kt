package com.example.sudoku.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class TicTacToeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TicTacToeState())
    val uiState: StateFlow<TicTacToeState> = _uiState.asStateFlow()

    fun setGameMode(mode: GameMode) {
        if (_uiState.value.gameMode == mode) return
        _uiState.value = _uiState.value.copy(gameMode = mode)
        resetRound()
    }

    fun onCellClick(row: Int, col: Int) {
        val state = _uiState.value

        // 이미 게임이 끝나거나 해당 칸에 이미 기호가 놓인 경우 무시
        if (state.winner != null || state.isDraw || state.board[row][col] != null) return

        // VS_AI 모드에서 AI 차례일 때는 사용자 터치 방지
        if (state.gameMode == GameMode.VS_AI && state.currentPlayer == Player.O) return

        makeMove(row, col)
    }

    private fun makeMove(row: Int, col: Int) {
        val state = _uiState.value
        val player = state.currentPlayer

        // 새로운 보드 상태 만들기
        val newBoard = state.board.mapIndexed { r, list ->
            list.mapIndexed { c, p ->
                if (r == row && c == col) player else p
            }
        }

        // 승리 검사
        val winLine = checkWinner(newBoard, player)
        if (winLine != null) {
            val newXWins = if (player == Player.X) state.xWins + 1 else state.xWins
            val newOWins = if (player == Player.O) state.oWins + 1 else state.oWins
            _uiState.value = state.copy(
                board = newBoard,
                winner = player,
                winningLine = winLine,
                xWins = newXWins,
                oWins = newOWins
            )
            return
        }

        // 무승부 검사
        val isFull = newBoard.all { rowList -> rowList.all { it != null } }
        if (isFull) {
            _uiState.value = state.copy(
                board = newBoard,
                isDraw = true,
                draws = state.draws + 1
            )
            return
        }

        // 다음 차례로 변경
        val nextPlayer = player.next()
        _uiState.value = state.copy(
            board = newBoard,
            currentPlayer = nextPlayer
        )

        // AI 차례일 경우 AI 이동 수행
        if (state.gameMode == GameMode.VS_AI && nextPlayer == Player.O) {
            viewModelScope.launch {
                delay(300L) // 실제 플레이어처럼 느껴지도록 살짝 딜레이
                executeAiMove(newBoard)
            }
        }
    }

    private fun executeAiMove(currentBoard: List<List<Player?>>) {
        val state = _uiState.value
        if (state.winner != null || state.isDraw) return

        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0..2) {
            for (c in 0..2) {
                if (currentBoard[r][c] == null) {
                    emptyCells.add(Pair(r, c))
                }
            }
        }
        if (emptyCells.isEmpty()) return

        // 1) AI(O)가 이번 수로 바로 이길 수 있는지 검사
        val aiWinMove = emptyCells.firstOrNull { (r, c) ->
            val testBoard = simulateMove(currentBoard, r, c, Player.O)
            checkWinner(testBoard, Player.O) != null
        }
        if (aiWinMove != null) {
            makeMove(aiWinMove.first, aiWinMove.second)
            return
        }

        // 2) 사용자(X)가 이기는 것을 차단해야 하는지 검사
        val blockUserMove = emptyCells.firstOrNull { (r, c) ->
            val testBoard = simulateMove(currentBoard, r, c, Player.X)
            checkWinner(testBoard, Player.X) != null
        }
        if (blockUserMove != null) {
            makeMove(blockUserMove.first, blockUserMove.second)
            return
        }

        // 3) 가운데 셀 (1, 1) 선점
        if (emptyCells.contains(Pair(1, 1))) {
            makeMove(1, 1)
            return
        }

        // 4) 무작위 칸 선택
        val randomMove = emptyCells.random(Random)
        makeMove(randomMove.first, randomMove.second)
    }

    private fun simulateMove(board: List<List<Player?>>, row: Int, col: Int, player: Player): List<List<Player?>> {
        return board.mapIndexed { r, list ->
            list.mapIndexed { c, p ->
                if (r == row && c == col) player else p
            }
        }
    }

    private fun checkWinner(board: List<List<Player?>>, player: Player): List<Pair<Int, Int>>? {
        // 가로 3줄
        for (r in 0..2) {
            if (board[r][0] == player && board[r][1] == player && board[r][2] == player) {
                return listOf(Pair(r, 0), Pair(r, 1), Pair(r, 2))
            }
        }
        // 세로 3줄
        for (c in 0..2) {
            if (board[0][c] == player && board[1][c] == player && board[2][c] == player) {
                return listOf(Pair(0, c), Pair(1, c), Pair(2, c))
            }
        }
        // 대각선 ↘
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) {
            return listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
        }
        // 대각선 ↙
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) {
            return listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
        }

        return null
    }

    fun resetRound() {
        val current = _uiState.value
        _uiState.value = current.copy(
            board = List(3) { List(3) { null } },
            currentPlayer = Player.X,
            winner = null,
            isDraw = false,
            winningLine = null
        )
    }

    fun resetScores() {
        _uiState.value = TicTacToeState(gameMode = _uiState.value.gameMode)
    }
}
