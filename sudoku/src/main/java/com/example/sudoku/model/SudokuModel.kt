package com.example.sudoku.model

enum class Difficulty(val label: String, val cluesCount: Int) {
    EASY("쉬움", 42),
    MEDIUM("보통", 34),
    HARD("어려움", 26)
}

data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int = 0,             // 0 = 빈칸, 1~9 = 입력된 값
    val solutionValue: Int = 0,     // 정답 값
    val isGiven: Boolean = false,   // 초기 고정 문제 숫자 여부
    val isError: Boolean = false    // 잘못 입력된 숫자 (빨간색 표시)
)
