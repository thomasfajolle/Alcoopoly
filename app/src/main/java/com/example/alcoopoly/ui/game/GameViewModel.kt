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
    fun startNewGame(playerDataList: List<String>) {
        if (_uiState.value.players.isNotEmpty()) return

        val colors = listOf(
            0xFFFF5252, 0xFF448AFF, 0xFF69F0AE, 0xFFFFD740, 0xFFE040FB, 0xFFFF6E40
        )

        val newPlayers = playerDataList.mapIndexed { index, dataString ->
            // On sépare le Nom et l'Avatar avec le caractère "|"
            val parts = dataString.split("|")
            val name = parts.getOrElse(0) { "Joueur $index" }
            val avatar = parts.getOrElse(1) { "😊" } // Emoji par défaut si bug

            val color = colors.getOrElse(index) { 0xFF9E9E9E }

            Player(
                id = index + 1,
                name = name,
                color = color,
                avatar = avatar // <--- On l'enregistre ici
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
        viewModelScope.launch {
            // 1. On sauvegarde la position de départ pour savoir si on a passé la case départ à la fin
            val startPosition = _uiState.value.currentPlayer.position
            var currentPosition = startPosition

            // 2. BOUCLE D'ANIMATION : On avance case par case
            repeat(steps) {
                delay(350) // Vitesse du déplacement (350ms par case = rythme agréable)

                // Calcul de la case suivante (+1)
                currentPosition = (currentPosition + 1) % 40

                // Mise à jour de l'affichage pour voir le pion bouger
                _uiState.update { state ->
                    val updatedPlayers = state.players.toMutableList()
                    val me = updatedPlayers[state.currentPlayerIndex]
                    updatedPlayers[state.currentPlayerIndex] = me.copy(position = currentPosition)

                    // On update l'état pour déclencher le scroll automatique de la liste
                    state.copy(players = updatedPlayers)
                }
            }

            // 3. EFFET FOCUS FINAL
            // Une fois arrivé, on attend un peu pour que le joueur voie la case "zoomer"
            delay(800)

            // 4. LOGIQUE DES RÈGLES (Une fois l'animation finie)
            // On recalcule si on a passé le départ en comparant le début et la fin
            val finalPosition = currentPosition
            val passedStart = finalPosition < startPosition // Si 2 < 38, on a bouclé
            val isLandingOnStart = finalPosition == 0

            if (passedStart && !isLandingOnStart) {
                // --- PASSAGE DÉPART (Bonus +5) ---
                _uiState.update { state ->
                    val updatedPlayers = state.players.toMutableList()
                    val me = updatedPlayers[state.currentPlayerIndex]
                    updatedPlayers[state.currentPlayerIndex] = me.copy(drinksGiven = me.drinksGiven + 5)

                    state.copy(
                        players = updatedPlayers,
                        turnState = TurnState.SPECIAL_EVENT_ACTION,
                        eventTitle = "🍷 Cave Départ (Passage)",
                        eventMessage = "Tu passes devant la Cave ! Distribue 5 gorgées.",
                        isResolvingStartPass = true
                    )
                }
            } else {
                // --- RÉSOLUTION NORMALE ---
                resolveCurrentCase()
            }
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

    // Fonction appelée quand on clique sur "OK" sur une carte
    fun onDismissCard() {
        val state = _uiState.value
        val card = state.currentCard

        // On ferme d'abord la carte visuellement
        _uiState.update { it.copy(
            turnState = TurnState.POST_CASE_ACTIONS, // Par défaut, on finit le tour
            currentCard = null
        )}

        // Ensuite, on applique les effets spéciaux de déplacement si besoin
        if (card != null) {
            applyCardEffect(card)
        }
    }

    private fun applyCardEffect(card: Card) {
        viewModelScope.launch {
            // Petite pause pour que ce soit naturel après la fermeture de la fenêtre
            delay(500)

            when (card.id) {
                // --- RETOUR CAVE DÉPART ---
                106, 141 -> {
                    teleportPlayer(0, "Oups... Retour à la case départ !")
                }

                // --- SOIRÉE BDE (Case 36) ---
                105 -> {
                    // La case 36 est à l'index 35
                    teleportPlayer(35, "Téléportation à la Soirée BDE !")
                }

                // --- DATE ELISA (Case 38) ---
                146 -> {
                    // La case 38 est à l'index 37
                    teleportPlayer(37, "Bonne chance pour ton date...")
                }

                // --- MERCREDI (Bar'bu - Case 16) ---
                145 -> {
                    teleportPlayer(15, "Direction le Bar'bu !")
                }

                // --- SPACE CAKE (Reculer) ---
                // ID 143 : "Fais les deux prochains tours en reculant"
                // C'est complexe à coder (état persistant), pour l'instant on fait reculer de 3 cases direct
                143 -> {
                    val currentPos = _uiState.value.currentPlayer.position
                    val newPos = (currentPos - 3 + 40) % 40
                    teleportPlayer(newPos, "Tu es trop défoncé... Tu recules.")
                }
            }
        }
    }

    // Fonction utilitaire pour déplacer le joueur sans lancer les dés
    private suspend fun teleportPlayer(targetIndex: Int, message: String) {
        // 1. Mise à jour de la position
        _uiState.update { state ->
            val updatedPlayers = state.players.toMutableList()
            updatedPlayers[state.currentPlayerIndex] = updatedPlayers[state.currentPlayerIndex].copy(position = targetIndex)
            state.copy(players = updatedPlayers)
        }

        // 2. On déclenche un petit message pour expliquer ce qui se passe
        triggerSpecialEvent(
            title = "✨ TÉLÉPORTATION",
            message = message
        )

        // Note : Après le clic sur "OK" de ce message, resolveCurrentCase sera appelé si besoin
        // via la logique existante de onDismissSpecialEvent, ou on finit le tour.
        // Ici, on a mis turnState à POST_CASE_ACTIONS dans onDismissCard,
        // donc le triggerSpecialEvent va repasser l'état à SPECIAL_EVENT_ACTION.
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
        // Sécurités
        if (state.purchaseResult == "SUCCESS" || state.purchaseAttempts >= 2 || state.isRolling) return

        viewModelScope.launch {
            // 1. DÉBUT ANIMATION
            // On active le mode "Roulement" et on efface le résultat précédent pour que ce soit neutre
            _uiState.update { it.copy(
                isRolling = true,
                purchaseResult = "" // On vide le statut (plus de "Raté" ou "Bravo" affiché)
            )}

            // 2. ANIMATION (Chiffres qui défilent)
            repeat(15) { // Un peu plus long pour le suspense (15 x 60ms = ~1 sec)
                _uiState.update { it.copy(lastPurchaseRoll = Random.nextInt(1, 7)) }
                delay(60)
            }

            // 3. VRAI CALCUL
            val roll = Random.nextInt(1, 7)
            val success = roll >= state.purchaseTarget
            val newAttempts = state.purchaseAttempts + 1

            // On met à jour les gorgées bues
            val updatedPlayers = state.players.toMutableList()
            val me = updatedPlayers[state.currentPlayerIndex]
            updatedPlayers[state.currentPlayerIndex] = me.copy(drinksTaken = me.drinksTaken + roll)

            // 4. RÉSULTAT FINAL
            if (success) {
                // --- SUCCÈS ---
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
                    isRolling = false, // Fin de l'animation
                    turnState = TurnState.PROPERTY_BUY_ACTION
                )}
            } else {
                // --- ÉCHEC ---
                val resultState = if (newAttempts >= 2) "FAILED_FINAL" else "FAILED_RETRY"
                _uiState.update { it.copy(
                    players = updatedPlayers,
                    lastPurchaseRoll = roll,
                    purchaseAttempts = newAttempts,
                    purchaseResult = resultState,
                    isRolling = false, // Fin de l'animation
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
