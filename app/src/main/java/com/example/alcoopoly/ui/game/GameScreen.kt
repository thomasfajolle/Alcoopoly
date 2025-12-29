package com.example.alcoopoly.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alcoopoly.data.enums.CardType
import com.example.alcoopoly.model.Card
import com.example.alcoopoly.model.game.GameState
import com.example.alcoopoly.model.game.TurnState
import com.example.alcoopoly.ui.components.DieView
import com.example.alcoopoly.ui.components.PrisonAnimation
import com.example.alcoopoly.ui.game.components.BoardListView
import com.example.alcoopoly.ui.game.tabs.CardsListScreen
import com.example.alcoopoly.ui.game.tabs.PortfolioScreen

// Enumération pour gérer les 3 onglets
enum class GameTab { BOARD, PORTFOLIO, CARDS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    playerNames: List<String>,
    viewModel: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateHome: () -> Unit
) {
    // 1. Initialisation de la partie
    // On vérifie que la partie n'est pas déjà lancée pour éviter de reset si l'écran tourne
    val gameState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // GESTION VIBRATION
    LaunchedEffect(gameState.triggerPrisonAnim) {
        // Si l'animation prison se lance ET que les vibrations sont activées
        if (gameState.triggerPrisonAnim && gameState.isVibrationEnabled) {
            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            // Vibre pendant 500ms (compatible anciennes versions Android)
            vibrator?.vibrate(500)
        }
    }

    // 2. États locaux
    var showMenu by remember { mutableStateOf(false) } // État pour afficher le menu
    var currentTab by remember { mutableStateOf(GameTab.BOARD) }

    LaunchedEffect(Unit) {
        // On lance seulement si la liste des joueurs est vide (première fois)
        if (gameState.players.isEmpty()) {
            viewModel.startNewGame(playerNames)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Alcoopoly",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    // BOUTON PARAMÈTRES (ROUE DENTÉE)
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            // 3. Barre de Navigation en bas
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Plateau") },
                    selected = currentTab == GameTab.BOARD,
                    onClick = { currentTab = GameTab.BOARD }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                    label = { Text("Joueurs") },
                    selected = currentTab == GameTab.PORTFOLIO,
                    onClick = { currentTab = GameTab.PORTFOLIO }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Cartes") },
                    selected = currentTab == GameTab.CARDS,
                    onClick = { currentTab = GameTab.CARDS }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {

            // 4. Contenu principal qui change selon l'onglet
            when (currentTab) {
                GameTab.BOARD -> BoardTabContent(gameState, viewModel)
                GameTab.PORTFOLIO -> PortfolioScreen(
                    players = gameState.players,
                    board = gameState.board,
                    onPlayerQuit = { playerId ->
                        viewModel.onPlayerQuit(playerId)
                    }
                )
                GameTab.CARDS -> CardsListScreen(
                    chanceCards = gameState.allChanceCards,
                    miniGameCards = gameState.allMiniGameCards,
                    onAddCard = { type, title, desc -> viewModel.addCustomCard(type, title, desc) },
                    onEditCard = { card, title, desc -> viewModel.updateCard(card, title, desc) },
                    onDeleteCard = { card -> viewModel.deleteCard(card) },
                    onRestoreCard = { card -> viewModel.restoreCard(card) }
                )
            }

            // 5. LES DIALOGUES (S'affichent par-dessus tout)

            // --- MENU PAUSE ---
            if (showMenu) {
                GameMenuDialog(
                    gameState = gameState, // <-- On passe l'état
                    onDismiss = { showMenu = false },
                    onRestart = {
                        showMenu = false
                        viewModel.restartGame()
                    },
                    onQuit = {
                        showMenu = false
                        onNavigateHome()
                    },
                    // Les nouveaux callbacks :
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleVibration = { viewModel.toggleVibration() }
                )
            }

            // --- DIALOGUE D'ACHAT ---
            if (gameState.turnState == TurnState.PROPERTY_BUY_ACTION) {
                val currentCase = gameState.board[gameState.currentPlayer.position]
                BuyPropertyDialog(
                    caseName = currentCase.name,
                    targetScore = gameState.purchaseTarget,
                    attempts = gameState.purchaseAttempts,
                    lastRoll = gameState.lastPurchaseRoll,
                    resultState = gameState.purchaseResult,
                    isRolling = gameState.isRolling,
                    onRoll = { viewModel.onRollForPurchase() },
                    onPass = { viewModel.onSkipBuy() }
                )
            }

            // --- DIALOGUE LOYER / DISTRIBUTION ---
            if (gameState.turnState == TurnState.RENT_PAYMENT_ACTION) {
                val currentCase = gameState.board[gameState.currentPlayer.position]
                val owner = gameState.players.find { it.id == currentCase.ownerId }
                val isMine = currentCase.ownerId == gameState.currentPlayer.id
                RentDialog(
                    caseName = currentCase.name,
                    ownerName = owner?.name ?: "Inconnu",
                    rentAmount = gameState.pendingRent,
                    isMyProperty = isMine,
                    onConfirm = { viewModel.onConfirmRent() }
                )
            }

            // --- DIALOGUE ÉVÉNEMENT SPÉCIAL ---
            if (gameState.turnState == TurnState.SPECIAL_EVENT_ACTION) {
                SpecialEventDialog(
                    title = gameState.eventTitle,
                    message = gameState.eventMessage,
                    onDismiss = { viewModel.onDismissSpecialEvent() }
                )
            }

            // --- DIALOGUE CARTE ---
            if (gameState.turnState == TurnState.CARD_DRAW_ACTION && gameState.currentCard != null) {
                CardDisplayDialog(
                    card = gameState.currentCard!!,
                    onDismiss = { viewModel.onDismissCard() }
                )
                // --- ANIMATION PRISON ---
                // On l'affiche si le GameState nous dit de le faire
                PrisonAnimation(
                    isVisible = gameState.triggerPrisonAnim,
                    onAnimationFinished = {
                        // Quand c'est fini (2 sec), on prévient le ViewModel pour qu'il reset la variable
                        viewModel.onPrisonAnimationFinished()
                    }
                )
            }
        }
    }
}

/**
 * Cette fonction contient tout ce qui concerne le Plateau de jeu pur.
 * (C'est ce qui était directement dans le Scaffold avant).
 */
@Composable
fun BoardTabContent(
    gameState: GameState,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- INFO JOUEUR ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tour actuel :", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = gameState.currentPlayer.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(gameState.currentPlayer.color.toInt())
                    )
                }
                // Affiche le dé si on a un résultat OU si c'est en train de rouler
                // Zone des Dés
                if (gameState.diceResult > 0 || gameState.isRolling) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // DÉ 1 (Utilise DieView avec une taille adaptée, ex: 30.dp ou 40.dp)
                        DieView(
                            value = gameState.die1,
                            isRolling = gameState.isRolling,
                            size = 40.dp
                        )
                        // DÉ 2
                        DieView(
                            value = gameState.die2,
                            isRolling = gameState.isRolling,
                            size = 40.dp
                        )
                    }
                }
            }
        }

        // --- PLATEAU ---
        Text(
            text = "Plateau de jeu",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        BoardListView(
            board = gameState.board,
            players = gameState.players,
            currentPlayerId = gameState.currentPlayer.id,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- BOUTON PRINCIPAL ---
        Button(
            onClick = {
                when (gameState.turnState) {
                    TurnState.ROLL_DICE -> viewModel.onRollDice()
                    TurnState.PRISON_TURN -> viewModel.onRollPrison()
                    TurnState.POST_CASE_ACTIONS -> viewModel.onEndTurn()
                    else -> { }
                }
            },
            enabled = !gameState.isRolling && ( // AJOUT DE !gameState.isRolling
                    gameState.turnState == TurnState.ROLL_DICE ||
                            gameState.turnState == TurnState.POST_CASE_ACTIONS ||
                            gameState.turnState == TurnState.PRISON_TURN),

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = when (gameState.turnState) {
                    TurnState.ROLL_DICE -> "🎲 LANCER LES DÉS"
                    TurnState.PRISON_TURN -> "⛓️ TENTER L'ÉVASION (8+)"
                    TurnState.MOVE_PLAYER -> "Déplacement..."
                    TurnState.RESOLVE_CASE -> "Résolution..."
                    TurnState.POST_CASE_ACTIONS -> "FIN DU TOUR"
                    else -> "..."
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ==========================================
//          COMPOSANTS DE DIALOGUE
// ==========================================

@Composable
fun BuyPropertyDialog(
    caseName: String,
    targetScore: Int,
    attempts: Int,
    lastRoll: Int,
    resultState: String,
    isRolling: Boolean,
    onRoll: () -> Unit,
    onPass: () -> Unit
) {
    val isSuccess = resultState == "SUCCESS"
    val isFinalFailure = resultState == "FAILED_FINAL"
    val isFinished = isSuccess || isFinalFailure

    AlertDialog(
        onDismissRequest = { },
        title = {
            when {
                isRolling -> Text("🎲 Lancer en cours...")
                isSuccess -> Text("🎉 BRAVO ! 🎉")
                isFinalFailure -> Text("💀 ÉCHEC FINAL 💀")
                else -> Text("Propriété Libre ! 🎲")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // --- CAS 1 : C'EST FINI (Gagné ou Perdu définitivement) ---
                if (isFinished && !isRolling) {
                    Text("Tu as fait :", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Affiche le DÉ STATIQUE (Gros)
                    DieView(value = lastRoll, isRolling = false, size = 80.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("🍺 TU BOIS $lastRoll GORGÉES", fontWeight = FontWeight.Bold, color = Color.Red)

                    Spacer(modifier = Modifier.height(16.dp))
                    if (isSuccess) {
                        Text("Cette propriété est maintenant à toi !", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    } else {
                        Text("Tu as raté tes 2 essais... La propriété reste libre.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    }
                }

                // --- CAS 2 : EN COURS (Premier lancer ou Retentative) ---
                else {
                    Text("Vous êtes sur : $caseName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("OBJECTIF : Faire $targetScore+ avec 1 dé", fontWeight = FontWeight.Bold)

                    if (!isRolling) {
                        Text("Tentative : ${attempts + 1} / 2")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("⚠️ Tu bois le dé quoi qu'il arrive !", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    // Affiche le dé si on a lancé ou si ça tourne
                    if (lastRoll > 0 || isRolling) {
                        Spacer(modifier = Modifier.height(24.dp))

                        val displayValue = if (lastRoll == 0) 1 else lastRoll

                        DieView(
                            value = displayValue,
                            isRolling = isRolling,
                            size = 70.dp
                        )

                        // --- C'EST ICI QUE J'AI FAIT LA MODIFICATION ---
                        if (!isRolling && resultState == "FAILED_RETRY") {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Message clair pour la boisson
                            Text(
                                text = "Raté ! Tu bois $lastRoll gorgées 🍺",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Mais tu as une 2ème chance !",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isRolling) {
                Button(onClick = {}, enabled = false) { Text("...") }
            } else if (isFinished) {
                Button(onClick = onPass) {
                    Text(if (isSuccess) "OK, super !" else "Tant pis...")
                }
            } else {
                Button(onClick = onRoll) {
                    Text(if (attempts == 0) "🎲 Lancer le dé" else "🎲 Retenter")
                }
            }
        },
        dismissButton = {
            if (!isFinished && !isRolling) {
                OutlinedButton(onClick = onPass) {
                    Text("Laisser tomber")
                }
            }
        }
    )
}
@Composable
fun RentDialog(
    caseName: String,
    ownerName: String,
    rentAmount: Int,
    isMyProperty: Boolean,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(if (isMyProperty) "🏠 Bienvenue chez toi !" else "💸 Paye ton loyer !")
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lieu : $caseName")
                Spacer(modifier = Modifier.height(16.dp))

                if (isMyProperty) {
                    Text("Tu es le propriétaire.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("👉 DISTRIBUE", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    Text("$rentAmount gorgées", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("à qui tu veux !", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Propriétaire : $ownerName", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🍺 TU BOIS", fontWeight = FontWeight.Bold, color = Color.Red)
                    Text("$rentAmount gorgées", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(if (isMyProperty) "C'est distribué !" else "C'est bu !")
            }
        }
    )
}

@Composable
fun SpecialEventDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("C'est fait !")
            }
        }
    )
}

@Composable
fun CardDisplayDialog(
    card: Card,
    onDismiss: () -> Unit
) {
    val cardColor = if (card.type == CardType.MINI_JEU) Color(0xFFCE93D8) else Color(0xFFFFCC80)
    val title = if (card.type == CardType.MINI_JEU) "🎮 MINI-JEU" else "🍀 CHANCE"

    AlertDialog(
        onDismissRequest = { },
        containerColor = cardColor,
        title = {
            Text(text = title, fontWeight = FontWeight.Black, fontSize = 24.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = card.description,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("OK, Compris", color = Color.White)
            }
        }
    )
}
@Composable
fun DiceSection(
    gameState: GameState,
    onRollDice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- LES DÉS VISUELS ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Si les dés valent 0 (début de tour), on affiche peut-être des points d'interrogation ou rien,
                // mais ici on affiche 1 par défaut ou la valeur actuelle.
                val d1 = if (gameState.die1 == 0) 1 else gameState.die1
                val d2 = if (gameState.die2 == 0) 1 else gameState.die2

                DieView(value = d1, isRolling = gameState.isRolling, size = 50.dp)
                DieView(value = d2, isRolling = gameState.isRolling, size = 50.dp)
            }

            // --- BOUTON LANCER ---
            Button(
                onClick = onRollDice,
                enabled = !gameState.isRolling && gameState.turnState == TurnState.ROLL_DICE,
                modifier = Modifier.height(50.dp)
            ) {
                Text(
                    text = if (gameState.isRolling) "..." else "LANCER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}