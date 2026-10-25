package com.example.sudoku.tictactoe

enum class Player(val symbol: String, val label: String) {
    X("❌", "X"),
    O("⭕", "O");

    fun next(): Player = if (this == X) O else X
}

enum class GameMode(val label: String) {
    PVP("2인 플레이"),
    VS_AI("컴퓨터(AI) 대결")
}

data class TicTacToeState(
    val board: List<List<Player?>> = List(3) { List(3) { null } },
    val currentPlayer: Player = Player.X,
    val gameMode: GameMode = GameMode.PVP,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val winningLine: List<Pair<Int, Int>>? = null,
    val xWins: Int = 0,
    val oWins: Int = 0,
    val draws: Int = 0
)
