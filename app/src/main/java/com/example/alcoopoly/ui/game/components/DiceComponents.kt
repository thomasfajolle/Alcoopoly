package com.example.alcoopoly.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DieView(
    value: Int,
    isRolling: Boolean = false, // Permet d'ajouter un petit effet de secousse
    size: Dp = 50.dp
) {
    // Petit effet de rotation aléatoire quand ça roule
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val currentRotation = if (isRolling) rotation else 0f

    Box(
        modifier = Modifier
            .size(size)
            .rotate(currentRotation)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        // Dessin des points selon la valeur
        when (value) {
            1 -> DotCenter()
            2 -> { DotTopLeft(); DotBottomRight() }
            3 -> { DotTopLeft(); DotCenter(); DotBottomRight() }
            4 -> { DotTopLeft(); DotTopRight(); DotBottomLeft(); DotBottomRight() }
            5 -> { DotTopLeft(); DotTopRight(); DotCenter(); DotBottomLeft(); DotBottomRight() }
            6 -> {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Dot(); Dot() }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Dot(); Dot() }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Dot(); Dot() }
                }
            }
        }
    }
}

// --- UTILITAIRES DE POINTS ---

@Composable
fun BoxScope.DotCenter() {
    Dot(Modifier.align(Alignment.Center))
}
@Composable
fun BoxScope.DotTopLeft() {
    Dot(Modifier.align(Alignment.TopStart))
}
@Composable
fun BoxScope.DotTopRight() {
    Dot(Modifier.align(Alignment.TopEnd))
}
@Composable
fun BoxScope.DotBottomLeft() {
    Dot(Modifier.align(Alignment.BottomStart))
}
@Composable
fun BoxScope.DotBottomRight() {
    Dot(Modifier.align(Alignment.BottomEnd))
}

@Composable
fun Dot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(10.dp) // Taille du point
            .background(Color.Black, RoundedCornerShape(50))
    )
}