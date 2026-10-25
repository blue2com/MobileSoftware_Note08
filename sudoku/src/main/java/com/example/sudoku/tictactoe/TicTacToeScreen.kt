package com.example.sudoku.tictactoe

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(
    onBackClick: () -> Unit,
    viewModel: TicTacToeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "틱택토 (Tic-Tac-Toe)",
                        fontWeight = FontWeight.Bold
                    )
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. 모드 선택 (2인 플레이 / AI 대결)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GameMode.entries.forEach { mode ->
                    FilterChip(
                        selected = uiState.gameMode == mode,
                        onClick = { viewModel.setGameMode(mode) },
                        label = { Text(mode.label, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // 2. 스코어보드
            ScoreBoard(state = uiState)

            // 3. 게임 상태 안내 문구
            StatusBanner(state = uiState)

            // 4. 3x3 틱택토 보드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(4.dp)
            ) {
                TicTacToeGrid(
                    state = uiState,
                    onCellClick = { r, c -> viewModel.onCellClick(r, c) }
                )
            }

            // 5. 게임 컨트롤 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { viewModel.resetRound() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New Round",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("다음 라운드")
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(
                    onClick = { viewModel.resetScores() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("점수 초기화")
                }
            }
        }
    }
}

@Composable
fun ScoreBoard(state: TicTacToeState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreItem(
                label = if (state.gameMode == GameMode.VS_AI) "❌ 나" else "❌ 플레이어 1",
                score = state.xWins,
                color = Color(0xFFE53935)
            )
            ScoreItem(
                label = "🤝 무승부",
                score = state.draws,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ScoreItem(
                label = if (state.gameMode == GameMode.VS_AI) "⭕ 컴퓨터" else "⭕ 플레이어 2",
                score = state.oWins,
                color = Color(0xFF1E88E5)
            )
        }
    }
}

@Composable
fun ScoreItem(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBanner(state: TicTacToeState) {
    val message = when {
        state.winner != null -> "🎉 ${state.winner.symbol} 승리하였습니다!"
        state.isDraw -> "🤝 아쉽게도 무승부입니다!"
        else -> {
            if (state.gameMode == GameMode.VS_AI && state.currentPlayer == Player.O) {
                "⭕ 컴퓨터(AI)의 순서..."
            } else {
                "${state.currentPlayer.symbol} (${state.currentPlayer.label}) 차례입니다"
            }
        }
    }

    Text(
        text = message,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = if (state.winner != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun TicTacToeGrid(
    state: TicTacToeState,
    onCellClick: (Int, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        for (r in 0..2) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (c in 0..2) {
                    val player = state.board[r][c]
                    val isWinningCell = state.winningLine?.contains(Pair(r, c)) == true

                    val backgroundColor = if (isWinningCell) {
                        Color(0xFFFFD54F) // 승리 라인 강조 (Gold/Yellow)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(3.dp)
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isWinningCell) 3.dp else 1.dp,
                                color = if (isWinningCell) Color(0xFFFFA000) else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onCellClick(r, c) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (player != null) {
                            Text(
                                text = player.symbol,
                                fontSize = 48.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
