package com.example.sudoku.util

import com.example.sudoku.model.Difficulty
import com.example.sudoku.model.SudokuCell
import kotlin.random.Random

object SudokuGenerator {

    /**
     * 난이도에 알맞은 새로운 스도쿠 보드(81개 셀 리스트)를 생성합니다.
     */
    fun generate(difficulty: Difficulty): List<SudokuCell> {
        val solutionBoard = Array(9) { IntArray(9) }
        fillBoard(solutionBoard)

        val puzzleBoard = Array(9) { IntArray(9) }
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                puzzleBoard[r][c] = solutionBoard[r][c]
            }
        }

        // 81개 칸 중 (81 - cluesCount) 개의 칸을 비우기(0으로 설정)
        val cellsToRemove = 81 - difficulty.cluesCount
        val positions = (0 until 81).shuffled(Random).take(cellsToRemove)

        for (pos in positions) {
            val r = pos / 9
            val c = pos % 9
            puzzleBoard[r][c] = 0
        }

        val cells = mutableListOf<SudokuCell>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val value = puzzleBoard[r][c]
                val solVal = solutionBoard[r][c]
                cells.add(
                    SudokuCell(
                        row = r,
                        col = c,
                        value = value,
                        solutionValue = solVal,
                        isGiven = value != 0,
                        isError = false
                    )
                )
            }
        }
        return cells
    }

    /**
     * 무작위 백트래킹으로 완전한 9x9 스도쿠 보드를 채웁니다.
     */
    private fun fillBoard(board: Array<IntArray>): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (board[row][col] == 0) {
                    val numbers = (1..9).shuffled(Random)
                    for (num in numbers) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num
                            if (fillBoard(board)) {
                                return true
                            }
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    /**
     * 해당 위치(row, col)에 num을 놓을 수 있는지 검사합니다.
     */
    private fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until 9) {
            if (board[row][i] == num) return false
            if (board[i][col] == num) return false
        }
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if (board[r][c] == num) return false
            }
        }
        return true
    }
}
