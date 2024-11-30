package com.example.firebase

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


@Composable
fun MainScreen(navController: NavController, model: GameModel, gameId: String?) {
    val db = Firebase.firestore
    val gameState = remember { mutableStateOf<Game?>(null) }
    val winnerOfGame = remember { mutableStateOf<String?>(null) }
    val game = gameState.value

    LaunchedEffect(gameId) {
        if (gameId != null) {
            db.collection("games").document(gameId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {

                        println("Error fetching game: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val gameSnap = snapshot.toObject(Game::class.java)
                        gameState.value = gameSnap

                        if (gameSnap != null && gameSnap.gameState == "finished" && winnerOfGame.value == null) {
                            winnerOfGame.value = gameSnap.winner
                        }
                    } else {

                        println("game doesn't exist or has been removed.")
                        navController.navigate("LobbyScreen")
                    }
                }
        }
    }

    if (game != null && game.gameState == "pending" && gameId != null) {
        val currentPlayerId = model.localPlayerId.value
        currentPlayerId?.let { playerId ->
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Ready to play!") },
                text = { Text("press ready to start the game.") },
                confirmButton = {
                    Button(onClick = {
                        val readyField =
                            if (playerId == game.player1Id) "player1ReadyOrNot" else "player2ReadyOrNot"
                        db.collection("games").document(gameId).update(readyField, true)
                    }) {
                        Text("i am ready ")
                    }
                },
                dismissButton = {}
            )
        }
    }

    LaunchedEffect(game) {
        if (game != null && game.player1ReadyOrNot && game.player2ReadyOrNot && game.gameState == "pending") {
            db.collection("games").document(gameId!!).update("gameState", "ongoing")
        }
    }

    val playerMap by model.playerMap.collectAsStateWithLifecycle()

    if (game != null && game.gameState == "ongoing" && gameId != null) {
        TicTacToeBoard(
            game = game,
            playerMap = playerMap,
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

                    val winnerNumber = checkWinner(updatedBoard)
                    if (winnerNumber != null) {

                        val winnerName = if (winnerNumber == 1) {
                            playerMap[game.player1Id]?.name ?: "Unknown player"
                        } else {
                            playerMap[game.player2Id]?.name ?: "Unknown player"
                        }

                        db.collection("games").document(gameId).update(
                            mapOf(
                                "gameState" to "finished",
                                "winner" to winnerName
                            )
                        )
                        winnerOfGame.value = winnerName
                    }
                }
            }
        )
    }

    if (winnerOfGame.value != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("We have a winner") },
            text = {
                Text(
                    text = "${winnerOfGame.value} is Victorious!"
                )
            },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("LobbyScreen")
                }) {
                    Text("Back to Lobby it is then")
                }
            }
        )
    }

    if (game == null) {
        Text("Loading game...")
    }
}

