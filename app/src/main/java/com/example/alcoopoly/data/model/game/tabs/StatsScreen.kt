package com.example.alcoopoly.ui.game.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoopoly.model.Player

@Composable
fun StatsScreen(players: List<Player>) {
    // On trie les joueurs : celui qui a le plus bu en premier
    val sortedPlayers = players.sortedByDescending { it.drinksTaken }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("🏆 CLASSEMENT GÉNÉRAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Qui tient le mieux l'alcool ?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            itemsIndexed(sortedPlayers) { index, player ->
                StatItem(index + 1, player)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatItem(rank: Int, player: Player) {
    // Couleurs pour le podium
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Or
        2 -> Color(0xFFC0C0C0) // Argent
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rang
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(rankColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.Bold,
                    color = if (rank <= 3) Color.White else Color.Black
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Joueur
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (player.inPrison) {
                    Text("🔒 En cellule de dégrisement", color = Color.Red, fontSize = 12.sp)
                }
            }

            // Stats Gorgées
            Column(horizontalAlignment = Alignment.End) {
                Text("🍺 ${player.drinksTaken} bues", fontWeight = FontWeight.Bold, color = Color.Red)
                Text("👉 ${player.drinksGiven} données", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}