package com.vinni.dndcharacterlist.core.rules.levelup

import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert

sealed interface LevelUpResult {
    data class Ready(
        val preview: LevelUpPreview,
        val character: CharacterUpsert
    ) : LevelUpResult

    data class Blocked(
        val preview: LevelUpPreview,
        val reason: String
    ) : LevelUpResult
}
