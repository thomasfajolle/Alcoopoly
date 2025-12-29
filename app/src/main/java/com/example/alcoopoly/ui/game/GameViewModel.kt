package com.example.alcoopoly.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alcoopoly.data.BoardData
import com.example.alcoopoly.data.CardData
import com.example.alcoopoly.data.repository.CardRepository
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

class GameViewModel(application: Application) : AndroidViewModel(application) {
    // 1. On instancie le Repository
    private val repository = CardRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(GameState(
        players = emptyList(),
        board = BoardData.defaultBoard,
        // 2. On charge les cartes via le repository (ce qui récupère la sauvegarde ou les défauts)
        allChanceCards = repository.loadChanceCards(),
        allMiniGameCards = repository.loadMiniGameCards(),
        chanceCardsStack = repository.loadChanceCards().filter { it.isActive }.toMutableList().apply { shuffle() },
        miniGameCardsStack = repository.loadMiniGameCards().filter { it.isActive }.toMutableList().apply { shuffle() }
    ))
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
            // On initialise la PIOCHE (Mélangée)
            chanceCardsStack = CardData.initialChanceCards.toMutableList().apply { shuffle() },
            miniGameCardsStack = CardData.initialMiniGameCards.toMutableList().apply { shuffle() },
            // On initialise la BIBLIOTHÈQUE (Toutes les cartes, triées par ID pour l'ordre)
            allChanceCards = CardData.initialChanceCards,
            allMiniGameCards = CardData.initialMiniGameCards,
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
            state.eventTitle.contains("DOUBLE") -> {
                _uiState.update { it.copy(turnState = TurnState.MOVE_PLAYER) }
                movePlayer(state.diceResult)
            }

            // --- AJOUT ICI ---
            state.eventTitle.contains("TÉLÉPORTATION") -> {
                // Après une téléportation, on résout la case d'arrivée (Achat/Loyer)
                resolveCurrentCase()
            }
            // CAS REJOUE (Carte 139)
            state.eventTitle.contains("REJOUE") -> {
                _uiState.update { it.copy(
                    turnState = TurnState.ROLL_DICE, // On repart au lancer
                    diceResult = 0, die1 = 0, die2 = 0, // Reset visuel
                    isDoubles = false, replayAvailable = false
                )}
            }
            state.isEscapingPrison -> {
                if (state.currentPlayer.inPrison) {
                    _uiState.update { it.copy(isEscapingPrison = false, turnState = TurnState.POST_CASE_ACTIONS) }
                } else {
                    _uiState.update { it.copy(isEscapingPrison = false, turnState = TurnState.MOVE_PLAYER) }
                    movePlayer(state.diceResult)
                }
            }

            state.isResolvingStartPass -> {
                _uiState.update { it.copy(isResolvingStartPass = false) }
                resolveCurrentCase()
            }

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
        val currentPos = state.currentPlayer.position
        val currentCase = state.board[currentPos]

        // --- CAS : ALLEZ EN PRISON (Case 31 -> Index 30) ---
        if (currentPos == 30) {
            // On ouvre une coroutine pour pouvoir appeler teleportPlayer
            viewModelScope.launch {
                // 1. On active l'animation ET on change l'état
                _uiState.update { it.copy(
                    triggerPrisonAnim = true,
                    turnState = TurnState.SPECIAL_EVENT_ACTION
                ) }

                // 2. On envoie en prison (Fonction suspendue)
                teleportPlayer(10, "Direction la cellule de dégrisement !")

                // 3. On force le statut prison (car teleportPlayer ne met pas inPrison = true par défaut)
                _uiState.update { st ->
                    val players = st.players.toMutableList()
                    val me = players[st.currentPlayerIndex]
                    players[st.currentPlayerIndex] = me.copy(inPrison = true, prisonTurns = 0)
                    st.copy(players = players)
                }
            }
            return // On arrête la fonction ici
        }

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
            val state = _uiState.value

            when (card.id) {
                // --- DÉPLACEMENTS (101-107) ---
                101 -> { // Bonne année -> Départ
                    teleportPlayer(0, "Bonne année ! Retour case départ.")
                }
                // ID 102 : ALCOOL AU VOLANT (PRISON)
                102 -> {
                    val prisonIndex = 10
                    _uiState.update { st ->
                        val updatedPlayers = st.players.toMutableList()
                        val me = updatedPlayers[st.currentPlayerIndex]

                        updatedPlayers[st.currentPlayerIndex] = me.copy(
                            position = prisonIndex,
                            inPrison = true,
                            prisonTurns = 0
                        )

                        st.copy(
                            players = updatedPlayers,
                            triggerPrisonAnim = true // <--- ON DÉCLENCHE L'ANIMATION ICI
                        )
                    }
                    triggerSpecialEvent("🚔 PRISON !", "Pas de chance... Tu vas en cellule de dégrisement.")
                }
                103 -> { // C'est Mercredi -> Bar'bu (Case 16)
                    // Logique spéciale : Si c'est possédé, on paie. Si c'est libre, on achète PAS.
                    // On fait le déplacement manuellement ici sans passer par teleportPlayer pour éviter resolveCurrentCase standard
                    val targetIndex = 15
                    _uiState.update { st ->
                        val players = st.players.toMutableList()
                        players[st.currentPlayerIndex] = players[st.currentPlayerIndex].copy(position = targetIndex)
                        st.copy(players = players)
                    }

                    val barBu = state.board[targetIndex]
                    if (barBu.ownerId != null && barBu.ownerId != state.currentPlayer.id) {
                        // Appartient à quelqu'un -> On paie le loyer
                        val rent = calculateRent(barBu, state.players)
                        _uiState.update { it.copy(turnState = TurnState.RENT_PAYMENT_ACTION, pendingRent = rent) }
                    } else {
                        // Libre ou à moi -> Juste un message, pas d'achat
                        triggerSpecialEvent("🍺 BAR'BU", "Tu es au bar ! (Tu ne peux pas acheter, juste consommer).")
                    }
                }
                104 -> teleportPlayer(37, "Bonne chance pour ton date...") // Case 38
                105 -> teleportPlayer(35, "Direction la Soirée BDE !") // Case 36
                106 -> { // Oubli Tel -> Bar précédent
                    val newPos = findNearestBarBackwards(state.currentPlayer.position)
                    teleportPlayer(newPos, "Tu retournes au bar précédent chercher ton tel.")
                }
                107 -> { // Space Cake -> Recule (Simulation : Recule de 3 cases)
                    val current = state.currentPlayer.position
                    val newPos = (current - 3 + 40) % 40
                    teleportPlayer(newPos, "Tu es trop défoncé... Tu recules.")
                }

                // --- POSITIONS & VOLS (108, 122, 124) ---
                108 -> swapPositionWithRandom() // Vis ma vie

                124 -> swapIdentityWithRandomPlayer() // Vol d'identité

                122 -> stealRandomProperty() // Expropriation (On utilise vol au hasard)
                121 -> stealRandomProperty() // Vol de propriété (On simplifie en vol hasard pour l'appli)
                123 -> stealRandomProperty() // OPA Hostile (On simplifie en vol hasard)

                // ID 136 : DEALER (Simulation de fuite)
                136 -> {
                    var attempts = 0
                    var penalty = 0
                    var isDouble = false

                    // On simule jusqu'à un double (max 10 essais pour éviter l'infini)
                    while (!isDouble && attempts < 15) {
                        attempts++
                        val d1 = Random.nextInt(1, 7)
                        val d2 = Random.nextInt(1, 7)
                        if (d1 == d2) {
                            isDouble = true
                        } else {
                            penalty += 2 // 2 gorgées par raté
                        }
                    }

                    // On applique la pénalité
                    _uiState.update { st ->
                        val players = st.players.toMutableList()
                        val me = players[st.currentPlayerIndex]
                        players[st.currentPlayerIndex] = me.copy(drinksTaken = me.drinksTaken + penalty)
                        st.copy(players = players)
                    }

                    if (isDouble) {
                        triggerSpecialEvent("🏃 COURS FOREST !", "Tu as semé le dealer au bout de $attempts essais.\nTu as dû boire $penalty gorgées dans la panique.")
                    } else {
                        triggerSpecialEvent("💀 RIP", "Tu as couru 15 fois sans faire de double... Le dealer t'a rattrapé. Tu as bu $penalty gorgées pour rien.")
                    }
                }

                // ID 139 : REJOUE
                139 -> {
                    // On affiche le message. La logique de "Rejouer" se fera dans onDismissSpecialEvent
                    triggerSpecialEvent("🎲 REJOUE", "C'est ton jour de chance ! Relance les dés immédiatement.")
                }

                // Pour les autres cartes (Texte uniquement), rien ne se passe, le tour finit.
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
    // --- UTILITAIRES POUR LES CARTES ---

    // Échange toutes les données de jeu entre le joueur actuel et une cible aléatoire
    private fun swapIdentityWithRandomPlayer() {
        _uiState.update { state ->
            val players = state.players.toMutableList()
            val meIndex = state.currentPlayerIndex
            val me = players[meIndex]

            // Choisir une cible (pas moi)
            val targets = players.filter { it.id != me.id }
            if (targets.isEmpty()) return@update state // Pas assez de joueurs

            val target = targets.random()
            val targetIndex = players.indexOf(target)

            // On échange TOUT sauf l'identité visuelle (Nom, Avatar, Couleur, ID restent)
            // On échange : Position, Propriétés, Gorgées, Prison
            val newMe = me.copy(
                position = target.position,
                ownedCases = target.ownedCases,
                drinksTaken = target.drinksTaken,
                drinksGiven = target.drinksGiven,
                inPrison = target.inPrison,
                prisonTurns = target.prisonTurns
            )

            val newTarget = target.copy(
                position = me.position,
                ownedCases = me.ownedCases,
                drinksTaken = me.drinksTaken,
                drinksGiven = me.drinksGiven,
                inPrison = me.inPrison,
                prisonTurns = me.prisonTurns
            )

            // Mise à jour des propriétés sur le plateau (OwnerId)
            val newBoard = state.board.map { case ->
                when (case.ownerId) {
                    me.id -> case.copy(ownerId = target.id) // Ce qui était à moi est à lui
                    target.id -> case.copy(ownerId = me.id) // Ce qui était à lui est à moi
                    else -> case
                }
            }

            players[meIndex] = newMe
            players[targetIndex] = newTarget

            state.copy(players = players, board = newBoard)
        }
        triggerSpecialEvent("🎭 VOL D'IDENTITÉ", "INCROYABLE ! Tu as échangé ta vie (position, propriétés, gorgées...) avec un autre joueur au hasard !")
    }

    // Vole une propriété au hasard à un joueur au hasard
    private fun stealRandomProperty() {
        val state = _uiState.value
        val me = state.currentPlayer

        // Trouver les joueurs qui ont des propriétés (pas moi)
        val richPlayers = state.players.filter { it.id != me.id && it.ownedCases.isNotEmpty() }

        if (richPlayers.isEmpty()) {
            triggerSpecialEvent("😢 ECHEC", "Personne n'a de propriété à voler...")
            return
        }

        // Choisir une victime et une propriété
        val victim = richPlayers.random()
        val propertyIdToSteal = victim.ownedCases.random()
        val propertyName = state.board.find { it.id == propertyIdToSteal }?.name ?: "Inconnue"

        _uiState.update { st ->
            val players = st.players.toMutableList()
            val victimIndex = players.indexOfFirst { it.id == victim.id }
            val meIndex = st.currentPlayerIndex

            // Mise à jour des listes de propriétés
            val newVictim = players[victimIndex].copy(ownedCases = players[victimIndex].ownedCases - propertyIdToSteal)
            val newMe = players[meIndex].copy(ownedCases = players[meIndex].ownedCases + propertyIdToSteal)

            players[victimIndex] = newVictim
            players[meIndex] = newMe

            // Mise à jour du plateau
            val newBoard = st.board.map { case ->
                if (case.id == propertyIdToSteal) case.copy(ownerId = me.id) else case
            }

            st.copy(players = players, board = newBoard)
        }
        triggerSpecialEvent("🏴‍☠️ VOL !", "Tu as volé '$propertyName' à ${victim.name} !")
    }

    // Échange de position avec un joueur au hasard
    private fun swapPositionWithRandom() {
        _uiState.update { state ->
            val players = state.players.toMutableList()
            val meIndex = state.currentPlayerIndex
            val targets = players.indices.filter { it != meIndex } // Indices des autres

            if (targets.isEmpty()) return@update state

            val targetIndex = targets.random()
            val me = players[meIndex]
            val target = players[targetIndex]

            val tempPos = me.position
            players[meIndex] = me.copy(position = target.position)
            players[targetIndex] = target.copy(position = tempPos)

            state.copy(players = players)
        }
        triggerSpecialEvent("🔄 VIS MA VIE", "Tu as échangé ta place avec un autre joueur !")
    }

    // Cherche le bar le plus proche en arrière
    private fun findNearestBarBackwards(currentPos: Int): Int {
        val bars = listOf(35, 25, 15, 5) // Indices des bars (36, 26, 16, 6) inversés
        // On cherche le premier bar qui est plus petit que ma position
        return bars.firstOrNull { it < currentPos } ?: 35 // Si je suis case 2, le bar précédent est le 36 (index 35)
    }
    // =========================================================================
    //               GESTION DES CARTES (AVEC SAUVEGARDE)
    // =========================================================================

    fun addCustomCard(type: CardType, title: String, description: String) {
        _uiState.update { state ->
            val allCards = if (type == CardType.CHANCE) state.allChanceCards else state.allMiniGameCards
            val maxId = allCards.maxOfOrNull { it.id } ?: 300
            val newId = maxId + 1
            val fullText = "$title\n$description"
            val newCard = Card(newId, fullText, type, isActive = true)

            if (type == CardType.CHANCE) {
                val newList = state.allChanceCards + newCard
                repository.saveChanceCards(newList) // <--- SAUVEGARDE
                state.copy(
                    allChanceCards = newList,
                    chanceCardsStack = (state.chanceCardsStack + newCard).toMutableList()
                )
            } else {
                val newList = state.allMiniGameCards + newCard
                repository.saveMiniGameCards(newList) // <--- SAUVEGARDE
                state.copy(
                    allMiniGameCards = newList,
                    miniGameCardsStack = (state.miniGameCardsStack + newCard).toMutableList()
                )
            }
        }
    }

    fun deleteCard(card: Card) {
        val disabledCard = card.copy(isActive = false)
        _uiState.update { state ->
            if (card.type == CardType.CHANCE) {
                val newList = state.allChanceCards.map { if (it.id == card.id) disabledCard else it }
                repository.saveChanceCards(newList) // <--- SAUVEGARDE
                state.copy(
                    allChanceCards = newList,
                    chanceCardsStack = state.chanceCardsStack.filter { it.id != card.id }.toMutableList()
                )
            } else {
                val newList = state.allMiniGameCards.map { if (it.id == card.id) disabledCard else it }
                repository.saveMiniGameCards(newList) // <--- SAUVEGARDE
                state.copy(
                    allMiniGameCards = newList,
                    miniGameCardsStack = state.miniGameCardsStack.filter { it.id != card.id }.toMutableList()
                )
            }
        }
    }

    fun restoreCard(card: Card) {
        val enabledCard = card.copy(isActive = true)
        _uiState.update { state ->
            if (card.type == CardType.CHANCE) {
                val newList = state.allChanceCards.map { if (it.id == card.id) enabledCard else it }
                repository.saveChanceCards(newList) // <--- SAUVEGARDE
                state.copy(
                    allChanceCards = newList,
                    chanceCardsStack = (state.chanceCardsStack + enabledCard).toMutableList()
                )
            } else {
                val newList = state.allMiniGameCards.map { if (it.id == card.id) enabledCard else it }
                repository.saveMiniGameCards(newList) // <--- SAUVEGARDE
                state.copy(
                    allMiniGameCards = newList,
                    miniGameCardsStack = (state.miniGameCardsStack + enabledCard).toMutableList()
                )
            }
        }
    }

    fun updateCard(oldCard: Card, newTitle: String, newDescription: String) {
        _uiState.update { state ->
            val fullText = "$newTitle\n$newDescription"
            val updatedCard = oldCard.copy(description = fullText)

            if (oldCard.type == CardType.CHANCE) {
                val newList = state.allChanceCards.map { if (it.id == oldCard.id) updatedCard else it }
                repository.saveChanceCards(newList) // <--- SAUVEGARDE
                state.copy(
                    allChanceCards = newList,
                    chanceCardsStack = state.chanceCardsStack.map { if (it.id == oldCard.id) updatedCard else it }.toMutableList()
                )
            } else {
                val newList = state.allMiniGameCards.map { if (it.id == oldCard.id) updatedCard else it }
                repository.saveMiniGameCards(newList) // <--- SAUVEGARDE
                state.copy(
                    allMiniGameCards = newList,
                    miniGameCardsStack = state.miniGameCardsStack.map { if (it.id == oldCard.id) updatedCard else it }.toMutableList()
                )
            }
        }
    }

    // =========================================================================
    //                        GESTION DE LA PARTIE (MENU PAUSE)
    // =========================================================================
    // Important : Mise à jour de restartGame pour bien recharger les cartes
    fun restartGame() {
        val currentPlayers = _uiState.value.players

        // On recharge depuis le disque pour être sûr d'avoir la dernière version
        val savedChance = repository.loadChanceCards()
        val savedMini = repository.loadMiniGameCards()

        _uiState.update { state ->
            val resetPlayers = currentPlayers.map { player ->
                player.copy(
                    position = 0,
                    drinksTaken = 0,
                    drinksGiven = 0,
                    ownedCases = emptyList(),
                    inPrison = false,
                    prisonTurns = 0
                )
            }

            GameState(
                players = resetPlayers,
                board = BoardData.defaultBoard,
                turnState = TurnState.START_TURN,
                allChanceCards = savedChance,
                allMiniGameCards = savedMini,
                chanceCardsStack = savedChance.filter { it.isActive }.toMutableList().apply { shuffle() },
                miniGameCardsStack = savedMini.filter { it.isActive }.toMutableList().apply { shuffle() },
                turnNumber = 1,
                currentPlayerIndex = 0,
                // On garde les préférences utilisateur
                isSoundEnabled = state.isSoundEnabled,
                isVibrationEnabled = state.isVibrationEnabled
            )
        }
        advanceGameLoop()
    }

    fun onPrisonAnimationFinished() {
        _uiState.update { it.copy(triggerPrisonAnim = false) }
    }
    // --- GESTION DES PARAMÈTRES ---

    fun toggleSound() {
        _uiState.update { it.copy(isSoundEnabled = !it.isSoundEnabled) }
    }

    fun toggleVibration() {
        _uiState.update { it.copy(isVibrationEnabled = !it.isVibrationEnabled) }
    }
}
