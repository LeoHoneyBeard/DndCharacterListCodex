package com.vinni.dndcharacterlist.feature.character.levelup.domain

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import kotlin.math.max

data class LevelUpPlan(
    val characterId: Long,
    val name: String,
    val currentLevel: Int,
    val nextLevel: Int,
    val currentHitPoints: Int,
    val currentHitPointsMax: Int,
    val recommendedHitPointGain: Int,
    val hitDieLabel: String?,
    val proficiencyBonus: Int,
    val nextProficiencyBonus: Int,
    val subclassPrompt: String? = null,
    val className: String
)

class CharacterLevelUpPlanner(
    private val rulesRepository: RulesRepository
) {
    fun createPlan(character: CharacterRecord): LevelUpPlan? {
        if (character.level !in 1..19) return null

        val classDefinition = resolveClassDefinition(character)
        val recommendedHitPointGain = classDefinition?.let {
            averageHitPointGain(it.hitDie, constitutionModifier(character.constitution))
        } ?: 1
        val nextLevel = character.level + 1

        return LevelUpPlan(
            characterId = character.id,
            name = character.name,
            currentLevel = character.level,
            nextLevel = nextLevel,
            currentHitPoints = character.hitPoints,
            currentHitPointsMax = character.hitPointsMax,
            recommendedHitPointGain = recommendedHitPointGain,
            hitDieLabel = classDefinition?.let { "d${it.hitDie}" },
            proficiencyBonus = proficiencyBonus(character.level),
            nextProficiencyBonus = proficiencyBonus(nextLevel),
            subclassPrompt = classDefinition
                ?.takeIf { it.subclassLevel == nextLevel && character.subclass.isBlank() }
                ?.let { "${it.name} chooses a subclass at level $nextLevel." },
            className = character.characterClass.ifBlank { classDefinition?.name.orEmpty() }
        )
    }

    private fun resolveClassDefinition(character: CharacterRecord): ClassDefinition? {
        val ruleset = character.ruleset
            .takeIf(String::isNotBlank)
            ?.let { storedRuleset -> runCatching { Ruleset.valueOf(storedRuleset) }.getOrNull() }
            ?: Ruleset.PHB_2014
        val content = rulesRepository.getRuleset(ruleset)
        return content.classes.firstOrNull { it.id.equals(character.classId, ignoreCase = true) }
            ?: content.classes.firstOrNull { it.name.equals(character.characterClass, ignoreCase = true) }
    }

    private fun constitutionModifier(score: Int): Int = Math.floorDiv(score - 10, 2)

    private fun averageHitPointGain(hitDie: Int, constitutionModifier: Int): Int {
        return max(1, (hitDie / 2) + 1 + constitutionModifier)
    }

    private fun proficiencyBonus(level: Int): Int = ((level - 1) / 4) + 2
}
