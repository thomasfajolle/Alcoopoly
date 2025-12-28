package com.example.alcoopoly.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PrisonAnimation(
    isVisible: Boolean,
    onAnimationFinished: () -> Unit
) {
    // On gère l'état d'affichage local pour pouvoir déclencher la fin
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(2000) // Les barreaux restent 2 secondes
            onAnimationFinished()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it }, // Arrive du haut
            animationSpec = tween(500) // Vitesse de chute (0.5s) "CLANG!"
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it }, // Repart vers le haut
            animationSpec = tween(500)
        ),
        modifier = Modifier.fillMaxSize() // Prend tout l'écran
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)) // Fond un peu sombre
        ) {
            // DESSIN DES BARREAUX
            Row(modifier = Modifier.fillMaxSize()) {
                // On crée 7 barreaux verticaux
                repeat(7) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp) // Espace entre les barreaux
                            .background(Color.DarkGray)
                    )
                }
            }

            // Un barreau horizontal pour faire "Cellule"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.Center)
                    .background(Color.DarkGray)
            )

            // TEXTE
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Red)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "L'abus d'alcool...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        }
    }
}