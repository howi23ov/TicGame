package com.example.firebase

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
  //  var AreOnline = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        model.listenForChallenges()
        db.collection("players").addSnapshotListener { value, error ->
            if (error == null && value != null) {
                val players = value.toObjects(Player::class.java)
                coroutineScope.launch {
                    playerList.emit(players)
                   // AreOnline.value = players.all { it.status == "online" }
                }
            }
        }
    }

    val players by playerList.collectAsStateWithLifecycle() // kan va denna som är rr
    val challenge = model.incomingChallenge.value

    challenge?.let {
        AlertDialog(
            onDismissRequest = { model.incomingChallenge.value = null },
            title = { Text("You have been challenged!") },
            text = { Text("Player ${it.player1Id} has challenged you to a game.") },
            confirmButton = {
                Button(onClick = {
                    db.collection("games").document(it.gameId).update("gameState", "ongoing")
                    navController.navigate("MainScreen/${it.gameId}")
                    model.incomingChallenge.value = null
                }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(onClick = {
                    db.collection("games").document(it.gameId).delete()
                    model.incomingChallenge.value = null
                }) {
                    Text("Decline")
                }
            }
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(players) { player ->
                ListItem(
                    headlineContent = {
                        Text("Name: ${player.name}")
                    },
                    supportingContent = {
                        Text("Status: ${player.status}")
                    },
                    trailingContent = {
                        Button(onClick = {
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
                        }) {
                            Text("Challenge")
                        }
                    }
                )
            }
        }


    }
}