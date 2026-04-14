package com.vinni.dndcharacterlist.core.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityMethod
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityScores
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationDraft
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.CharacterCreationRulesEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterCreationRulesEngineTest {

    private val repository = Phb2014RulesRepository()
    private val engine = CharacterCreationRulesEngine(repository)

    @Test
    fun appliesRaceAndSubraceBonusesToFinalAbilities() {
        val result = engine.derive(
            draft(
                raceId = "elf",
                subraceId = "high_elf",
                classId = "wizard",
                backgroundId = "sage",
                baseAbilities = AbilityScores(8, 15, 13, 14, 12, 10),
                selectedClassSkills = setOf("arcana", "history")
            )
        )

        assertEquals(17, result.finalAbilities?.dexterity)
        assertEquals(15, result.finalAbilities?.intelligence)
    }

    @Test
    fun exposesSavingThrowProficienciesFromClass() {
        val result = engine.derive(
            draft(
                raceId = "human",
                classId = "fighter",
                backgroundId = "soldier",
                baseAbilities = AbilityScores(15, 13, 14, 10, 12, 8),
                selectedClassSkills = setOf("athletics", "survival")
            )
        )

        assertEquals(
            setOf(AbilityType.STRENGTH, AbilityType.CONSTITUTION),
            result.savingThrowProficiencies
        )
    }

    @Test
    fun calculatesLevelOneHitPointsFromHitDieAndConModifier() {
        val result = engine.derive(
            draft(
                raceId = "dwarf",
                subraceId = "hill_dwarf",
                classId = "fighter",
                backgroundId = "soldier",
                baseAbilities = AbilityScores(15, 12, 14, 10, 13, 8),
                selectedClassSkills = setOf("athletics", "history")
            )
        )

        assertEquals(13, result.maxHitPoints)
        assertEquals(13, result.currentHitPoints)
    }

    @Test
    fun derivesProficiencyBonusForLevelOneCharacter() {
        val result = engine.derive(
            draft(
                raceId = "human",
                classId = "rogue",
                backgroundId = "urchin",
                baseAbilities = AbilityScores(8, 15, 13, 12, 10, 14),
                selectedClassSkills = setOf("acrobatics", "stealth", "investigation", "perception")
            )
        )

        assertEquals(2, result.proficiencyBonus)
    }

    @Test
    fun exposesSubclassOptionsOnlyWhenClassGetsSubclassAtLevelOne() {
        val clericResult = engine.derive(
            draft(
                raceId = "human",
                classId = "cleric",
                backgroundId = "acolyte",
                baseAbilities = AbilityScores(10, 12, 14, 8, 15, 13),
                selectedClassSkills = setOf("insight", "religion")
            )
        )
        val fighterResult = engine.derive(
            draft(
                raceId = "human",
                classId = "fighter",
                backgroundId = "soldier",
                baseAbilities = AbilityScores(15, 12, 14, 10, 13, 8),
                selectedClassSkills = setOf("athletics", "history")
            )
        )

        assertTrue(clericResult.availableSubclassOptions.isNotEmpty())
        assertTrue(fighterResult.availableSubclassOptions.isEmpty())
    }

    @Test
    fun exposesSpellSlotsForFullCasterAtLevelOne() {
        val result = engine.derive(
            draft(
                raceId = "elf",
                subraceId = "high_elf",
                classId = "wizard",
                backgroundId = "sage",
                baseAbilities = AbilityScores(8, 15, 13, 14, 12, 10),
                selectedClassSkills = setOf("arcana", "history")
            )
        )

        assertEquals(2, result.spellSlots?.firstLevel)
    }

    @Test
    fun reportsBackgroundSkillConflictsWhenReplacementIsMissing() {
        val result = engine.derive(
            draft(
                raceId = "human",
                classId = "cleric",
                backgroundId = "acolyte",
                baseAbilities = AbilityScores(10, 12, 14, 8, 15, 13),
                selectedClassSkills = setOf("insight", "religion")
            )
        )

        assertTrue(result.validationIssues.any { it.key.startsWith("background_skill_conflict_") })
    }

    @Test
    fun usesReplacementSkillWhenBackgroundSkillOverlaps() {
        val result = engine.derive(
            draft(
                raceId = "human",
                classId = "cleric",
                backgroundId = "acolyte",
                baseAbilities = AbilityScores(10, 12, 14, 8, 15, 13),
                selectedClassSkills = setOf("insight", "religion"),
                selectedReplacementSkills = mapOf(
                    "insight" to "medicine",
                    "religion" to "history"
                )
            )
        )

        assertTrue(result.skillProficiencies.containsAll(setOf("insight", "religion", "medicine", "history")))
        assertFalse(result.validationIssues.any { it.key.startsWith("background_skill_conflict_") })
    }

    private fun draft(
        raceId: String,
        classId: String,
        backgroundId: String,
        baseAbilities: AbilityScores,
        selectedClassSkills: Set<String>,
        subraceId: String? = null,
        selectedReplacementSkills: Map<String, String> = emptyMap()
    ): CharacterCreationDraft {
        return CharacterCreationDraft(
            ruleset = Ruleset.PHB_2014,
            name = "Test Hero",
            raceId = raceId,
            subraceId = subraceId,
            classId = classId,
            backgroundId = backgroundId,
            level = 1,
            abilityMethod = AbilityMethod.MANUAL,
            baseAbilities = baseAbilities,
            selectedClassSkills = selectedClassSkills,
            selectedReplacementSkills = selectedReplacementSkills
        )
    }
}


