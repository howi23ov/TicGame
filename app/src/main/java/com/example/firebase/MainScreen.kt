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

    // Lyssna på spelet från Firestore
    LaunchedEffect(gameId) {
        if (gameId != null) {
            db.collection("games").document(gameId)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val game = snapshot.toObject(Game::class.java)
                        gameState.value = game

                        if (game != null && game.gameState == "finished" && winnerOfGame.value == null) {
                            winnerOfGame.value = game.winner
                        }
                    }
                }
        }
    }

    val game = gameState.value

    if (game != null && game.gameState == "pending" && gameId != null) {
        val currentPlayerId = model.localPlayerId.value
        currentPlayerId?.let { playerId ->
            AlertDialog(
                onDismissRequest = {  },
                title = { Text("Get Ready!") },
                text = { Text("Click 'I'm Ready' to start the game.") },
                confirmButton = {
                    Button(onClick = {
                        val readyField =
                            if (playerId == game.player1Id) "player1ReadyOrNot" else "player2ReadyOrNot"
                        db.collection("games").document(gameId).update(readyField, true)
                    }) {
                        Text("I'm Ready")
                    }
                },
                dismissButton = {}
            )
        }
    }

    // det här Startar spelet när båda spelarna är redo
    LaunchedEffect(game) {
        if (game != null && game.player1ReadyOrNot && game.player2ReadyOrNot && game.gameState == "pending") {
            db.collection("games").document(gameId!!).update("gameState", "ongoing")
        }
    }

    if (game != null && game.gameState == "ongoing" && gameId != null) {
        TicTacToeBoard(
            game = game,
            onTileClick = { index ->
                if (game.gameBoard[index] == 0 && game.currentPlayer == model.localPlayerId.value) {
                    val updatedBoard = game.gameBoard.toMutableList()
                    updatedBoard[index] = if (game.currentPlayer == game.player1Id) 1 else 2

                    val nextPlayer =
                        if (game.currentPlayer == game.player1Id) game.player2Id else game.player1Id

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
    }

    winnerOfGame.value?.let { winner ->
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("We have a winner!") },
            text = {
                Text(
                    text = if (winner == 1) "Player X wins!" else "Player O wins!"
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

    if (game == null) {
        Text("Loading game...")
    }
}


