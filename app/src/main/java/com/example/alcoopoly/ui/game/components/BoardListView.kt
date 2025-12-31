package com.example.alcoopoly.ui.game.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoopoly.data.enums.CaseType
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
    val listState = rememberLazyListState()
    val activePlayerPosition = players.find { it.id == currentPlayerId }?.position ?: 0

    // Scroll automatique pour centrer la case active
    LaunchedEffect(activePlayerPosition) {
        listState.animateScrollToItem(index = activePlayerPosition, scrollOffset = -300)
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp)
    ) {
        items(board) { case ->
            val isCurrentCase = (case.id - 1) == activePlayerPosition

            BoardCaseCard(
                boardCase = case,
                playersOnCase = players.filter { it.position == (case.id - 1) },
                allPlayers = players,
                isActive = isCurrentCase
            )
        }
    }
}

@Composable
fun BoardCaseCard(
    boardCase: BoardCase,
    playersOnCase: List<Player>,
    allPlayers: List<Player>,
    isActive: Boolean
) {
    // --- ANIMATIONS ---
    val scale by animateFloatAsState(targetValue = if (isActive) 1.05f else 0.95f, label = "scale")
    val alpha by animateFloatAsState(targetValue = if (isActive) 1f else 0.7f, label = "alpha")
    val elevation by animateDpAsState(targetValue = if (isActive) 12.dp else 2.dp, label = "elevation")

    // --- LOGIQUE PROPRIÉTAIRE ---
    val owner = if (boardCase.ownerId != null) allPlayers.find { it.id == boardCase.ownerId } else null

    // CORRECTION 1 : Utiliser toULong() pour éviter les couleurs invisibles
    val ownerColor = if (owner != null) Color(owner.color.toULong()) else Color.Transparent
    val isOwned = owner != null

    val isPropertyCard = boardCase.type == CaseType.PROPRIETE || boardCase.type == CaseType.BAR
    val cardBackgroundColor = if (isPropertyCard) Color.White else getSpecialCaseColor(boardCase.type)
    val headerColor = if (isPropertyCard) getFamilyColor(boardCase.familyId) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            // CORRECTION 2 : La bordure doit bien s'afficher maintenant
            .border(
                width = if (isOwned) 3.dp else 0.dp,
                color = if (isOwned) ownerColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column {
            // Bandeau couleur (Famille)
            if (isPropertyCard) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(headerColor)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // --- CONTENU GAUCHE ---
                Column(modifier = Modifier.weight(1f)) {

                    // Ligne Titre + Emoji
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isPropertyCard && boardCase.type != CaseType.SIMPLE_VISITE) {
                            Text(
                                text = getCaseTypeEmoji(boardCase.type),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        Text(
                            text = boardCase.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // --- SOUS-TITRE (PRIX / PROPRIO / DESCRIPTION) ---
                    if (isOwned) {
                        // CORRECTION 3 : Affichage du nom ET du loyer en couleur
                        Column {
                            Text(
                                text = "Chez ${owner!!.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ownerColor // Nom en couleur
                            )
                            // Si 'rent' n'existe pas, remplace par 'loyer' ou 'price'
                            Text(
                                text = "Loyer : ${boardCase.price} 🍺",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    } else if (boardCase.price > 0) {
                        // A VENDRE
                        Text(
                            text = "Prix : ${boardCase.price} 🍺",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // DESCRIPTION SPECIALE
                        Text(
                            text = getCaseDescription(boardCase.type),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.7f),
                            maxLines = 2
                        )
                    }
                }

                // --- PIONS (DROITE) ---
                if (playersOnCase.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                        playersOnCase.forEach { player ->
                            PlayerToken(player)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerToken(player: Player) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            // CORRECTION 4 : Couleur du pion corrigée ici aussi
            .background(Color(player.color.toULong()))
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = player.avatar,
            fontSize = 22.sp
        )
    }
}

// --- COULEURS ET UTILITAIRES (INCHANGÉS) ---
fun getSpecialCaseColor(type: CaseType): Color {
    return when (type) {
        CaseType.DEPART -> Color(0xFFC8E6C9)
        CaseType.CHANCE -> Color(0xFFFFCC80)
        CaseType.MINI_JEU -> Color(0xFFE1BEE7)
        CaseType.BASSINE_REMPLIR,
        CaseType.BASSINE_BOIRE -> Color(0xFFB3E5FC)
        CaseType.ALLER_PRISON -> Color(0xFFFFCDD2)
        CaseType.SIMPLE_VISITE -> Color(0xFFF5F5F5)
        CaseType.JARDIN_ENFANT -> Color(0xFFFFF9C4)
        else -> Color.White
    }
}

fun getCaseTypeEmoji(type: CaseType): String {
    return when (type) {
        CaseType.DEPART -> "🚩"
        CaseType.CHANCE -> "🍀"
        CaseType.MINI_JEU -> "🎮"
        CaseType.BASSINE_REMPLIR, CaseType.BASSINE_BOIRE -> "🪣"
        CaseType.ALLER_PRISON -> "👮"
        CaseType.SIMPLE_VISITE -> "⛓️"
        CaseType.JARDIN_ENFANT -> "👶"
        else -> ""
    }
}

fun getCaseDescription(type: CaseType): String {
    return when (type) {
        CaseType.DEPART -> "Passage +5 / Arrêt +10"
        CaseType.CHANCE -> "Tire une carte"
        CaseType.MINI_JEU -> "Joue un jeu"
        CaseType.BASSINE_REMPLIR -> "Verse dans la bassine"
        CaseType.BASSINE_BOIRE -> "Bois toute la bassine"
        CaseType.ALLER_PRISON -> "Direction cellule !"
        CaseType.SIMPLE_VISITE -> "Juste de passage..."
        CaseType.JARDIN_ENFANT -> "Enlève un vêtement"
        else -> ""
    }
}