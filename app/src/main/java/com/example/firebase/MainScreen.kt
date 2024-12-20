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
    val currentGame = remember { mutableStateOf<Game?>(null) }
    val winnerOfGame = remember { mutableStateOf<String?>(null) }
    val onGoingGame = currentGame.value


    LaunchedEffect(gameId) {
        if (gameId != null) {
            db.collection("games")
                .document(gameId)
                .addSnapshotListener { listenCurrentGame, error ->
                    if (error != null) {
                        println("Error fetching game: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (listenCurrentGame != null && listenCurrentGame.exists()) {
                        val currentGameToObject = listenCurrentGame.toObject(Game::class.java)
                        currentGame.value = currentGameToObject

                        if (currentGameToObject != null) {
                            when (currentGameToObject.gameState) {
                                "cancelled" -> {
                                    println("game was cancelled.")
                                    model.incomingChallenge.value = null
                                    navController.navigate("LobbyScreen")
                                }
                                "finished" -> {
                                    winnerOfGame.value = currentGameToObject.winner
                                    model.incomingChallenge.value = null
                                }
                                "tie" -> {
                                    winnerOfGame.value = "It's a tie!"
                                    model.incomingChallenge.value = null
                                }
                            }
                        }
                    } else {
                        println("game doesn't exist or has been removed.")
                        model.incomingChallenge.value = null
                        navController.navigate("LobbyScreen")
                    }
                }
        }
    }


    if (onGoingGame != null && onGoingGame.gameState == "pending" && gameId != null) {
        val currentPlayerId = model.localPlayerId.value
        if (currentPlayerId != null) {
            AlertDialog(
                onDismissRequest = {  },
                title = { Text("Get Ready!") },
                text = { Text("Click I am Ready to start the game, or Cancel to go back to the lobby.") },
                confirmButton = {
                    Button(onClick = {
                        val readyField =
                            if (currentPlayerId == onGoingGame.player1Id) "player1ReadyOrNot" else "player2ReadyOrNot"
                        db.collection("games").document(gameId).update(readyField, true)
                    }) {
                        Text("i am Ready")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        db.collection("games").document(gameId).update("gameState", "cancelled")
                            .addOnSuccessListener {
                                navController.navigate("LobbyScreen")
                            }
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }


    LaunchedEffect(onGoingGame) {
        if (onGoingGame != null && onGoingGame.player1ReadyOrNot && onGoingGame.player2ReadyOrNot && onGoingGame.gameState == "pending") {
            db.collection("games").document(gameId!!).update("gameState", "ongoing")
        }
    }

    val playerMap by model.playerMap.collectAsStateWithLifecycle()


    if (onGoingGame != null && onGoingGame.gameState == "ongoing" && gameId != null) {
        ConnectFourBoard(
            game = onGoingGame,
            playerMap = playerMap,
            onTileClick = { col ->
                val rows = 6
                val columns = 7
                val board = onGoingGame.gameBoard.toMutableList()

                /*
               0  1  2  3  4  5  6
               7  8  9 10 11 12 13
              14 15 16 17 18 19 20
              21 22 23 24 25 26 27
              28 29 30 31 32 33 34
              35 36 37 38 39 40 41
              */

                var findFirstFreeIndexInColumn: Int? = null

                var row = rows - 1
                while (row >= 0){

                    val index = row * columns + col
                    if (board[index] == 0) {
                        findFirstFreeIndexInColumn = index
                        break
                    }
                    row--
                }

                if (findFirstFreeIndexInColumn != null && onGoingGame.currentPlayer == model.localPlayerId.value) {
                    board[findFirstFreeIndexInColumn] = if (onGoingGame.currentPlayer == onGoingGame.player1Id) 1 else 2

                    val nextPlayer =
                        if (onGoingGame.currentPlayer == onGoingGame.player1Id) onGoingGame.player2Id else onGoingGame.player1Id

                    db.collection("games").document(gameId).update(
                        mapOf(
                            "gameBoard" to board,
                            "currentPlayer" to nextPlayer
                        )
                    )

                    val winner = checkWinner(board)
                    if (winner != null) {
                        val winnerName = if (winner == 1) {
                            playerMap[onGoingGame.player1Id]?.name ?: "Unknown player"
                        } else {
                            playerMap[onGoingGame.player2Id]?.name ?: "Unknown player"
                        }

                        db.collection("games").document(gameId).update(
                            mapOf(
                                "gameState" to "finished",
                                "winner" to winnerName
                            )
                        )
                        winnerOfGame.value = winnerName
                    } else if (isBoardFull(board)) {
                        db.collection("games").document(gameId).update(
                            mapOf(
                                "gameState" to "tie"
                            )
                        )
                        winnerOfGame.value = "It's a tie!"
                    }
                }
            }
        )
    }

    if (winnerOfGame.value != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(
                if (winnerOfGame.value == "It's a tie!")
                    "Game Over" else "The Winner is") },
            text = {
                Text(
                    text = winnerOfGame.value!!
                )
            },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("LobbyScreen")
                }) {Text("back to lobby it is then")}
            }
        )
    }

    if (onGoingGame == null) {
        Text("Loading game...")
    }
}