package com.example.alcoopoly.data.model

import com.example.alcoopoly.data.enums.CaseType

data class BoardCase(
    val id: Int,
    val name: String,
    val type: CaseType,

    val familyId: Int? = null,
    val row: Int? = null,

    var ownerId: Int? = null
)
