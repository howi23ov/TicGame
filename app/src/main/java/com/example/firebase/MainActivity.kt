package com.example.firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.firebase.ui.theme.FirebaseTheme
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirebaseTheme {
                LobbyScreen()

            }
        }
    }
}

@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(): State<T> {
    val state = remember { mutableStateOf(value) }
    LaunchedEffect(this) {
        collect { state.value = it }
    }
    return state
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FirebaseTheme {
        LobbyScreen()
    }
}

/*

Two players
The board is 6x7 fields, 6 rows and 7 columns
Two different discs (you can create your own design)
4 in a row/column/diagonal win
If the board fills up without any player having 4 in a row, it is a draw
Create a user-friendly adaptive interface, design the grid and the discs
Display the current game state, including player names, the board
If you want, you can work with animations, sounds, and transitions to enhance the user-experience

Implementation:
Create a player with a name
Connect to the server and join the lobby
Show a list of all players in the lobby
Challenge a player for a game
Get challenged for a game, show games
Accept or decline a game challenge
Whoever challenged the other player, takes the first turn later
Send player ready message, if both players are ready, start the game (can be automatic in this game if you like)
First player selects the first column (x, 0)
Coordinates are x: 0-6, 0x0 is at the top-left-corner
Check for winning condition
Player sends the turn to the other player
When the game has finished, show a result screen and return to the lobby afterwards

*/


