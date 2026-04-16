package com.vinni.dndcharacterlist.core.rules

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.BackgroundDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.HitPointEngine
import com.vinni.dndcharacterlist.core.rules.creation.rules.RaceDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent
import com.vinni.dndcharacterlist.core.rules.creation.rules.SkillDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition
import com.vinni.dndcharacterlist.core.rules.levelup.CharacterLevelUpRules
import com.vinni.dndcharacterlist.core.rules.levelup.LevelUpResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterLevelUpRulesTest {

    @Test
    fun prepareLevelUpAdvancesHitPointsWhenNoMandatoryChoiceExists() {
        val rules = CharacterLevelUpRules(Phb2014RulesRepository(), HitPointEngine())
        val character = baseCharacter(
            classId = "sorcerer",
            characterClass = "Sorcerer",
            subclassId = "wild_magic",
            subclass = "Wild Magic",
            constitution = 14,
            level = 1,
            hitPoints = 9,
            hitPointsMax = 9
        )

        val result = rules.prepareLevelUp(character, selectedSubclassId = null)

        assertTrue(result is LevelUpResult.Ready)
        val ready = result as LevelUpResult.Ready
        assertEquals(2, ready.character.level)
        assertEquals(15, ready.character.hitPoints)
        assertEquals(15, ready.character.hitPointsMax)
        assertEquals("wild_magic", ready.character.subclassId)
    }

    @Test
    fun prepareLevelUpBlocksWhenRequiredSubclassChoiceIsMissing() {
        val rules = CharacterLevelUpRules(RequiredSubclassRulesRepository(), HitPointEngine())
        val character = baseCharacter(
            classId = "test_mage",
            characterClass = "Test Mage",
            level = 1,
            hitPoints = 8,
            hitPointsMax = 8
        )

        val result = rules.prepareLevelUp(character, selectedSubclassId = null)

        assertTrue(result is LevelUpResult.Blocked)
        val blocked = result as LevelUpResult.Blocked
        assertEquals("Choose a subclass before applying the level up.", blocked.reason)
    }

    @Test
    fun prepareLevelUpAppliesSelectedSubclassWhenChoiceIsRequired() {
        val rules = CharacterLevelUpRules(RequiredSubclassRulesRepository(), HitPointEngine())
        val character = baseCharacter(
            classId = "test_mage",
            characterClass = "Test Mage",
            level = 1,
            hitPoints = 8,
            hitPointsMax = 8
        )

        val result = rules.prepareLevelUp(character, selectedSubclassId = "storm")

        assertTrue(result is LevelUpResult.Ready)
        val ready = result as LevelUpResult.Ready
        assertEquals("storm", ready.character.subclassId)
        assertEquals("Storm Path", ready.character.subclass)
        assertEquals(2, ready.character.level)
    }

    @Test
    fun previewExposesWizardSubclassChoicesAtLevelTwo() {
        val rules = CharacterLevelUpRules(Phb2014RulesRepository(), HitPointEngine())
        val character = baseCharacter(
            classId = "wizard",
            characterClass = "Wizard",
            level = 1
        )

        val preview = rules.preview(character)

        assertEquals(null, preview.blockingReason)
        assertTrue(preview.requiresSubclassSelection)
        assertEquals("evocation", preview.availableSubclasses.first { it.name == "School of Evocation" }.id)
    }

    private fun baseCharacter(
        classId: String = "wizard",
        characterClass: String = "Wizard",
        subclassId: String = "",
        subclass: String = "",
        constitution: Int = 12,
        level: Int = 1,
        hitPoints: Int = 8,
        hitPointsMax: Int = hitPoints
    ): CharacterRecord {
        return CharacterRecord(
            ruleset = Ruleset.PHB_2014.name,
            name = "Aylin",
            classId = classId,
            characterClass = characterClass,
            subclassId = subclassId,
            subclass = subclass,
            raceId = "elf",
            race = "Elf",
            subraceId = "high_elf",
            alignment = "Neutral Good",
            backgroundId = "sage",
            background = "Sage",
            level = level,
            abilityMethod = "STANDARD_ARRAY",
            armorClass = 12,
            hitPoints = hitPoints,
            hitPointsMax = hitPointsMax,
            strength = 8,
            dexterity = 14,
            constitution = constitution,
            intelligence = 16,
            wisdom = 12,
            charisma = 10,
            savingThrowProficiencies = listOf("INTELLIGENCE", "WISDOM"),
            skillProficiencies = listOf("arcana", "history"),
            notes = "",
            updatedAt = 1L
        )
    }

    private class RequiredSubclassRulesRepository : RulesRepository {
        override fun getRuleset(ruleset: Ruleset): RulesContent {
            return RulesContent(
                races = listOf(RaceDefinition("elf", "Elf", emptyMap())),
                classes = listOf(
                    ClassDefinition(
                        id = "test_mage",
                        name = "Test Mage",
                        hitDie = 8,
                        primaryAbilities = setOf(AbilityType.INTELLIGENCE),
                        savingThrowProficiencies = setOf(AbilityType.INTELLIGENCE),
                        skillChoiceCount = 2,
                        skillOptions = emptySet(),
                        subclassLevel = 2,
                        subclasses = listOf(
                            SubclassDefinition("storm", "Storm Path"),
                            SubclassDefinition("void", "Void Path")
                        )
                    )
                ),
                backgrounds = listOf(BackgroundDefinition("sage", "Sage", emptySet())),
                skills = listOf(SkillDefinition("arcana", "Arcana", AbilityType.INTELLIGENCE))
            )
        }
    }
}
