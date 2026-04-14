package com.vinni.dndcharacterlist.core.rules.creation.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityScores
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import kotlin.random.Random

object AbilityGenerationRules {

    val standardArray: List<Int> = listOf(15, 14, 13, 12, 10, 8)

    fun defaultScoresForMethod(): AbilityScores = AbilityScores(8, 8, 8, 8, 8, 8)

    fun standardArrayDefaultAssignment(): AbilityScores {
        return AbilityScores(
            strength = 15,
            dexterity = 14,
            constitution = 13,
            intelligence = 12,
            wisdom = 10,
            charisma = 8
        )
    }

    fun pointBuyCost(scores: AbilityScores): Int {
        return listOf(
            scores.strength,
            scores.dexterity,
            scores.constitution,
            scores.intelligence,
            scores.wisdom,
            scores.charisma
        ).sumOf(::pointCostForScore)
    }

    fun pointBuyRemaining(scores: AbilityScores): Int = 27 - pointBuyCost(scores)

    fun isStandardArray(scores: AbilityScores): Boolean {
        return listOf(
            scores.strength,
            scores.dexterity,
            scores.constitution,
            scores.intelligence,
            scores.wisdom,
            scores.charisma
        ).sortedDescending() == standardArray.sortedDescending()
    }

    fun rollSet(random: Random = Random.Default): AbilityScores {
        return AbilityScores(
            strength = rollScore(random),
            dexterity = rollScore(random),
            constitution = rollScore(random),
            intelligence = rollScore(random),
            wisdom = rollScore(random),
            charisma = rollScore(random)
        )
    }

    fun updateAbility(
        scores: AbilityScores,
        abilityType: AbilityType,
        newValue: Int
    ): AbilityScores {
        return when (abilityType) {
            AbilityType.STRENGTH -> scores.copy(strength = newValue)
            AbilityType.DEXTERITY -> scores.copy(dexterity = newValue)
            AbilityType.CONSTITUTION -> scores.copy(constitution = newValue)
            AbilityType.INTELLIGENCE -> scores.copy(intelligence = newValue)
            AbilityType.WISDOM -> scores.copy(wisdom = newValue)
            AbilityType.CHARISMA -> scores.copy(charisma = newValue)
        }
    }

    private fun pointCostForScore(score: Int): Int {
        return when (score) {
            8 -> 0
            9 -> 1
            10 -> 2
            11 -> 3
            12 -> 4
            13 -> 5
            14 -> 7
            15 -> 9
            else -> Int.MAX_VALUE / 4
        }
    }

    private fun rollScore(random: Random): Int {
        val dice = List(4) { random.nextInt(1, 7) }.sortedDescending()
        return dice.take(3).sum()
    }
}


