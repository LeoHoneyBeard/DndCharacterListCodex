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
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}




