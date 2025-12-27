package com.example.alcoopoly.data

import com.example.alcoopoly.data.enums.CaseType
import com.example.alcoopoly.model.BoardCase

object BoardData {
    // Liste officielle des 40 cases du plateau (Index 0 à 39)
    val defaultBoard = listOf(
        // --- RANGÉE 1 (Difficulté 2+) ---
        BoardCase(id = 1, name = "Cave Départ", type = CaseType.DEPART),
        BoardCase(id = 2, name = "Allée de la 8.6", type = CaseType.PROPRIETE, familyId = 1, price = 2),
        BoardCase(id = 3, name = "Mini-jeu", type = CaseType.MINI_JEU),
        BoardCase(id = 4, name = "Allée de la Villageoise", type = CaseType.PROPRIETE, familyId = 1, price = 2),
        BoardCase(id = 5, name = "Chance", type = CaseType.CHANCE),
        BoardCase(id = 6, name = "ShamBar", type = CaseType.BAR, familyId = 99, price = 4), // Famille 99 = Bars
        BoardCase(id = 7, name = "Boulevard de la Kro", type = CaseType.PROPRIETE, familyId = 2, price = 2),
        BoardCase(id = 8, name = "Bassine (Remplir)", type = CaseType.BASSINE_REMPLIR),
        BoardCase(id = 9, name = "Allée de la Cuvée de Promo", type = CaseType.PROPRIETE, familyId = 2, price = 2),
        BoardCase(id = 10, name = "Allée de la HK", type = CaseType.PROPRIETE, familyId = 2, price = 2),

        // --- RANGÉE 2 (Difficulté 3+) ---
        BoardCase(id = 11, name = "Bar'ban (Simple visite)", type = CaseType.SIMPLE_VISITE),
        BoardCase(id = 12, name = "Impasse du Muscadet", type = CaseType.PROPRIETE, familyId = 3, price = 3),
        BoardCase(id = 13, name = "Chance", type = CaseType.CHANCE),
        BoardCase(id = 14, name = "Avenue de la Grim", type = CaseType.PROPRIETE, familyId = 3, price = 3),
        BoardCase(id = 15, name = "Av. du Beaujolais Nouveau", type = CaseType.PROPRIETE, familyId = 3, price = 3),
        BoardCase(id = 16, name = "Bar'bu", type = CaseType.BAR, familyId = 99, price = 4),
        BoardCase(id = 17, name = "Rue de la Leffe", type = CaseType.PROPRIETE, familyId = 4, price = 3),
        BoardCase(id = 18, name = "Mini-jeu", type = CaseType.MINI_JEU),
        BoardCase(id = 19, name = "Promenade de la Syrah", type = CaseType.PROPRIETE, familyId = 4, price = 3),
        BoardCase(id = 20, name = "Rue de la Kwak", type = CaseType.PROPRIETE, familyId = 4, price = 3),

        // --- RANGÉE 3 (Difficulté 4+) ---
        BoardCase(id = 21, name = "Jardin d'enfant", type = CaseType.JARDIN_ENFANT),
        BoardCase(id = 22, name = "Boulevard du Chianti", type = CaseType.PROPRIETE, familyId = 5, price = 4),
        BoardCase(id = 23, name = "Chance", type = CaseType.CHANCE),
        BoardCase(id = 24, name = "Allée de la Chouffe", type = CaseType.PROPRIETE, familyId = 5, price = 4),
        BoardCase(id = 25, name = "Avenue du Riesling", type = CaseType.PROPRIETE, familyId = 5, price = 4),
        BoardCase(id = 26, name = "LTB", type = CaseType.BAR, familyId = 99, price = 4),
        BoardCase(id = 27, name = "Boulevard de la Duvel", type = CaseType.PROPRIETE, familyId = 6, price = 4),
        BoardCase(id = 28, name = "Avenue du Saint-Emilion", type = CaseType.PROPRIETE, familyId = 6, price = 4),
        BoardCase(id = 29, name = "Chance", type = CaseType.CHANCE),
        BoardCase(id = 30, name = "Rue de la Chimay", type = CaseType.PROPRIETE, familyId = 6, price = 4),

        // --- RANGÉE 4 (Difficulté 5+) ---
        BoardCase(id = 31, name = "ALLEZ EN BAR'BAN !", type = CaseType.ALLER_PRISON),
        BoardCase(id = 32, name = "Avenue du Meursault", type = CaseType.PROPRIETE, familyId = 7, price = 5),
        BoardCase(id = 33, name = "Rue de la Cuvée des Trolls", type = CaseType.PROPRIETE, familyId = 7, price = 5),
        BoardCase(id = 34, name = "Bassine (Remplir)", type = CaseType.BASSINE_REMPLIR),
        BoardCase(id = 35, name = "Rue du Pommard", type = CaseType.PROPRIETE, familyId = 7, price = 5),
        BoardCase(id = 36, name = "Soirée BDE", type = CaseType.BAR, familyId = 99, price = 4),
        BoardCase(id = 37, name = "Chance", type = CaseType.CHANCE),
        BoardCase(id = 38, name = "Avenue du Saint-Amour", type = CaseType.PROPRIETE, familyId = 8, price = 5),
        BoardCase(id = 39, name = "CUL SEC LA BASSINE !", type = CaseType.BASSINE_BOIRE),
        BoardCase(id = 40, name = "Rue de la Paix-Dieu", type = CaseType.PROPRIETE, familyId = 8, price = 5)
    )
}