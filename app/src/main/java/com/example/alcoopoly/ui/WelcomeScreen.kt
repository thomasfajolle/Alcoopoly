package com.example.alcoopoly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Une classe simple pour stocker temporairement les infos avant de lancer le jeu
data class NewPlayerInfo(val name: String, val avatar: String)

@Composable
fun WelcomeScreen(
    onStartGame: (List<String>) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    // Liste des joueurs créés
    var addedPlayers by remember { mutableStateOf(listOf<NewPlayerInfo>()) }

    // Liste des Emojis disponibles
    val availableEmojis = listOf("🦁", "🐯", "🐻", "🐨", "🐸", "🐔", "🦄", "🐝", "🐞", "🐠", "🐢", "🦖", "👽", "👻", "🤖", "💩", "🍺", "🍷", "🍹", "🍆", "🍑", "🍄", "🌶️", "🧀", "🌭", "🍕", "🍟")

    // Emoji actuellement sélectionné
    var selectedEmoji by remember { mutableStateOf(availableEmojis.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("ALCOOPOLY 🍻", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Choisis ton combattant", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // --- SÉLECTEUR D'AVATAR ---
        Text("1. Choisis un Avatar :", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(availableEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .border(
                            width = if (selectedEmoji == emoji) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clickable { selectedEmoji = emoji },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- CHAMP NOM ---
        Text("2. Entre ton nom :", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // On affiche l'emoji choisi à gauche du champ
            Text(selectedEmoji, fontSize = 32.sp, modifier = Modifier.padding(end = 8.dp))

            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nom du joueur") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newName.isNotBlank()) {
                        addedPlayers = addedPlayers + NewPlayerInfo(newName, selectedEmoji)
                        newName = ""
                        // On change d'emoji par défaut pour varier (prendre un au hasard)
                        selectedEmoji = availableEmojis.random()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- LISTE DES JOUEURS ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(addedPlayers) { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(player.avatar, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(player.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { addedPlayers = addedPlayers - player }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BOUTON LANCER ---
        Button(
            onClick = {
                // On transforme la liste d'objets en liste de Strings formatés "Nom|Avatar"
                val formattedList = addedPlayers.map { "${it.name}|${it.avatar}" }
                onStartGame(formattedList)
            },
            enabled = addedPlayers.size >= 2,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Text(if (addedPlayers.size < 2) "Ajoutez au moins 2 joueurs" else "C'EST PARTI ! 🚀", fontSize = 18.sp)
        }
    }
}