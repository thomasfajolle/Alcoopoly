package com.example.alcoopoly.data.model

import com.example.alcoopoly.data.enums.FamilyType

data class Family(
    val id: Int,
    val name: String,
    val type: FamilyType,
    val baseSips: Int,
    val caseIds: List<Int>
)
