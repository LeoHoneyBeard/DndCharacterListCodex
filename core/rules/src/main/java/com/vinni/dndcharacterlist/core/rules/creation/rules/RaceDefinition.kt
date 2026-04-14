package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType

data class RaceDefinition(
    val id: String,
    val name: String,
    val baseAbilityBonuses: Map<AbilityType, Int> = emptyMap(),
    val subraces: List<SubraceDefinition> = emptyList()
)


