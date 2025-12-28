package com.example.alcoopoly.ui.game.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoopoly.data.enums.CardType
import com.example.alcoopoly.model.Card

@Composable
fun CardsListScreen(
    chanceCards: List<Card>,
    miniGameCards: List<Card>,
    onAddCard: (CardType, String, String) -> Unit,
    onEditCard: (Card, String, String) -> Unit,
    onDeleteCard: (Card) -> Unit,
    onRestoreCard: (Card) -> Unit // <--- Nouveau callback
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Chance, 1 = Mini-Jeu
    var showDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<Card?>(null) }

    // Compteurs (Cartes ACTIVES uniquement)
    val activeChanceCount = chanceCards.count { it.isActive }
    val activeMiniGameCount = miniGameCards.count { it.isActive }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    cardToEdit = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // --- ONGLETS AVEC COMPTEURS ---
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("CHANCE ($activeChanceCount)") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("MINI-JEUX ($activeMiniGameCount)") }
                )
            }

            // --- PRÉPARATION DES LISTES ---
            val fullList = if (selectedTab == 0) chanceCards else miniGameCards
            val currentType = if (selectedTab == 0) CardType.CHANCE else CardType.MINI_JEU

            // Séparation Active / Supprimée
            val activeList = fullList.filter { it.isActive }
            val deletedList = fullList.filter { !it.isActive }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. LISTE DES CARTES ACTIVES
                items(activeList) { card ->
                    CardItemRow(
                        card = card,
                        isDeleted = false,
                        onEdit = {
                            cardToEdit = card
                            showDialog = true
                        },
                        onDelete = { onDeleteCard(card) },
                        onRestore = {}
                    )
                }

                // 2. SECTION CORBEILLE (si non vide)
                if (deletedList.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Text(
                            text = "🗑️ CORBEILLE (${deletedList.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(deletedList) { card ->
                        CardItemRow(
                            card = card,
                            isDeleted = true,
                            onEdit = {},
                            onDelete = {}, // Impossible de supprimer définitivement pour l'instant (sécurité)
                            onRestore = { onRestoreCard(card) }
                        )
                    }
                }

                // Espace FAB
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // --- DIALOGUE ---
            if (showDialog) {
                CardEditorDialog(
                    cardToEdit = cardToEdit,
                    defaultType = currentType,
                    onDismiss = { showDialog = false },
                    onConfirm = { type, title, desc ->
                        if (cardToEdit == null) {
                            onAddCard(type, title, desc)
                        } else {
                            onEditCard(cardToEdit!!, title, desc)
                        }
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun CardItemRow(
    card: Card,
    isDeleted: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    val lines = card.description.split("\n")
    val title = lines.firstOrNull() ?: "Sans titre"
    val desc = lines.drop(1).joinToString("\n")

    // Couleur grisée si supprimée
    val cardColor = if (isDeleted) Color.LightGray.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isDeleted) Color.Gray else MaterialTheme.colorScheme.onSurface

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(if (isDeleted) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            // BOUTONS ACTIONS
            if (isDeleted) {
                // Mode Restaurer
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restaurer", tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Mode Edition / Suppression
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CardEditorDialog(
    cardToEdit: Card?,
    defaultType: CardType,
    onDismiss: () -> Unit,
    onConfirm: (CardType, String, String) -> Unit
) {
    val initialTitle = cardToEdit?.description?.split("\n")?.firstOrNull() ?: ""
    val initialDesc = cardToEdit?.description?.split("\n")?.drop(1)?.joinToString("\n") ?: ""

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDesc) }
    val type = cardToEdit?.type ?: defaultType

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (cardToEdit == null) "Nouvelle Carte" else "Modifier la Carte") },
        text = {
            Column {
                Text(
                    text = "Type : ${if(type == CardType.CHANCE) "Chance 🍀" else "Mini-Jeu 🎮"}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(type, title, description) }) {
                Text("Sauvegarder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}