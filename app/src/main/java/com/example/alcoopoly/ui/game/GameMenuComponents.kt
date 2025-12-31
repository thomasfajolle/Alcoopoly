package com.example.alcoopoly.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameMenuDialog(
    gameState: com.example.alcoopoly.model.game.GameState, // On a besoin de l'état pour les switchs
    onDismiss: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
    onToggleSound: () -> Unit,      // Callback
    onToggleVibration: () -> Unit,   // Callback
    viewModel: com.example.alcoopoly.ui.game.GameViewModel
) {
    var showRules by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showCardMode by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    var showQuitConfirm by remember { mutableStateOf(false) }

    if (showCardMode) {
        // On affiche le dialogue de mode cartes
        CardModeDialog(
            viewModel = viewModel,
            onDismiss = { showCardMode = false }
        )
    } else if (showRules) {
        FullRulesDialog(onDismiss = { showRules = false })
    } else if (showSettings) {
        SettingsDialog(
            isSoundOn = gameState.isSoundEnabled,
            isVibrationOn = gameState.isVibrationEnabled,
            onToggleSound = onToggleSound,
            onToggleVibration = onToggleVibration,
            onDismiss = { showSettings = false }
        )
    } else if (showCredits) {
        CreditsDialog(onDismiss = { showCredits = false })
    } else if (showQuitConfirm) {
        AlertDialog(
            onDismissRequest = { showQuitConfirm = false },
            title = { Text("Quitter la partie ?") },
            text = { Text("Toute progression sera perdue. Voulez-vous vraiment retourner à l'accueil ?") },
            confirmButton = {
                Button(
                    onClick = onQuit,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Oui, quitter") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showQuitConfirm = false }) { Text("Annuler") }
            }
        )
    } else {
        // MENU PRINCIPAL
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MENU PAUSE")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuButton("Reprendre") { onDismiss() }
                    MenuButton("🃏 Mode Cartes Uniquement") { showCardMode = true }
                    MenuButton("Recommencer la partie") {
                        onRestart()
                        onDismiss()
                    }
                    Divider()
                    MenuButton("Règles complètes") { showRules = true }
                    MenuButton("Paramètres") { showSettings = true }
                    MenuButton("Crédits") { showCredits = true }
                    Divider()
                    Button(
                        onClick = { showQuitConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Quitter la partie") }
                }
            },
            confirmButton = {}
        )
    }
}

// --- BOÎTE DE DIALOGUE PARAMÈTRES (Mise à jour) ---
@Composable
fun SettingsDialog(
    isSoundOn: Boolean,
    isVibrationOn: Boolean,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paramètres") },
        text = {
            Column {
                // Son
                SettingsRow(
                    label = "Effets Sonores",
                    checked = isSoundOn,
                    onCheckedChange = { onToggleSound() }
                )

                // Vibration
                SettingsRow(
                    label = "Vibrations",
                    checked = isVibrationOn,
                    onCheckedChange = { onToggleVibration() }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Note : Le mode sombre suit les paramètres de votre téléphone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Retour") } }
    )
}

@Composable
fun SettingsRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurface)
    }
}

// --- SOUS-MENUS ---

@Composable
fun FullRulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Règles du Jeu", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Intro & Matériel
                Text(
                    "Le but : Survivre et devenir le plus grand propriétaire (ou le dernier debout).",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                // --- AJOUT POINT 1 : MATÉRIEL ---
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.3f)).padding(8.dp)) {
                    Text(
                        "📱 Note : Ce jeu se joue sur un seul téléphone qui passe de main en main.\n" +
                                "🎲 Prévoyez de vrais dés et un jeu de cartes physique pour certains défis !",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider()

                // 1. Déplacements
                RuleSection(
                    icon = "🎲",
                    title = "DÉPLACEMENT & DOUBLES",
                    content = "Lance les dés virtuels pour avancer.\n" +
                            "• DOUBLE : Distribue la valeur d'un dé en gorgées et rejoue.\n" +
                            "• 3 Doubles à la suite = Prison directe !"
                )

                // 2. Propriétés & Bars
                RuleSection(
                    icon = "🏠",
                    title = "PROPRIÉTÉS & BARS",
                    content = "• Case LIBRE : Tu peux l'acheter. Lance 1 dé : si tu fais le score cible, c'est à toi ! Sinon, tu bois le résultat.\n" +
                            "• Case POSSÉDÉE : Tu bois le loyer indiqué au propriétaire.\n" +
                            "• COULEUR : Avoir toutes les propriétés d'une couleur double les loyers !"
                )

                // --- MODIF POINT 2 : LES BARS ---
                RuleSection(
                    icon = "🍺",
                    title = "LES BARS (Ex: Bar'bu)",
                    content = "Ils fonctionnent comme des propriétés spéciales.\n" +
                            "Plus tu possèdes de Bars différents, plus le loyer que les autres te paient est élevé !"
                )

                // 3. Prison
                RuleSection(
                    icon = "👮",
                    title = "LE BAR'BAN (Prison)",
                    content = "Tu es bloqué. Pour sortir : fais un score de 8+ aux dés.\n" +
                            "• Réussite : Tu sors et avances.\n" +
                            "• Échec : Tu bois le total et restes bloqué."
                )

                // 4. Bassine
                RuleSection(
                    icon = "🪣",
                    title = "LA BASSINE",
                    content = "Verre commun au centre.\n" +
                            "• Case REMPLIR : Verse un peu de ton verre.\n" +
                            "• Case BOIRE : Cul sec de la bassine !"
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("C'est compris !") }
        }
    )
}

// Petit composant utilitaire pour faire joli
@Composable
fun RuleSection(icon: String, title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CardModeDialog(
    viewModel: com.example.alcoopoly.ui.game.GameViewModel,
    onDismiss: () -> Unit
) {
    // État pour afficher la carte tirée
    var currentCard by remember { mutableStateOf<com.example.alcoopoly.model.Card?>(null) }

    if (currentCard != null) {
        // Si une carte est tirée, on l'affiche (on réutilise ton dialogue existant)
        com.example.alcoopoly.ui.game.CardDisplayDialog(
            card = currentCard!!,
            onDismiss = { currentCard = null } // Quand on ferme la carte, on revient au choix
        )
    } else {
        // Choix du type de carte
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Mode Fin de Soirée 🥴") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Plus la force de jouer ? Tirez juste des cartes !", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { currentCard = viewModel.drawRandomCardOnly(com.example.alcoopoly.data.enums.CardType.CHANCE) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Tirer une CHANCE 🍀")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { currentCard = viewModel.drawRandomCardOnly(com.example.alcoopoly.data.enums.CardType.MINI_JEU) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Tirer un MINI-JEU 🎲")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Retour au menu") }
            }
        )
    }
}
@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paramètres") },
        text = {
            Column {
                Text("Bientôt disponible :", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(16.dp))
                SettingsRow("Sons", false)
                SettingsRow("Vibrations", false)
                SettingsRow("Mode Sombre", true)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Retour") } }
    )
}

@Composable
fun SettingsRow(label: String, checked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = { /* TODO later */ }, enabled = false)
    }
}

@Composable
fun CreditsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crédits") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("ALCOOPOLY", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Développé avec ❤️ (et de la bière)")
                Text("Version 1.0")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Merci à toute l'équipe de test !", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Fermer") } }
    )
}