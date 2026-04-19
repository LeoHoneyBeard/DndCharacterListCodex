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
import com.vinni.dndcharacterlist.core.rules.levelup.CharacterLevelUpRules
import com.vinni.dndcharacterlist.feature.character.levelup.domain.ApplyLevelUpResult
import com.vinni.dndcharacterlist.feature.character.levelup.domain.ApplyLevelUpUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyLevelUpUseCaseTest {

    @Test
    fun appliesReadyLevelUpAndPersistsUpdatedCharacter() = runBlocking {
        val repository = FakeCharacterRepository(
            baseCharacter(
                id = 7L,
                classId = "barbarian",
                characterClass = "Barbarian",
                constitution = 14,
                level = 1,
                hitPoints = 14,
                hitPointsMax = 14
            )
        )
        val useCase = ApplyLevelUpUseCase(repository, CharacterLevelUpRules(DefaultReadyRulesRepository()))

        val result = useCase(7L, selectedSubclassId = null)

        assertTrue(result is ApplyLevelUpResult.Applied)
        val saved = repository.getCharacterBlocking(7L)
        assertEquals(2, saved?.level)
        assertEquals(23, saved?.hitPoints)
        assertEquals(23, saved?.hitPointsMax)
    }

    @Test
    fun returnsMissingCharacterWhenRepositoryLookupFails() = runBlocking {
        val useCase = ApplyLevelUpUseCase(FakeCharacterRepository(), CharacterLevelUpRules(DefaultReadyRulesRepository()))

        val result = useCase(404L, selectedSubclassId = null)

        assertTrue(result is ApplyLevelUpResult.MissingCharacter)
    }

    private fun baseCharacter(
        id: Long,
        classId: String,
        characterClass: String,
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
            subclassId = "",
            subclass = "",
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
                level = character.level,
                hitPoints = character.hitPoints,
                hitPointsMax = character.hitPointsMax ?: existing.hitPointsMax,
                subclassId = character.subclassId ?: existing.subclassId,
                subclass = character.subclass
            )
            characters.value = characters.value.map { item -> if (item.id == existing.id) updated else item }
        }

        override suspend fun createCharacter(character: CharacterRecord): Long = error("unused")

        override suspend fun deleteCharacter(id: Long) = Unit

        fun getCharacterBlocking(id: Long): CharacterRecord? = characters.value.firstOrNull { it.id == id }
    }

    private class DefaultReadyRulesRepository : RulesRepository {
        override fun getRuleset(ruleset: Ruleset): RulesContent {
            return RulesContent(
                races = listOf(RaceDefinition("elf", "Elf", emptyMap())),
                classes = listOf(
                    ClassDefinition(
                        id = "barbarian",
                        name = "Barbarian",
                        hitDie = 12,
                        primaryAbilities = setOf(AbilityType.STRENGTH),
                        savingThrowProficiencies = setOf(AbilityType.STRENGTH),
                        skillChoiceCount = 2,
                        skillOptions = emptySet(),
                        subclassLevel = 3
                    )
                ),
                backgrounds = listOf(BackgroundDefinition("sage", "Sage", emptySet())),
                skills = listOf(SkillDefinition("arcana", "Arcana", AbilityType.INTELLIGENCE))
            )
        }
    }
}
