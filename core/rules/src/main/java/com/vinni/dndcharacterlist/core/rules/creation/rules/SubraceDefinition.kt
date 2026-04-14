package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType

data class SubraceDefinition(
    val id: String,
    val name: String,
    val abilityBonuses: Map<AbilityType, Int> = emptyMap()
)


