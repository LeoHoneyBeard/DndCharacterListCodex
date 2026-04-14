package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationDraft
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityMethod
import com.vinni.dndcharacterlist.core.rules.creation.model.ValidationIssue

class CreationValidationEngine {

    fun validateDraft(
        draft: CharacterCreationDraft,
        race: RaceDefinition?,
        subrace: SubraceDefinition?,
        classDefinition: ClassDefinition?,
        background: BackgroundDefinition?
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()

        if (draft.name.isBlank()) {
            issues += ValidationIssue("name_required", "Name is required.")
        }
        if (race == null) {
            issues += ValidationIssue("race_required", "Race is required.")
        }
        if (draft.subraceId != null && subrace == null) {
            issues += ValidationIssue("subrace_invalid", "Selected subrace is invalid.")
        }
        if (classDefinition == null) {
            issues += ValidationIssue("class_required", "Class is required.")
        }
        if (background == null) {
            issues += ValidationIssue("background_required", "Background is required.")
        }
        if (draft.level != 1) {
            issues += ValidationIssue("level_invalid", "Character creation currently supports level 1 only.")
        }
        if (draft.abilityMethod == null) {
            issues += ValidationIssue("ability_method_required", "Ability generation method is required.")
        }
        val baseAbilities = draft.baseAbilities
        if (baseAbilities == null) {
            issues += ValidationIssue("abilities_required", "Base ability scores are required.")
        } else {
            when (draft.abilityMethod) {
                AbilityMethod.STANDARD_ARRAY -> {
                    if (!AbilityGenerationRules.isStandardArray(baseAbilities)) {
                        issues += ValidationIssue(
                            "abilities_standard_array_invalid",
                            "Standard array must use exactly 15, 14, 13, 12, 10, 8."
                        )
                    }
                }

                AbilityMethod.POINT_BUY -> {
                    val values = listOf(
                        baseAbilities.strength,
                        baseAbilities.dexterity,
                        baseAbilities.constitution,
                        baseAbilities.intelligence,
                        baseAbilities.wisdom,
                        baseAbilities.charisma
                    )
                    if (values.any { it !in 8..15 } || AbilityGenerationRules.pointBuyCost(baseAbilities) > 27) {
                        issues += ValidationIssue(
                            "abilities_point_buy_invalid",
                            "Point buy scores must stay within 8..15 and cost at most 27 points."
                        )
                    }
                }

                AbilityMethod.ROLL -> {
                    val values = listOf(
                        baseAbilities.strength,
                        baseAbilities.dexterity,
                        baseAbilities.constitution,
                        baseAbilities.intelligence,
                        baseAbilities.wisdom,
                        baseAbilities.charisma
                    )
                    if (values.any { it !in 3..18 }) {
                        issues += ValidationIssue(
                            "abilities_roll_invalid",
                            "Rolled scores must stay within 3..18."
                        )
                    }
                }

                AbilityMethod.MANUAL,
                null -> Unit
            }
        }
        if (classDefinition != null && draft.subclassId != null) {
            val subclassAvailableAtLevelOne = classDefinition.subclassLevel?.let { draft.level >= it } == true
            if (!subclassAvailableAtLevelOne) {
                issues += ValidationIssue(
                    "subclass_not_available",
                    "Selected class does not choose a subclass at level 1."
                )
            } else if (classDefinition.subclasses.none { it.id == draft.subclassId }) {
                issues += ValidationIssue("subclass_invalid", "Selected subclass is invalid.")
            }
        }

        return issues
    }
}


