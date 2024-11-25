package com.example.firebase

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import java.lang.reflect.Modifier


@Composable
fun NewPlayerScreen(navController: NavController, model: GameModel){
    var playerName by remember { mutableStateOf("") }

    // ska lägga till och modifera column sen för styling

    Text("Enter Your name please!")

    OutlinedTextField(
        value = playerName,
        onValueChange = {playerName = it},
        label = {Text("Write name here")},
        //modifier = Modifier.fillMaxWidth() /// fyller hela skärmen

    )

    Button(
        onClick = {
            if(playerName.isNotBlank()){
                val newPlayerId = model.db.collection("players").document().id
                val newPlayer = Player(playerID = newPlayerId, name = playerName, status = "online")

            }

        }
    ) { }




}