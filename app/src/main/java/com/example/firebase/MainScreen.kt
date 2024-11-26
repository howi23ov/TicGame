package com.example.firebase

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun MainScreen(navController: NavController, model: GameModel, gameId: String?) {
    val db = Firebase.firestore
    val gameState = remember { mutableStateOf<Game?>(null) }
    val winnerOfGame = remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(gameId) {
        if (gameId != null) {
            db.collection("games").document(gameId)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val game = snapshot.toObject(Game::class.java)
                        gameState.value = game

                        if (game?.gameState == "finished" && winnerOfGame.value == null) {
                            winnerOfGame.value = checkWinner(game.gameBoard)
                        }
                    }
                }
        }
    }

    val game = gameState.value

    winnerOfGame.value?.let { winner ->
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("Finnaly we have a winner!") },
            text = {
                Text(
                    text = if (winner == 1) "Player X has won this round!" else "Player O has won this round!"
                )
            },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("LobbyScreen")
                }) {
                    Text("Back to Lobby")
                }
            }
        )
    }

    if (game != null && gameId != null) {
        TicTacToeBoard(
            game = game,
            onTileClick = { index ->
                if (game.gameBoard[index] == 0 && game.currentPlayer == model.localPlayerId.value) {
                    val updatedBoard = game.gameBoard.toMutableList()
                    updatedBoard[index] = if (game.currentPlayer == game.player1Id) 1 else 2

                    val nextPlayer = if (game.currentPlayer == game.player1Id) game.player2Id else game.player1Id

                    db.collection("games").document(gameId).update(
                        mapOf(
                            "gameBoard" to updatedBoard,
                            "currentPlayer" to nextPlayer
                        )
                    )

                    val winner = checkWinner(updatedBoard)
                    if (winner != null) {
                        db.collection("games").document(gameId).update(
                            mapOf(
                                "gameState" to "finished",
                                "winner" to winner
                            )
                        )
                        winnerOfGame.value = winner
                    }
                }
            }
        )
    } else {
        Text("Loading game...")
    }
}


