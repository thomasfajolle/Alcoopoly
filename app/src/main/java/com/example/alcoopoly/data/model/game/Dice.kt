package com.example.alcoopoly.game

import kotlin.random.Random

/**
 * Classe utilitaire pour la gestion des dés.
 * Totalement indépendante du reste du jeu.
 */
class Dice {

    /**
     * Lance un dé classique (1 à 6).
     */
    fun rollOne(): Int {
        return Random.nextInt(1, 7)
    }

    /**
     * Lance deux dés pour le déplacement.
     * @return une paire (dé1, dé2)
     */
    fun rollTwo(): Pair<Int, Int> {
        return Pair(rollOne(), rollOne())
    }
}
