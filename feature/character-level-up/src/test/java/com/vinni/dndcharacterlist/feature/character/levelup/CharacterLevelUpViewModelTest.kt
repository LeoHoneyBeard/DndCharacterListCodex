package com.vinni.dndcharacterlist.feature.character.levelup

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.BackgroundDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.ClassDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.RaceDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent
import com.vinni.dndcharacterlist.core.rules.creation.rules.SkillDefinition
import com.vinni.dndcharacterlist.core.rules.creation.rules.SubclassDefinition
import com.vinni.dndcharacterlist.core.rules.levelup.CharacterLevelUpRules
import com.vinni.dndcharacterlist.feature.character.levelup.presentation.CharacterLevelUpViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterLevelUpViewModelTest {

    @Test
    fun applyLevelUpPersistsNextLevelForReadyCharacter() {
        val repository = FakeCharacterRepository(
            baseCharacter(
                id = 7L,
                classId = "sorcerer",
                characterClass = "Sorcerer",
                subclassId = "wild_magic",
                subclass = "Wild Magic",
                constitution = 14,
                level = 1,
                hitPoints = 9,
                hitPointsMax = 9
            )
        )
        val viewModel = CharacterLevelUpViewModel(
            repository = repository,
            levelUpRules = CharacterLevelUpRules(DefaultReadyRulesRepository()),
            characterId = 7L,
            launchAsync = { block -> runBlocking { block() } }
        )

        var applied = false
        viewModel.applyLevelUp { applied = true }

        val saved = repository.getCharacterBlocking(7L)
        assertTrue(applied)
        assertEquals(2, saved?.level)
        assertEquals(15, saved?.hitPoints)
        assertFalse(viewModel.uiState.isApplying)
        assertTrue(viewModel.uiState.completed)
    }

    @Test
    fun applyLevelUpBlocksUntilRequiredSubclassIsSelected() {
        val repository = FakeCharacterRepository(
            baseCharacter(id = 9L, classId = "test_mage", characterClass = "Test Mage", level = 1)
        )
        val viewModel = CharacterLevelUpViewModel(
            repository = repository,
            levelUpRules = CharacterLevelUpRules(RequiredSubclassRulesRepository()),
            characterId = 9L,
            launchAsync = { block -> runBlocking { block() } }
        )

        assertTrue(viewModel.uiState.requiresSubclassSelection)
        assertFalse(viewModel.uiState.canApply)

        viewModel.applyLevelUp {}

        assertEquals(null, viewModel.uiState.actionErrorMessage)
        assertEquals(1, repository.getCharacterBlocking(9L)?.level)

        viewModel.selectSubclass("storm")
        assertTrue(viewModel.uiState.canApply)
        viewModel.applyLevelUp {}

        assertEquals(2, repository.getCharacterBlocking(9L)?.level)
        assertEquals("storm", repository.getCharacterBlocking(9L)?.subclassId)
    }

    @Test
    fun missingCharacterShowsBlockingState() {
        val repository = FakeCharacterRepository()
        val viewModel = CharacterLevelUpViewModel(
            repository = repository,
            levelUpRules = CharacterLevelUpRules(DefaultReadyRulesRepository()),
            characterId = 404L,
            launchAsync = { block -> runBlocking { block() } }
        )

        assertFalse(viewModel.uiState.isLoading)
        assertEquals("Character no longer exists. Reopen it from the list.", viewModel.uiState.blockingMessage)
        assertFalse(viewModel.uiState.canApply)
    }

    @Test
    fun callbackFailureDoesNotRollBackAppliedLevelUp() {
        val repository = FakeCharacterRepository(
            baseCharacter(
                id = 5L,
                classId = "sorcerer",
                characterClass = "Sorcerer",
                subclassId = "wild_magic",
                subclass = "Wild Magic",
                level = 1
            )
        )
        val viewModel = CharacterLevelUpViewModel(
            repository = repository,
            levelUpRules = CharacterLevelUpRules(DefaultReadyRulesRepository()),
            characterId = 5L,
            launchAsync = { block -> runBlocking { block() } }
        )

        val result = runCatching {
            viewModel.applyLevelUp { throw IllegalStateException("navigation failed") }
        }

        assertTrue(result.isSuccess)
        assertEquals(2, repository.getCharacterBlocking(5L)?.level)
        assertEquals("Level up applied, but navigation failed. Try again.", viewModel.uiState.actionErrorMessage)
    }

    @Test
    fun cancellationIsRethrown() {
        val repository = CancellingCharacterRepository(
            baseCharacter(
                id = 1L,
                classId = "sorcerer",
                characterClass = "Sorcerer",
                subclassId = "wild_magic",
                subclass = "Wild Magic",
                level = 1
            )
        )
        val viewModel = CharacterLevelUpViewModel(
            repository = repository,
            levelUpRules = CharacterLevelUpRules(DefaultReadyRulesRepository()),
            characterId = 1L,
            launchAsync = { block -> runBlocking { block() } }
        )

        val result = runCatching { viewModel.applyLevelUp {} }

        assertTrue(result.exceptionOrNull() is CancellationException)
    }

    private fun baseCharacter(
        id: Long,
        classId: String,
        characterClass: String,
        subclassId: String = "",
        subclass: String = "",
        constitution: Int = 12,
        level: Int,
        hitPoints: Int = 8,
        hitPointsMax: Int = hitPoints
    ): CharacterRecord {
        return CharacterRecord(
            id = id,
            ruleset = Ruleset.PHB_2014.name,
            name = "Aylin",
            classId = classId,
            characterClass = characterClass,
            subclassId = subclassId,
            subclass = subclass,
            raceId = "elf",
            race = "Elf",
            subraceId = "high_elf",
            alignment = "Neutral Good",
            backgroundId = "sage",
            background = "Sage",
            level = level,
            abilityMethod = "STANDARD_ARRAY",
            armorClass = 12,
            hitPoints = hitPoints,
            hitPointsMax = hitPointsMax,
            strength = 8,
            dexterity = 14,
            constitution = constitution,
            intelligence = 16,
            wisdom = 12,
            charisma = 10,
            savingThrowProficiencies = listOf("INTELLIGENCE", "WISDOM"),
            skillProficiencies = listOf("arcana", "history"),
            notes = "",
            updatedAt = 1L
        )
    }

    private class FakeCharacterRepository(vararg initialCharacters: CharacterRecord) : CharacterRepository {
        private val characters = MutableStateFlow(initialCharacters.toList())

        override fun observeCharacters(): Flow<List<CharacterRecord>> = characters

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> {
            return characters.map { items -> items.firstOrNull { it.id == id } }
        }

        override suspend fun getCharacter(id: Long): CharacterRecord? {
            return characters.value.firstOrNull { it.id == id }
        }

        override suspend fun saveCharacter(character: CharacterUpsert) {
            val existing = characters.value.firstOrNull { it.id == character.id } ?: error("missing character")
            val updated = existing.copy(
                ruleset = character.ruleset ?: existing.ruleset,
                classId = character.classId ?: existing.classId,
                characterClass = character.characterClass,
                subclassId = character.subclassId ?: existing.subclassId,
                subclass = character.subclass,
                raceId = character.raceId ?: existing.raceId,
                race = character.race,
                subraceId = character.subraceId ?: existing.subraceId,
                alignment = character.alignment,
                backgroundId = character.backgroundId ?: existing.backgroundId,
                background = character.background,
                level = character.level,
                abilityMethod = character.abilityMethod ?: existing.abilityMethod,
                armorClass = character.armorClass,
                hitPoints = character.hitPoints,
                hitPointsMax = character.hitPointsMax ?: existing.hitPointsMax,
                strength = character.strength,
                dexterity = character.dexterity,
                constitution = character.constitution,
                intelligence = character.intelligence,
                wisdom = character.wisdom,
                charisma = character.charisma,
                savingThrowProficiencies = character.savingThrowProficiencies ?: existing.savingThrowProficiencies,
                skillProficiencies = character.skillProficiencies ?: existing.skillProficiencies,
                notes = character.notes
            )
            characters.value = characters.value.map { item -> if (item.id == existing.id) updated else item }
        }

        override suspend fun createCharacter(character: CharacterRecord): Long = error("unused")

        override suspend fun deleteCharacter(id: Long) = Unit

        fun getCharacterBlocking(id: Long): CharacterRecord? = characters.value.firstOrNull { it.id == id }
    }

    private class CancellingCharacterRepository(
        private val character: CharacterRecord
    ) : CharacterRepository {
        override fun observeCharacters(): Flow<List<CharacterRecord>> = MutableStateFlow(emptyList())

        override fun observeCharacter(id: Long): Flow<CharacterRecord?> = MutableStateFlow(null)

        override suspend fun getCharacter(id: Long): CharacterRecord = character

        override suspend fun saveCharacter(character: CharacterUpsert) {
            throw CancellationException("cancelled")
        }

        override suspend fun createCharacter(character: CharacterRecord): Long = 0L

        override suspend fun deleteCharacter(id: Long) = Unit
    }

    private class DefaultReadyRulesRepository : RulesRepository {
        override fun getRuleset(ruleset: Ruleset): RulesContent {
            return RulesContent(
                races = listOf(RaceDefinition("elf", "Elf", emptyMap())),
                classes = listOf(
                    ClassDefinition(
                        id = "sorcerer",
                        name = "Sorcerer",
                        hitDie = 6,
                        primaryAbilities = setOf(AbilityType.CHARISMA),
                        savingThrowProficiencies = setOf(AbilityType.CHARISMA),
                        skillChoiceCount = 2,
                        skillOptions = emptySet(),
                        subclassLevel = 1,
                        subclasses = listOf(SubclassDefinition("wild_magic", "Wild Magic"))
                    )
                ),
                backgrounds = listOf(BackgroundDefinition("sage", "Sage", emptySet())),
                skills = listOf(SkillDefinition("arcana", "Arcana", AbilityType.INTELLIGENCE))
            )
        }
    }

    private class RequiredSubclassRulesRepository : RulesRepository {
        override fun getRuleset(ruleset: Ruleset): RulesContent {
            return RulesContent(
                races = listOf(RaceDefinition("elf", "Elf", emptyMap())),
                classes = listOf(
                    ClassDefinition(
                        id = "test_mage",
                        name = "Test Mage",
                        hitDie = 8,
                        primaryAbilities = setOf(AbilityType.INTELLIGENCE),
                        savingThrowProficiencies = setOf(AbilityType.INTELLIGENCE),
                        skillChoiceCount = 2,
                        skillOptions = emptySet(),
                        subclassLevel = 2,
                        subclasses = listOf(
                            SubclassDefinition("storm", "Storm Path"),
                            SubclassDefinition("void", "Void Path")
                        )
                    )
                ),
                backgrounds = listOf(BackgroundDefinition("sage", "Sage", emptySet())),
                skills = listOf(SkillDefinition("arcana", "Arcana", AbilityType.INTELLIGENCE))
            )
        }
    }
}
