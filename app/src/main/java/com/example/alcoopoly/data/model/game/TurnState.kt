package com.example.alcoopoly.game

/**
 * Enum représentant les différents états possibles
 * d'un tour de jeu.
 */
enum class TurnState {

    /** Initialisation du tour */
    START_TURN,

    /** Vérification du statut du joueur (libre ou prison) */
    CHECK_PLAYER_STATUS,

    /** Gestion d'un tour en prison (bar'ban) */
    PRISON_TURN,

    /** Lancer des dés pour le déplacement (2 dés) */
    ROLL_MOVE_DICE,

    /** Déplacement du joueur sur le plateau */
    MOVE_PLAYER,

    /** Résolution de la case d'arrivée */
    RESOLVE_CASE,

    /** Tentative d'achat d'une propriété (1 dé, 2 essais max) */
    PROPERTY_PURCHASE_ATTEMPT,

    /** Actions facultatives après résolution de la case */
    POST_CASE_ACTIONS,

    /** Fin du tour, passage au joueur suivant */
    END_TURN
}
