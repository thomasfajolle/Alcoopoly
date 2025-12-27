package com.example.alcoopoly.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alcoopoly.data.enums.CardType
import com.example.alcoopoly.model.Card
import com.example.alcoopoly.model.game.GameState
import com.example.alcoopoly.model.game.TurnState
import com.example.alcoopoly.ui.game.components.BoardListView
import com.example.alcoopoly.ui.game.tabs.CardsListScreen
import com.example.alcoopoly.ui.game.tabs.StatsScreen

// Enumération pour gérer les 3 onglets
enum class GameTab { BOARD, STATS, CARDS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    playerNames: List<String>,
    viewModel: GameViewModel = viewModel()
) {
    // 1. Initialisation de la partie
    LaunchedEffect(Unit) {
        viewModel.startNewGame(playerNames)
    }

    // 2. Récupération des états
    val gameState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(GameTab.BOARD) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Place pour un futur logo (décommente la ligne ci-dessous quand tu auras une image)
                        // Image(painter = painterResource(id = R.drawable.logo), contentDescription = null, modifier = Modifier.size(32.dp))
                        // Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "ALCOOPOLY",
                            style = MaterialTheme.typography.headlineMedium, // Plus gros
                            fontWeight = FontWeight.Black, // Plus gras
                            letterSpacing = 2.sp // Espacement des lettres style "Cinéma"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Classement") },
                    selected = currentTab == GameTab.STATS,
                    onClick = { currentTab = GameTab.STATS }
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
                GameTab.STATS -> StatsScreen(gameState.players)
                GameTab.CARDS -> CardsListScreen()
            }

            // 5. LES DIALOGUES (S'affichent par-dessus tout, peu importe l'onglet)

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
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
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
                if (gameState.diceResult > 0 || gameState.isRolling) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (gameState.isRolling) Color(0xFFFFD700) else Color.White, // Jaune quand ça roule
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "🎲 ${gameState.diceResult}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // --- PLATEAU ---
        Text(
            text = "Plateau de jeu",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
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

            modifier = Modifier.fillMaxWidth().height(56.dp)
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
    isRolling: Boolean, // <--- Nouveau paramètre
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
                isRolling -> Text("🎲 Lancer en cours...") // Titre neutre pendant l'anim
                isSuccess -> Text("🎉 BRAVO ! 🎉")
                isFinalFailure -> Text("💀 ÉCHEC FINAL 💀")
                else -> Text("Propriété Libre ! 🎲")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isFinished && !isRolling) {
                    // --- RÉSULTAT FINAL (Fixe) ---
                    Text("Tu as fait :", style = MaterialTheme.typography.bodyMedium)
                    Text("$lastRoll", style = MaterialTheme.typography.displayMedium,
                        color = if(isSuccess) Color(0xFF4CAF50) else Color.Red,
                        fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🍺 TU BOIS $lastRoll GORGÉES", fontWeight = FontWeight.Bold, color = Color.Red)

                    Spacer(modifier = Modifier.height(16.dp))
                    if (isSuccess) {
                        Text("Cette propriété est maintenant à toi !", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("Tu as raté tes 2 essais... La propriété reste libre.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    // --- ÉCRAN DE JEU (ou Animation) ---
                    Text("Vous êtes sur : $caseName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("OBJECTIF : Faire $targetScore+ avec 1 dé", fontWeight = FontWeight.Bold)

                    if (!isRolling) {
                        Text("Tentative : ${attempts + 1} / 2")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("⚠️ Tu bois le dé quoi qu'il arrive !", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    // Affiche le dé si on a lancé ou si ça tourne
                    if (lastRoll > 0) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Si ça roule : Couleur OR (neutre). Sinon : ROUGE (car on boit).
                        val diceColor = if (isRolling) Color(0xFFFFD700) else Color.Red

                        Text(
                            text = if (isRolling) "$lastRoll" else "Résultat : $lastRoll",
                            style = MaterialTheme.typography.headlineMedium,
                            color = diceColor,
                            fontWeight = FontWeight.Bold
                        )

                        // On n'affiche le message d'échec que si l'animation est FINIE
                        if (!isRolling && resultState == "FAILED_RETRY") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Raté ! Mais tu peux retenter.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isRolling) {
                // Bouton désactivé pendant l'animation
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
            // On cache le bouton abandonner pendant que ça roule ou si c'est fini
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