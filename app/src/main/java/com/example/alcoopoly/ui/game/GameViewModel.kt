package com.example.alcoopoly.ui.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.alcoopoly.model.game.GameState
import com.example.alcoopoly.model.game.TurnState
import com.example.alcoopoly.model.Player
import com.example.alcoopoly.model.BoardCase
import com.example.alcoopoly.data.BoardData
import com.example.alcoopoly.data.enums.CaseType
import kotlin.random.Random
import com.example.alcoopoly.data.CardData
import com.example.alcoopoly.data.enums.CardType
import com.example.alcoopoly.model.Card
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    // Supprime le "init" du début de la classe !

    /**
     * Cette fonction est appelée par l'écran de jeu au démarrage
     * pour créer les joueurs basés sur ce qu'on a saisi à l'accueil.
     */
    fun startNewGame(playerNames: List<String>) {
        // Si la partie est déjà lancée (joueurs existent), on ne fait rien pour ne pas reset
        if (_uiState.value.players.isNotEmpty()) return

        // On génère les joueurs avec des couleurs automatiques
        val colors = listOf(
            0xFFFF5252, // Rouge
            0xFF448AFF, // Bleu
            0xFF69F0AE, // Vert
            0xFFFFD740, // Jaune
            0xFFE040FB, // Violet
            0xFFFF6E40  // Orange
        )

        val newPlayers = playerNames.mapIndexed { index, name ->
            val color = colors.getOrElse(index) { 0xFF9E9E9E } // Couleur grise si plus de 6 joueurs
            Player(
                id = index + 1,
                name = name,
                color = color
            )
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

    /**
     * Boucle principale qui fait avancer les états automatiques
     */
    fun advanceGameLoop() {
        val currentState = _uiState.value.turnState
        val currentPlayer = _uiState.value.currentPlayer

        when (currentState) {
            TurnState.START_TURN -> {
                _uiState.update { it.copy(turnState = TurnState.CHECK_PLAYER_STATUS) }
                advanceGameLoop()
            }

            TurnState.CHECK_PLAYER_STATUS -> {
                if (currentPlayer.inPrison) {
                    _uiState.update { it.copy(turnState = TurnState.PRISON_TURN) }
                } else {
                    _uiState.update { it.copy(turnState = TurnState.ROLL_DICE) }
                }
            }
            else -> { /* En attente action utilisateur */ }
        }
    }

    // --- ACTIONS DE JEU ---

    fun onRollDice() {
        if (_uiState.value.turnState != TurnState.ROLL_DICE) return

        viewModelScope.launch {
            // 1. On dit que ça roule
            _uiState.update { it.copy(isRolling = true) }
            // 2. Animation : On change les valeurs aléatoirement 10 fois très vite
            repeat(10) {
                _uiState.update { it.copy(
                    diceResult = Random.nextInt(2, 13) // Entre 2 et 12
                )}
                delay(80) // Petite pause de 80ms
            }
            // 3. VRAI LANCER FINAL
            val d1 = Random.nextInt(1, 7)
            val d2 = Random.nextInt(1, 7)
            val total = d1 + d2
            val isDouble = d1 == d2
            _uiState.update { it.copy(
                isRolling = false, // Fini de rouler
                diceResult = total,
                isDoubles = isDouble,
                turnState = TurnState.MOVE_PLAYER
            )}
            // 4. On déplace le joueur (après une petite seconde pour lire le résultat)
            delay(1000)
            movePlayer(total)
        }
    }

    // --- GESTION PRISON (BAR'BAN) ---
    fun onRollPrison() {
        val d1 = Random.nextInt(1, 7)
        val d2 = Random.nextInt(1, 7)
        val total = d1 + d2
        val isDouble = d1 == d2 // (Optionnel si tu veux garder la règle du double, mais ta règle 8+ prévaut)

        // RÈGLE : Il faut faire 8 ou plus pour sortir
        val isSuccess = total >= 8

        _uiState.update { state ->
            val updatedPlayers = state.players.toMutableList()
            val me = updatedPlayers[state.currentPlayerIndex]

            var title = ""
            var msg = ""
            var newInPrison = true // Par défaut on reste

            if (isSuccess) {
                // --- SUCCÈS : SORTIE ---
                newInPrison = false
                title = "🔓 ÉVASION RÉUSSIE !"
                msg = "Bravo ! Tu as fait $total (Objectif 8+).\nTu sors de prison et tu avances."

                updatedPlayers[state.currentPlayerIndex] = me.copy(inPrison = false)
            } else {
                // --- ÉCHEC : ON RESTE ---
                newInPrison = true
                title = "🔒 RATÉ..."
                msg = "Tu as fait $total (Objectif 8+).\nTu restes bloqué et tu bois $total gorgées !"

                // Pénalité : On boit le score des dés
                updatedPlayers[state.currentPlayerIndex] = me.copy(
                    drinksTaken = me.drinksTaken + total,
                    inPrison = true // Reste explicitement true
                )
            }

            state.copy(
                diceResult = total,
                isDoubles = isDouble,
                players = updatedPlayers,
                // On déclenche l'affichage du résultat
                turnState = TurnState.SPECIAL_EVENT_ACTION,
                eventTitle = title,
                eventMessage = msg,
                isEscapingPrison = true // Marqueur pour dire "C'est une tentative d'évasion"
            )
        }
    }
    private fun movePlayer(steps: Int) {
        var triggerPassStart = false

        _uiState.update { state ->
            val currentPlayer = state.currentPlayer
            val oldPosition = currentPlayer.position
            val newPosition = (oldPosition + steps) % 40

            // On vérifie si on a bouclé (passé de 39 à 2 par exemple)
            val passedStart = newPosition < oldPosition

            // On vérifie si on est tombé PILE sur le départ (Index 0)
            val isLandingOnStart = newPosition == 0

            var drinksBonus = 0

            var nextTurnState = TurnState.RESOLVE_CASE
            var eventTitle = ""
            var eventMessage = ""
            var isResolvingStart = false

            // MODIFICATION ICI :
            // On déclenche le "+5 Passage" SEULEMENT si on a passé le départ SANS s'arrêter dessus.
            // Si on s'arrête dessus (isLandingOnStart), on ne fait rien ici,
            // c'est resolveCurrentCase qui gérera le "+10 Arrêt".
            if (passedStart && !isLandingOnStart) {
                drinksBonus = 5
                triggerPassStart = true
                nextTurnState = TurnState.SPECIAL_EVENT_ACTION
                eventTitle = "🍷 Cave Départ (Passage)"
                eventMessage = "Tu passes devant la Cave ! Distribue 5 gorgées."
                isResolvingStart = true
            }

            val updatedPlayers = state.players.toMutableList()
            val me = updatedPlayers[state.currentPlayerIndex]

            updatedPlayers[state.currentPlayerIndex] = me.copy(
                position = newPosition,
                drinksGiven = me.drinksGiven + drinksBonus
            )

            state.copy(
                players = updatedPlayers,
                turnState = nextTurnState,
                eventTitle = eventTitle,
                eventMessage = eventMessage,
                isResolvingStartPass = isResolvingStart
            )
        }

        // Si on n'a pas déclenché le passage (donc soit trajet normal, soit atterrissage pile sur départ),
        // on lance la résolution tout de suite.
        if (!triggerPassStart) {
            resolveCurrentCase()
        }
    }

    /**
     * LOGIQUE CŒUR : Décide quoi faire sur la case actuelle
     */
    private fun resolveCurrentCase() {
        val state = _uiState.value
        val currentCase = state.board[state.currentPlayer.position]

        when (currentCase.type) {
            // --- CAS 1 : PROPRIÉTÉS & BARS ---
            CaseType.PROPRIETE, CaseType.BAR -> {
                if (currentCase.ownerId == null) {
                    // --- CASE LIBRE : TENTATIVE D'ACHAT ---
                    // Difficulté selon la rangée (1-10=2, 11-20=3, etc.)
                    val difficulty = when {
                        currentCase.id <= 10 -> 2
                        currentCase.id <= 20 -> 3
                        currentCase.id <= 30 -> 4
                        else -> 5
                    }

                    _uiState.update { it.copy(
                        turnState = TurnState.PROPERTY_BUY_ACTION,
                        purchaseTarget = difficulty,
                        purchaseAttempts = 0,
                        lastPurchaseRoll = 0,
                        purchaseResult = ""
                    )}
                }
                else {
                    // --- LOYER (Mise à jour) ---
                    // On calcule juste le montant et on change d'état
                    // L'application des gorgées se fera quand l'utilisateur cliquera sur "OK"
                    val rent = calculateRent(currentCase, state.players)

                    _uiState.update { it.copy(
                        turnState = TurnState.RENT_PAYMENT_ACTION,
                        pendingRent = rent
                    )}
                }
            }

            // --- CAS 2 : CAVE DÉPART (ARRÊT) ---
            CaseType.DEPART -> {
                triggerSpecialEvent(
                    title = "🍷 Cave Départ !",
                    message = "Tu t'arrêtes pile poil à la cave ! Tu distribues 10 gorgées."
                )
                // Appliquer l'effet (+10 gorgées à donner)
                addDrinksGiven(10)
            }

            // --- CAS 3 : BASSINE ---
            CaseType.BASSINE_REMPLIR -> {
                triggerSpecialEvent(
                    title = "🪣 La Bassine",
                    message = "Verse ce que tu veux dans la bassine centrale !"
                )
            }
            CaseType.BASSINE_BOIRE -> {
                triggerSpecialEvent(
                    title = "🤮 CUL SEC !",
                    message = "Désolé... Tu dois boire TOUTE la bassine !"
                )
            }

            // --- CAS 4 : PRISON ---
            CaseType.ALLER_PRISON -> {
                // Téléportation Case 11 (Index 10)
                val prisonIndex = 10
                val updatedPlayers = state.players.toMutableList()
                val me = updatedPlayers[state.currentPlayerIndex]
                updatedPlayers[state.currentPlayerIndex] = me.copy(
                    position = prisonIndex,
                    inPrison = true,
                    prisonTurns = 0
                )

                _uiState.update { it.copy(players = updatedPlayers) }

                triggerSpecialEvent(
                    title = "🚔 BAR'BAN !",
                    message = "Tu as trop bu. Direction la cellule de dégrisement (Case 11). Tu es bloqué !"
                )
            }
            CaseType.SIMPLE_VISITE -> {
                // on affiche un message rassurant
                triggerSpecialEvent(
                    title = "👮 Bar'ban (Simple Visite)",
                    message = "Tout va bien ! Tu n'es que de passage. Tu peux narguer ceux qui sont enfermés 😜"
                )
            }

            // --- CAS 5 : JARDIN D'ENFANT ---
            CaseType.JARDIN_ENFANT -> {
                triggerSpecialEvent(
                    title = "👶 Jardin d'Enfant",
                    message = "Il fait chaud ici... Enlève un vêtement !"
                )
            }

            // --- CAS 6 : CARTES ---
            CaseType.CHANCE -> {
                drawCard(CardType.CHANCE)
            }
            CaseType.MINI_JEU -> {
                drawCard(CardType.MINI_JEU)
            }
        }
    }

    // --- UTILITAIRES POUR ALLÉGER LE CODE ---

    private fun triggerSpecialEvent(title: String, message: String) {
        _uiState.update { it.copy(
            turnState = TurnState.SPECIAL_EVENT_ACTION,
            eventTitle = title,
            eventMessage = message
        )}
    }

    private fun addDrinksGiven(amount: Int) {
        _uiState.update { state ->
            val updatedPlayers = state.players.toMutableList()
            val me = updatedPlayers[state.currentPlayerIndex]
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksGiven = me.drinksGiven + amount)
            state.copy(players = updatedPlayers)
        }
    }
    private fun drawCard(requestedType: CardType) {
        _uiState.update { state ->
            var cardToDisplay: Card? = null

            // On récupère les piles actuelles
            val chanceStack = state.chanceCardsStack
            val miniGameStack = state.miniGameCardsStack

            // RÈGLE : Si c'est Mini-Jeu mais qu'il n'y en a plus -> On prend Chance
            val effectiveType = if (requestedType == CardType.MINI_JEU && miniGameStack.isEmpty()) {
                CardType.CHANCE
            } else {
                requestedType
            }

            if (effectiveType == CardType.MINI_JEU) {
                // Pioche Mini-Jeu
                if (miniGameStack.isNotEmpty()) {
                    cardToDisplay = miniGameStack.removeAt(0) // On prend la première
                    // Optionnel : On la remet au fond si tu veux des cycles infinis,
                    // mais ta règle suggère qu'elles s'épuisent, donc on ne la remet pas pour l'instant.
                }
            } else {
                // Pioche Chance
                if (chanceStack.isNotEmpty()) {
                    cardToDisplay = chanceStack.removeAt(0)
                } else {
                    // Sécurité : Si TOUT est vide (plus de chance, plus de mini-jeu)
                    cardToDisplay = Card(0, "Plus aucune carte disponible ! Reposez-vous.", CardType.CHANCE)
                }
            }

            state.copy(
                turnState = TurnState.CARD_DRAW_ACTION,
                currentCard = cardToDisplay,
                // On sauvegarde les listes modifiées (une carte en moins)
                chanceCardsStack = chanceStack,
                miniGameCardsStack = miniGameStack
            )
        }
    }

    // Fonction pour fermer la carte
    fun onDismissCard() {
        _uiState.update { it.copy(
            turnState = TurnState.POST_CASE_ACTIONS,
            currentCard = null
        )}
    }
    // Fonction appelée quand on clique sur "OK" dans le message spécial
    fun onDismissSpecialEvent() {
        val state = _uiState.value

        when {
            // CAS 1 : On vient de tenter une évasion
            state.isEscapingPrison -> {
                if (state.currentPlayer.inPrison) {
                    // Échec : Le joueur est toujours en prison -> Fin du tour
                    _uiState.update { it.copy(
                        isEscapingPrison = false,
                        turnState = TurnState.POST_CASE_ACTIONS
                    )}
                } else {
                    // Succès : Le joueur est libre -> Il avance du montant des dés
                    _uiState.update { it.copy(isEscapingPrison = false) }
                    movePlayer(state.diceResult) // On utilise le résultat du lancer d'évasion
                }
            }

            // CAS 2 : On vient de passer la Case Départ
            state.isResolvingStartPass -> {
                _uiState.update { it.copy(isResolvingStartPass = false) }
                resolveCurrentCase()
            }

            // CAS 3 : Autres messages (Simple info) -> Fin du tour
            else -> {
                _uiState.update { it.copy(turnState = TurnState.POST_CASE_ACTIONS) }
            }
        }
    }

    // --- LOGIQUE D'ACHAT SPÉCIFIQUE (1 DÉ) ---
    fun onRollForPurchase() {
        val state = _uiState.value
        if (state.purchaseResult == "SUCCESS" || state.purchaseAttempts >= 2) return

        viewModelScope.launch {
            // Animation sur la variable lastPurchaseRoll pour l'affichage
            repeat(10) {
                _uiState.update { it.copy(lastPurchaseRoll = Random.nextInt(1, 7)) }
                delay(80)
            }

            // Vrai calcul
            val roll = Random.nextInt(1, 7)
            val success = roll >= state.purchaseTarget
            val newAttempts = state.purchaseAttempts + 1

            // On met à jour les gorgées bues tout de suite
            val updatedPlayers = state.players.toMutableList()
            val me = updatedPlayers[state.currentPlayerIndex]
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksTaken = me.drinksTaken + roll)

            // Logique de succès/échec
            if (success) {
                val currentPos = state.currentPlayer.position
                val currentCase = state.board[currentPos]
                val newCase = currentCase.copy(ownerId = me.id)
                val newBoard = state.board.toMutableList()
                newBoard[currentPos] = newCase
                updatedPlayers[state.currentPlayerIndex] = updatedPlayers[state.currentPlayerIndex].copy(ownedCases = me.ownedCases + currentCase.id)

                _uiState.update { it.copy(
                    board = newBoard,
                    players = updatedPlayers,
                    lastPurchaseRoll = roll,
                    purchaseAttempts = newAttempts,
                    purchaseResult = "SUCCESS",
                    turnState = TurnState.PROPERTY_BUY_ACTION
                )}
            } else {
                val resultState = if (newAttempts >= 2) "FAILED_FINAL" else "FAILED_RETRY"
                _uiState.update { it.copy(
                    players = updatedPlayers,
                    lastPurchaseRoll = roll,
                    purchaseAttempts = newAttempts,
                    purchaseResult = resultState,
                    // Si échec final, on reste sur l'écran pour montrer le résultat, sinon on update juste l'état
                    turnState = TurnState.PROPERTY_BUY_ACTION
                )}
            }
        }
    }

    fun onSkipBuy() {
        _uiState.update { it.copy(turnState = TurnState.POST_CASE_ACTIONS) }
    }

    fun onEndTurn() {
        _uiState.update { state ->
            val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
            state.copy(
                currentPlayerIndex = nextIndex,
                turnState = TurnState.START_TURN,
                diceResult = 0,
                isDoubles = false
            )
        }
        advanceGameLoop()
    }
    /**
     * Applique les gorgées (Boire ou Donner) et termine l'action
     */
    fun onConfirmRent() {
        val state = _uiState.value
        val currentCase = state.board[state.currentPlayer.position]
        val rent = state.pendingRent
        val updatedPlayers = state.players.toMutableList()
        val me = updatedPlayers[state.currentPlayerIndex]

        if (currentCase.ownerId == me.id) {
            // C'est chez moi -> Je donne (j'ajoute aux stats drinksGiven)
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksGiven = me.drinksGiven + rent)
        } else {
            // C'est chez l'autre -> Je bois (j'ajoute aux stats drinksTaken)
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksTaken = me.drinksTaken + rent)

            // Optionnel : On peut aussi ajouter aux stats "drinksGiven" du propriétaire
            val ownerIndex = updatedPlayers.indexOfFirst { it.id == currentCase.ownerId }
            if (ownerIndex != -1) {
                val owner = updatedPlayers[ownerIndex]
                updatedPlayers[ownerIndex] = owner.copy(drinksGiven = owner.drinksGiven + rent)
            }
        }

        _uiState.update { it.copy(
            players = updatedPlayers,
            turnState = TurnState.POST_CASE_ACTIONS,
            pendingRent = 0
        )}
    }

    // Calcul du loyer (Bar ou Famille)
    private fun calculateRent(case: BoardCase, players: List<Player>): Int {
        if (case.type == CaseType.BAR) {
            val owner = players.find { it.id == case.ownerId } ?: return 0
            // On compte les bars (IDs supposés 6, 16, 26, 36)
            val nbBars = owner.ownedCases.count { id -> listOf(6, 16, 26, 36).contains(id) }
            return nbBars * 4
        } else {
            return case.familyId ?: 1
        }
    }

}
