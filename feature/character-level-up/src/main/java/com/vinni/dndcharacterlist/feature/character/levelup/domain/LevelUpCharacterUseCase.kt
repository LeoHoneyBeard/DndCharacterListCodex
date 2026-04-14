package com.vinni.dndcharacterlist.feature.character.levelup.domain

import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository

class LevelUpCharacterUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(character: CharacterRecord, hitPointGain: Int): CharacterRecord {
        require(hitPointGain >= 1) { "Hit point gain must be at least 1." }
        require(character.level in 1..19) { "Only characters below level 20 can level up." }

        val updatedCharacter = character.copy(
            level = character.level + 1,
            hitPoints = character.hitPoints + hitPointGain,
            hitPointsMax = character.hitPointsMax + hitPointGain
        )
        repository.updateCharacter(updatedCharacter)
        return updatedCharacter
    }
}
