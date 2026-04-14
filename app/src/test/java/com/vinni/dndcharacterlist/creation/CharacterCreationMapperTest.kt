package com.vinni.dndcharacterlist.creation

import com.vinni.dndcharacterlist.creation.mapper.CharacterCreationMapper
import com.vinni.dndcharacterlist.creation.model.AbilityMethod
import com.vinni.dndcharacterlist.creation.model.AbilityScores
import com.vinni.dndcharacterlist.creation.model.AbilityType
import com.vinni.dndcharacterlist.creation.model.CharacterCreationDraft
import com.vinni.dndcharacterlist.creation.model.DerivedCharacterStats
import com.vinni.dndcharacterlist.creation.model.Ruleset
import com.vinni.dndcharacterlist.creation.repository.Phb2014RulesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterCreationMapperTest {

    private val repository = Phb2014RulesRepository()
    private val mapper = CharacterCreationMapper()

    @Test
    fun mapsDraftAndDerivedStatsIntoPersistedCharacterEntity() {
        val draft = CharacterCreationDraft(
            ruleset = Ruleset.PHB_2014,
            name = "Aylin",
            raceId = "elf",
            subraceId = "high_elf",
            classId = "wizard",
            backgroundId = "sage",
            level = 1,
            abilityMethod = AbilityMethod.STANDARD_ARRAY,
            baseAbilities = AbilityScores(8, 14, 13, 15, 12, 10),
            selectedClassSkills = setOf("arcana", "history")
        )
        val derived = DerivedCharacterStats(
            finalAbilities = AbilityScores(8, 16, 13, 16, 12, 10),
            abilityModifiers = AbilityScores(-1, 3, 1, 3, 1, 0),
            proficiencyBonus = 2,
            savingThrowProficiencies = setOf(AbilityType.INTELLIGENCE, AbilityType.WISDOM),
            skillProficiencies = setOf("arcana", "history"),
            maxHitPoints = 7,
            currentHitPoints = 7
        )

        val entity = mapper.toEntity(
            draft = draft,
            derived = derived,
            rulesContent = repository.getRuleset(Ruleset.PHB_2014)
        )

        assertEquals("PHB_2014", entity.ruleset)
        assertEquals("elf", entity.raceId)
        assertEquals("Elf", entity.race)
        assertEquals("high_elf", entity.subraceId)
        assertEquals("wizard", entity.classId)
        assertEquals("Wizard", entity.characterClass)
        assertEquals("sage", entity.backgroundId)
        assertEquals("Sage", entity.background)
        assertEquals(7, entity.hitPoints)
        assertEquals(7, entity.hitPointsMax)
        assertEquals(13, entity.armorClass)
        assertEquals("STANDARD_ARRAY", entity.abilityMethod)
        assertTrue(entity.savingThrowProficienciesSerialized.contains("INTELLIGENCE"))
        assertTrue(entity.skillProficienciesSerialized.contains("arcana"))
    }
}
