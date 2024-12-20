
package com.example.firebase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun ConnectFourBoard(
    game: Game,
    playerMap: Map<String, Player>,
    onTileClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val player = playerMap[game.currentPlayer]
        val currentPlayerName = if (player != null) player.name else "Unknown Player"
        val currentDiscColor = if (game.currentPlayer == game.player1Id) "Red" else "Yellow"

        Text(
            text = "Current Player is: $currentDiscColor ($currentPlayerName)",
            modifier = Modifier.padding(bottom = 20.dp)
        )

        for (row in 0 until 6) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .padding(5.dp)
                            .clickable { onTileClick(col) }
                            .background(
                                color = when (game.gameBoard[index]) {
                                    1 -> Color.Red
                                    2 -> Color.Yellow
                                    else -> Color.Black
                                },
                                shape = CircleShape
                            )

                    )
                }
            }
        }
    }
}



/*
0  1  2  3  4  5  6
7  8  9 10 11 12 13
14 15 16 17 18 19 20
21 22 23 24 25 26 27
28 29 30 31 32 33 34
35 36 37 38 39 40 41
*/