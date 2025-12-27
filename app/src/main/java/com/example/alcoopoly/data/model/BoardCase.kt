package com.example.alcoopoly.model

import com.example.alcoopoly.data.enums.CaseType // Assure-toi d'avoir ton Enum CaseType

data class BoardCase(
    val id: Int,
    val name: String,
    val type: CaseType, // Utilise ton Enum existant
    val familyId: Int? = null,
    val price: Int = 0, // Prix en gorgées ou devise
    val ownerId: Int? = null // Null si personne ne l'a acheté
)