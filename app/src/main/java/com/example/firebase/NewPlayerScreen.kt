package com.example.firebase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier



@Composable
fun NewPlayerScreen(navController: NavController, model: GameModel){
    var playerName by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) { 
        Text("Enter Your name please!")

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Write name here") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (playerName.isNotBlank()) {
                    val newPlayerId = model.db.collection("players").document().id
                    val newPlayer = Player(playerID = newPlayerId, name = playerName, status = "online")

                    model.db.collection("players").document(newPlayerId).set(newPlayer)
                        .addOnSuccessListener {
                            model.localPlayerId.value = newPlayerId
                            navController.navigate("LobbyScreen")
                        }
                }
            }
        ) {
            Text("Join Game")
        }
    }


}


