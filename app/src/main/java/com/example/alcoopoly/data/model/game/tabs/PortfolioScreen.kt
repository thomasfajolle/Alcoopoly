package com.example.alcoopoly.ui.game.tabs

import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoopoly.model.BoardCase
import com.example.alcoopoly.model.Player
import com.example.alcoopoly.ui.utils.getFamilyColor
import androidx.compose.runtime.getValue // Indispensable pour le 'by'
import androidx.compose.runtime.setValue // Indispensable pour le 'by'
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun PortfolioScreen(
    players: List<Player>,
    board: List<BoardCase>,
    onPlayerQuit: (Int) -> Unit // <--- On reçoit la fonction du ViewModel ici
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- TITRE ---
        item {
            Text(
                text = "💼 PORTEFEUILLES",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Qui possède quoi ?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // --- LISTE DES JOUEURS ---
        items(players) { player ->
            // C'est ici qu'on fait le lien
            PlayerPortfolioItem(
                player = player,
                board = board,
                onQuitClick = { onPlayerQuit(player.id) }
            )
        }

        // --- SÉPARATEUR ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- RÈGLES DU JEU ---
        item {
            RulesSection() // Ta fonction d'affichage des règles
        }

        // Espace pour le bas (navigation)
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun PlayerPortfolioItem(
    player: Player,
    board: List<BoardCase>,
    onQuitClick: () -> Unit // <--- Le callback pour l'abandon
) {
    // État pour afficher/cacher la popup de confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- POPUP DE CONFIRMATION ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Abandonner la partie ?") },
            text = { Text("Attention : Toutes les propriétés de ${player.name} seront libérées immédiatement. Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onQuitClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Oui, quitter")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    // --- CARTE DU JOUEUR ---
    // On récupère les objets 'BoardCase' correspondant aux IDs possédés
    val properties = player.ownedCases.mapNotNull { id ->
        board.find { it.id == id }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // EN-TÊTE : Avatar + Nom + Bouton Quitter
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(player.color.toInt()))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(player.avatar, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Nom et Statut
                Column(modifier = Modifier.weight(1f)) {
                    Text(player.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (player.inPrison) {
                        Text("🔒 En Prison", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    } else {
                        Text("${properties.size} propriétés", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // BOUTON "POUBELLE" (Abandon)
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Close, // Ou Icons.Default.Delete selon tes imports
                        contentDescription = "Abandonner",
                        tint = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // LISTE DES PROPRIÉTÉS (BADGES)
            if (properties.isEmpty()) {
                Text(
                    text = "Aucune propriété",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(properties) { prop ->
                        PropertyBadge(prop) // Assure-toi que cette fonction existe toujours en bas de ton fichier
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyBadge(case: BoardCase) {
    val familyColor = getFamilyColor(case.familyId)

    Surface(
        color = familyColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = case.name,
                color = Color.White, // Texte blanc sur fond couleur
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RulesSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // Fond Jaune Pastel
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFB300)), // Bordure Orange
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📜", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RÈGLES RAPIDES",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            RuleItem("🎲 DÉPLACEMENT", "Double = Rejoue + Distribue la valeur du dé (ex: Double 3 = Donne 3).")
            Divider(color = Color(0xFFFFB300), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            RuleItem("🏠 ACHAT", "Case libre ? 1 dé. Réussite = Achat. Échec = Bois le dé.")
            Divider(color = Color(0xFFFFB300), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            RuleItem("💸 LOYER", "Chez un joueur : TU BOIS. Chez toi : TU DISTRIBUES.")
            Divider(color = Color(0xFFFFB300), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            RuleItem("🚩 DÉPART", "Passage : Donne 5. Arrêt : Donne 10.")
            Divider(color = Color(0xFFFFB300), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            RuleItem("👮 PRISON", "Pour sortir : Fais 8+ avec 2 dés. Sinon bois et reste.")
        }
    }
}

@Composable
fun RuleItem(title: String, desc: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(desc, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF424242))
    }
}