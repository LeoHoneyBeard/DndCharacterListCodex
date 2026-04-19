package com.vinni.dndcharacterlist.feature.character.editor.domain

import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.editor.CharacterEditorDraft
import com.vinni.dndcharacterlist.core.rules.editor.CharacterEditorRules

class UpdateCharacterUseCase(
    private val repository: CharacterRepository,
    private val editorRules: CharacterEditorRules
) {
    suspend operator fun invoke(
        characterId: Long?,
        draft: CharacterEditorDraft
    ) {
        val resolved = requireNotNull(editorRules.resolveSelections(draft)) {
            "Character editor draft must be valid before update."
        }
        repository.saveCharacter(
            CharacterUpsert(
                id = characterId,
                name = draft.name.trim(),
                ruleset = resolved.ruleset.name,
                classId = resolved.classDefinition.id,
                characterClass = resolved.classDefinition.name,
                subclassId = resolved.subclass?.id ?: "",
                subclass = resolved.subclass?.name.orEmpty(),
                raceId = resolved.race.id,
                race = resolved.race.name,
                subraceId = draft.subraceId,
                alignment = draft.alignment.trim(),
                backgroundId = resolved.background.id,
                background = resolved.background.name,
                level = draft.level.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                armorClass = draft.armorClass.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                hitPoints = draft.hitPoints.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                strength = draft.strength.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                dexterity = draft.dexterity.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                constitution = draft.constitution.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                intelligence = draft.intelligence.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                wisdom = draft.wisdom.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                charisma = draft.charisma.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                notes = draft.notes.trim()
            )
        )
    }
}
