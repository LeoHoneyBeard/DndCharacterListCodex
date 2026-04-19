package com.vinni.dndcharacterlist.feature.character.list.presentation

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CharacterListViewModelTest {

    @Test
    fun `toListItem omits blank and null-like labels from summary`() {
        val record = CharacterRecord(
            id = 42L,
            name = "Nyx",
            characterClass = "null",
            subclass = " ",
            race = " Tiefling ",
            alignment = "Chaotic Neutral",
            background = "Criminal",
            level = 3,
            armorClass = 15,
            hitPoints = 21,
            strength = 8,
            dexterity = 16,
            constitution = 14,
            intelligence = 12,
            wisdom = 10,
            charisma = 18,
            notes = "",
            updatedAt = 1L
        )

        assertEquals(
            CharacterListItem(
                id = 42L,
                name = "Nyx",
                summary = "Lvl 3 | Tiefling | AC 15 | HP 21"
            ),
            record.toListItem()
        )
    }

    @Test
    fun `uiState exposes populated content separately from loading`() {
        val repository = FakeCharacterRepository(
            listOf(
                character(
                    id = 1L,
                    name = "Aylin",
                    characterClass = "Wizard",
                    race = "Elf",
                    level = 2,
                    armorClass = 12,
                    hitPoints = 9,
                    updatedAt = 1L
                )
            )
        )

        val viewModel = CharacterListViewModel(
            repository = repository,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.characters.size)
        assertEquals(null, viewModel.uiState.value.errorMessage)
        assertEquals(CharacterListSortMode.UPDATED_AT, viewModel.uiState.value.sortMode)
        assertEquals(listOf("Wizard"), viewModel.uiState.value.availableClasses)
        assertEquals(listOf("Elf"), viewModel.uiState.value.availableRaces)
    }

    @Test
    fun `uiState exposes repository failure separately from empty state`() {
        val viewModel = CharacterListViewModel(
            repository = ErrorCharacterRepository(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(emptyList<CharacterListItem>(), viewModel.uiState.value.characters)
        assertEquals("Couldn't load characters. Try again.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `search query filters visible characters by name`() {
        val repository = FakeCharacterRepository(
            listOf(
                character(id = 1L, name = "Aylin", updatedAt = 3L),
                character(id = 2L, name = "Borin", updatedAt = 2L),
                character(id = 3L, name = "Aya", updatedAt = 1L)
            )
        )
        val viewModel = CharacterListViewModel(
            repository = repository,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )

        viewModel.updateSearchQuery("ay")

        assertEquals("ay", viewModel.uiState.value.searchQuery)
        assertEquals(listOf("Aylin", "Aya"), viewModel.uiState.value.characters.map(CharacterListItem::name))
        assertEquals(true, viewModel.uiState.value.hasSavedCharacters)
    }

    @Test
    fun `sort mode changes reorder the same visible list`() {
        val repository = FakeCharacterRepository(
            listOf(
                character(id = 1L, name = "Borin", level = 2, updatedAt = 1L),
                character(id = 2L, name = "Aylin", level = 5, updatedAt = 2L),
                character(id = 3L, name = "Cora", level = 3, updatedAt = 3L)
            )
        )
        val viewModel = CharacterListViewModel(
            repository = repository,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )

        assertEquals(listOf("Cora", "Aylin", "Borin"), viewModel.uiState.value.characters.map(CharacterListItem::name))

        viewModel.setSortMode(CharacterListSortMode.NAME)
        assertEquals(listOf("Aylin", "Borin", "Cora"), viewModel.uiState.value.characters.map(CharacterListItem::name))

        viewModel.setSortMode(CharacterListSortMode.LEVEL)
        assertEquals(listOf("Aylin", "Cora", "Borin"), viewModel.uiState.value.characters.map(CharacterListItem::name))
    }

    @Test
    fun `class race and level filters combine with search and sorting`() {
        val repository = FakeCharacterRepository(
            listOf(
                character(id = 1L, name = "Aylin", characterClass = "Wizard", race = "Elf", level = 4, updatedAt = 1L),
                character(id = 2L, name = "Ayla", characterClass = "Wizard", race = "Elf", level = 7, updatedAt = 4L),
                character(id = 3L, name = "Borin", characterClass = "Wizard", race = "Dwarf", level = 7, updatedAt = 3L),
                character(id = 4L, name = "Ayla Storm", characterClass = "Ranger", race = "Elf", level = 7, updatedAt = 2L)
            )
        )
        val viewModel = CharacterListViewModel(
            repository = repository,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )

        viewModel.updateSearchQuery("ay")
        viewModel.setClassFilter("Wizard")
        viewModel.setRaceFilter("Elf")
        viewModel.setLevelFilter(CharacterListLevelFilter.LEVELS_5_TO_10)
        viewModel.setSortMode(CharacterListSortMode.NAME)

        assertEquals(listOf("Ayla"), viewModel.uiState.value.characters.map(CharacterListItem::name))
        assertEquals("Wizard", viewModel.uiState.value.classFilter)
        assertEquals("Elf", viewModel.uiState.value.raceFilter)
        assertEquals(CharacterListLevelFilter.LEVELS_5_TO_10, viewModel.uiState.value.levelFilter)
        assertEquals(true, viewModel.uiState.value.hasActiveFilters)
    }

    @Test
    fun `level filter buckets include expected boundaries`() {
        val repository = FakeCharacterRepository(
            listOf(
                character(id = 1L, name = "Four", level = 4, updatedAt = 4L),
                character(id = 2L, name = "Five", level = 5, updatedAt = 3L),
                character(id = 3L, name = "Ten", level = 10, updatedAt = 2L),
                character(id = 4L, name = "Eleven", level = 11, updatedAt = 1L),
                character(id = 5L, name = "Sixteen", level = 16, updatedAt = 5L),
                character(id = 6L, name = "Seventeen", level = 17, updatedAt = 6L)
            )
        )
        val viewModel = CharacterListViewModel(
            repository = repository,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )

        viewModel.setLevelFilter(CharacterListLevelFilter.LEVELS_1_TO_4)
        assertEquals(listOf("Four"), viewModel.uiState.value.characters.map(CharacterListItem::name))

        viewModel.setLevelFilter(CharacterListLevelFilter.LEVELS_5_TO_10)
        assertEquals(listOf("Five", "Ten"), viewModel.uiState.value.characters.map(CharacterListItem::name))

        viewModel.setLevelFilter(CharacterListLevelFilter.LEVELS_11_TO_16)
        assertEquals(listOf("Sixteen", "Eleven"), viewModel.uiState.value.characters.map(CharacterListItem::name))

        viewModel.setLevelFilter(CharacterListLevelFilter.LEVELS_17_TO_20)
        assertEquals(listOf("Seventeen"), viewModel.uiState.value.characters.map(CharacterListItem::name))
    }

    private fun character(
        id: Long,
        name: String,
        characterClass: String = "Wizard",
        race: String = "Elf",
        level: Int = 1,
        armorClass: Int = 12,
        hitPoints: Int = 8,
        updatedAt: Long
    ): CharacterRecord {
        return CharacterRecord(
            id = id,
            name = name,
            characterClass = characterClass,
            subclass = "",
            race = race,
            alignment = "Neutral Good",
            background = "Sage",
            level = level,
            armorClass = armorClass,
            hitPoints = hitPoints,
            strength = 8,
            dexterity = 14,
            constitution = 12,
            intelligence = 16,
            wisdom = 10,
            charisma = 13,
            notes = "",
            updatedAt = updatedAt
        )
    }

    private class FakeCharacterRepository(
        initialCharacters: List<CharacterRecord>
    ) : CharacterRepository {
        private val characters = MutableStateFlow(initialCharacters)

        override fun observeCharacters(): Flow<List<CharacterRecord>> = characters

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(null)

        override suspend fun getCharacter(id: Long): CharacterRecord? = null

        override suspend fun saveCharacter(character: CharacterUpsert) = Unit

        override suspend fun createCharacter(character: CharacterRecord): Long = 0L

        override suspend fun deleteCharacter(id: Long) = Unit
    }

    private class ErrorCharacterRepository : CharacterRepository {
        override fun observeCharacters(): Flow<List<CharacterRecord>> = flow { error("boom") }

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(null)

        override suspend fun getCharacter(id: Long): CharacterRecord? = null

        override suspend fun saveCharacter(character: CharacterUpsert) = Unit

        override suspend fun createCharacter(character: CharacterRecord): Long = 0L

        override suspend fun deleteCharacter(id: Long) = Unit
    }
}
