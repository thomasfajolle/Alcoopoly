package com.example.alcoopoly.model

import com.example.alcoopoly.data.enums.CardType // Si tu as un enum, sinon String

data class Card(
    val id: Int,
    val description: String,
    val type: CardType // CHANCE ou MINI_JEU
)