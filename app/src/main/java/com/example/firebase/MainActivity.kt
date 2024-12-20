package com.example.firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebase.ui.theme.FirebaseTheme
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {            // compose-innehåll som visas i appen
            FirebaseTheme {     // ett temainställningspaket som definerar utseendet på appen
                ConnectFourBoard() // huvudfunktion, sätter upp navigeringslogiken
            }
        }
    }
}

@Composable         // hanterar spelets struktur och navigering
fun ConnectFourBoard() {
    val navController = rememberNavController() // håller reda på navigering
    val gameModel = remember { GameModel() }       // skapar kommer ihåg instans av game model.

    LaunchedEffect(Unit) {
        gameModel.initGame()
    }


    //    navController = navController, kopplar en NavController till denna navHost
    NavHost(navController = navController, startDestination = "NewPlayerScreen") {

        composable("NewPlayerScreen") {
            NewPlayerScreen(navController, gameModel)
        }

        composable("MainScreen/{gameId}") { navigationDestination ->
            val gameId = navigationDestination.arguments?.getString("gameId")
            MainScreen(navController, gameModel, gameId)
        }

        composable("LobbyScreen") {
            LobbyScreen(navController, gameModel)
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

/*
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FirebaseTheme {
        ConnectFourBoard()
    }
}
*/