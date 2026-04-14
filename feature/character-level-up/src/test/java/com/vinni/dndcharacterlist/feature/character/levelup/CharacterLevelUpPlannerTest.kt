package com.vinni.dndcharacterlist.feature.character.levelup

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.feature.character.levelup.domain.CharacterLevelUpPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterLevelUpPlannerTest {
    private val planner = CharacterLevelUpPlanner(Phb2014RulesRepository())

    @Test
    fun usesAverageHitPointGainForKnownClass() {
        val plan = planner.createPlan(
            CharacterRecord(
                id = 1L,
                ruleset = Ruleset.PHB_2014.name,
                name = "Lae'zel",
                classId = "fighter",
                characterClass = "Fighter",
                subclass = "",
                race = "Githyanki",
                alignment = "",
                background = "Soldier",
                level = 4,
                armorClass = 17,
                hitPoints = 34,
                hitPointsMax = 34,
                strength = 17,
                dexterity = 13,
                constitution = 14,
                intelligence = 11,
                wisdom = 12,
                charisma = 10,
                notes = "",
                updatedAt = 0L
            )
        )

        requireNotNull(plan)
        assertEquals(5, plan.nextLevel)
        assertEquals(8, plan.recommendedHitPointGain)
        assertEquals("d10", plan.hitDieLabel)
        assertEquals(2, plan.proficiencyBonus)
        assertEquals(3, plan.nextProficiencyBonus)
    }

    @Test
    fun returnsNullWhenCharacterIsAlreadyMaxLevel() {
        val plan = planner.createPlan(
            CharacterRecord(
                id = 2L,
                name = "Gale",
                classId = "wizard",
                characterClass = "Wizard",
                subclass = "",
                race = "Human",
                alignment = "",
                background = "Sage",
                level = 20,
                armorClass = 12,
                hitPoints = 100,
                hitPointsMax = 100,
                strength = 8,
                dexterity = 14,
                constitution = 14,
                intelligence = 18,
                wisdom = 12,
                charisma = 10,
                notes = "",
                updatedAt = 0L
            )
        )

        assertNull(plan)
    }
}
