package com.vinni.dndcharacterlist.core.rules.creation.mapper

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationDraft
import com.vinni.dndcharacterlist.core.rules.creation.model.DerivedCharacterStats
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent

class CharacterCreationMapper {

    fun toCharacterRecord(
        draft: CharacterCreationDraft,
        derived: DerivedCharacterStats,
        rulesContent: RulesContent
    ): CharacterRecord {
        val race = rulesContent.races.first { it.id == draft.raceId }
        val subrace = draft.subraceId?.let { id -> race.subraces.firstOrNull { it.id == id } }
        val classDefinition = rulesContent.classes.first { it.id == draft.classId }
        val subclass = draft.subclassId?.let { id -> classDefinition.subclasses.firstOrNull { it.id == id } }
        val background = rulesContent.backgrounds.first { it.id == draft.backgroundId }
        val finalAbilities = checkNotNull(derived.finalAbilities)
        val maxHitPoints = checkNotNull(derived.maxHitPoints)

        return CharacterRecord(
            ruleset = draft.ruleset.name,
            name = draft.name.trim(),
            classId = classDefinition.id,
            characterClass = classDefinition.name,
            subclassId = subclass?.id.orEmpty(),
            subclass = subclass?.name.orEmpty(),
            raceId = race.id,
            race = race.name,
            subraceId = subrace?.id.orEmpty(),
            alignment = "",
            backgroundId = background.id,
            background = background.name,
            level = draft.level,
            abilityMethod = draft.abilityMethod?.name.orEmpty(),
            armorClass = 10 + (derived.abilityModifiers?.dexterity ?: 0),
            hitPoints = maxHitPoints,
            hitPointsMax = maxHitPoints,
            strength = finalAbilities.strength,
            dexterity = finalAbilities.dexterity,
            constitution = finalAbilities.constitution,
            intelligence = finalAbilities.intelligence,
            wisdom = finalAbilities.wisdom,
            charisma = finalAbilities.charisma,
            savingThrowProficiencies = derived.savingThrowProficiencies.map { it.name },
            skillProficiencies = derived.skillProficiencies.sorted(),
            notes = "",
            updatedAt = System.currentTimeMillis()
        )
    }
}


