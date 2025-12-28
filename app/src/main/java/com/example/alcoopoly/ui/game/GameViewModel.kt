package com.example.alcoopoly.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alcoopoly.data.BoardData
import com.example.alcoopoly.data.CardData
import com.example.alcoopoly.data.enums.CardType
import com.example.alcoopoly.data.enums.CaseType
import com.example.alcoopoly.model.BoardCase
import com.example.alcoopoly.model.Card
import com.example.alcoopoly.model.Player
import com.example.alcoopoly.model.game.GameState
import com.example.alcoopoly.model.game.TurnState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    fun startNewGame(playerDataList: List<String>) {
        if (_uiState.value.players.isNotEmpty()) return

        val colors = listOf(0xFFFF5252, 0xFF448AFF, 0xFF69F0AE, 0xFFFFD740, 0xFFE040FB, 0xFFFF6E40)
        val newPlayers = playerDataList.mapIndexed { index, dataString ->
            val parts = dataString.split("|")
            val name = parts.getOrElse(0) { "Joueur ${index + 1}" }
            val avatar = parts.getOrElse(1) { "😊" }
            val color = colors.getOrElse(index) { 0xFF9E9E9E }
            Player(id = index + 1, name = name, color = color, avatar = avatar)
        }

        _uiState.update { it.copy(
            players = newPlayers,
            board = BoardData.defaultBoard,
            turnState = TurnState.START_TURN,
            chanceCardsStack = CardData.initialChanceCards.toMutableList().apply { shuffle() },
            miniGameCardsStack = CardData.initialMiniGameCards.toMutableList().apply { shuffle() },
            turnNumber = 1,
            currentPlayerIndex = 0
        )}
        advanceGameLoop()
    }

    // C'est ici que la boucle se joue : au début du tour, on vérifie si on est bloqué
    fun advanceGameLoop() {
        val currentState = _uiState.value.turnState
        val currentPlayer = _uiState.value.currentPlayer

        when (currentState) {
            TurnState.START_TURN -> {
                _uiState.update { it.copy(turnState = TurnState.CHECK_PLAYER_STATUS) }
                advanceGameLoop() // On continue immédiatement
            }
            TurnState.CHECK_PLAYER_STATUS -> {
                if (currentPlayer.inPrison) {
                    // Si en prison -> On active le mode PRISON
                    _uiState.update { it.copy(turnState = TurnState.PRISON_TURN) }
                } else {
                    // Sinon -> On lance les dés normalement
                    _uiState.update { it.copy(turnState = TurnState.ROLL_DICE) }
                }
            }
            else -> { }
        }
    }

    // =========================================================================
    //                        DÉS & DOUBLE (Jeu Normal)
    // =========================================================================

    fun onRollDice() {
        if (_uiState.value.turnState != TurnState.ROLL_DICE) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRolling = true) }

            repeat(10) {
                _uiState.update {
                    val d1 = Random.nextInt(1, 7)
                    val d2 = Random.nextInt(1, 7)
                    it.copy(die1 = d1, die2 = d2, diceResult = d1 + d2)
                }
                delay(80)
            }

            val d1 = Random.nextInt(1, 7)
            val d2 = Random.nextInt(1, 7)
            val total = d1 + d2
            val isDouble = d1 == d2

            _uiState.update { it.copy(
                isRolling = false,
                die1 = d1,
                die2 = d2,
                diceResult = total,
                isDoubles = isDouble,
                replayAvailable = isDouble
            )}

            delay(500)

            if (isDouble) {
                triggerSpecialEvent(
                    title = "🎲 DOUBLE $d1 !",
                    message = "Joli ! Distribue $d1 gorgées (à l'oral).\nTu rejoueras après ce tour !"
                )
            } else {
                _uiState.update { it.copy(turnState = TurnState.MOVE_PLAYER) }
                movePlayer(total)
            }
        }
    }

    // =========================================================================
    //                        PRISON (Logique stricte 8+)
    // =========================================================================

    fun onRollPrison() {
        val d1 = Random.nextInt(1, 7)
        val d2 = Random.nextInt(1, 7)
        val total = d1 + d2

        // RÈGLE : Sortie uniquement si 8 ou plus
        val isSuccess = total >= 8

        _uiState.update { state ->
            val updatedPlayers = state.players.toMutableList()
            val me = updatedPlayers[state.currentPlayerIndex]
            var title = ""
            var msg = ""

            if (isSuccess) {
                // SUCCÈS : On libère le joueur
                updatedPlayers[state.currentPlayerIndex] = me.copy(inPrison = false)
                title = "🔓 ÉVASION RÉUSSIE !"
                msg = "Bravo ! Tu as fait $total (8+).\nTu es libre et tu avances."
            } else {
                // ÉCHEC : On force le maintien en prison + Pénalité boisson
                updatedPlayers[state.currentPlayerIndex] = me.copy(
                    drinksTaken = me.drinksTaken + total,
                    inPrison = true // Il reste bien bloqué
                )
                title = "🔒 RATÉ..."
                msg = "Tu as fait $total (moins de 8).\nBois $total gorgées et RESTE EN PRISON !"
            }

            state.copy(
                diceResult = total,
                die1 = d1, die2 = d2,
                players = updatedPlayers,
                turnState = TurnState.SPECIAL_EVENT_ACTION,
                eventTitle = title,
                eventMessage = msg,
                isEscapingPrison = true // Marqueur pour onDismissSpecialEvent
            )
        }
    }

    // =========================================================================
    //                        ÉVÉNEMENTS (Gestion des suites)
    // =========================================================================

    fun onDismissSpecialEvent() {
        val state = _uiState.value

        when {
            // CAS 1 : C'est le message "DOUBLE" -> On bouge
            state.eventTitle.contains("DOUBLE") -> {
                _uiState.update { it.copy(turnState = TurnState.MOVE_PLAYER) }
                movePlayer(state.diceResult)
            }

            // CAS 2 : Prison
            state.isEscapingPrison -> {
                if (state.currentPlayer.inPrison) {
                    // ÉCHEC (Toujours en prison) : On ne bouge pas, on finit le tour.
                    _uiState.update { it.copy(isEscapingPrison = false, turnState = TurnState.POST_CASE_ACTIONS) }
                } else {
                    // SUCCÈS (Libéré) : On bouge !
                    _uiState.update { it.copy(isEscapingPrison = false, turnState = TurnState.MOVE_PLAYER) }
                    movePlayer(state.diceResult)
                }
            }

            // CAS 3 : Passage Départ
            state.isResolvingStartPass -> {
                _uiState.update { it.copy(isResolvingStartPass = false) }
                resolveCurrentCase()
            }

            // AUTRES (Info simple)
            else -> {
                _uiState.update { it.copy(turnState = TurnState.POST_CASE_ACTIONS) }
            }
        }
    }

    // =========================================================================
    //                        DÉPLACEMENT
    // =========================================================================

    private fun movePlayer(steps: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(turnState = TurnState.MOVE_PLAYER) }
            val startPosition = _uiState.value.currentPlayer.position
            var currentPosition = startPosition

            repeat(steps) {
                delay(350)
                currentPosition = (currentPosition + 1) % 40
                _uiState.update { state ->
                    val updatedPlayers = state.players.toMutableList()
                    val me = updatedPlayers[state.currentPlayerIndex]
                    updatedPlayers[state.currentPlayerIndex] = me.copy(position = currentPosition)
                    state.copy(players = updatedPlayers)
                }
            }
            delay(800)

            val finalPosition = currentPosition
            val passedStart = if (steps > 0) finalPosition < startPosition else false
            val isLandingOnStart = finalPosition == 0

            if (passedStart && !isLandingOnStart) {
                addDrinksGiven(5)
                _uiState.update { state ->
                    state.copy(
                        turnState = TurnState.SPECIAL_EVENT_ACTION,
                        eventTitle = "🍷 Cave Départ (Passage)",
                        eventMessage = "Tu passes devant la Cave ! Distribue 5 gorgées.",
                        isResolvingStartPass = true
                    )
                }
            } else {
                resolveCurrentCase()
            }
        }
    }
    // =========================================================================
    //                        GESTION ABANDON
    // =========================================================================

    fun onPlayerQuit(playerId: Int) {
        val state = _uiState.value
        val playerToRemoveIndex = state.players.indexOfFirst { it.id == playerId }
        if (playerToRemoveIndex == -1) return

        // 1. On libère toutes ses propriétés sur le plateau
        val newBoard = state.board.map { case ->
            if (case.ownerId == playerId) case.copy(ownerId = null) else case
        }

        // 2. On retire le joueur de la liste
        val newPlayers = state.players.filter { it.id != playerId }

        // 3. On gère la fin de partie (s'il ne reste qu'un joueur)
        if (newPlayers.size < 2) {
            // Tu pourrais ici déclencher un écran de victoire,
            // pour l'instant on laisse tourner mais on pourrait rediriger.
        }

        // 4. On recalcule à qui c'est le tour
        // Si le joueur qui partait jouait AVANT moi, mon index diminue de 1.
        // Si le joueur qui part EST celui qui joue, le tour passe au suivant (qui prend son index).
        var newCurrentIndex = state.currentPlayerIndex
        var newTurnState = state.turnState

        if (playerToRemoveIndex < state.currentPlayerIndex) {
            newCurrentIndex = (state.currentPlayerIndex - 1).coerceAtLeast(0)
        } else if (playerToRemoveIndex == state.currentPlayerIndex) {
            // C'était son tour ! Le joueur suivant prend le relais immédiatement
            // On s'assure que l'index ne dépasse pas la taille de la nouvelle liste
            if (newCurrentIndex >= newPlayers.size) {
                newCurrentIndex = 0
            }
            // On réinitialise l'état du tour pour le nouveau joueur (comme un début de tour)
            newTurnState = TurnState.ROLL_DICE
        }

        _uiState.update { it.copy(
            board = newBoard,
            players = newPlayers,
            currentPlayerIndex = newCurrentIndex,
            turnState = newTurnState,
            // On reset les dés et valeurs temporaires pour éviter les bugs
            diceResult = 0, die1 = 0, die2 = 0,
            isDoubles = false, replayAvailable = false,
            purchaseResult = "", pendingRent = 0
        )}

        // Si c'était le tour du joueur qui est parti, on relance la boucle pour vérifier le statut du nouveau joueur
        if (playerToRemoveIndex == state.currentPlayerIndex) {
            // Petite pause ou message optionnel ?
            advanceGameLoop()
        }
    }

    // =========================================================================
    //                        FIN DU TOUR & REJOUER
    // =========================================================================

    fun onEndTurn() {
        val state = _uiState.value

        // Si Double (et pas en prison) -> REJOUE
        if (state.replayAvailable && !state.currentPlayer.inPrison) {
            _uiState.update { it.copy(
                turnState = TurnState.ROLL_DICE, // Retour case départ
                replayAvailable = false,
                isDoubles = false,
                diceResult = 0, die1 = 0, die2 = 0,
                purchaseResult = "", lastPurchaseRoll = 0
            )}
        } else {
            // JOUEUR SUIVANT
            passToNextPlayer()
        }
    }

    private fun passToNextPlayer() {
        _uiState.update { state ->
            val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
            state.copy(
                currentPlayerIndex = nextIndex,
                turnNumber = if (nextIndex == 0) state.turnNumber + 1 else state.turnNumber,

                // CORRECTION MAJEURE ICI :
                // On ne met pas ROLL_DICE direct, on met CHECK_PLAYER_STATUS
                // pour que advanceGameLoop vérifie si ce joueur est en prison.
                turnState = TurnState.CHECK_PLAYER_STATUS,

                diceResult = 0, die1 = 0, die2 = 0,
                isDoubles = false,
                purchaseResult = "",
                lastPurchaseRoll = 0,
                replayAvailable = false
            )
        }
        // Important : On relance la boucle logique pour traiter le statut du nouveau joueur
        advanceGameLoop()
    }

    // =========================================================================
    //                        RÉSOLUTION CASES
    // =========================================================================

    private fun resolveCurrentCase() {
        val state = _uiState.value
        val currentCase = state.board[state.currentPlayer.position]

        when (currentCase.type) {
            CaseType.PROPRIETE, CaseType.BAR -> {
                if (currentCase.ownerId == null) {
                    val difficulty = if (currentCase.id <= 10) 2 else if (currentCase.id <= 20) 3 else if (currentCase.id <= 30) 4 else 5
                    _uiState.update { it.copy(turnState = TurnState.PROPERTY_BUY_ACTION, purchaseTarget = difficulty, purchaseAttempts = 0, lastPurchaseRoll = 0, purchaseResult = "") }
                } else {
                    val rent = calculateRent(currentCase, state.players)
                    _uiState.update { it.copy(turnState = TurnState.RENT_PAYMENT_ACTION, pendingRent = rent) }
                }
            }
            CaseType.DEPART -> {
                addDrinksGiven(10)
                triggerSpecialEvent("🍷 Cave Départ !", "Arrêt pile poil ! Tu distribues 10 gorgées.")
            }
            CaseType.BASSINE_REMPLIR -> triggerSpecialEvent("🪣 La Bassine", "Verse ce que tu veux dans la bassine centrale !")
            CaseType.BASSINE_BOIRE -> triggerSpecialEvent("🤮 CUL SEC !", "Désolé... Tu dois boire TOUTE la bassine !")
            CaseType.ALLER_PRISON -> {
                val prisonIndex = 10
                val updatedPlayers = state.players.toMutableList()
                val me = updatedPlayers[state.currentPlayerIndex]
                updatedPlayers[state.currentPlayerIndex] = me.copy(position = prisonIndex, inPrison = true, prisonTurns = 0)
                _uiState.update { it.copy(players = updatedPlayers) }
                triggerSpecialEvent("🚔 BAR'BAN !", "Direction la cellule de dégrisement. Tu es bloqué !")
            }
            CaseType.SIMPLE_VISITE -> triggerSpecialEvent("👮 Bar'ban", "Tout va bien, tu n'es que de passage.")
            CaseType.JARDIN_ENFANT -> triggerSpecialEvent("👶 Jardin d'Enfant", "Il fait chaud... Enlève un vêtement !")
            CaseType.CHANCE -> drawCard(CardType.CHANCE)
            CaseType.MINI_JEU -> drawCard(CardType.MINI_JEU)
        }
    }

    // =========================================================================
    //                        ACHAT & LOYER
    // =========================================================================

    fun onRollForPurchase() {
        val state = _uiState.value
        if (state.purchaseResult == "SUCCESS" || state.purchaseAttempts >= 2 || state.isRolling) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRolling = true, purchaseResult = "") }
            repeat(15) {
                _uiState.update { it.copy(lastPurchaseRoll = Random.nextInt(1, 7)) }
                delay(60)
            }
            val roll = Random.nextInt(1, 7)
            val success = roll >= state.purchaseTarget
            val newAttempts = state.purchaseAttempts + 1
            val updatedPlayers = state.players.toMutableList()
            val me = updatedPlayers[state.currentPlayerIndex]
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksTaken = me.drinksTaken + roll)

            if (success) {
                val currentPos = state.currentPlayer.position
                val currentCase = state.board[currentPos]
                val newCase = currentCase.copy(ownerId = me.id)
                val newBoard = state.board.toMutableList()
                newBoard[currentPos] = newCase
                updatedPlayers[state.currentPlayerIndex] = updatedPlayers[state.currentPlayerIndex].copy(ownedCases = me.ownedCases + currentCase.id)
                _uiState.update { it.copy(
                    board = newBoard, players = updatedPlayers, lastPurchaseRoll = roll,
                    purchaseAttempts = newAttempts, purchaseResult = "SUCCESS", isRolling = false, turnState = TurnState.PROPERTY_BUY_ACTION
                )}
            } else {
                val resultState = if (newAttempts >= 2) "FAILED_FINAL" else "FAILED_RETRY"
                _uiState.update { it.copy(
                    players = updatedPlayers, lastPurchaseRoll = roll, purchaseAttempts = newAttempts,
                    purchaseResult = resultState, isRolling = false, turnState = TurnState.PROPERTY_BUY_ACTION
                )}
            }
        }
    }

    fun onSkipBuy() {
        _uiState.update { it.copy(turnState = TurnState.POST_CASE_ACTIONS) }
    }

    fun onConfirmRent() {
        val state = _uiState.value
        val currentCase = state.board[state.currentPlayer.position]
        val rent = state.pendingRent
        val me = state.currentPlayer
        val updatedPlayers = state.players.toMutableList()

        if (currentCase.ownerId == me.id) {
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksGiven = me.drinksGiven + rent)
        } else {
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksTaken = me.drinksTaken + rent)
            val ownerIndex = updatedPlayers.indexOfFirst { it.id == currentCase.ownerId }
            if (ownerIndex != -1) {
                val owner = updatedPlayers[ownerIndex]
                updatedPlayers[ownerIndex] = owner.copy(drinksGiven = owner.drinksGiven + rent)
            }
        }
        _uiState.update { it.copy(players = updatedPlayers, turnState = TurnState.POST_CASE_ACTIONS, pendingRent = 0) }
    }

    // =========================================================================
    //                        CARTES & UTILITAIRES
    // =========================================================================

    private fun drawCard(requestedType: CardType) {
        _uiState.update { state ->
            var cardToDisplay: Card? = null
            val chanceStack = state.chanceCardsStack
            val miniGameStack = state.miniGameCardsStack
            val effectiveType = if (requestedType == CardType.MINI_JEU && miniGameStack.isEmpty()) CardType.CHANCE else requestedType

            if (effectiveType == CardType.MINI_JEU) {
                if (miniGameStack.isNotEmpty()) cardToDisplay = miniGameStack.removeAt(0)
            } else {
                if (chanceStack.isNotEmpty()) cardToDisplay = chanceStack.removeAt(0) else cardToDisplay = Card(0, "Plus aucune carte !", CardType.CHANCE)
            }
            state.copy(turnState = TurnState.CARD_DRAW_ACTION, currentCard = cardToDisplay, chanceCardsStack = chanceStack, miniGameCardsStack = miniGameStack)
        }
    }

    fun onDismissCard() {
        val state = _uiState.value
        val card = state.currentCard
        _uiState.update { it.copy(turnState = TurnState.POST_CASE_ACTIONS, currentCard = null) }
        if (card != null) applyCardEffect(card)
    }

    private fun applyCardEffect(card: Card) {
        viewModelScope.launch {
            delay(500)
            when (card.id) {
                106, 141 -> teleportPlayer(0, "Oups... Retour à la case départ !")
                105 -> teleportPlayer(35, "Téléportation à la Soirée BDE !")
                146 -> teleportPlayer(37, "Bonne chance pour ton date...")
                145 -> teleportPlayer(15, "Direction le Bar'bu !")
                143 -> {
                    val currentPos = _uiState.value.currentPlayer.position
                    val newPos = (currentPos - 3 + 40) % 40
                    teleportPlayer(newPos, "Tu es trop défoncé... Tu recules.")
                }
            }
        }
    }

    private suspend fun teleportPlayer(targetIndex: Int, message: String) {
        _uiState.update { state ->
            val updatedPlayers = state.players.toMutableList()
            updatedPlayers[state.currentPlayerIndex] = updatedPlayers[state.currentPlayerIndex].copy(position = targetIndex)
            state.copy(players = updatedPlayers)
        }
        triggerSpecialEvent("✨ TÉLÉPORTATION", message)
    }

    private fun triggerSpecialEvent(title: String, message: String) {
        _uiState.update { it.copy(turnState = TurnState.SPECIAL_EVENT_ACTION, eventTitle = title, eventMessage = message) }
    }

    private fun addDrinksGiven(amount: Int) {
        _uiState.update { state ->
            val updatedPlayers = state.players.toMutableList()
            val me = updatedPlayers[state.currentPlayerIndex]
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksGiven = me.drinksGiven + amount)
            state.copy(players = updatedPlayers)
        }
    }

    private fun calculateRent(case: BoardCase, players: List<Player>): Int {
        if (case.type == CaseType.BAR) {
            val owner = players.find { it.id == case.ownerId } ?: return 0
            val nbBars = owner.ownedCases.count { id -> listOf(6, 16, 26, 36).contains(id) }
            return nbBars * 4
        } else { return case.familyId ?: 1 }
    }
}