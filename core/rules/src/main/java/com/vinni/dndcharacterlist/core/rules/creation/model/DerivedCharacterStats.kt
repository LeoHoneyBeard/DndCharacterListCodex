package com.vinni.dndcharacterlist.core.rules.creation.model

data class DerivedCharacterStats(
    val finalAbilities: AbilityScores? = null,
    val abilityModifiers: AbilityScores? = null,
    val proficiencyBonus: Int? = null,
    val savingThrowProficiencies: Set<AbilityType> = emptySet(),
    val skillProficiencies: Set<SkillId> = emptySet(),
    val maxHitPoints: Int? = null,
    val currentHitPoints: Int? = null,
    val spellSlots: SpellSlots? = null,
    val availableSubclassOptions: List<String> = emptyList(),
    val validationIssues: List<ValidationIssue> = emptyList()
)

