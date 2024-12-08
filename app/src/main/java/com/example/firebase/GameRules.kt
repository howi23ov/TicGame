package com.example.firebase


fun checkWinner(board: List<Int>): Int? {
    val winningCombinations = listOf(


        // (|)
        listOf(0, 7, 14, 21), listOf(7, 14, 21, 28), listOf(14, 21, 28, 35),
        listOf(1, 8, 15, 22), listOf(8, 15, 22, 29), listOf(15, 22, 29, 36),
        listOf(2, 9, 16, 23), listOf(9, 16, 23, 30), listOf(16, 23, 30, 37),
        listOf(3, 10, 17, 24), listOf(10, 17, 24, 31), listOf(17, 24, 31, 38),
        listOf(4, 11, 18, 25), listOf(11, 18, 25, 32), listOf(18, 25, 32, 39),
        listOf(5, 12, 19, 26), listOf(12, 19, 26, 33), listOf(19, 26, 33, 40),
        listOf(6, 13, 20, 27), listOf(13, 20, 27, 34), listOf(20, 27, 34, 41),

        // (--)
        listOf(0, 1, 2, 3), listOf(1, 2, 3, 4), listOf(2, 3, 4, 5), listOf(3, 4, 5, 6),
        listOf(7, 8, 9, 10), listOf(8, 9, 10, 11), listOf(9, 10, 11, 12), listOf(10, 11, 12, 13),
        listOf(14, 15, 16, 17), listOf(15, 16, 17, 18), listOf(16, 17, 18, 19), listOf(17, 18, 19, 20),
        listOf(21, 22, 23, 24), listOf(22, 23, 24, 25), listOf(23, 24, 25, 26), listOf(24, 25, 26, 27),
        listOf(28, 29, 30, 31), listOf(29, 30, 31, 32), listOf(30, 31, 32, 33), listOf(31, 32, 33, 34),
        listOf(35, 36, 37, 38), listOf(36, 37, 38, 39), listOf(37, 38, 39, 40), listOf(38, 39, 40, 41),

        // (/)
        listOf(3, 9, 15, 21), listOf(4, 10, 16, 22), listOf(5, 11, 17, 23), listOf(6, 12, 18, 24),
        listOf(10, 16, 22, 28), listOf(11, 17, 23, 29), listOf(12, 18, 24, 30), listOf(13, 19, 25, 31),
        listOf(17, 23, 29, 35), listOf(18, 24, 30, 36), listOf(19, 25, 31, 37), listOf(20, 26, 32, 38),

        // (\) --------------------------------
        listOf(0, 8, 16, 24), listOf(1, 9, 17, 25), listOf(2, 10, 18, 26), listOf(3, 11, 19, 27),
        listOf(7, 15, 23, 31), listOf(8, 16, 24, 32), listOf(9, 17, 25, 33), listOf(10, 18, 26, 34),
        listOf(14, 22, 30, 38), listOf(15, 23, 31, 39), listOf(16, 24, 32, 40), listOf(17, 25, 33, 41),


        )

    for (combination in winningCombinations) {
        val (a, b, c, d) = combination
        if (board[a] != 0 && board[a] == board[b] && board[b] == board[c] && board[c] == board[d]) {
            return board[a]
        }
    }
    return null
}

// brädet är
/*
 0  1  2  3  4  5  6
 7  8  9 10 11 12 13
14 15 16 17 18 19 20
21 22 23 24 25 26 27
28 29 30 31 32 33 34
35 36 37 38 39 40 41
 */