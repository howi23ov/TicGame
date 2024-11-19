package com.example.firebase

data class Player(
    var playerID: String = "",
    var name: String = "",
    var status: String = "",
    var score: Int = 0,

    )

fun addScore(score: Int){
    // detta görs sen
}

fun changeTurn(){
    //  currentPlayer = if (currentPlayer == player1) player2 else player1
}