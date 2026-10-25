package com.example.sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import com.example.sudoku.model.Difficulty
import com.example.sudoku.model.SudokuCell

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    onBackClick: () -> Unit = {},
    viewModel: SudokuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "스도쿠 (Sudoku)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTime(uiState.timerSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. 난이도 선택 & 새 게임 버튼
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Difficulty.entries.forEach { difficulty ->
                        FilterChip(
                            selected = uiState.difficulty == difficulty,
                            onClick = { viewModel.startNewGame(difficulty) },
                            label = { Text(difficulty.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { viewModel.startNewGame() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Game",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("새 게임")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.resetCurrentGame() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("다시 하기")
                    }
                }
            }

            // 2. 9x9 스도쿠 보드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(2.dp)
            ) {
                SudokuGrid(
                    board = uiState.board,
                    selectedCell = uiState.selectedCell,
                    onCellClick = { r, c -> viewModel.selectCell(r, c) }
                )
            }

            // 3. 넘버패드 및 입력 액션
            NumberPad(
                onNumberClick = { num -> viewModel.inputNumber(num) },
                onClearClick = { viewModel.clearCell() }
            )
        }

        // 4. 성공 다이얼로그
        if (uiState.isCompleted) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("🎉 스도쿠 완성!") },
                text = { Text("축하합니다! ${formatTime(uiState.timerSeconds)} 만에 스도쿠를 모두 맞추셨습니다.") },
                confirmButton = {
                    Button(onClick = { viewModel.startNewGame() }) {
                        Text("새 게임 시작")
                    }
                }
            )
        }
    }
}

@Composable
fun SudokuGrid(
    board: List<SudokuCell>,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit
) {
    val selectedValue = selectedCell?.let { (r, c) ->
        val idx = r * 9 + c
        if (idx in board.indices) board[idx].value else 0
    } ?: 0

    Column(modifier = Modifier.fillMaxSize()) {
        for (r in 0 until 9) {
            val topBorderThickness = if (r % 3 == 0 && r != 0) 2.dp else 0.5.dp
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (c in 0 until 9) {
                    val leftBorderThickness = if (c % 3 == 0 && c != 0) 2.dp else 0.5.dp
                    val cell = if (r * 9 + c < board.size) board[r * 9 + c] else SudokuCell(r, c)

                    val isSelected = selectedCell?.first == r && selectedCell.second == c
                    val isSameRowOrCol = selectedCell?.let { it.first == r || it.second == c } ?: false
                    val isSameBox = selectedCell?.let { (sr, sc) -> (r / 3 == sr / 3) && (c / 3 == sc / 3) } ?: false
                    val isSameValue = cell.value != 0 && cell.value == selectedValue

                    val backgroundColor = when {
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        isSameValue -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        isSameRowOrCol || isSameBox -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    // 텍스트 색상 지정:
                    // 1) 틀린 숫자 -> 빨간색 (Color(0xFFD32F2F))
                    // 2) 문제 숫자 -> 기본 테마 진한 색상
                    // 3) 사용자 입력 맞는 숫자 -> 파란색 (Color(0xFF1976D2))
                    val textColor = when {
                        cell.isError -> Color(0xFFD32F2F)
                        cell.isGiven -> MaterialTheme.colorScheme.onSurface
                        else -> Color(0xFF1976D2)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(
                                start = leftBorderThickness,
                                top = topBorderThickness
                            )
                            .background(backgroundColor)
                            .clickable { onCellClick(r, c) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell.value != 0) {
                            Text(
                                text = cell.value.toString(),
                                fontSize = 20.sp,
                                fontWeight = if (cell.isGiven || cell.isError) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (num in 1..9) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clickable { onNumberClick(num) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onClearClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("선택한 칸 지우기", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(java.util.Locale.getDefault(), "⏱ %02d:%02d", m, s)
}
