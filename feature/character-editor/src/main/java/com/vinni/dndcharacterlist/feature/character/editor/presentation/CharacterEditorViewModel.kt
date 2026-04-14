package com.vinni.dndcharacterlist.feature.character.editor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vinni.dndcharacterlist.core.domain.model.CharacterUpsert
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class CharacterEditorUiState(
    val characterId: Long? = null,
    val name: String = "",
    val characterClass: String = "",
    val subclass: String = "",
    val race: String = "",
    val alignment: String = "",
    val background: String = "",
    val level: String = "1",
    val armorClass: String = "10",
    val hitPoints: String = "0",
    val strength: String = "10",
    val dexterity: String = "10",
    val constitution: String = "10",
    val intelligence: String = "10",
    val wisdom: String = "10",
    val charisma: String = "10",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val validationMessage: String? = null,
    val saveErrorMessage: String? = null,
    val completedAction: EditorCompletedAction? = null
) {
    val nameError: String?
        get() = if (name.isBlank()) "Name is required." else null

    val levelError: String?
        get() = if (level.toIntOrNull()?.let { it in 1..20 } != true) {
            "Level must be between 1 and 20."
        } else {
            null
        }

    val armorClassError: String?
        get() = if (armorClass.toIntOrNull()?.let { it >= 0 } != true) {
            "Armor Class must be 0 or higher."
        } else {
            null
        }

    val hitPointsError: String?
        get() = if (hitPoints.toIntOrNull()?.let { it >= 0 } != true) {
            "Hit Points must be 0 or higher."
        } else {
            null
        }

    val abilityScoreError: String?
        get() {
            val stats = listOf(strength, dexterity, constitution, intelligence, wisdom, charisma)
            return if (stats.any { it.toIntOrNull()?.let { score -> score in 1..30 } != true }) {
                "Ability scores must be between 1 and 30."
            } else {
                null
            }
        }

    val canDelete: Boolean
        get() = characterId != null

    val isSaveEnabled: Boolean
        get() = !isSaving && validate() == null

    val proficiencyBonus: Int
        get() = ((level.toIntOrNull() ?: 1).coerceIn(1, 20) - 1) / 4 + 2

    fun abilityModifier(score: String): Int? {
        return score.toIntOrNull()?.let { parsedScore ->
            Math.floorDiv(parsedScore - 10, 2)
        }
    }

    fun validate(): String? {
        return nameError
            ?: levelError
            ?: armorClassError
            ?: hitPointsError
            ?: abilityScoreError
    }
}

enum class EditorCompletedAction {
    SAVED,
    DELETED
}

class CharacterEditorViewModel(
    private val repository: CharacterRepository,
    characterId: Long?,
    private val launchAsync: ((block: suspend () -> Unit) -> Unit)? = null
) : ViewModel() {

    var uiState by mutableStateOf(CharacterEditorUiState(isLoading = characterId != null))
        private set

    init {
        if (characterId != null) {
            launchBlock {
                val character = repository.getCharacter(characterId)
                uiState = if (character == null) {
                    CharacterEditorUiState(characterId = characterId)
                } else {
                    CharacterEditorUiState(
                        characterId = character.id,
                        name = character.name,
                        characterClass = character.characterClass,
                        subclass = character.subclass,
                        race = character.race,
                        alignment = character.alignment,
                        background = character.background,
                        level = character.level.toString(),
                        armorClass = character.armorClass.toString(),
                        hitPoints = character.hitPoints.toString(),
                        strength = character.strength.toString(),
                        dexterity = character.dexterity.toString(),
                        constitution = character.constitution.toString(),
                        intelligence = character.intelligence.toString(),
                        wisdom = character.wisdom.toString(),
                        charisma = character.charisma.toString(),
                        notes = character.notes
                    )
                }
            }
        }
    }

    fun update(update: CharacterEditorUiState.() -> CharacterEditorUiState) {
        uiState = uiState.update().clearMessages()
    }

    fun save(onSaved: () -> Unit) {
        if (uiState.completedAction == EditorCompletedAction.SAVED) {
            try {
                onSaved()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                uiState = uiState.copy(saveErrorMessage = "Character saved, but navigation failed. Try again.")
            }
            return
        }
        if (uiState.isSaving) return

        val validationMessage = uiState.validate()
        if (validationMessage != null) {
            uiState = uiState.copy(validationMessage = validationMessage, saveErrorMessage = null)
            return
        }

        uiState = uiState.copy(isSaving = true, saveErrorMessage = null)
        launchBlock {
            try {
                repository.saveCharacter(
                    CharacterUpsert(
                        id = uiState.characterId,
                        name = uiState.name.trim(),
                        characterClass = uiState.characterClass.trim(),
                        subclass = uiState.subclass.trim(),
                        race = uiState.race.trim(),
                        alignment = uiState.alignment.trim(),
                        background = uiState.background.trim(),
                        level = uiState.level.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        armorClass = uiState.armorClass.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                        hitPoints = uiState.hitPoints.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                        strength = uiState.strength.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                        dexterity = uiState.dexterity.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                        constitution = uiState.constitution.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                        intelligence = uiState.intelligence.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                        wisdom = uiState.wisdom.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                        charisma = uiState.charisma.toIntOrNull()?.coerceIn(1, 30) ?: 10,
                        notes = uiState.notes.trim()
                    )
                )
                uiState = uiState.copy(completedAction = EditorCompletedAction.SAVED)
                try {
                    onSaved()
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    uiState = uiState.copy(saveErrorMessage = "Character saved, but navigation failed. Try again.")
                }
            } catch (_: IllegalArgumentException) {
                uiState = uiState.copy(saveErrorMessage = "Character no longer exists. Reopen it from the list.")
            } finally {
                uiState = uiState.copy(isSaving = false)
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = uiState.characterId ?: return
        if (uiState.completedAction == EditorCompletedAction.DELETED) {
            try {
                onDeleted()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                uiState = uiState.copy(saveErrorMessage = "Character deleted, but navigation failed. Try again.")
            }
            return
        }
        launchBlock {
            repository.deleteCharacter(id)
            uiState = uiState.copy(completedAction = EditorCompletedAction.DELETED)
            try {
                onDeleted()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                uiState = uiState.copy(saveErrorMessage = "Character deleted, but navigation failed. Try again.")
            }
        }
    }

    private fun launchBlock(block: suspend () -> Unit) {
        launchAsync?.invoke(block) ?: viewModelScope.launch { block() }
    }
}

private fun CharacterEditorUiState.clearMessages(): CharacterEditorUiState {
    return copy(
        validationMessage = null,
        saveErrorMessage = null,
        completedAction = null
    )
}

