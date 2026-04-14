package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.SkillId
import com.vinni.dndcharacterlist.core.rules.creation.model.ValidationIssue

data class ProficiencyResult(
    val skillProficiencies: Set<SkillId>,
    val validationIssues: List<ValidationIssue>
)

class ProficiencyEngine {

    fun deriveSkillProficiencies(
        draftSelectedSkills: Set<SkillId>,
        replacementSkills: Map<SkillId, SkillId>,
        availableSkills: Set<SkillId>,
        classChoiceCount: Int,
        backgroundSkills: Set<SkillId>
    ): ProficiencyResult {
        val issues = mutableListOf<ValidationIssue>()

        val invalidClassSelections = draftSelectedSkills - availableSkills
        if (invalidClassSelections.isNotEmpty()) {
            issues += ValidationIssue(
                key = "class_skills_invalid",
                message = "Selected class skills contain invalid options."
            )
        }

        if (draftSelectedSkills.size != classChoiceCount) {
            issues += ValidationIssue(
                key = "class_skills_count",
                message = "Choose exactly $classChoiceCount class skills."
            )
        }

        val finalSkills = draftSelectedSkills.intersect(availableSkills).toMutableSet()

        backgroundSkills.forEach { backgroundSkill ->
            if (finalSkills.add(backgroundSkill)) {
                return@forEach
            }

            val replacement = replacementSkills[backgroundSkill]
            if (replacement == null) {
                issues += ValidationIssue(
                    key = "background_skill_conflict_$backgroundSkill",
                    message = "Background skill $backgroundSkill conflicts with an existing proficiency."
                )
                return@forEach
            }

            when {
                replacement !in availableSkills && replacement !in backgroundSkills -> {
                    issues += ValidationIssue(
                        key = "background_skill_replacement_invalid_$backgroundSkill",
                        message = "Replacement skill $replacement is not valid."
                    )
                }

                replacement in finalSkills -> {
                    issues += ValidationIssue(
                        key = "background_skill_replacement_duplicate_$backgroundSkill",
                        message = "Replacement skill $replacement is already selected."
                    )
                }

                else -> finalSkills += replacement
            }
        }

        return ProficiencyResult(
            skillProficiencies = finalSkills,
            validationIssues = issues
        )
    }
}


