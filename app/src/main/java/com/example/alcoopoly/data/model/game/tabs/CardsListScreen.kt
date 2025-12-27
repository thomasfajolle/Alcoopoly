package com.example.alcoopoly.ui.game.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alcoopoly.data.CardData
import com.example.alcoopoly.model.Card

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardsListScreen() {
    val chanceCards = CardData.initialChanceCards
    val miniGameCards = CardData.initialMiniGameCards

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        // SECTION MINI-JEUX
        stickyHeader {
            HeaderSection("🎮 MINI-JEUX (${miniGameCards.size})", Color(0xFFCE93D8))
        }
        items(miniGameCards) { card ->
            CardListItem(card)
        }

        // SECTION CHANCE
        stickyHeader {
            HeaderSection("🍀 CHANCE (${chanceCards.size})", Color(0xFFFFCC80))
        }
        items(chanceCards) { card ->
            CardListItem(card)
        }
    }
}

@Composable
fun HeaderSection(title: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(16.dp)
    ) {
        Text(title, fontWeight = FontWeight.Black, color = Color.Black)
    }
}

@Composable
fun CardListItem(card: Card) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(card.description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = Color.LightGray)
    }
}