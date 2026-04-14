package com.vinni.dndcharacterlist.feature.character.editor

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.feature.character.editor.presentation.CharacterEditorViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterEditorViewModelTest {
    @Test
    fun saveCallbackFailureIsRecoverableWithoutSecondSave() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            characterId = null,
            launchAsync = { block -> runBlocking { block() } }
        )

        viewModel.update {
            copy(
                name = "Aylin",
                characterClass = "Wizard",
                race = "Elf",
                background = "Sage"
            )
        }

        runCatching { viewModel.save { throw IllegalStateException("nav") } }

        assertEquals("Character saved, but navigation failed. Try again.", viewModel.uiState.saveErrorMessage)
        assertEquals(1, repository.saveCalls)

        viewModel.save {}

        assertEquals(1, repository.saveCalls)
    }

    @Test
    fun deleteCallbackFailureIsRecoverableWithoutSecondDelete() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            characterId = 42L,
            launchAsync = { block -> runBlocking { block() } }
        )

        runCatching { viewModel.delete { throw IllegalStateException("nav") } }

        assertEquals("Character deleted, but navigation failed. Try again.", viewModel.uiState.saveErrorMessage)
        assertEquals(1, repository.deleteCalls)

        viewModel.delete {}

        assertEquals(1, repository.deleteCalls)
    }

    private class FakeCharacterRepository : CharacterRepository {
        var saveCalls = 0
        var deleteCalls = 0

        override fun observeCharacters(): Flow<List<CharacterRecord>> = MutableStateFlow(emptyList())

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(null)

        override suspend fun getCharacter(id: Long): CharacterRecord? = null

        override suspend fun saveCharacter(character: CharacterUpsert) {
            saveCalls += 1
        }

        override suspend fun createCharacter(character: CharacterRecord): Long = 1L

        override suspend fun deleteCharacter(id: Long) {
            deleteCalls += 1
        }
    }
}
