package com.example.alcoopoly.data.model

import com.example.alcoopoly.data.enums.CardType

data class Card(
    val id: Int,
    val title: String,
    val description: String,
    val type: CardType
)
