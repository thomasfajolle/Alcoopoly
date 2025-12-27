package com.example.alcoopoly.model

data class Player(
    val id: Int,
    val name: String,
    val color: Long,
    val avatar: String = "😊",

    // État du pion
    val position: Int = 0,
    val inPrison: Boolean = false,
    val prisonTurns: Int = 0,

    // --- NOUVEAUX CHAMPS AJOUTÉS POUR CORRIGER LES ERREURS ---

    // Liste des IDs des cases achetées par le joueur
    val ownedCases: List<Int> = emptyList(),

    // Stats de jeu
    val drinksTaken: Int = 0, // Gorgées bues
    val drinksGiven: Int = 0, // Gorgées données (loyers perçus)
    val money: Int = 0        // (Optionnel pour plus tard)
)