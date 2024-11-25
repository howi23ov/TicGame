package com.example.firebase

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow

class GameModel: ViewModel() {
    val db = Firebase.firestore                              // initzierar en referens till firestore-databasen
    var localPlayerId = mutableStateOf<String?>(null)  // håller reda på spelarens lokala ID som är asscocierat med den enhet som kör appen
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap()) // är en statedlow som håller en karta över spelare. nyckeln är är spelarens firestore dokument, intizieras till en tom karta med empty
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap()) // håller reda på spelarens data och är en liknande playerMap. datan lagras i firebasen unde games
    val incomingChallenge = mutableStateOf<Game?>(null)

    fun initGame() {              // lyssnar på uppdateringar från firestore och uppdaterar stateflow egenskaper

        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {             // om firestore lyssningen misslyckades
                    return@addSnapshotListener   // avslutar den aktuella exekveringen av addsnapshotlistener
                }

                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Player::class.java)!!
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
    fun listenForChallenges() {     //ZZZ
        db.collection("games")
            .whereEqualTo("player2Id", localPlayerId.value)
            .whereEqualTo("gameState", "pending")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    for (doc in value.documents) {
                        val game = doc.toObject(Game::class.java)
                        if (game != null) {
                            incomingChallenge.value = game
                        }
                    }
                }
            }
    }
}



data class Game(
    var gameId: String = "",    // lade till denna i test
    var gameState: String = "pending",
    var player1Id: String = "",
    var player2Id: String = "",
    var gameBoard: List<Int> = List(9) { 0 },
)
