package com.vinni.dndcharacterlist.data

import kotlinx.coroutines.flow.Flow

class CharacterRepository(
    private val characterDao: CharacterDao
) {
    fun observeCharacters(): Flow<List<CharacterEntity>> = characterDao.observeAll()

    fun observeCharacter(id: Long): Flow<CharacterEntity?> = characterDao.observeById(id)

    suspend fun getCharacter(id: Long): CharacterEntity? = characterDao.getById(id)

    suspend fun saveCharacter(
        id: Long?,
        name: String,
        characterClass: String,
        subclass: String,
        race: String,
        alignment: String,
        background: String,
        level: Int,
        armorClass: Int,
        hitPoints: Int,
        strength: Int,
        dexterity: Int,
        constitution: Int,
        intelligence: Int,
        wisdom: Int,
        charisma: Int,
        notes: String
    ) {
        val timestamp = System.currentTimeMillis()
        if (id == null) {
            characterDao.insert(
                CharacterEntity(
                    name = name,
                    characterClass = characterClass,
                    subclass = subclass,
                    race = race,
                    alignment = alignment,
                    background = background,
                    level = level,
                    abilityMethod = "",
                    armorClass = armorClass,
                    hitPoints = hitPoints,
                    hitPointsMax = hitPoints,
                    strength = strength,
                    dexterity = dexterity,
                    constitution = constitution,
                    intelligence = intelligence,
                    wisdom = wisdom,
                    charisma = charisma,
                    savingThrowProficienciesSerialized = "",
                    skillProficienciesSerialized = "",
                    notes = notes,
                    updatedAt = timestamp
                )
            )
        } else {
            characterDao.update(
                CharacterEntity(
                    id = id,
                    name = name,
                    characterClass = characterClass,
                    subclass = subclass,
                    race = race,
                    alignment = alignment,
                    background = background,
                    level = level,
                    abilityMethod = "",
                    armorClass = armorClass,
                    hitPoints = hitPoints,
                    hitPointsMax = hitPoints,
                    strength = strength,
                    dexterity = dexterity,
                    constitution = constitution,
                    intelligence = intelligence,
                    wisdom = wisdom,
                    charisma = charisma,
                    savingThrowProficienciesSerialized = "",
                    skillProficienciesSerialized = "",
                    notes = notes,
                    updatedAt = timestamp
                )
            )
        }
    }

    suspend fun createCharacter(character: CharacterEntity): Long {
        return characterDao.insert(character)
    }

    suspend fun deleteCharacter(id: Long) {
        characterDao.deleteById(id)
    }
}
