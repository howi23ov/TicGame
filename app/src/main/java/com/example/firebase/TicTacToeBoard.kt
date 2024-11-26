package com.example.firebase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun TicTacToeBoard(game: Game, onTileClick: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Current Player is: ${if (game.currentPlayer == game.player1Id) "X" else "O"}"
        )
        for (row in 0..2) {
            Row(modifier = Modifier.padding(12.dp)) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    Box(modifier = Modifier
                            .size(110.dp)
                            .padding(5.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { onTileClick(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        val text = when (game.gameBoard[index]) {
                            1 -> "X"
                            2 -> "O"
                            else -> ""
                        }
                        Text(text = text)
                    }
                }
            }
        }
    }
}

