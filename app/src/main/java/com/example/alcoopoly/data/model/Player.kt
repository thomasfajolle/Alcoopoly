package com.example.alcoopoly.data.model

data class Player(
    val id: Int,
    val name: String,
    val color: Int,

    var position: Int = 1,
    var inPrison: Boolean = false,
    var prisonTurns: Int = 0,

    val ownedCases: MutableList<Int> = mutableListOf(),

    var drinksTaken: Int = 0,
    var drinksGiven: Int = 0
)
