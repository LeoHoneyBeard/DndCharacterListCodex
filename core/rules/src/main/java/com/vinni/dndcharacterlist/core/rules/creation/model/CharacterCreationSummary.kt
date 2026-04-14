package com.vinni.dndcharacterlist.core.rules.creation.model

data class CharacterCreationSummary(
    val draft: CharacterCreationDraft,
    val derived: DerivedCharacterStats,
    val display: CharacterDisplaySnapshot
)

