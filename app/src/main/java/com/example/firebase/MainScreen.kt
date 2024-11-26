package com.example.firebase

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
    val gameState = remember {mutableStateOf<Game?>(null)}

    LaunchedEffect(gameId) {
        if (gameId != null){
            db.collection("games").document(gameId)
                .addSnapshotListener {snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()  ){
                        gameState.value = snapshot.toObject(Game::class.java)
                    }
                }

        }
    }
    val game = gameState.value

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
                            "gameState", "finished"
                        )
                        navController.navigate("LobbyScreen")
                    }
                }
            }
        )
    } else {
        Text("Loading game...")
    }
}