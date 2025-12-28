package com.example.alcoopoly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.alcoopoly.ui.WelcomeScreen
import com.example.alcoopoly.ui.game.GameScreen
import com.example.alcoopoly.ui.theme.AlcoopolyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlcoopolyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. On crée le contrôleur de navigation
                    val navController = rememberNavController()

                    // 2. On définit les routes (Les écrans de l'appli)
                    NavHost(navController = navController, startDestination = "home") {

                        // --- ÉCRAN D'ACCUEIL ---
                        composable("home") {
                            WelcomeScreen(
                                onStartGame = { names ->
                                    // On transforme la liste ["Tom", "Léa"] en string "Tom,Léa" pour la passer
                                    val namesString = names.joinToString(",")
                                    navController.navigate("game/$namesString")
                                }
                            )
                        }

                        // --- ÉCRAN DE JEU (C'EST ICI QU'ON MODIFIE) ---
                        composable(
                            route = "game/{playerNames}",
                            arguments = listOf(navArgument("playerNames") { type = NavType.StringType })
                        ) { backStackEntry ->
                            // On récupère les noms passés depuis l'accueil
                            val namesString = backStackEntry.arguments?.getString("playerNames") ?: ""
                            val playerNames = namesString.split(",").filter { it.isNotBlank() }

                            // APPEL DE GAME SCREEN
                            GameScreen(
                                playerNames = playerNames,
                                // C'est ici qu'on branche le tuyau pour le retour à la maison !
                                onNavigateHome = {
                                    // On retourne à "home" en effaçant l'historique de la partie
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}