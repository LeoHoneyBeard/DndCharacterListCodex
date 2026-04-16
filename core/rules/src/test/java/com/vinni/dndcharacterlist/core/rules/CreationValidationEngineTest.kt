package com.vinni.dndcharacterlist.core.rules

import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityMethod
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityScores
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationDraft
import com.vinni.dndcharacterlist.core.rules.creation.rules.BackgroundDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.CreationValidationEngine
import com.vinni.dndcharacterlist.core.rules.creation.rules.RaceDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreationValidationEngineTest {

    private val engine = CreationValidationEngine()

    @Test
    fun validateDraftFlagsMissingRequiredSelections() {
        val issues = engine.validateDraft(
            draft = CharacterCreationDraft(level = 2),
            race = null,
            subrace = null,
            classDefinition = null,
            background = null
        )

        assertEquals(
            listOf(
                "name_required",
                "race_required",
                "class_required",
                "background_required",
                "level_invalid",
                "ability_method_required",
                "abilities_required"
            ),
            issues.map { it.key }
        )
    }

    @Test
    fun validateDraftRejectsInvalidPointBuyAssignments() {
        val issues = engine.validateDraft(
            draft = validDraft().copy(
                abilityMethod = AbilityMethod.POINT_BUY,
                baseAbilities = AbilityScores(15, 15, 15, 15, 12, 8)
            ),
            race = race(),
            subrace = null,
            classDefinition = subclassAtLevelTwoClass(),
            background = background()
        )

        assertEquals(listOf("abilities_point_buy_invalid"), issues.map { it.key })
    }

    @Test
    fun validateDraftRejectsSubclassBeforeItBecomesAvailable() {
        val issues = engine.validateDraft(
            draft = validDraft().copy(subclassId = "champion"),
            race = race(),
            subrace = null,
            classDefinition = subclassAtLevelTwoClass(),
            background = background()
        )

        assertEquals(listOf("subclass_not_available"), issues.map { it.key })
    }

    @Test
    fun validateDraftRejectsUnknownSubclassWhenLevelAllowsSelection() {
        val issues = engine.validateDraft(
            draft = validDraft().copy(subclassId = "unknown"),
            race = race(),
            subrace = null,
            classDefinition = subclassAtLevelOneClass(),
            background = background()
        )

        assertEquals(listOf("subclass_invalid"), issues.map { it.key })
    }

    @Test
    fun validateDraftAcceptsValidLevelOneSelections() {
        val issues = engine.validateDraft(
            draft = validDraft().copy(subclassId = "life"),
            race = race(),
            subrace = null,
            classDefinition = subclassAtLevelOneClass(),
            background = background()
        )

        assertTrue(issues.isEmpty())
    }

    private fun validDraft(): CharacterCreationDraft {
        return CharacterCreationDraft(
            name = "Aylin",
            raceId = "elf",
            classId = "cleric",
            backgroundId = "sage",
            abilityMethod = AbilityMethod.STANDARD_ARRAY,
            baseAbilities = AbilityScores(15, 14, 13, 12, 10, 8)
        )
    }

    private fun race(): RaceDefinition {
        return RaceDefinition(
            id = "elf",
            name = "Elf"
        )
    }

    private fun background(): BackgroundDefinition {
        return BackgroundDefinition(
            id = "sage",
            name = "Sage",
            grantedSkills = emptySet()
        )
    }

    private fun subclassAtLevelTwoClass(): ClassDefinition {
        return ClassDefinition(
            id = "fighter",
            name = "Fighter",
            hitDie = 10,
            primaryAbilities = setOf(AbilityType.STRENGTH),
            savingThrowProficiencies = setOf(AbilityType.STRENGTH, AbilityType.CONSTITUTION),
            skillChoiceCount = 2,
            skillOptions = emptySet(),
            subclassLevel = 2,
            subclasses = listOf(SubclassDefinition("champion", "Champion"))
        )
    }

    private fun subclassAtLevelOneClass(): ClassDefinition {
        return ClassDefinition(
            id = "cleric",
            name = "Cleric",
            hitDie = 8,
            primaryAbilities = setOf(AbilityType.WISDOM),
            savingThrowProficiencies = setOf(AbilityType.WISDOM, AbilityType.CHARISMA),
            skillChoiceCount = 2,
            skillOptions = emptySet(),
            subclassLevel = 1,
            subclasses = listOf(SubclassDefinition("life", "Life Domain"))
        )
    }
}
