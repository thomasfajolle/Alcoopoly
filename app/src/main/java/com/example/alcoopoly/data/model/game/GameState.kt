package com.example.alcoopoly.model.game

import com.example.alcoopoly.model.BoardCase
import com.example.alcoopoly.model.Card
import com.example.alcoopoly.model.Player

/**
 * Contient tout l'état de la partie.
 */
data class GameState(
    // Données générales
    val players: List<Player> = emptyList(),
    val board: List<BoardCase> = emptyList(),
    val chanceCards: List<Card> = emptyList(),
    val miniGameCards: List<Card> = emptyList(),
    val pendingRent: Int = 0,           // Combien on doit boire ou donner
    val isResolvingStartPass: Boolean = false, // Est-ce qu'on affiche le dialogue "Passage Départ" ?
    val isEscapingPrison: Boolean = false, // Pour gérer la suite après le lancer de prison ---
    val chanceCardsStack: MutableList<Card> = mutableListOf(),
    val miniGameCardsStack: MutableList<Card> = mutableListOf(),
    val currentCard: Card? = null,

    // État du tour courant
    val currentPlayerIndex: Int = 0,
    val turnState: TurnState = TurnState.START_TURN,

    // Dés de déplacement (2 dés)
    val diceResult: Int = 0,
    val isDoubles: Boolean = false,

    // LOGIQUE D'ACHAT (1 dé) ---
    val purchaseAttempts: Int = 0,      // Nombre d'essais effectués (0, 1 ou 2)
    val purchaseTarget: Int = 0,        // Score à battre (2, 3, 4 ou 5)
    val lastPurchaseRoll: Int = 0,      // Dernier résultat du dé d'achat
    val purchaseResult: String = "",    // "SUCCESS", "FAILED_RETRY", "FAILED_FINAL"

    // Message d'événement spécial ---
    val eventTitle: String = "",
    val eventMessage: String = "",

    // État global
    val bassineAmount: Int = 0,
    val turnNumber: Int = 1
) {
    val currentPlayer: Player
        get() = if (players.isNotEmpty()) players[currentPlayerIndex] else Player(0, "Err", 0)
}