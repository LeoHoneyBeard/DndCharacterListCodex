package com.vinni.dndcharacterlist.feature.character.editor

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.core.rules.editor.CharacterEditorRules
import com.vinni.dndcharacterlist.feature.character.editor.presentation.CharacterEditorViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterEditorViewModelTest {
    @Test
    fun validSavePersistsCanonicalIdsAndNames() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            characterId = null,
            launchAsync = { block -> runBlocking { block() } }
        )

        viewModel.update {
            copy(
                name = "Aylin",
                characterClass = "Wizard",
                race = "Human",
                background = "Sage",
                level = "1",
                armorClass = "12",
                hitPoints = "8",
                strength = "8",
                dexterity = "14",
                constitution = "12",
                intelligence = "15",
                wisdom = "10",
                charisma = "13"
            )
        }

        viewModel.save {}

        assertEquals("wizard", repository.lastSavedCharacter?.classId)
        assertEquals("Human", repository.lastSavedCharacter?.race)
        assertEquals("sage", repository.lastSavedCharacter?.backgroundId)
        assertNull(viewModel.uiState.validationMessage)
    }

    @Test
    fun invalidSaveIsBlockedBeforePersistence() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            characterId = null,
            launchAsync = { block -> runBlocking { block() } }
        )

        viewModel.update {
            copy(
                name = "Aylin",
                characterClass = "Time Lord",
                race = "Human",
                background = "Sage"
            )
        }

        viewModel.save {}

        assertEquals("Choose a supported class.", viewModel.uiState.validationMessage)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun saveCallbackFailureIsRecoverableWithoutSecondSave() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            characterId = null,
            launchAsync = { block -> runBlocking { block() } }
        )

        viewModel.update {
            copy(
                name = "Aylin",
                characterClass = "Wizard",
                race = "Human",
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
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            characterId = 42L,
            launchAsync = { block -> runBlocking { block() } }
        )

        viewModel.requestDeleteConfirmation()
        runCatching { viewModel.confirmDelete { throw IllegalStateException("nav") } }

        assertEquals("Character deleted, but navigation failed. Try again.", viewModel.uiState.saveErrorMessage)
        assertEquals(1, repository.deleteCalls)

        viewModel.confirmDelete {}

        assertEquals(1, repository.deleteCalls)
    }

    @Test
    fun deleteRequiresExplicitConfirmation() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            characterId = 42L,
            launchAsync = { block -> runBlocking { block() } }
        )

        viewModel.requestDeleteConfirmation()

        assertEquals(true, viewModel.uiState.isDeleteConfirmationVisible)
        assertEquals(0, repository.deleteCalls)

        viewModel.dismissDeleteConfirmation()

        assertEquals(false, viewModel.uiState.isDeleteConfirmationVisible)
        assertEquals(0, repository.deleteCalls)
    }

    @Test
    fun deleteReportsMissingCharacterInsteadOfSucceedingSilently() {
        val repository = FakeCharacterRepository(deleteError = IllegalArgumentException("missing"))
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            characterId = 42L,
            launchAsync = { block -> runBlocking { block() } }
        )

        viewModel.requestDeleteConfirmation()
        viewModel.confirmDelete {}

        assertEquals(1, repository.deleteCalls)
        assertEquals("Character no longer exists. Reopen it from the list.", viewModel.uiState.saveErrorMessage)
        assertNull(viewModel.uiState.completedAction)
    }

    private class FakeCharacterRepository(
        private val deleteError: IllegalArgumentException? = null
    ) : CharacterRepository {
        var saveCalls = 0
        var deleteCalls = 0
        var lastSavedCharacter: CharacterUpsert? = null

        override fun observeCharacters(): Flow<List<CharacterRecord>> = MutableStateFlow(emptyList())

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(null)

        override suspend fun getCharacter(id: Long): CharacterRecord? = null

        override suspend fun saveCharacter(character: CharacterUpsert) {
            saveCalls += 1
            lastSavedCharacter = character
        }

        override suspend fun createCharacter(character: CharacterRecord): Long = 1L

        override suspend fun deleteCharacter(id: Long) {
            deleteCalls += 1
            deleteError?.let { throw it }
        }
    }
}
