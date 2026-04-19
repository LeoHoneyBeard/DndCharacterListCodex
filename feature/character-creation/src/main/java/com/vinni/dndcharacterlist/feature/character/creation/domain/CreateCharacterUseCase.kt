package com.vinni.dndcharacterlist.feature.character.creation.domain

import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.creation.mapper.CharacterCreationMapper
import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationDraft
import com.vinni.dndcharacterlist.core.rules.creation.model.DerivedCharacterStats
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent

class CreateCharacterUseCase(
    private val repository: CharacterRepository,
    private val mapper: CharacterCreationMapper
) {
    suspend operator fun invoke(
        draft: CharacterCreationDraft,
        derived: DerivedCharacterStats,
        rulesContent: RulesContent
    ): Long {
        return repository.createCharacter(
            mapper.toCharacterRecord(
                draft = draft,
                derived = derived,
                rulesContent = rulesContent
            )
        )
    }
}
