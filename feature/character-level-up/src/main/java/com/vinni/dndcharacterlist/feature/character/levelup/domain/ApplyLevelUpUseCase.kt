package com.vinni.dndcharacterlist.feature.character.levelup.domain

import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.levelup.CharacterLevelUpRules
import com.vinni.dndcharacterlist.core.rules.levelup.LevelUpResult

sealed interface ApplyLevelUpResult {
    data object MissingCharacter : ApplyLevelUpResult
    data class Blocked(
        val previewBlockingReason: String?,
        val reason: String
    ) : ApplyLevelUpResult

    data class Applied(
        val previewBlockingReason: String?,
        val character: CharacterUpsert
    ) : ApplyLevelUpResult
}

class ApplyLevelUpUseCase(
    private val repository: CharacterRepository,
    private val levelUpRules: CharacterLevelUpRules
) {
    suspend operator fun invoke(
        characterId: Long,
        selectedSubclassId: String?
    ): ApplyLevelUpResult {
        val character = repository.getCharacter(characterId) ?: return ApplyLevelUpResult.MissingCharacter
        return when (val result = levelUpRules.prepareLevelUp(character, selectedSubclassId)) {
            is LevelUpResult.Blocked -> {
                ApplyLevelUpResult.Blocked(
                    previewBlockingReason = result.preview.blockingReason,
                    reason = result.reason
                )
            }

            is LevelUpResult.Ready -> {
                repository.saveCharacter(result.character)
                ApplyLevelUpResult.Applied(
                    previewBlockingReason = result.preview.blockingReason,
                    character = result.character
                )
            }
        }
    }
}
