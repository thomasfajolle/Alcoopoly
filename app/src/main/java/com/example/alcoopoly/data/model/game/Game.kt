package com.example.alcoopoly.game

/**
 * Représente l'état minimal du jeu nécessaire
 * pour gérer un tour.
 *
 * Cette classe sera enrichie progressivement
 * pour inclure joueurs, plateau, cartes, propriétés, etc.
 */
class Game {

    /** Index du joueur courant */
    var currentPlayerIndex: Int = 0

    /** Position actuelle du joueur courant */
    var currentPlayerPosition: Int = 0

    /** Indique si le joueur courant est en prison (bar'ban) */
    var isCurrentPlayerInPrison: Boolean = false

    /** Compteur de tentatives ratées en prison */
    var prisonFailedAttempts: Int = 0

    /** Machine à états du tour */
    val turnStateMachine: TurnStateMachine by lazy { TurnStateMachine(this) }
}
