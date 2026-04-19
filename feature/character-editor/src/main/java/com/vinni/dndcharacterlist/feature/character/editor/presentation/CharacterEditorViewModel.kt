package com.vinni.dndcharacterlist.feature.character.editor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.creation.model.ValidationIssue
import com.vinni.dndcharacterlist.core.rules.editor.CharacterEditorDraft
import com.vinni.dndcharacterlist.core.rules.editor.CharacterEditorRules
import com.vinni.dndcharacterlist.feature.character.editor.domain.DeleteCharacterUseCase
import com.vinni.dndcharacterlist.feature.character.editor.domain.UpdateCharacterUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class CharacterEditorUiState(
    val characterId: Long? = null,
    val ruleset: String = "PHB_2014",
    val name: String = "",
    val characterClass: String = "",
    val subclass: String = "",
    val race: String = "",
    val subraceId: String = "",
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
    val classPresets: List<String> = emptyList(),
    val subclassPresets: List<String> = emptyList(),
    val racePresets: List<String> = emptyList(),
    val backgroundPresets: List<String> = emptyList(),
    val validationIssues: List<ValidationIssue> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isDeleteConfirmationVisible: Boolean = false,
    val isDiscardConfirmationVisible: Boolean = false,
    val validationMessage: String? = null,
    val saveErrorMessage: String? = null,
    val completedAction: EditorCompletedAction? = null
) {
    val nameError: String?
        get() = validationIssue("name_required")

    val levelError: String?
        get() = validationIssue("level_invalid")

    val armorClassError: String?
        get() = validationIssue("armor_class_invalid")

    val hitPointsError: String?
        get() = validationIssue("hit_points_invalid")

    val abilityScoreError: String?
        get() = validationIssue("ability_scores_invalid")

    val classError: String?
        get() = validationIssue("class_required")

    val subclassError: String?
        get() = validationIssue("subclass_required")
            ?: validationIssue("subclass_invalid")
            ?: validationIssue("subclass_not_available")
            ?: validationIssue("subclass_unsupported")

    val raceError: String?
        get() = validationIssue("race_required") ?: validationIssue("subrace_required")

    val backgroundError: String?
        get() = validationIssue("background_required")

    val canDelete: Boolean
        get() = characterId != null

    val isSaveEnabled: Boolean
        get() = !isSaving && validationIssues.isEmpty()

    val proficiencyBonus: Int
        get() = ((level.toIntOrNull() ?: 1).coerceIn(1, 20) - 1) / 4 + 2

    fun abilityModifier(score: String): Int? {
        return score.toIntOrNull()?.let { parsedScore ->
            Math.floorDiv(parsedScore - 10, 2)
        }
    }

    fun toDraft(): CharacterEditorDraft {
        return CharacterEditorDraft(
            ruleset = ruleset,
            name = name,
            characterClass = characterClass,
            subclass = subclass,
            race = race,
            subraceId = subraceId,
            alignment = alignment,
            background = background,
            level = level,
            armorClass = armorClass,
            hitPoints = hitPoints,
            strength = strength,
            dexterity = dexterity,
            constitution = constitution,
            intelligence = intelligence,
            wisdom = wisdom,
            charisma = charisma,
            notes = notes
        )
    }

    private fun validationIssue(key: String): String? {
        return validationIssues.firstOrNull { it.key == key }?.message
    }
}

enum class EditorCompletedAction {
    SAVED,
    DELETED
}

class CharacterEditorViewModel(
    private val repository: CharacterRepository,
    private val editorRules: CharacterEditorRules,
    private val updateCharacter: UpdateCharacterUseCase,
    private val deleteCharacter: DeleteCharacterUseCase,
    characterId: Long?,
    private val launchAsync: ((block: suspend () -> Unit) -> Unit)? = null
) : ViewModel() {
    private var persistedDraft = CharacterEditorUiState().toDraft()

    var uiState by mutableStateOf(
        CharacterEditorUiState(
            isLoading = characterId != null
        ).withRules(editorRules).withDirtyState()
    )
        private set

    init {
        if (characterId != null) {
            launchBlock {
                val character = repository.getCharacter(characterId)
                uiState = if (character == null) {
                    persistedDraft = CharacterEditorUiState(characterId = characterId).toDraft()
                    CharacterEditorUiState(characterId = characterId).withRules(editorRules).withDirtyState()
                } else {
                    val loadedState = CharacterEditorUiState(
                        characterId = character.id,
                        ruleset = character.ruleset.ifBlank { "PHB_2014" },
                        name = character.name,
                        characterClass = character.characterClass,
                        subclass = character.subclass,
                        race = character.race,
                        subraceId = character.subraceId,
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
                    ).withRules(editorRules)
                    persistedDraft = loadedState.toDraft()
                    loadedState.withDirtyState()
                }
            }
        }
    }

    fun update(update: CharacterEditorUiState.() -> CharacterEditorUiState) {
        uiState = uiState
            .update()
            .clearMessages()
            .withRules(editorRules)
            .withDirtyState()
    }

    fun requestExit(onExit: () -> Unit) {
        when {
            uiState.isDeleteConfirmationVisible -> {
                uiState = uiState.copy(isDeleteConfirmationVisible = false)
            }

            uiState.isDiscardConfirmationVisible -> {
                uiState = uiState.copy(isDiscardConfirmationVisible = false)
            }

            uiState.isSaving -> Unit
            uiState.hasUnsavedChanges -> {
                uiState = uiState.copy(
                    isDiscardConfirmationVisible = true,
                    validationMessage = null,
                    saveErrorMessage = null
                )
            }

            else -> onExit()
        }
    }

    fun dismissExitConfirmation() {
        if (!uiState.isDiscardConfirmationVisible) return
        uiState = uiState.copy(isDiscardConfirmationVisible = false)
    }

    fun confirmExit(onExit: () -> Unit) {
        uiState = uiState.copy(isDiscardConfirmationVisible = false)
        onExit()
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

        val validationMessage = uiState.validationIssues.firstOrNull()?.message
        if (validationMessage != null) {
            uiState = uiState.copy(validationMessage = validationMessage, saveErrorMessage = null)
            return
        }

        uiState = uiState.copy(isSaving = true, saveErrorMessage = null)
        launchBlock {
            try {
                val draft = uiState.toDraft()
                val resolved = editorRules.resolveSelections(draft)
                if (resolved == null) {
                    uiState = uiState.copy(
                        isSaving = false,
                        validationMessage = uiState.validationIssues.firstOrNull()?.message
                    )
                    return@launchBlock
                }
                updateCharacter(uiState.characterId, draft)
                persistedDraft = draft
                uiState = uiState.copy(
                    completedAction = EditorCompletedAction.SAVED,
                    isDiscardConfirmationVisible = false
                ).withDirtyState()
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

    fun requestDeleteConfirmation() {
        if (!uiState.canDelete || uiState.isSaving) return
        uiState = uiState.copy(
            isDeleteConfirmationVisible = true,
            isDiscardConfirmationVisible = false,
            validationMessage = null,
            saveErrorMessage = null
        )
    }

    fun dismissDeleteConfirmation() {
        if (!uiState.isDeleteConfirmationVisible) return
        uiState = uiState.copy(isDeleteConfirmationVisible = false)
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        if (!uiState.canDelete) return
        uiState = uiState.copy(isDeleteConfirmationVisible = false)
        delete(onDeleted)
    }

    private fun delete(onDeleted: () -> Unit) {
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
            try {
                deleteCharacter(id)
                uiState = uiState.copy(completedAction = EditorCompletedAction.DELETED)
                try {
                    onDeleted()
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    uiState = uiState.copy(saveErrorMessage = "Character deleted, but navigation failed. Try again.")
                }
            } catch (_: IllegalArgumentException) {
                uiState = uiState.copy(
                    isDeleteConfirmationVisible = false,
                    saveErrorMessage = "Character no longer exists. Reopen it from the list."
                )
            }
        }
    }

    private fun launchBlock(block: suspend () -> Unit) {
        launchAsync?.invoke(block) ?: viewModelScope.launch { block() }
    }

    private fun CharacterEditorUiState.withDirtyState(): CharacterEditorUiState {
        return copy(
            hasUnsavedChanges = completedAction == null && toDraft() != persistedDraft
        )
    }
}

private fun CharacterEditorUiState.clearMessages(): CharacterEditorUiState {
    return copy(
        isDeleteConfirmationVisible = false,
        isDiscardConfirmationVisible = false,
        validationMessage = null,
        saveErrorMessage = null,
        completedAction = null
    )
}

private fun CharacterEditorUiState.withRules(
    editorRules: CharacterEditorRules
): CharacterEditorUiState {
    val rulesContent = editorRules.rulesContentFor(ruleset)
    val selectedClass = rulesContent.classes.firstOrNull { candidate ->
        candidate.name.equals(characterClass.trim(), ignoreCase = true) ||
            candidate.id.equals(characterClass.trim(), ignoreCase = true)
    }
    return copy(
        classPresets = rulesContent.classes.map { it.name },
        subclassPresets = selectedClass?.subclasses?.map { it.name }.orEmpty(),
        racePresets = rulesContent.races.map { it.name },
        backgroundPresets = rulesContent.backgrounds.map { it.name },
        validationIssues = editorRules.validate(toDraft())
    )
}

