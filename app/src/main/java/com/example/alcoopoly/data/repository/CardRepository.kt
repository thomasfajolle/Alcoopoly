package com.example.alcoopoly.data.repository

import android.content.Context
import com.example.alcoopoly.data.CardData
import com.example.alcoopoly.model.Card
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CardRepository(context: Context) {

    private val prefs = context.getSharedPreferences("alcoopoly_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Clés de sauvegarde
    private val KEY_CHANCE = "saved_chance_cards"
    private val KEY_MINIGAME = "saved_minigame_cards"

    // --- CHARGEMENT ---

    fun loadChanceCards(): List<Card> {
        val json = prefs.getString(KEY_CHANCE, null)
        return if (json != null) {
            // Si on a une sauvegarde, on la convertit en liste
            val type = object : TypeToken<List<Card>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Sinon, on charge les défauts (fichier CardData)
            CardData.initialChanceCards
        }
    }

    fun loadMiniGameCards(): List<Card> {
        val json = prefs.getString(KEY_MINIGAME, null)
        return if (json != null) {
            val type = object : TypeToken<List<Card>>() {}.type
            gson.fromJson(json, type)
        } else {
            CardData.initialMiniGameCards
        }
    }

    // --- SAUVEGARDE ---

    fun saveChanceCards(cards: List<Card>) {
        val json = gson.toJson(cards)
        prefs.edit().putString(KEY_CHANCE, json).apply()
    }

    fun saveMiniGameCards(cards: List<Card>) {
        val json = gson.toJson(cards)
        prefs.edit().putString(KEY_MINIGAME, json).apply()
    }

    // Pour réinitialiser aux valeurs d'usine si besoin
    fun resetToFactorySettings() {
        prefs.edit().clear().apply()
    }
}