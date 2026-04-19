package com.vinni.dndcharacterlist.feature.character.detail.presentation

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadsCharacterIntoUiState() = runTest {
        val repository = FakeCharacterRepository(
            character(
                id = 7L,
                level = 4,
                race = "Elf",
                characterClass = "Wizard",
                subclass = "School of Evocation",
                hitPoints = 27,
                hitPointsMax = 32,
                savingThrowProficiencies = listOf("INTELLIGENCE", "WISDOM"),
                skillProficiencies = listOf("arcana", "sleight_of_hand"),
                notes = "Knows too much"
            )
        )

        val viewModel = CharacterDetailViewModel(repository, characterId = 7L)
        advanceUntilIdle()

        val model = requireNotNull(viewModel.uiState.character)
        assertFalse(viewModel.uiState.isLoading)
        assertEquals("Level 4 | Elf | Wizard | School of Evocation", model.subtitle)
        assertEquals("PHB 2014", model.ruleset)
        assertEquals(
            listOf("Class: Wizard", "Subclass: School of Evocation", "Level 4"),
            model.progressionDetails
        )
        assertEquals(27, model.hitPoints)
        assertEquals(32, model.hitPointsMax)
        assertEquals(listOf("Intelligence", "Wisdom"), model.savingThrowProficiencies)
        assertEquals(listOf("Arcana", "Sleight Of Hand"), model.skillProficiencies)
        assertEquals(listOf("STR", "DEX", "CON", "INT", "WIS", "CHA"), model.stats.map(StatValue::label))
        assertEquals(listOf(8, 14, 12, 16, 10, 13), model.stats.map(StatValue::value))
        assertEquals("Knows too much", model.notes)
        assertTrue(model.canLevelUp)
    }

    @Test
    fun omitsBlankSegmentsAndBlocksLevelUpAtCap() = runTest {
        val repository = FakeCharacterRepository(
            character(
                id = 9L,
                level = 20,
                race = "",
                characterClass = "Fighter",
                subclass = ""
            )
        )

        val viewModel = CharacterDetailViewModel(repository, characterId = 9L)
        advanceUntilIdle()

        val model = requireNotNull(viewModel.uiState.character)
        assertEquals("Level 20 | Fighter", model.subtitle)
        assertFalse(model.canLevelUp)
    }

    @Test
    fun missingCharacterLeavesEmptyLoadedState() = runTest {
        val viewModel = CharacterDetailViewModel(FakeCharacterRepository(), characterId = 404L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.character)
    }

    @Test
    fun duplicateCreatesNewDraftWithoutChangingSourceCharacter() = runTest {
        val sourceCharacter = character(
            id = 7L,
            level = 4,
            race = "Elf",
            characterClass = "Wizard",
            subclass = "School of Evocation",
            hitPoints = 27,
            hitPointsMax = 32,
            notes = "Knows too much"
        )
        val repository = FakeCharacterRepository(sourceCharacter)
        val viewModel = CharacterDetailViewModel(repository, characterId = 7L)
        var duplicatedId: Long? = null

        advanceUntilIdle()
        viewModel.duplicate { newId -> duplicatedId = newId }
        advanceUntilIdle()

        assertEquals(100L, duplicatedId)
        assertEquals(7L, repository.sourceCharacter?.id)
        assertEquals(100L, repository.createdCharacter?.id)
        assertEquals(sourceCharacter.name, repository.createdCharacter?.name)
        assertEquals(sourceCharacter.classId, repository.createdCharacter?.classId)
        assertEquals(sourceCharacter.subclassId, repository.createdCharacter?.subclassId)
        assertEquals(sourceCharacter.raceId, repository.createdCharacter?.raceId)
        assertEquals(sourceCharacter.backgroundId, repository.createdCharacter?.backgroundId)
        assertEquals(sourceCharacter.notes, repository.createdCharacter?.notes)
    }

    private fun character(
        id: Long,
        level: Int,
        race: String,
        characterClass: String,
        subclass: String,
        hitPoints: Int = 32,
        hitPointsMax: Int = hitPoints,
        savingThrowProficiencies: List<String> = emptyList(),
        skillProficiencies: List<String> = emptyList(),
        notes: String = ""
    ): CharacterRecord {
        return CharacterRecord(
            id = id,
            ruleset = "PHB_2014",
            name = "Aylin",
            classId = "wizard",
            characterClass = characterClass,
            subclassId = if (subclass.isBlank()) "" else "evocation",
            subclass = subclass,
            raceId = if (race.isBlank()) "" else "elf",
            race = race,
            subraceId = "",
            alignment = "Neutral Good",
            backgroundId = "sage",
            background = "Sage",
            level = level,
            abilityMethod = "STANDARD_ARRAY",
            armorClass = 15,
            hitPoints = hitPoints,
            hitPointsMax = hitPointsMax,
            strength = 8,
            dexterity = 14,
            constitution = 12,
            intelligence = 16,
            wisdom = 10,
            charisma = 13,
            savingThrowProficiencies = savingThrowProficiencies,
            skillProficiencies = skillProficiencies,
            notes = notes,
            updatedAt = 1L
        )
    }

    private class FakeCharacterRepository(vararg initialCharacters: CharacterRecord) : CharacterRepository {
        private val characters = MutableStateFlow(initialCharacters.toList())
        var sourceCharacter: CharacterRecord? = initialCharacters.firstOrNull()
        var createdCharacter: CharacterRecord? = null

        override fun observeCharacters(): Flow<List<CharacterRecord>> = characters

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> {
            return characters.map { items -> items.firstOrNull { it.id == id } }
        }

        override suspend fun getCharacter(id: Long): CharacterRecord? {
            return characters.value.firstOrNull { it.id == id }
        }

        override suspend fun saveCharacter(character: CharacterUpsert) = Unit

        override suspend fun createCharacter(character: CharacterRecord): Long {
            val nextId = 100L
            createdCharacter = character.copy(id = nextId)
            characters.value = characters.value + requireNotNull(createdCharacter)
            return nextId
        }

        override suspend fun deleteCharacter(id: Long) = Unit
    }

    class MainDispatcherRule(
        private val dispatcher: TestDispatcher = StandardTestDispatcher()
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
