package com.vinni.dndcharacterlist.feature.character.editor.domain

import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository

class DeleteCharacterUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(characterId: Long) {
        repository.deleteCharacter(characterId)
    }
}
