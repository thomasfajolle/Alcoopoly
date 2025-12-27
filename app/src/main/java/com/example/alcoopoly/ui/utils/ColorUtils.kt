package com.example.alcoopoly.ui.utils

import androidx.compose.ui.graphics.Color

// Fonction simple pour récupérer la couleur d'une famille
fun getFamilyColor(familyId: Int?): Color {
    return when (familyId) {
        1 -> Color(0xFF8D6E63) // Marron
        2 -> Color(0xFF42A5F5) // Bleu Ciel
        3 -> Color(0xFFEC407A) // Rose
        4 -> Color(0xFFFFA726) // Orange
        5 -> Color(0xFFEF5350) // Rouge
        6 -> Color(0xFFFFEE58) // Jaune
        7 -> Color(0xFF66BB6A) // Vert
        8 -> Color(0xFF3F51B5) // Bleu Foncé
        99 -> Color(0xFFBDBDBD) // Bars (Gris)
        else -> Color.Transparent // Cases spéciales
    }
}