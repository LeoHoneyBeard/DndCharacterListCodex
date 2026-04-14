package com.vinni.dndcharacterlist.core.data.repository

import com.vinni.dndcharacterlist.core.data.local.CharacterEntity
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert

internal fun CharacterEntity.toDomain(): CharacterRecord {
    return CharacterRecord(
        id = id,
        ruleset = ruleset,
        name = name,
        classId = classId,
        characterClass = characterClass,
        subclassId = subclassId,
        subclass = subclass,
        raceId = raceId,
        race = race,
        subraceId = subraceId,
        alignment = alignment,
        backgroundId = backgroundId,
        background = background,
        level = level,
        abilityMethod = abilityMethod,
        armorClass = armorClass,
        hitPoints = hitPoints,
        hitPointsMax = hitPointsMax,
        strength = strength,
        dexterity = dexterity,
        constitution = constitution,
        intelligence = intelligence,
        wisdom = wisdom,
        charisma = charisma,
        savingThrowProficiencies = savingThrowProficienciesSerialized.splitSerializedValues(),
        skillProficiencies = skillProficienciesSerialized.splitSerializedValues(),
        notes = notes,
        updatedAt = updatedAt
    )
}

internal fun CharacterRecord.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = id,
        ruleset = ruleset,
        name = name,
        classId = classId,
        characterClass = characterClass,
        subclassId = subclassId,
        subclass = subclass,
        raceId = raceId,
        race = race,
        subraceId = subraceId,
        alignment = alignment,
        backgroundId = backgroundId,
        background = background,
        level = level,
        abilityMethod = abilityMethod,
        armorClass = armorClass,
        hitPoints = hitPoints,
        hitPointsMax = hitPointsMax,
        strength = strength,
        dexterity = dexterity,
        constitution = constitution,
        intelligence = intelligence,
        wisdom = wisdom,
        charisma = charisma,
        savingThrowProficienciesSerialized = savingThrowProficiencies.joinToString(","),
        skillProficienciesSerialized = skillProficiencies.joinToString(","),
        notes = notes,
        updatedAt = updatedAt
    )
}

internal fun CharacterUpsert.toEntity(timestamp: Long): CharacterEntity {
    return CharacterEntity(
        id = id ?: 0,
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
}

private fun String.splitSerializedValues(): List<String> {
    return split(',')
        .map(String::trim)
        .filter(String::isNotBlank)
}
