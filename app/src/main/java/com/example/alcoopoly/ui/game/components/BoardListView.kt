package com.example.alcoopoly.ui.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun BoardListView(
    board: List<BoardCase>,
    players: List<Player>,
    currentPlayerId: Int,
    modifier: Modifier = Modifier
) {
    // Permet de scroller automatiquement vers le joueur actif
    val listState = rememberLazyListState()
    val activePlayerPosition = players.find { it.id == currentPlayerId }?.position ?: 0

    // Effet : Quand le joueur bouge, on scroll vers sa position
    LaunchedEffect(activePlayerPosition) {
        listState.animateScrollToItem(index = activePlayerPosition)
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
    ) {
        items(board) { case ->
            BoardCaseItem(
                boardCase = case,
                playersOnCase = players.filter { it.position == (case.id - 1) }, // id 1 = index 0
                allPlayers = players // <--- AJOUTER CETTE LIGNE
            )
            // Petit séparateur
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
        }
    }
}

@Composable
fun BoardCaseItem(
    boardCase: BoardCase,
    playersOnCase: List<Player>,
    allPlayers: List<Player> = emptyList() // Nouveau paramètre
) {
    // On cherche si la case a un propriétaire
    val owner = if (boardCase.ownerId != null) allPlayers.find { it.id == boardCase.ownerId } else null

    // Couleur du propriétaire (ou gris transparent par défaut)
    val ownerColor = if (owner != null) Color(owner.color.toInt()) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            // Si propriétaire : petite bordure colorée autour de la case
            .border(
                width = if (owner != null) 2.dp else 0.dp,
                color = ownerColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp), // Padding interne pour pas que le texte touche la bordure
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Indicateur de Famille (inchangé)
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(getFamilyColor(boardCase.familyId))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Textes
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = boardCase.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            // Affichage du Propriétaire ou du Prix
            if (owner != null) {
                Text(
                    text = "Chez ${owner.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ownerColor,
                    fontWeight = FontWeight.Bold
                )
            } else if (boardCase.price > 0) {
                Text(
                    text = "Prix : ${boardCase.price} gorgées",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        // 3. Pions (inchangé)
        Row {
            playersOnCase.forEach { player ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(player.color.toInt()))
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.name.first().toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}