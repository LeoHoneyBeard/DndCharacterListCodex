package com.vinni.dndcharacterlist.creation

import com.vinni.dndcharacterlist.creation.model.AbilityMethod
import com.vinni.dndcharacterlist.creation.model.AbilityScores
import com.vinni.dndcharacterlist.creation.model.CharacterCreationStep
import com.vinni.dndcharacterlist.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.data.CharacterDao
import com.vinni.dndcharacterlist.data.CharacterEntity
import com.vinni.dndcharacterlist.data.CharacterRepository
import com.vinni.dndcharacterlist.ui.creation.CharacterCreationViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

class CharacterCreationViewModelTest {

    private val fakeDao = FakeCharacterDao()
    private val viewModel = CharacterCreationViewModel(
        repository = Phb2014RulesRepository(),
        characterRepository = CharacterRepository(fakeDao),
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
        assertEquals(1, fakeDao.characterCount())
    }

    private class FakeCharacterDao : CharacterDao {
        private val characters = MutableStateFlow<List<CharacterEntity>>(emptyList())

        override fun observeAll(): Flow<List<CharacterEntity>> = characters

        override fun observeById(id: Long): Flow<CharacterEntity?> {
            return MutableStateFlow(characters.value.firstOrNull { it.id == id })
        }

        override suspend fun getById(id: Long): CharacterEntity? {
            return characters.value.firstOrNull { it.id == id }
        }

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

        fun characterCount(): Int = characters.value.size
    }
}
