package com.vinni.dndcharacterlist.core.data.repository

import com.vinni.dndcharacterlist.core.data.local.CharacterDao
import com.vinni.dndcharacterlist.core.data.local.CharacterEntity
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

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

    @Test
    fun saveCharacterThrowsWhenExistingRowIsMissing() = runBlocking {
        val repository = RoomCharacterRepository(FakeCharacterDao())
        val upsert = CharacterUpsert(
            id = 999L,
            name = "Missing",
            characterClass = "Wizard",
            subclass = "",
            race = "Elf",
            alignment = "",
            background = "Sage",
            level = 1,
            armorClass = 12,
            hitPoints = 8,
            strength = 8,
            dexterity = 14,
            constitution = 12,
            intelligence = 16,
            wisdom = 10,
            charisma = 10,
            notes = ""
        )

        val result = runCatching { repository.saveCharacter(upsert) }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    private class FakeCharacterDao : CharacterDao {
        private val characters = MutableStateFlow<List<CharacterEntity>>(emptyList())

        override fun observeAll(): Flow<List<CharacterEntity>> = characters

        override fun observeById(id: Long): Flow<CharacterEntity?> {
            return MutableStateFlow(characters.value.firstOrNull { it.id == id })
        }

        override suspend fun getById(id: Long): CharacterEntity? = characters.value.firstOrNull { it.id == id }

        override suspend fun insert(character: CharacterEntity): Long {
            val nextId = (characters.value.maxOfOrNull(CharacterEntity::id) ?: 0L) + 1L
            characters.value = characters.value + character.copy(id = nextId)
            return nextId
        }

        override suspend fun update(character: CharacterEntity) {
            characters.value = characters.value.map { existing ->
                if (existing.id == character.id) character else existing
            }
        }

        override suspend fun deleteById(id: Long) {
            characters.value = characters.value.filterNot { it.id == id }
        }
    }
}
