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
        // offset -300 place l'élément vers le milieu/haut de l'écran
        listState.animateScrollToItem(index = activePlayerPosition, scrollOffset = -300)
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp), // On utilise toute la largeur
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
    // --- ANIMATIONS (Focus & Zoom) ---
    val scale by animateFloatAsState(targetValue = if (isActive) 1.05f else 0.95f, label = "scale")
    val alpha by animateFloatAsState(targetValue = if (isActive) 1f else 0.7f, label = "alpha") // 0.7 pour garder de la visibilité
    val elevation by animateDpAsState(targetValue = if (isActive) 12.dp else 2.dp, label = "elevation")

    // --- LOGIQUE D'AFFICHAGE ---
    val owner = if (boardCase.ownerId != null) allPlayers.find { it.id == boardCase.ownerId } else null
    val ownerColor = if (owner != null) Color(owner.color.toInt()) else Color.Transparent
    val isOwned = owner != null

    // Distinguer Propriétés vs Cases Spéciales
    val isPropertyCard = boardCase.type == CaseType.PROPRIETE || boardCase.type == CaseType.BAR

    // COULEURS DE FOND
    // Si propriété : Blanc.
    // Si spécial : Couleur pastelle spécifique (très lisible)
    val cardBackgroundColor = if (isPropertyCard) Color.White else getSpecialCaseColor(boardCase.type)

    // Bandeau couleur (uniquement pour les propriétés)
    val headerColor = if (isPropertyCard) getFamilyColor(boardCase.familyId) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .border(
                width = if (isOwned) 3.dp else 0.dp, // Pas de bordure grise moche, juste bordure proprio si acheté
                color = if (isOwned) ownerColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column {
            // Bandeau couleur (Famille) - Uniquement pour Propriétés/Bars
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
                    .padding(vertical = 16.dp, horizontal = 16.dp), // Padding confortable
                verticalAlignment = Alignment.CenterVertically
            ) {

                // --- CONTENU GAUCHE ---
                Column(modifier = Modifier.weight(1f)) {

                    // Ligne Titre + Emoji
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isPropertyCard && boardCase.type != CaseType.SIMPLE_VISITE) {
                            Text(
                                text = getCaseTypeEmoji(boardCase.type),
                                fontSize = 24.sp, // Emoji plus gros
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        Text(
                            text = boardCase.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black // Noir forcé pour lisibilité max
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Ligne Sous-titre (Prix, Proprio, Description)
                    if (isOwned) {
                        Text(
                            text = "Chez ${owner!!.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ownerColor // Couleur du joueur
                        )
                    } else if (boardCase.price > 0) {
                        Text(
                            text = "${boardCase.price} 🍺",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray, // Gris foncé
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Description case spéciale
                        Text(
                            text = getCaseDescription(boardCase.type),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.7f), // Noir légèrement transparent
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
            .background(Color(player.color.toInt()))
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // ICI : On affiche l'avatar au lieu de la lettre
        Text(
            text = player.avatar,
            fontSize = 22.sp // Taille confortable pour un emoji
        )
    }
}

// --- COULEURS DES CASES SPÉCIALES (PASTEL) ---
fun getSpecialCaseColor(type: CaseType): Color {
    return when (type) {
        CaseType.DEPART -> Color(0xFFC8E6C9)       // Vert Pastel (Départ)
        CaseType.CHANCE -> Color(0xFFFFCC80)       // Orange Pastel (Chance)
        CaseType.MINI_JEU -> Color(0xFFE1BEE7)     // Violet Pastel (Mini-Jeu)
        CaseType.BASSINE_REMPLIR,
        CaseType.BASSINE_BOIRE -> Color(0xFFB3E5FC) // Bleu Pastel (Bassine)
        CaseType.ALLER_PRISON -> Color(0xFFFFCDD2) // Rouge Pastel (Prison)
        CaseType.SIMPLE_VISITE -> Color(0xFFF5F5F5)// Gris très clair (Visite)
        CaseType.JARDIN_ENFANT -> Color(0xFFFFF9C4)// Jaune Pastel (Enfant)
        else -> Color.White
    }
}

// Utilitaires Emoji et Description (Inchangés)
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