package com.vinni.dndcharacterlist.core.data.repository

import com.vinni.dndcharacterlist.core.data.local.CharacterEntity
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterEntityMapperTest {
    @Test
    fun mergeIntoPreservesNonEditableMetadata() {
        val existing = CharacterEntity(
            id = 42L,
            ruleset = "PHB_2014",
            name = "Old Name",
            classId = "wizard",
            characterClass = "Wizard",
            subclassId = "evocation",
            subclass = "Evocation",
            raceId = "elf",
            race = "Elf",
            subraceId = "high_elf",
            alignment = "Neutral Good",
            backgroundId = "sage",
            background = "Sage",
            level = 3,
            abilityMethod = "STANDARD_ARRAY",
            armorClass = 14,
            hitPoints = 16,
            hitPointsMax = 16,
            strength = 8,
            dexterity = 14,
            constitution = 12,
            intelligence = 18,
            wisdom = 10,
            charisma = 10,
            savingThrowProficienciesSerialized = "INTELLIGENCE,WISDOM",
            skillProficienciesSerialized = "arcana,history",
            notes = "old",
            updatedAt = 1L
        )
        val upsert = CharacterUpsert(
            id = 42L,
            name = "New Name",
            characterClass = "Wizard",
            subclass = "Evocation",
            race = "Elf",
            alignment = "Lawful Good",
            background = "Sage",
            level = 4,
            armorClass = 15,
            hitPoints = 20,
            strength = 8,
            dexterity = 14,
            constitution = 12,
            intelligence = 18,
            wisdom = 10,
            charisma = 10,
            notes = "new"
        )

        val merged = upsert.mergeInto(existing, timestamp = 99L)

        assertEquals("PHB_2014", merged.ruleset)
        assertEquals("wizard", merged.classId)
        assertEquals("evocation", merged.subclassId)
        assertEquals("elf", merged.raceId)
        assertEquals("high_elf", merged.subraceId)
        assertEquals("sage", merged.backgroundId)
        assertEquals("STANDARD_ARRAY", merged.abilityMethod)
        assertEquals("INTELLIGENCE,WISDOM", merged.savingThrowProficienciesSerialized)
        assertEquals("arcana,history", merged.skillProficienciesSerialized)
        assertEquals(16, merged.hitPointsMax)
        assertEquals("New Name", merged.name)
        assertEquals("Lawful Good", merged.alignment)
        assertEquals(20, merged.hitPoints)
        assertEquals(99L, merged.updatedAt)
    }
}
