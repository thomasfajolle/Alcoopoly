package com.example.alcoopoly.model.game

enum class TurnState {
    START_TURN,           // Début du tour, reset des variables
    CHECK_PLAYER_STATUS,  // Vérifie si le joueur est en prison ou libre
    PRISON_TURN,          // Tour spécifique si en prison
    ROLL_DICE,            // Attente du lancer de dés
    MOVE_PLAYER,          // Animation/Déplacement du pion
    RESOLVE_CASE,         // Application des règles de la case (Loyer, Carte, etc.)
    PROPERTY_BUY_ACTION,  // (Optionnel) Le joueur peut acheter la case
    RENT_PAYMENT_ACTION, // Paiement du loyer / Distribution
    SPECIAL_EVENT_ACTION, // Affiche les messages Bassine, Prison, etc.
    CARD_DRAW_ACTION,      // Affichage d'une carte
    POST_CASE_ACTIONS,    // Fin d'action, attente de fin de tour
    END_TURN              // Changement de joueur
}