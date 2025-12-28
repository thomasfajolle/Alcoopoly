package com.example.alcoopoly.ui.game

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
    onDismiss: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    // États pour gérer les sous-menus
    var showRules by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    var showQuitConfirm by remember { mutableStateOf(false) }

    if (showRules) {
        FullRulesDialog(onDismiss = { showRules = false })
    } else if (showSettings) {
        SettingsDialog(onDismiss = { showSettings = false })
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
        // --- MENU PRINCIPAL ---
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
                    ) {
                        Text("Quitter la partie")
                    }
                }
            },
            confirmButton = {} // Pas de bouton par défaut, on gère tout dans le content
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
                    .verticalScroll(rememberScrollState()), // Permet de scroller si le texte est long
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Intro
                Text(
                    "Le but est simple : Survivre et devenir le plus grand propriétaire foncier (ou le dernier debout).",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Divider()

                // 1. Déplacements
                RuleSection(
                    icon = "🎲",
                    title = "DÉPLACEMENT & DOUBLES",
                    content = "Lance les 2 dés pour avancer.\n" +
                            "• Si tu fais un DOUBLE : Tu distribues la valeur d'un dé en gorgées et tu rejoues.\n" +
                            "• 3 Doubles à la suite = Prison directe !"
                )

                // 2. Propriétés
                RuleSection(
                    icon = "🏠",
                    title = "LES PROPRIÉTÉS",
                    content = "• Case LIBRE : Tu peux tenter de l'acheter. Lance 1 dé. Si tu fais le score demandé (ou plus), c'est à toi ! Sinon, tu bois le résultat du dé.\n" +
                            "• Case POSSÉDÉE : Tu bois le loyer indiqué au propriétaire.\n" +
                            "• Posséder toutes les cartes d'une couleur double les loyers !"
                )

                // 3. Prison
                RuleSection(
                    icon = "👮",
                    title = "LE BAR'BAN (Prison)",
                    content = "Tu es bloqué ici.\n" +
                            "Pour sortir, tu dois lancer les dés et faire un score de 8 ou plus.\n" +
                            "• Réussite : Tu sors et tu avances.\n" +
                            "• Échec : Tu bois le total des dés et tu restes bloqué."
                )

                // 4. Bassine
                RuleSection(
                    icon = "🪣",
                    title = "LA BASSINE",
                    content = "Prévoyez un verre commun au milieu de la table.\n" +
                            "• Case REMPLIR : Verse un peu de ton verre dans la bassine.\n" +
                            "• Case BOIRE : Bois tout le contenu de la bassine (Cul Sec) !"
                )

                // 5. Bar'bu
                RuleSection(
                    icon = "🍺",
                    title = "LE BAR'BU",
                    content = "C'est une zone de consommation pure.\n" +
                            "On ne peut pas acheter ces cases.\n" +
                            "Si personne ne possède la case : Tu bois juste un coup.\n" +
                            "Si quelqu'un possède la case (via carte chance) : Tu paies le loyer."
                )

                Divider()

                Text(
                    "L'abus d'alcool est dangereux pour la santé. Sachez vous arrêter ou passer votre tour si nécessaire.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("C'est compris !")
            }
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