package com.vinni.dndcharacterlist.feature.character.levelup

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.creation.repository.Phb2014RulesRepository
import com.vinni.dndcharacterlist.feature.character.levelup.domain.CharacterLevelUpPlanner
import com.vinni.dndcharacterlist.feature.character.levelup.domain.LevelUpCharacterUseCase
import com.vinni.dndcharacterlist.feature.character.levelup.presentation.CharacterLevelUpViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterLevelUpViewModelTest {
    @Test
    fun applyLevelUpRetriesNavigationWithoutDuplicatingSave() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterLevelUpViewModel(
            repository = repository,
            levelUpPlanner = CharacterLevelUpPlanner(Phb2014RulesRepository()),
            levelUpCharacter = LevelUpCharacterUseCase(repository),
            characterId = repository.character.id,
            launchAsync = { block -> runBlocking { block() } }
        )

        runCatching { viewModel.applyLevelUp { throw IllegalStateException("nav") } }

        assertEquals("Level up applied, but navigation failed. Try again.", viewModel.uiState.errorMessage)
        assertEquals(1, repository.updateCalls)

        viewModel.applyLevelUp {}

        assertEquals(1, repository.updateCalls)
    }

    private class FakeCharacterRepository : CharacterRepository {
        var updateCalls = 0
        var character = CharacterRecord(
            id = 42L,
            name = "Wyll",
            classId = "warlock",
            characterClass = "Warlock",
            subclass = "",
            race = "Human",
            alignment = "",
            background = "Noble",
            level = 2,
            armorClass = 13,
            hitPoints = 17,
            hitPointsMax = 17,
            strength = 8,
            dexterity = 14,
            constitution = 14,
            intelligence = 13,
            wisdom = 10,
            charisma = 17,
            notes = "",
            updatedAt = 0L
        )

        override fun observeCharacters(): Flow<List<CharacterRecord>> = MutableStateFlow(listOf(character))

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(character)

        override suspend fun getCharacter(id: Long): CharacterRecord? = character.takeIf { it.id == id }

        override suspend fun saveCharacter(character: CharacterUpsert) = Unit

        override suspend fun createCharacter(character: CharacterRecord): Long = character.id

        override suspend fun updateCharacter(character: CharacterRecord) {
            updateCalls += 1
            this.character = character
        }

        override suspend fun deleteCharacter(id: Long) = Unit
    }
}
