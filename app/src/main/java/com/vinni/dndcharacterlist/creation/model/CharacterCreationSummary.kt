package com.vinni.dndcharacterlist.creation.model

data class CharacterCreationSummary(
    val draft: CharacterCreationDraft,
    val derived: DerivedCharacterStats,
    val display: CharacterDisplaySnapshot
)
