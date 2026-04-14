package com.vinni.dndcharacterlist.core.rules.creation.model

data class CharacterCreationDraft(
    val ruleset: Ruleset = Ruleset.PHB_2014,
    val name: String = "",
    val raceId: String? = null,
    val subraceId: String? = null,
    val classId: String? = null,
    val subclassId: String? = null,
    val backgroundId: String? = null,
    val level: Int = 1,
    val abilityMethod: AbilityMethod? = null,
    val baseAbilities: AbilityScores? = null,
    val selectedClassSkills: Set<SkillId> = emptySet(),
    val selectedReplacementSkills: Map<SkillId, SkillId> = emptyMap()
)

