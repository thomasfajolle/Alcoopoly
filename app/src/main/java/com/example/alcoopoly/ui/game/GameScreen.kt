package com.example.alcoopoly.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alcoopoly.model.game.TurnState
import com.example.alcoopoly.ui.game.components.BoardListView
import com.example.alcoopoly.model.Card
import com.example.alcoopoly.data.enums.CardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Alcoopoly - Tour ${gameState.turnNumber}") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
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
                    if (gameState.diceResult > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text("🎲 ${gameState.diceResult}", style = MaterialTheme.typography.titleMedium, color = Color.Black)
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
                        TurnState.PRISON_TURN -> viewModel.onRollPrison() // <--- AJOUTE ÇA
                        TurnState.POST_CASE_ACTIONS -> viewModel.onEndTurn()
                        else -> { }
                    }
                },
                // Active le bouton si on est en Prison
                enabled = gameState.turnState == TurnState.ROLL_DICE ||
                        gameState.turnState == TurnState.POST_CASE_ACTIONS ||
                        gameState.turnState == TurnState.PRISON_TURN,

                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = when (gameState.turnState) {
                        TurnState.ROLL_DICE -> "🎲 LANCER LES DÉS"
                        TurnState.PRISON_TURN -> "⛓️ TENTER L'ÉVASION (8+)" // <--- TEXTE SPÉCIFIQUE
                        TurnState.MOVE_PLAYER -> "Déplacement..."
                        TurnState.RESOLVE_CASE -> "Résolution..."
                        TurnState.POST_CASE_ACTIONS -> "FIN DU TOUR"
                        else -> "..."
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // --- DIALOGUE D'ACHAT (LOGIQUE DÉ) ---
        if (gameState.turnState == TurnState.PROPERTY_BUY_ACTION) {
            val currentCase = gameState.board[gameState.currentPlayer.position]

            BuyPropertyDialog(
                caseName = currentCase.name,
                targetScore = gameState.purchaseTarget,
                attempts = gameState.purchaseAttempts,
                lastRoll = gameState.lastPurchaseRoll,
                resultState = gameState.purchaseResult,
                onRoll = { viewModel.onRollForPurchase() },
                onPass = { viewModel.onSkipBuy() }
            )
        }
        // ... Bloc d'achat existant ...

        // --- DIALOGUE LOYER / DISTRIBUTION ---
        if (gameState.turnState == TurnState.RENT_PAYMENT_ACTION) {
            val currentCase = gameState.board[gameState.currentPlayer.position]
            // On cherche le nom du propriétaire
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
        // --- DIALOGUE ÉVÉNEMENT SPÉCIAL (Bassine, Prison, Départ...) ---
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

// COMPOSANT FENÊTRE D'ACHAT
@Composable
fun BuyPropertyDialog(
    caseName: String,
    targetScore: Int,
    attempts: Int,
    lastRoll: Int,
    resultState: String,
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
                isSuccess -> Text("🎉 BRAVO ! 🎉")
                isFinalFailure -> Text("💀 ÉCHEC FINAL 💀")
                else -> Text("Propriété Libre ! 🎲")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isFinished) {
                    // --- RÉSULTAT FINAL (Succès OU Échec) ---
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
                    // --- EN COURS ---
                    Text("Vous êtes sur : $caseName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("OBJECTIF : Faire $targetScore+ avec 1 dé", fontWeight = FontWeight.Bold)
                    Text("Tentative : ${attempts + 1} / 2")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("⚠️ Tu bois le dé quoi qu'il arrive !", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    if (lastRoll > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Dernier lancer : $lastRoll 🍺", style = MaterialTheme.typography.headlineSmall, color = Color.Red)
                        Text("Raté ! Mais tu peux retenter.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            if (isFinished) {
                // Bouton de fin (Succès ou Échec total)
                Button(onClick = onPass) {
                    Text(if (isSuccess) "OK, super !" else "Tant pis...")
                }
            } else {
                // Bouton de jeu
                Button(onClick = onRoll) {
                    Text(if (attempts == 0) "🎲 Lancer le dé" else "🎲 Retenter")
                }
            }
        },
        dismissButton = {
            if (!isFinished) {
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
        onDismissRequest = { }, // On oblige à cliquer sur le bouton
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
                    Text("👉 DISTRIBUE", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50)) // Vert
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
    // Couleur selon le type
    val cardColor = if (card.type == CardType.MINI_JEU) Color(0xFFCE93D8) else Color(0xFFFFCC80)
    val title = if (card.type == CardType.MINI_JEU) "🎮 MINI-JEU" else "🍀 CHANCE"

    AlertDialog(
        onDismissRequest = { },
        containerColor = cardColor, // Fond coloré
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
                    style = MaterialTheme.typography.headlineSmall, // Texte en gros
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