package com.example.firebase

fun checkWinner(board: List<Int>): Int? {
    val winningCombinations = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )
    for (combo in winningCombinations) {
        val (a, b, c) = combo
        if (board[a] != 0 && board[a] == board[b] && board[b] == board[c]) {
            return board[a]
        }
    }
    return null
}
