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
                CharacterRecord(
                    id = 1L,
                    name = "Aylin",
                    characterClass = "Wizard",
                    subclass = "",
                    race = "Elf",
                    alignment = "Neutral Good",
                    background = "Sage",
                    level = 2,
                    armorClass = 12,
                    hitPoints = 9,
                    strength = 8,
                    dexterity = 14,
                    constitution = 12,
                    intelligence = 16,
                    wisdom = 10,
                    charisma = 13,
                    notes = "",
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
