package com.vinni.dndcharacterlist.core.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityScores
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.rules.AbilityGenerationRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AbilityGenerationRulesTest {

    @Test
    fun standardArrayValidationMatchesExpectedMultiset() {
        assertTrue(
            AbilityGenerationRules.isStandardArray(
                AbilityScores(15, 14, 13, 12, 10, 8)
            )
        )
    }

    @Test
    fun pointBuyRemainingUsesPhb2014Costs() {
        val scores = AbilityScores(15, 15, 15, 8, 8, 8)

        assertEquals(0, AbilityGenerationRules.pointBuyRemaining(scores))
    }

    @Test
    fun rollSetProducesScoresWithinExpectedRange() {
        val scores = AbilityGenerationRules.rollSet(Random(42))

        assertTrue(scores.strength in 3..18)
        assertTrue(scores.dexterity in 3..18)
        assertTrue(scores.constitution in 3..18)
        assertTrue(scores.intelligence in 3..18)
        assertTrue(scores.wisdom in 3..18)
        assertTrue(scores.charisma in 3..18)
    }

    @Test
    fun updateAbilityChangesOnlyTargetStat() {
        val updated = AbilityGenerationRules.updateAbility(
            scores = AbilityScores(8, 8, 8, 8, 8, 8),
            abilityType = AbilityType.WISDOM,
            newValue = 15
        )

        assertEquals(8, updated.strength)
        assertEquals(15, updated.wisdom)
        assertEquals(8, updated.charisma)
    }
}


