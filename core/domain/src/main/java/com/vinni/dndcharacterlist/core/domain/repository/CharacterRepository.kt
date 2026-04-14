package com.vinni.dndcharacterlist.core.domain.repository

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun observeCharacters(): Flow<List<CharacterRecord>>

    fun observeCharacter(id: Long): Flow<CharacterRecord?>

    suspend fun getCharacter(id: Long): CharacterRecord?

    suspend fun saveCharacter(character: CharacterUpsert)

    suspend fun createCharacter(character: CharacterRecord): Long

    suspend fun updateCharacter(character: CharacterRecord)

    suspend fun deleteCharacter(id: Long)
}
