package com.vinni.dndcharacterlist.feature.character.creation

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityMethod
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityScores
import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationStep
import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.feature.character.creation.presentation.CharacterCreationViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterCreationViewModelTest {

    private val fakeRepository = FakeCharacterRepository()
    private val viewModel = CharacterCreationViewModel(
        repository = Phb2014RulesRepository(),
        characterRepository = fakeRepository,
        launchCreate = { block -> runBlocking { block() } }
    )

    @Test
    fun nextStepRequiresOriginFields() {
        viewModel.nextStep()

        assertEquals(CharacterCreationStep.ORIGIN, viewModel.uiState.currentStep)
        assertEquals("Name is required.", viewModel.uiState.stepError)
    }

    @Test
    fun wizardPreservesDraftAcrossStepNavigation() {
        viewModel.updateName("Aylin")
        viewModel.updateRace("elf")
        viewModel.updateSubrace("high_elf")
        viewModel.updateBackground("sage")
        viewModel.nextStep()
        viewModel.updateClass("wizard")
        viewModel.nextStep()
        viewModel.updateAbilityMethod(AbilityMethod.MANUAL)
        viewModel.updateBaseAbilities(AbilityScores(8, 15, 13, 14, 12, 10))
        viewModel.nextStep()
        viewModel.previousStep()

        assertEquals(CharacterCreationStep.ABILITIES, viewModel.uiState.currentStep)
        assertEquals("Aylin", viewModel.uiState.draft.name)
        assertEquals("elf", viewModel.uiState.draft.raceId)
        assertEquals("wizard", viewModel.uiState.draft.classId)
        assertEquals(15, viewModel.uiState.draft.baseAbilities?.dexterity)
    }

    @Test
    fun submitOnlyActivatesOnSummaryStep() {
        viewModel.createCharacter {}
        assertFalse(viewModel.uiState.isSubmitting)

        var createdId: Long? = null
        viewModel.updateName("Aylin")
        viewModel.updateRace("elf")
        viewModel.updateSubrace("high_elf")
        viewModel.updateBackground("sage")
        viewModel.nextStep()
        viewModel.updateClass("wizard")
        viewModel.nextStep()
        viewModel.updateAbilityMethod(AbilityMethod.MANUAL)
        viewModel.updateBaseAbilities(AbilityScores(8, 15, 13, 14, 12, 10))
        viewModel.nextStep()
        viewModel.toggleClassSkill("arcana")
        viewModel.toggleClassSkill("history")
        viewModel.updateReplacementSkill("arcana", "investigation")
        viewModel.updateReplacementSkill("history", "medicine")
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()

        assertEquals(CharacterCreationStep.SUMMARY, viewModel.uiState.currentStep)

        viewModel.createCharacter { createdId = it }

        assertFalse(viewModel.uiState.isSubmitting)
        assertEquals(1L, createdId)
        assertEquals(1, fakeRepository.characterCount())
    }

    @Test
    fun submitFailureResetsSubmittingAndExposesError() {
        val failingViewModel = CharacterCreationViewModel(
            repository = Phb2014RulesRepository(),
            characterRepository = FailingCharacterRepository(),
            launchCreate = { block -> runBlocking { block() } }
        )

        failingViewModel.updateName("Aylin")
        failingViewModel.updateRace("elf")
        failingViewModel.updateSubrace("high_elf")
        failingViewModel.updateBackground("sage")
        failingViewModel.nextStep()
        failingViewModel.updateClass("wizard")
        failingViewModel.nextStep()
        failingViewModel.updateAbilityMethod(AbilityMethod.MANUAL)
        failingViewModel.updateBaseAbilities(AbilityScores(8, 15, 13, 14, 12, 10))
        failingViewModel.nextStep()
        failingViewModel.toggleClassSkill("arcana")
        failingViewModel.toggleClassSkill("history")
        failingViewModel.updateReplacementSkill("arcana", "investigation")
        failingViewModel.updateReplacementSkill("history", "medicine")
        failingViewModel.nextStep()
        failingViewModel.nextStep()
        failingViewModel.nextStep()

        failingViewModel.createCharacter {}

        assertFalse(failingViewModel.uiState.isSubmitting)
        assertEquals("Failed to create character. Try again.", failingViewModel.uiState.stepError)
    }

    @Test
    fun dirtyCreationRequiresExplicitDiscardConfirmation() {
        var exited = false

        viewModel.updateName("Aylin")
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
    fun successfulCreateAllowsExitWithoutPrompt() {
        var exited = false

        viewModel.updateName("Aylin")
        viewModel.updateRace("elf")
        viewModel.updateSubrace("high_elf")
        viewModel.updateBackground("sage")
        viewModel.nextStep()
        viewModel.updateClass("wizard")
        viewModel.nextStep()
        viewModel.updateAbilityMethod(AbilityMethod.MANUAL)
        viewModel.updateBaseAbilities(AbilityScores(8, 15, 13, 14, 12, 10))
        viewModel.nextStep()
        viewModel.toggleClassSkill("arcana")
        viewModel.toggleClassSkill("history")
        viewModel.updateReplacementSkill("arcana", "investigation")
        viewModel.updateReplacementSkill("history", "medicine")
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()

        viewModel.createCharacter {}
        viewModel.requestExit { exited = true }

        assertFalse(viewModel.uiState.isDiscardConfirmationVisible)
        assertFalse(viewModel.uiState.hasUnsavedChanges)
        assertTrue(exited)
    }

    @Test
    fun callbackFailureDoesNotBecomePersistenceError() {
        val callbackFailingViewModel = CharacterCreationViewModel(
            repository = Phb2014RulesRepository(),
            characterRepository = fakeRepository,
            launchCreate = { block -> runBlocking { block() } }
        )

        callbackFailingViewModel.updateName("Aylin")
        callbackFailingViewModel.updateRace("elf")
        callbackFailingViewModel.updateSubrace("high_elf")
        callbackFailingViewModel.updateBackground("sage")
        callbackFailingViewModel.nextStep()
        callbackFailingViewModel.updateClass("wizard")
        callbackFailingViewModel.nextStep()
        callbackFailingViewModel.updateAbilityMethod(AbilityMethod.MANUAL)
        callbackFailingViewModel.updateBaseAbilities(AbilityScores(8, 15, 13, 14, 12, 10))
        callbackFailingViewModel.nextStep()
        callbackFailingViewModel.toggleClassSkill("arcana")
        callbackFailingViewModel.toggleClassSkill("history")
        callbackFailingViewModel.updateReplacementSkill("arcana", "investigation")
        callbackFailingViewModel.updateReplacementSkill("history", "medicine")
        callbackFailingViewModel.nextStep()
        callbackFailingViewModel.nextStep()
        callbackFailingViewModel.nextStep()

        val result = runCatching {
            callbackFailingViewModel.createCharacter { throw IllegalStateException("navigation failed") }
        }

        assertTrue(result.isSuccess)
        assertFalse(callbackFailingViewModel.uiState.isSubmitting)
        assertEquals("Character created, but navigation failed. Try again.", callbackFailingViewModel.uiState.stepError)
        assertEquals(1, fakeRepository.characterCount())

        callbackFailingViewModel.createCharacter {}

        assertEquals(1, fakeRepository.characterCount())
    }

    @Test
    fun cancellationIsRethrown() {
        val cancellingViewModel = CharacterCreationViewModel(
            repository = Phb2014RulesRepository(),
            characterRepository = CancellingCharacterRepository(),
            launchCreate = { block -> runBlocking { block() } }
        )

        cancellingViewModel.updateName("Aylin")
        cancellingViewModel.updateRace("elf")
        cancellingViewModel.updateSubrace("high_elf")
        cancellingViewModel.updateBackground("sage")
        cancellingViewModel.nextStep()
        cancellingViewModel.updateClass("wizard")
        cancellingViewModel.nextStep()
        cancellingViewModel.updateAbilityMethod(AbilityMethod.MANUAL)
        cancellingViewModel.updateBaseAbilities(AbilityScores(8, 15, 13, 14, 12, 10))
        cancellingViewModel.nextStep()
        cancellingViewModel.toggleClassSkill("arcana")
        cancellingViewModel.toggleClassSkill("history")
        cancellingViewModel.updateReplacementSkill("arcana", "investigation")
        cancellingViewModel.updateReplacementSkill("history", "medicine")
        cancellingViewModel.nextStep()
        cancellingViewModel.nextStep()
        cancellingViewModel.nextStep()

        val result = runCatching { cancellingViewModel.createCharacter {} }

        assertTrue(result.exceptionOrNull() is CancellationException)
        assertFalse(cancellingViewModel.uiState.isSubmitting)
        assertNull(cancellingViewModel.uiState.stepError)
    }

    private class FakeCharacterRepository : CharacterRepository {
        private val characters = MutableStateFlow<List<CharacterRecord>>(emptyList())

        override fun observeCharacters(): Flow<List<CharacterRecord>> = characters

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> {
            return characters.map { items -> items.firstOrNull { it.id == id } }
        }

        override suspend fun getCharacter(id: Long): CharacterRecord? {
            return characters.value.firstOrNull { it.id == id }
        }

        override suspend fun saveCharacter(character: CharacterUpsert) {
            val nextId = character.id ?: ((characters.value.maxOfOrNull(CharacterRecord::id) ?: 0L) + 1L)
            val record = CharacterRecord(
                id = nextId,
                name = character.name,
                characterClass = character.characterClass,
                subclass = character.subclass,
                race = character.race,
                alignment = character.alignment,
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
                updatedAt = System.currentTimeMillis()
            )
            characters.value = characters.value.filterNot { it.id == nextId } + record
        }

        override suspend fun createCharacter(character: CharacterRecord): Long {
            val nextId = (characters.value.maxOfOrNull(CharacterRecord::id) ?: 0L) + 1L
            characters.value = characters.value + character.copy(id = nextId)
            return nextId
        }

        override suspend fun deleteCharacter(id: Long) {
            characters.value = characters.value.filterNot { it.id == id }
        }

        fun characterCount(): Int = characters.value.size
    }

    private class FailingCharacterRepository : CharacterRepository {
        override fun observeCharacters(): Flow<List<CharacterRecord>> = MutableStateFlow(emptyList())

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(null)

        override suspend fun getCharacter(id: Long): CharacterRecord? = null

        override suspend fun saveCharacter(character: CharacterUpsert) = Unit

        override suspend fun createCharacter(character: CharacterRecord): Long {
            throw IllegalStateException("boom")
        }

        override suspend fun deleteCharacter(id: Long) = Unit
    }

    private class CancellingCharacterRepository : CharacterRepository {
        override fun observeCharacters(): Flow<List<CharacterRecord>> = MutableStateFlow(emptyList())

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(null)

        override suspend fun getCharacter(id: Long): CharacterRecord? = null

        override suspend fun saveCharacter(character: CharacterUpsert) = Unit

        override suspend fun createCharacter(character: CharacterRecord): Long {
            throw CancellationException("cancel")
        }

        override suspend fun deleteCharacter(id: Long) = Unit
    }
}




