package com.vinni.dndcharacterlist.core.rules.levelup

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.HitPointEngine
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent
import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition

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
        val requiresSubclassSelection = classDefinition?.subclassLevel == nextLevel && character.subclassId.isBlank()
        val availableSubclasses = classDefinition?.subclasses.orEmpty()
        val blockingReason = when {
            character.level >= MAX_LEVEL -> "Character is already at the maximum level."
            classDefinition == null -> "This character's class is not supported by the active rules."
            requiresSubclassSelection && availableSubclasses.isEmpty() ->
                "This class needs a subclass at level $nextLevel, but the active rules content does not define those subclasses yet."
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
            requiresSubclassSelection = requiresSubclassSelection,
            availableSubclasses = availableSubclasses,
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

        val selectedSubclass = preview.resolveSubclass(selectedSubclassId)
        if (preview.requiresSubclassSelection && selectedSubclass == null) {
            return LevelUpResult.Blocked(
                preview = preview,
                reason = "Choose a subclass before applying the level up."
            )
        }
        val appliedSubclass = if (preview.requiresSubclassSelection) requireNotNull(selectedSubclass) else null

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
        if (!requiresSubclassSelection) return null
        val requestedId = selectedSubclassId?.trim().orEmpty()
        if (requestedId.isBlank()) return null
        return availableSubclasses.firstOrNull { it.id == requestedId }
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
