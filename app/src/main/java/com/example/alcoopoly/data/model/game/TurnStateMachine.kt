package com.example.alcoopoly.game

/**
 * Machine à états gérant l'enchaînement
 * des différentes phases d'un tour de jeu.
 */
class TurnStateMachine(
    private val game: Game
) {

    /** État courant du tour */
    var currentState: TurnState = TurnState.START_TURN
        private set

    /**
     * Fait avancer la machine à l'état suivant
     * en fonction de l'état courant et du Game.
     */
    fun nextState() {
        currentState = when (currentState) {

            TurnState.START_TURN -> {
                TurnState.CHECK_PLAYER_STATUS
            }

            TurnState.CHECK_PLAYER_STATUS -> {
                if (game.isCurrentPlayerInPrison) {
                    TurnState.PRISON_TURN
                } else {
                    TurnState.ROLL_MOVE_DICE
                }
            }

            TurnState.PRISON_TURN -> {
                // La logique détaillée sera ajoutée plus tard
                // Pour l'instant : sortie immédiate simulée
                game.isCurrentPlayerInPrison = false
                game.prisonFailedAttempts = 0
                TurnState.ROLL_MOVE_DICE
            }

            TurnState.ROLL_MOVE_DICE -> {
                TurnState.MOVE_PLAYER
            }

            TurnState.MOVE_PLAYER -> {
                TurnState.RESOLVE_CASE
            }

            TurnState.RESOLVE_CASE -> {
                // Le choix réel dépendra du type de case
                TurnState.POST_CASE_ACTIONS
            }

            TurnState.PROPERTY_PURCHASE_ATTEMPT -> {
                TurnState.POST_CASE_ACTIONS
            }

            TurnState.POST_CASE_ACTIONS -> {
                TurnState.END_TURN
            }

            TurnState.END_TURN -> {
                TurnState.START_TURN
            }
        }
    }
}
