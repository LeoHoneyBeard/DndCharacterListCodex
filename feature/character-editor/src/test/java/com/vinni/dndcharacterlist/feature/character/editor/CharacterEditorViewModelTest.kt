package com.vinni.dndcharacterlist.feature.character.editor

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.core.rules.editor.CharacterEditorRules
import com.vinni.dndcharacterlist.feature.character.editor.domain.DeleteCharacterUseCase
import com.vinni.dndcharacterlist.feature.character.editor.domain.UpdateCharacterUseCase
import com.vinni.dndcharacterlist.feature.character.editor.presentation.CharacterEditorViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterEditorViewModelTest {
    @Test
    fun validSavePersistsCanonicalIdsAndNames() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            updateCharacter = UpdateCharacterUseCase(repository, CharacterEditorRules(Phb2014RulesRepository())),
            deleteCharacter = DeleteCharacterUseCase(repository),
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
            updateCharacter = UpdateCharacterUseCase(repository, CharacterEditorRules(Phb2014RulesRepository())),
            deleteCharacter = DeleteCharacterUseCase(repository),
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
            updateCharacter = UpdateCharacterUseCase(repository, CharacterEditorRules(Phb2014RulesRepository())),
            deleteCharacter = DeleteCharacterUseCase(repository),
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
    fun dirtyEditorRequiresExplicitDiscardConfirmation() {
        val repository = FakeCharacterRepository(
            existingCharacter = record(
                id = 42L,
                name = "Aylin",
                characterClass = "Wizard",
                race = "Human",
                background = "Sage"
            )
        )
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            updateCharacter = UpdateCharacterUseCase(repository, CharacterEditorRules(Phb2014RulesRepository())),
            deleteCharacter = DeleteCharacterUseCase(repository),
            characterId = 42L,
            launchAsync = { block -> runBlocking { block() } }
        )
        var exited = false

        viewModel.update { copy(name = "Aylin Stormweaver") }
        viewModel.requestExit { exited = true }

        assertTrue(viewModel.uiState.hasUnsavedChanges)
        assertTrue(viewModel.uiState.isDiscardConfirmationVisible)
        assertFalse(exited)

        viewModel.dismissExitConfirmation()
        assertFalse(viewModel.uiState.isDiscardConfirmationVisible)

        viewModel.requestExit { exited = true }
        viewModel.confirmExit { exited = true }

        assertTrue(exited)
    }

    @Test
    fun successfulSaveAllowsExitWithoutPrompt() {
        val repository = FakeCharacterRepository(
            existingCharacter = record(
                id = 42L,
                name = "Aylin",
                characterClass = "Wizard",
                race = "Human",
                background = "Sage"
            )
        )
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            updateCharacter = UpdateCharacterUseCase(repository, CharacterEditorRules(Phb2014RulesRepository())),
            deleteCharacter = DeleteCharacterUseCase(repository),
            characterId = 42L,
            launchAsync = { block -> runBlocking { block() } }
        )
        var exited = false

        viewModel.update { copy(name = "Aylin Stormweaver") }
        viewModel.save {}
        viewModel.requestExit { exited = true }

        assertFalse(viewModel.uiState.isDiscardConfirmationVisible)
        assertFalse(viewModel.uiState.hasUnsavedChanges)
        assertTrue(exited)
    }

    @Test
    fun deleteCallbackFailureIsRecoverableWithoutSecondDelete() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterEditorViewModel(
            repository = repository,
            editorRules = CharacterEditorRules(Phb2014RulesRepository()),
            updateCharacter = UpdateCharacterUseCase(repository, CharacterEditorRules(Phb2014RulesRepository())),
            deleteCharacter = DeleteCharacterUseCase(repository),
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
            updateCharacter = UpdateCharacterUseCase(repository, CharacterEditorRules(Phb2014RulesRepository())),
            deleteCharacter = DeleteCharacterUseCase(repository),
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
            updateCharacter = UpdateCharacterUseCase(repository, CharacterEditorRules(Phb2014RulesRepository())),
            deleteCharacter = DeleteCharacterUseCase(repository),
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
        private val deleteError: IllegalArgumentException? = null,
        existingCharacter: CharacterRecord? = null
    ) : CharacterRepository {
        var saveCalls = 0
        var deleteCalls = 0
        var lastSavedCharacter: CharacterUpsert? = null
        private var storedCharacter = existingCharacter

        override fun observeCharacters(): Flow<List<CharacterRecord>> = MutableStateFlow(emptyList())

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(null)

        override suspend fun getCharacter(id: Long): CharacterRecord? = storedCharacter?.takeIf { it.id == id }

        override suspend fun saveCharacter(character: CharacterUpsert) {
            saveCalls += 1
            lastSavedCharacter = character
            val savedId = character.id ?: storedCharacter?.id ?: 1L
            storedCharacter = CharacterRecord(
                id = savedId,
                ruleset = character.ruleset ?: "PHB_2014",
                name = character.name,
                classId = character.classId ?: "",
                characterClass = character.characterClass,
                subclassId = character.subclassId ?: "",
                subclass = character.subclass,
                raceId = character.raceId ?: "",
                race = character.race,
                subraceId = character.subraceId ?: "",
                alignment = character.alignment,
                backgroundId = character.backgroundId ?: "",
                background = character.background,
                level = character.level,
                armorClass = character.armorClass,
                hitPoints = character.hitPoints,
                strength = character.strength,
                dexterity = character.dexterity,
                constitution = character.constitution,
                intelligence = character.intelligence,
                wisdom = character.wisdom,
                charisma = character.charisma,
                notes = character.notes,
                savingThrowProficiencies = emptyList(),
                skillProficiencies = emptyList(),
                updatedAt = 0L
            )
        }

        override suspend fun createCharacter(character: CharacterRecord): Long = 1L

        override suspend fun deleteCharacter(id: Long) {
            deleteCalls += 1
            deleteError?.let { throw it }
            if (storedCharacter?.id == id) {
                storedCharacter = null
            }
        }
    }

    private fun record(
        id: Long,
        name: String,
        characterClass: String,
        race: String,
        background: String,
        level: Int = 1,
        armorClass: Int = 12,
        hitPoints: Int = 8
    ): CharacterRecord {
        return CharacterRecord(
            id = id,
            name = name,
            ruleset = "PHB_2014",
            classId = characterClass.lowercase(),
            characterClass = characterClass,
            subclassId = "",
            subclass = "",
            raceId = race.lowercase(),
            race = race,
            subraceId = "",
            alignment = "",
            backgroundId = background.lowercase(),
            background = background,
            level = level,
            armorClass = armorClass,
            hitPoints = hitPoints,
            strength = 10,
            dexterity = 14,
            constitution = 12,
            intelligence = 10,
            wisdom = 13,
            charisma = 8,
            notes = "",
            savingThrowProficiencies = emptyList(),
            skillProficiencies = emptyList(),
            updatedAt = 0L
        )
    }
}
