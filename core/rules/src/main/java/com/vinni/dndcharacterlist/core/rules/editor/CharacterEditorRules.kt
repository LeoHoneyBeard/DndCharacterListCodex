package com.vinni.dndcharacterlist.core.rules.editor

import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.model.ValidationIssue
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.BackgroundDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.RaceDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent
import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition

data class CharacterEditorResolvedSelections(
    val ruleset: Ruleset,
    val rulesContent: RulesContent,
    val race: RaceDefinition,
    val classDefinition: ClassDefinition,
    val background: BackgroundDefinition,
    val subclass: SubclassDefinition?
)

class CharacterEditorRules(
    private val repository: RulesRepository
) {

    fun rulesetFor(rawRuleset: String): Ruleset {
        return Ruleset.entries.firstOrNull { it.name == rawRuleset } ?: Ruleset.PHB_2014
    }

    fun rulesContentFor(rawRuleset: String): RulesContent {
        return repository.getRuleset(rulesetFor(rawRuleset))
    }

    fun validate(draft: CharacterEditorDraft): List<ValidationIssue> {
        val ruleset = rulesetFor(draft.ruleset)
        val content = repository.getRuleset(ruleset)
        val classDefinition = content.findClass(draft.characterClass)
        val race = content.findRace(draft.race)
        val background = content.findBackground(draft.background)
        val level = draft.level.toIntOrNull()
        val armorClass = draft.armorClass.toIntOrNull()
        val hitPoints = draft.hitPoints.toIntOrNull()
        val abilityScores = listOf(
            draft.strength,
            draft.dexterity,
            draft.constitution,
            draft.intelligence,
            draft.wisdom,
            draft.charisma
        ).map(String::toIntOrNull)
        val issues = mutableListOf<ValidationIssue>()

        if (draft.name.isBlank()) {
            issues += ValidationIssue("name_required", "Name is required.")
        }
        if (classDefinition == null) {
            issues += ValidationIssue("class_required", "Choose a supported class.")
        }
        if (race == null) {
            issues += ValidationIssue("race_required", "Choose a supported race.")
        }
        if (background == null) {
            issues += ValidationIssue("background_required", "Choose a supported background.")
        }
        if (level?.let { it in 1..20 } != true) {
            issues += ValidationIssue("level_invalid", "Level must be between 1 and 20.")
        }
        if (armorClass?.let { it >= 0 } != true) {
            issues += ValidationIssue("armor_class_invalid", "Armor Class must be 0 or higher.")
        }
        if (hitPoints?.let { it >= 0 } != true) {
            issues += ValidationIssue("hit_points_invalid", "Hit Points must be 0 or higher.")
        }
        if (abilityScores.any { it?.let { score -> score in 1..30 } != true }) {
            issues += ValidationIssue("ability_scores_invalid", "Ability scores must be between 1 and 30.")
        }
        if (race != null && race.subraces.isNotEmpty()) {
            val subrace = race.subraces.firstOrNull { it.id == draft.subraceId }
            if (subrace == null) {
                issues += ValidationIssue(
                    "subrace_required",
                    "Selected race requires a supported subrace. Keep an existing supported subrace or choose a race without subraces."
                )
            }
        }
        if (classDefinition != null && level != null) {
            issues += validateSubclass(draft.subclass, level, classDefinition)
        }

        return issues
    }

    fun resolveSelections(draft: CharacterEditorDraft): CharacterEditorResolvedSelections? {
        val issues = validate(draft)
        if (issues.isNotEmpty()) return null

        val ruleset = rulesetFor(draft.ruleset)
        val rulesContent = repository.getRuleset(ruleset)
        val classDefinition = requireNotNull(rulesContent.findClass(draft.characterClass))
        val race = requireNotNull(rulesContent.findRace(draft.race))
        val background = requireNotNull(rulesContent.findBackground(draft.background))
        val subclass = classDefinition.findSubclass(draft.subclass)

        return CharacterEditorResolvedSelections(
            ruleset = ruleset,
            rulesContent = rulesContent,
            race = race,
            classDefinition = classDefinition,
            background = background,
            subclass = subclass
        )
    }

    private fun validateSubclass(
        rawSubclass: String,
        level: Int,
        classDefinition: ClassDefinition
    ): List<ValidationIssue> {
        val trimmedSubclass = rawSubclass.trim()
        val subclassLevel = classDefinition.subclassLevel
        if (trimmedSubclass.isBlank()) {
            if (subclassLevel != null && level >= subclassLevel) {
                return if (classDefinition.subclasses.isEmpty()) {
                    listOf(
                        ValidationIssue(
                            "subclass_unsupported",
                            "This class requires a subclass at the current level, but the active rules content does not define supported subclasses."
                        )
                    )
                } else {
                    listOf(ValidationIssue("subclass_required", "Choose a supported subclass for this class and level."))
                }
            }
            return emptyList()
        }

        if (subclassLevel == null || level < subclassLevel) {
            return listOf(
                ValidationIssue(
                    "subclass_not_available",
                    "Selected class does not choose a subclass at the current level."
                )
            )
        }
        if (classDefinition.findSubclass(trimmedSubclass) == null) {
            return listOf(ValidationIssue("subclass_invalid", "Choose a supported subclass for this class and level."))
        }
        return emptyList()
    }

    private fun RulesContent.findClass(rawClass: String): ClassDefinition? {
        val normalized = rawClass.trim()
        return classes.firstOrNull { candidate ->
            candidate.id.equals(normalized, ignoreCase = true) || candidate.name.equals(normalized, ignoreCase = true)
        }
    }

    private fun RulesContent.findRace(rawRace: String): RaceDefinition? {
        val normalized = rawRace.trim()
        return races.firstOrNull { candidate ->
            candidate.id.equals(normalized, ignoreCase = true) || candidate.name.equals(normalized, ignoreCase = true)
        }
    }

    private fun RulesContent.findBackground(rawBackground: String): BackgroundDefinition? {
        val normalized = rawBackground.trim()
        return backgrounds.firstOrNull { candidate ->
            candidate.id.equals(normalized, ignoreCase = true) || candidate.name.equals(normalized, ignoreCase = true)
        }
    }

    private fun ClassDefinition.findSubclass(rawSubclass: String): SubclassDefinition? {
        val normalized = rawSubclass.trim()
        if (normalized.isBlank()) return null
        return subclasses.firstOrNull { candidate ->
            candidate.id.equals(normalized, ignoreCase = true) || candidate.name.equals(normalized, ignoreCase = true)
        }
    }
}
