package com.vinni.dndcharacterlist.feature.character.levelup.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.levelup.CharacterLevelUpRules
import com.vinni.dndcharacterlist.core.rules.levelup.LevelUpRequirement
import com.vinni.dndcharacterlist.core.rules.levelup.LevelUpResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class CharacterLevelUpUiState(
    val characterId: Long? = null,
    val characterName: String = "",
    val className: String = "",
    val currentSubclass: String = "",
    val currentLevel: Int = 0,
    val nextLevel: Int = 0,
    val currentHitPoints: Int = 0,
    val currentHitPointsMax: Int = 0,
    val hitPointIncrease: Int = 0,
    val nextHitPoints: Int = 0,
    val nextHitPointsMax: Int = 0,
    val subclassRequirement: SubclassRequirementUiModel? = null,
    val unsupportedRequirements: List<UnsupportedRequirementUiModel> = emptyList(),
    val selectedSubclassId: String? = null,
    val isLoading: Boolean = true,
    val isApplying: Boolean = false,
    val blockingMessage: String? = null,
    val actionErrorMessage: String? = null,
    val completed: Boolean = false
) {
    private val hasRequiredSubclassSelection: Boolean
        get() = subclassRequirement == null || !selectedSubclassId.isNullOrBlank()

    val canApply: Boolean
        get() = !isLoading &&
            !isApplying &&
            blockingMessage == null &&
            unsupportedRequirements.isEmpty() &&
            hasRequiredSubclassSelection &&
            characterId != null
}

data class SubclassRequirementUiModel(
    val title: String,
    val description: String,
    val options: List<SubclassOptionUiModel>
)

data class SubclassOptionUiModel(
    val id: String,
    val name: String
)

data class UnsupportedRequirementUiModel(
    val title: String,
    val description: String
)

class CharacterLevelUpViewModel(
    private val repository: CharacterRepository,
    private val levelUpRules: CharacterLevelUpRules,
    characterId: Long,
    private val launchAsync: ((block: suspend () -> Unit) -> Unit)? = null
) : ViewModel() {

    var uiState by mutableStateOf(CharacterLevelUpUiState())
        private set

    init {
        launchBlock {
            val character = repository.getCharacter(characterId)
            if (character == null) {
                uiState = CharacterLevelUpUiState(
                    characterId = characterId,
                    isLoading = false,
                    blockingMessage = "Character no longer exists. Reopen it from the list."
                )
                return@launchBlock
            }

            val preview = levelUpRules.preview(character)
            val selectedSubclassId = when {
                character.subclassId.isNotBlank() -> character.subclassId
                preview.subclassRequirement()?.options?.size == 1 ->
                    preview.subclassRequirement()?.options?.single()?.id
                else -> null
            }

            uiState = CharacterLevelUpUiState(
                characterId = character.id,
                characterName = character.name,
                className = character.characterClass,
                currentSubclass = character.subclass,
                currentLevel = preview.currentLevel,
                nextLevel = preview.nextLevel,
                currentHitPoints = preview.currentHitPoints,
                currentHitPointsMax = preview.currentHitPointsMax,
                hitPointIncrease = preview.hitPointIncrease,
                nextHitPoints = preview.nextHitPoints,
                nextHitPointsMax = preview.nextHitPointsMax,
                subclassRequirement = preview.subclassRequirement()?.toUiModel(),
                unsupportedRequirements = preview.unsupportedRequirements().map { requirement ->
                    UnsupportedRequirementUiModel(
                        title = requirement.title,
                        description = requirement.description
                    )
                },
                selectedSubclassId = selectedSubclassId,
                isLoading = false,
                blockingMessage = preview.blockingReason
            )
        }
    }

    fun selectSubclass(subclassId: String) {
        uiState = uiState.copy(
            selectedSubclassId = subclassId,
            actionErrorMessage = null,
            completed = false
        )
    }

    fun applyLevelUp(onApplied: () -> Unit) {
        if (uiState.completed) {
            navigateAfterApply(onApplied)
            return
        }
        if (!uiState.canApply) return

        val characterId = uiState.characterId ?: return
        uiState = uiState.copy(isApplying = true, actionErrorMessage = null)
        launchBlock {
            try {
                val character = repository.getCharacter(characterId)
                if (character == null) {
                    uiState = uiState.copy(
                        isApplying = false,
                        actionErrorMessage = "Character no longer exists. Reopen it from the list."
                    )
                    return@launchBlock
                }

                when (val result = levelUpRules.prepareLevelUp(character, uiState.selectedSubclassId)) {
                    is LevelUpResult.Blocked -> {
                        uiState = uiState.copy(
                            isApplying = false,
                            blockingMessage = result.preview.blockingReason,
                            actionErrorMessage = result.reason
                        )
                    }

                    is LevelUpResult.Ready -> {
                        repository.saveCharacter(result.character)
                        uiState = uiState.copy(
                            isApplying = false,
                            blockingMessage = result.preview.blockingReason,
                            actionErrorMessage = null,
                            completed = true
                        )
                        navigateAfterApply(onApplied)
                    }
                }
            } catch (error: CancellationException) {
                uiState = uiState.copy(isApplying = false)
                throw error
            } catch (_: IllegalArgumentException) {
                uiState = uiState.copy(
                    isApplying = false,
                    actionErrorMessage = "Character no longer exists. Reopen it from the list."
                )
            }
        }
    }

    private fun navigateAfterApply(onApplied: () -> Unit) {
        try {
            onApplied()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            uiState = uiState.copy(actionErrorMessage = "Level up applied, but navigation failed. Try again.")
        }
    }

    private fun launchBlock(block: suspend () -> Unit) {
        launchAsync?.invoke(block) ?: viewModelScope.launch { block() }
    }

    private fun com.vinni.dndcharacterlist.core.rules.levelup.LevelUpPreview.subclassRequirement():
        LevelUpRequirement.SubclassSelection? {
        return requirements.filterIsInstance<LevelUpRequirement.SubclassSelection>().firstOrNull()
    }

    private fun com.vinni.dndcharacterlist.core.rules.levelup.LevelUpPreview.unsupportedRequirements():
        List<LevelUpRequirement.UnsupportedChoice> {
        return requirements.filterIsInstance<LevelUpRequirement.UnsupportedChoice>()
    }

    private fun LevelUpRequirement.SubclassSelection.toUiModel(): SubclassRequirementUiModel {
        return SubclassRequirementUiModel(
            title = title,
            description = description,
            options = options.map { option ->
                SubclassOptionUiModel(id = option.id, name = option.name)
            }
        )
    }
}
