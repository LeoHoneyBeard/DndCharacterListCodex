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
        ruleset = ruleset.orEmpty(),
        name = name,
        classId = classId.orEmpty(),
        characterClass = characterClass,
        subclassId = subclassId.orEmpty(),
        subclass = subclass,
        raceId = raceId.orEmpty(),
        race = race,
        subraceId = subraceId.orEmpty(),
        alignment = alignment,
        backgroundId = backgroundId.orEmpty(),
        background = background,
        level = level,
        abilityMethod = abilityMethod.orEmpty(),
        armorClass = armorClass,
        hitPoints = hitPoints,
        hitPointsMax = hitPointsMax ?: hitPoints,
        strength = strength,
        dexterity = dexterity,
        constitution = constitution,
        intelligence = intelligence,
        wisdom = wisdom,
        charisma = charisma,
        savingThrowProficienciesSerialized = savingThrowProficiencies.orEmpty().joinToString(","),
        skillProficienciesSerialized = skillProficiencies.orEmpty().joinToString(","),
        notes = notes,
        updatedAt = timestamp
    )
}

internal fun CharacterUpsert.mergeInto(existing: CharacterEntity, timestamp: Long): CharacterEntity {
    return existing.copy(
        ruleset = ruleset ?: existing.ruleset,
        name = name,
        classId = classId ?: existing.classId,
        characterClass = characterClass,
        subclassId = subclassId ?: existing.subclassId,
        subclass = subclass,
        raceId = raceId ?: existing.raceId,
        race = race,
        subraceId = subraceId ?: existing.subraceId,
        alignment = alignment,
        backgroundId = backgroundId ?: existing.backgroundId,
        background = background,
        level = level,
        abilityMethod = abilityMethod ?: existing.abilityMethod,
        armorClass = armorClass,
        hitPoints = hitPoints,
        hitPointsMax = hitPointsMax ?: existing.hitPointsMax,
        strength = strength,
        dexterity = dexterity,
        constitution = constitution,
        intelligence = intelligence,
        wisdom = wisdom,
        charisma = charisma,
        savingThrowProficienciesSerialized = savingThrowProficiencies?.joinToString(",")
            ?: existing.savingThrowProficienciesSerialized,
        skillProficienciesSerialized = skillProficiencies?.joinToString(",")
            ?: existing.skillProficienciesSerialized,
        notes = notes,
        updatedAt = timestamp
    )
}

private fun String.splitSerializedValues(): List<String> {
    return split(',')
        .map(String::trim)
        .filter(String::isNotBlank)
}
