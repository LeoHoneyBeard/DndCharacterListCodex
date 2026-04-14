package com.vinni.dndcharacterlist.creation.rules

import com.vinni.dndcharacterlist.creation.model.AbilityType

data class RaceDefinition(
    val id: String,
    val name: String,
    val baseAbilityBonuses: Map<AbilityType, Int> = emptyMap(),
    val subraces: List<SubraceDefinition> = emptyList()
)
