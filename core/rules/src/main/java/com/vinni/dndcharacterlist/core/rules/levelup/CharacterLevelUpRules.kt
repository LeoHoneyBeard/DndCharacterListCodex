package com.vinni.dndcharacterlist.core.rules.levelup

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.HitPointEngine
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent
import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition
import com.vinni.dndcharacterlist.core.rules.levelup.LevelUpRequirement.SubclassSelection
import com.vinni.dndcharacterlist.core.rules.levelup.LevelUpRequirement.UnsupportedChoice

class CharacterLevelUpRules(
    private val repository: RulesRepository,
    private val hitPointEngine: HitPointEngine = HitPointEngine()
) {

    fun preview(character: CharacterRecord): LevelUpPreview {
        val ruleset = character.ruleset.toRulesetOrDefault()
        val content = repository.getRuleset(ruleset)
        val classDefinition = content.findClass(character)
        val nextLevel = (character.level + 1).coerceAtMost(MAX_LEVEL)
        val hitPointIncrease = classDefinition?.let { calculateHitPointIncrease(it, character.constitution) } ?: 0
        val requirements = classDefinition?.let {
            requiredChoices(
                character = character,
                classDefinition = it,
                nextLevel = nextLevel
            )
        }.orEmpty()
        val blockingReason = when {
            character.level >= MAX_LEVEL -> "Character is already at the maximum level."
            classDefinition == null -> "This character's class is not supported by the active rules."
            else -> null
        }

        return LevelUpPreview(
            currentLevel = character.level,
            nextLevel = nextLevel,
            currentHitPoints = character.hitPoints,
            currentHitPointsMax = character.hitPointsMax,
            hitPointIncrease = hitPointIncrease,
            nextHitPoints = character.hitPoints + hitPointIncrease,
            nextHitPointsMax = character.hitPointsMax + hitPointIncrease,
            requirements = requirements,
            blockingReason = blockingReason
        )
    }

    fun prepareLevelUp(
        character: CharacterRecord,
        selectedSubclassId: String?
    ): LevelUpResult {
        val preview = preview(character)
        preview.blockingReason?.let { reason ->
            return LevelUpResult.Blocked(preview = preview, reason = reason)
        }

        preview.unsupportedRequirement()?.let { requirement ->
            return LevelUpResult.Blocked(
                preview = preview,
                reason = requirement.description
            )
        }

        val selectedSubclass = preview.resolveSubclass(selectedSubclassId)
        val subclassRequirement = preview.subclassRequirement()
        if (subclassRequirement != null && selectedSubclass == null) {
            return LevelUpResult.Blocked(
                preview = preview,
                reason = subclassRequirement.description
            )
        }
        val appliedSubclass = if (subclassRequirement != null) requireNotNull(selectedSubclass) else null

        return LevelUpResult.Ready(
            preview = preview,
            character = CharacterUpsert(
                id = character.id,
                ruleset = character.ruleset,
                name = character.name,
                classId = character.classId,
                characterClass = character.characterClass,
                subclassId = appliedSubclass?.id ?: character.subclassId,
                subclass = appliedSubclass?.name ?: character.subclass,
                raceId = character.raceId,
                race = character.race,
                subraceId = character.subraceId,
                alignment = character.alignment,
                backgroundId = character.backgroundId,
                background = character.background,
                level = preview.nextLevel,
                abilityMethod = character.abilityMethod,
                armorClass = character.armorClass,
                hitPoints = preview.nextHitPoints,
                hitPointsMax = preview.nextHitPointsMax,
                strength = character.strength,
                dexterity = character.dexterity,
                constitution = character.constitution,
                intelligence = character.intelligence,
                wisdom = character.wisdom,
                charisma = character.charisma,
                savingThrowProficiencies = character.savingThrowProficiencies,
                skillProficiencies = character.skillProficiencies,
                notes = character.notes
            )
        )
    }

    private fun calculateHitPointIncrease(
        classDefinition: ClassDefinition,
        constitution: Int
    ): Int {
        val constitutionModifier = hitPointEngine.abilityModifier(constitution)
        return (classDefinition.hitDie / 2 + 1 + constitutionModifier).coerceAtLeast(1)
    }

    private fun LevelUpPreview.resolveSubclass(selectedSubclassId: String?): SubclassDefinition? {
        val requirement = subclassRequirement() ?: return null
        val requestedId = selectedSubclassId?.trim().orEmpty()
        if (requestedId.isBlank()) return null
        return requirement.options.firstOrNull { it.id == requestedId }
    }

    private fun LevelUpPreview.subclassRequirement(): SubclassSelection? {
        return requirements.filterIsInstance<SubclassSelection>().firstOrNull()
    }

    private fun LevelUpPreview.unsupportedRequirement(): UnsupportedChoice? {
        return requirements.filterIsInstance<UnsupportedChoice>().firstOrNull()
    }

    private fun requiredChoices(
        character: CharacterRecord,
        classDefinition: ClassDefinition,
        nextLevel: Int
    ): List<LevelUpRequirement> {
        return buildList {
            requiredSubclassChoice(character, classDefinition, nextLevel)?.let(::add)
            unsupportedMandatoryChoice(character, classDefinition, nextLevel)?.let(::add)
        }
    }

    private fun requiredSubclassChoice(
        character: CharacterRecord,
        classDefinition: ClassDefinition,
        nextLevel: Int
    ): LevelUpRequirement? {
        val requiresSubclassSelection = classDefinition.subclassLevel == nextLevel && character.subclassId.isBlank()
        if (!requiresSubclassSelection) return null
        if (classDefinition.subclasses.isEmpty()) {
            return UnsupportedChoice(
                title = "Subclass Choice",
                description = "This class needs a subclass at level $nextLevel, but the active rules content does not define those subclasses yet."
            )
        }
        return SubclassSelection(options = classDefinition.subclasses)
    }

    private fun unsupportedMandatoryChoice(
        character: CharacterRecord,
        classDefinition: ClassDefinition,
        nextLevel: Int
    ): UnsupportedChoice? {
        abilityScoreImprovementRequirement(character.classId, nextLevel)?.let { return it }
        fightingStyleRequirement(character.classId, nextLevel)?.let { return it }
        expertiseRequirement(character.classId, nextLevel)?.let { return it }
        spellChoiceRequirement(character.classId, classDefinition, nextLevel)?.let { return it }
        return null
    }

    private fun abilityScoreImprovementRequirement(classId: String, nextLevel: Int): UnsupportedChoice? {
        val standardLevels = setOf(4, 8, 12, 16, 19)
        val fighterLevels = setOf(6, 14)
        val rogueLevels = setOf(10)
        val requiresImprovement = nextLevel in standardLevels ||
            (classId == "fighter" && nextLevel in fighterLevels) ||
            (classId == "rogue" && nextLevel in rogueLevels)
        if (!requiresImprovement) return null
        return UnsupportedChoice(
            title = "Ability Score Improvement",
            description = "This level requires choosing an ability score improvement or feat, but level-up does not support that choice yet."
        )
    }

    private fun fightingStyleRequirement(classId: String, nextLevel: Int): UnsupportedChoice? {
        val requiresFightingStyle = (classId == "paladin" || classId == "ranger") && nextLevel == 2
        if (!requiresFightingStyle) return null
        return UnsupportedChoice(
            title = "Fighting Style",
            description = "This level requires choosing a fighting style, but level-up does not support that choice yet."
        )
    }

    private fun expertiseRequirement(classId: String, nextLevel: Int): UnsupportedChoice? {
        val bardLevels = setOf(3, 10)
        val rogueLevels = setOf(6)
        val requiresExpertise = (classId == "bard" && nextLevel in bardLevels) ||
            (classId == "rogue" && nextLevel in rogueLevels)
        if (!requiresExpertise) return null
        return UnsupportedChoice(
            title = "Expertise",
            description = "This level requires choosing expertise, but level-up does not support that choice yet."
        )
    }

    private fun spellChoiceRequirement(
        classId: String,
        classDefinition: ClassDefinition,
        nextLevel: Int
    ): UnsupportedChoice? {
        if (classDefinition.spellcasting == null) return null
        val requiresSpellChoice = when (classId) {
            "bard", "sorcerer", "warlock", "wizard" -> nextLevel >= 2
            "ranger" -> nextLevel >= 2
            else -> false
        }
        if (!requiresSpellChoice) return null
        return UnsupportedChoice(
            title = "Spell Selection",
            description = "This level requires updating spell choices, but level-up does not support spell selection yet."
        )
    }

    private fun RulesContent.findClass(character: CharacterRecord): ClassDefinition? {
        return classes.firstOrNull { it.id == character.classId }
            ?: classes.firstOrNull { it.name.equals(character.characterClass, ignoreCase = true) }
    }

    private fun String.toRulesetOrDefault(): Ruleset {
        return Ruleset.entries.firstOrNull { it.name == this } ?: Ruleset.PHB_2014
    }

    private companion object {
        const val MAX_LEVEL = 20
    }
}
