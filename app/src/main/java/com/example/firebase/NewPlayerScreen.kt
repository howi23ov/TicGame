package com.example.firebase

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext



@Composable
fun NewPlayerScreen(navController: NavController, model: GameModel) {
    val sharedPreferences = LocalContext.current
        .getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        val savedPlayerId = sharedPreferences.getString("playerId", null)
        if (savedPlayerId != null) {
            model.db.collection("players").document(savedPlayerId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        model.localPlayerId.value = savedPlayerId
                        navController.navigate("LobbyScreen")
                    } else {

                        sharedPreferences.edit().remove("playerId").apply()
                    }
                }
                .addOnFailureListener {
                    println("there was an error fetching player: ${it.message}")
                }
        }
    }

    if (model.localPlayerId.value == null) {
        var playerName by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter Your name please!")

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Write name here") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (playerName.isNotBlank()) {
                        val newPlayerId = model.db.collection("players").document().id
                        val newPlayer = Player(
                            playerID = newPlayerId,
                            name = playerName,
                            status = "online"
                        )

                        model.db.collection("players").document(newPlayerId).set(newPlayer)
                            .addOnSuccessListener {
                                sharedPreferences.edit()
                                    .putString("playerId", newPlayerId)
                                    .apply()
                                model.localPlayerId.value = newPlayerId
                                navController.navigate("LobbyScreen")
                            }
                            .addOnFailureListener { error ->
                                println("Error creating player: ${error.message}")
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join Game")
            }

            Spacer(modifier = Modifier.height(19.dp))


        }
    } else {
        Text("Loading...", modifier = Modifier.fillMaxSize())
    }
}






