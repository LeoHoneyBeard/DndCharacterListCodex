package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityScores
import kotlin.math.max

class AbilityScoreEngine {

    fun applyBonuses(
        baseScores: AbilityScores,
        race: RaceDefinition?,
        subrace: SubraceDefinition?
    ): AbilityScores {
        return baseScores
            .withBonus(race?.baseAbilityBonuses.orEmpty())
            .withBonus(subrace?.abilityBonuses.orEmpty())
    }

    fun modifiersFor(scores: AbilityScores): AbilityScores {
        return AbilityScores(
            strength = modifierFor(scores.strength),
            dexterity = modifierFor(scores.dexterity),
            constitution = modifierFor(scores.constitution),
            intelligence = modifierFor(scores.intelligence),
            wisdom = modifierFor(scores.wisdom),
            charisma = modifierFor(scores.charisma)
        )
    }

    private fun modifierFor(score: Int): Int = Math.floorDiv(score - 10, 2)
}


