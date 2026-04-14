package com.vinni.dndcharacterlist.core.data.repository

import com.vinni.dndcharacterlist.core.data.local.CharacterDao
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCharacterRepository(
    private val characterDao: CharacterDao
) : CharacterRepository {
    override fun observeCharacters(): Flow<List<CharacterRecord>> {
        return characterDao.observeAll().map { characters -> characters.map { it.toDomain() } }
    }

    override fun observeCharacter(id: Long): Flow<CharacterRecord?> {
        return characterDao.observeById(id).map { it?.toDomain() }
    }

    override suspend fun getCharacter(id: Long): CharacterRecord? {
        return characterDao.getById(id)?.toDomain()
    }

    override suspend fun saveCharacter(character: CharacterUpsert) {
        val characterId = character.id
        if (characterId == null) {
            characterDao.insert(character.toEntity(timestamp = System.currentTimeMillis()))
        } else {
            val existing = characterDao.getById(characterId)
            val entity = requireNotNull(existing) {
                "Character with id=$characterId no longer exists."
            }.let { current ->
                character.mergeInto(current, timestamp = System.currentTimeMillis())
            }
            val updatedRows = characterDao.update(entity)
            require(updatedRows == 1) {
                "Character with id=$characterId no longer exists."
            }
        }
    }

    override suspend fun createCharacter(character: CharacterRecord): Long {
        return characterDao.insert(character.toEntity())
    }

    override suspend fun updateCharacter(character: CharacterRecord) {
        val updatedRows = characterDao.update(character.copy(updatedAt = System.currentTimeMillis()).toEntity())
        require(updatedRows == 1) {
            "Character with id=${character.id} no longer exists."
        }
    }

    override suspend fun deleteCharacter(id: Long) {
        characterDao.deleteById(id)
    }
}
