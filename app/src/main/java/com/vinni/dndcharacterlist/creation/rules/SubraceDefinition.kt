package com.vinni.dndcharacterlist.creation.rules

import com.vinni.dndcharacterlist.creation.model.AbilityType

data class SubraceDefinition(
    val id: String,
    val name: String,
    val abilityBonuses: Map<AbilityType, Int> = emptyMap()
)
