package com.example.alcoopoly.data.model

import com.example.alcoopoly.data.enums.GamePhase

data class GameState(
    val players: MutableList<Player>,
    val board: List<BoardCase>,
    val families: List<Family>,
    val chanceCards: MutableList<Card>,
    val miniGameCards: MutableList<Card>,
    val bassine: Bassine,

    var currentPlayerIndex: Int = 0,
    var turnNumber: Int = 1,
    var gamePhase: GamePhase
)
