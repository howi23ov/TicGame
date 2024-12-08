package com.example.firebase

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow


class GameModel: ViewModel() {
    val db = Firebase.firestore
    var localPlayerId = mutableStateOf<String?>(null)
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())
    val incomingChallenge = mutableStateOf<Game?>(null)

    fun initGame() {              //

        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }


                if (value != null) {
                    val updatedMap = value.documents.associate { associatePlayer ->
                        associatePlayer.id to associatePlayer.toObject(Player::class.java)!!
                    }
                    playerMap.value = updatedMap
                }
            }



        db.collection("games")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Game::class.java)!!
                    }
                    gameMap.value = updatedMap
                }
            }

    }


    fun listenForChallenges() {
        val currentPlayerId = localPlayerId.value
        if (currentPlayerId != null) {
            Firebase.firestore.collection("games")
                .whereEqualTo("player2Id", currentPlayerId)
                .whereEqualTo("gameState", "pending")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        for (doc in snapshot.documents) {
                            val game = doc.toObject(Game::class.java)
                            if (game != null && game.gameState == "pending") {
                                incomingChallenge.value = game.copy(gameId = doc.id)
                                break
                            }
                        }
                    }
                }
        }
    }


}

fun isBoardFull(board: List<Int>): Boolean {
    return board.none { it == 0 }
}




data class Game(
    var gameId: String = "",
    var gameState: String = "pending",
    var player1Id: String = "",
    var player2Id: String = "",
    var gameBoard: List<Int> = List(42) { 0 },
    var currentPlayer: String = "",
    var player1ReadyOrNot: Boolean = false,
    var player2ReadyOrNot: Boolean = false,
    var winner: String? = null,
)

