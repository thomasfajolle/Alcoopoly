package com.example.alcoopoly.model

import com.example.alcoopoly.data.enums.CardType

data class Card(
    val id: Int,
    val description: String,
    val type: CardType,
    val isActive: Boolean = true // <--- NOUVEAU (Par défaut, la carte est active)
)