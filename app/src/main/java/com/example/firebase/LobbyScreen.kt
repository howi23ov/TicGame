
package com.example.firebase

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


@Composable
fun LobbyScreen(navController: NavHostController, model: GameModel) {
    val db = Firebase.firestore
    val playerList = remember { MutableStateFlow<List<Player>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        val currentPlayerId = model.localPlayerId.value
        model.listenForChallenges()
        if (currentPlayerId != null) {
            db.collection("games")
                .whereEqualTo("player1Id", currentPlayerId)
                .addSnapshotListener { value, error ->
                    if (error == null && value != null) {
                        for (doc in value.documents) {
                            val game = doc.toObject(Game::class.java)
                            if (game?.gameState == "declined") {
                                db.collection("games").document(doc.id).delete()
                                navController.navigate("LobbyScreen")
                            }
                        }
                    }
                }
        }

        db.collection("players").addSnapshotListener { value, error ->
            if (error == null && value != null) {
                val players = value.toObjects(Player::class.java)
                coroutineScope.launch {
                    playerList.emit(players)
                }
            }
        }

    }

    val players by playerList.collectAsStateWithLifecycle()
    val challenge = model.incomingChallenge.value

    val playerMap by model.playerMap.collectAsStateWithLifecycle()
    val challengersId = model.incomingChallenge.value?.player1Id
    val challengersName: String

    if (challengersId != null) {
        val player = playerMap[challengersId]
        if (player != null) {
            challengersName = player.name
        } else {
            challengersName = "Unknown Player"
        }
    } else {
        challengersName = "No Challenge"
    }

    // ________________
    if (challenge != null) {
        AlertDialog(
            onDismissRequest = { model.incomingChallenge.value = null },
            title = { Text("You have been challenged!") },
            text = { Text("Player ${challengersName} has challenged you to a game.") },
            confirmButton = {
                Button(onClick = {
                    db.collection("games").document(challenge.gameId).update(
                        mapOf(
                            "gameState" to "pending",
                            "currentPlayer" to challenge.player1Id
                        )
                    ).addOnSuccessListener {
                        navController.navigate("MainScreen/${challenge.gameId}")
                        model.incomingChallenge.value = null
                    }.addOnFailureListener { e ->
                        Log.e("LobbyScreen", "there was an issue to update game state: ${challenge.gameId}", e)
                    }
                }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(onClick = {
                    val gameId = challenge.gameId
                    if (!gameId.isNullOrEmpty()) {
                        db.collection("games").document(gameId).update("gameState", "declined")
                            .addOnSuccessListener {
                                model.incomingChallenge.value = null
                            }
                            .addOnFailureListener { e ->
                                Log.e("LobbyScreen", "there was an issue to update game state to declined: $gameId", e)
                                model.incomingChallenge.value = null
                            }
                    } else {
                        model.incomingChallenge.value = null
                    }
                }) {
                    Text("Decline")
                }
            }
        )
    }


    LaunchedEffect(model.localPlayerId.value) {
        model.incomingChallenge.value = null
    }


    val playerName: String = playerMap[model.localPlayerId.value]?.name ?: "Unknown Player"
    val filteredPlayers = players.filter { it.playerID != model.localPlayerId.value } //

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Green)
                    .padding(11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Connect Four - $playerName",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    ) { innerPadding ->
        if (filteredPlayers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "there are no players available sadly. you have to wait for others to join.",
                    modifier = Modifier.padding(22.dp))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(filteredPlayers) { player ->
                    ListItem(
                        headlineContent = {
                            Text("Name: ${player.name}")
                        },
                        supportingContent = {
                            Text("Status: ${player.status}")
                        },
                        trailingContent = {
                            Row {
                                Button(
                                    onClick = {
                                        val currentPlayerId = model.localPlayerId.value
                                        if (currentPlayerId != null) {
                                            val newGame = Game(
                                                player1Id = currentPlayerId,
                                                player2Id = player.playerID,
                                                gameState = "pending"
                                            )

                                            db.collection("games")
                                                .add(newGame)
                                                .addOnSuccessListener { documentReference ->
                                                    navController.navigate("MainScreen/${documentReference.id}")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("LobbyScreen", "Error creating game", e)
                                                }
                                        }
                                    }
                                ) {
                                    Text("Challenge")
                                }

                                // ________ detta är en delete knapp för att snabbare kunna ta bort alla spelare i firebase som skapas när jag gör många tester
                                // såklart inte optimalt att ha i en riktig app så kan tas bort eller kommentereas ut
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        db.collection("players").document(player.playerID)
                                            .delete()
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "LobbyScreen",
                                                    "Player ${player.name} deleted successfully."
                                                )
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(
                                                    "LobbyScreen",
                                                    "Error deleting player: ${player.name}",
                                                    e
                                                )
                                            }
                                    }
                                ) {
                                    Text("Delete")
                                }
                                // ________   kommentera ut bort hit
                            }
                        }
                    )
                }
            }

        }

    }

}