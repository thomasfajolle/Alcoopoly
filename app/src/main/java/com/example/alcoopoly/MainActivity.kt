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
        enableEdgeToEdge()
        setContent {
            AlcoopolyTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    // 1. On crée le contrôleur de navigation
                    val navController = rememberNavController()

                    // 2. On définit les routes (les écrans)
                    NavHost(navController = navController, startDestination = "welcome") {

                        // ÉCRAN 1 : ACCUEIL
                        composable("welcome") {
                            WelcomeScreen(
                                onStartGame = { playersList ->
                                    // playersList est maintenant ["Thomas|🦁", "Paul|👽"]
                                    // On les joint avec des virgules pour l'URL : "Thomas|🦁,Paul|👽"
                                    val namesString = playersList.joinToString(",")
                                    navController.navigate("game/$namesString")
                                }
                            )
                        }

                        // ÉCRAN 2 : JEU
                        // On définit que cette route attend un argument "names"
                        composable(
                            route = "game/{names}",
                            arguments = listOf(navArgument("names") { type = NavType.StringType })
                        ) { backStackEntry ->
                            // On récupère la string "Paul,Pierre" et on la remet en liste
                            val namesString = backStackEntry.arguments?.getString("names") ?: ""
                            val playerList = namesString.split(",")

                            // On lance l'écran de jeu
                            GameScreen(playerNames = playerList)
                        }
                    }
                }
            }
        }
    }
}