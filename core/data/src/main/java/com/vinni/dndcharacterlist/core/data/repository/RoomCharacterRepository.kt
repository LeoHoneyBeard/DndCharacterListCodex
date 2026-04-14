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
        val entity = character.toEntity(timestamp = System.currentTimeMillis())
        if (character.id == null) {
            characterDao.insert(entity)
        } else {
            characterDao.update(entity)
        }
    }

    override suspend fun createCharacter(character: CharacterRecord): Long {
        return characterDao.insert(character.toEntity())
    }

    override suspend fun deleteCharacter(id: Long) {
        characterDao.deleteById(id)
    }
}
