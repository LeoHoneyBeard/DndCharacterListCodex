package com.vinni.dndcharacterlist.feature.character.levelup.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.feature.character.levelup.domain.CharacterLevelUpPlanner
import com.vinni.dndcharacterlist.feature.character.levelup.domain.LevelUpCharacterUseCase
import com.vinni.dndcharacterlist.feature.character.levelup.domain.LevelUpPlan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class CharacterLevelUpUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val plan: LevelUpPlan? = null,
    val originalCharacter: CharacterRecord? = null,
    val hitPointGainInput: String = "",
    val errorMessage: String? = null,
    val completed: Boolean = false
) {
    val hitPointGainError: String?
        get() = when {
            plan == null -> null
            hitPointGainInput.toIntOrNull()?.let { it >= 1 } != true -> "HP gain must be 1 or higher."
            else -> null
        }

    val canApply: Boolean
        get() = !isSaving && plan != null && originalCharacter != null && hitPointGainError == null

    val projectedHitPointsMax: Int?
        get() = plan?.let { currentPlan ->
            hitPointGainInput.toIntOrNull()?.let { currentPlan.currentHitPointsMax + it }
        }
}

class CharacterLevelUpViewModel(
    private val repository: CharacterRepository,
    private val levelUpPlanner: CharacterLevelUpPlanner,
    private val levelUpCharacter: LevelUpCharacterUseCase,
    characterId: Long,
    private val launchAsync: ((block: suspend () -> Unit) -> Unit)? = null
) : ViewModel() {

    var uiState by mutableStateOf(CharacterLevelUpUiState())
        private set

    init {
        launchBlock {
            val character = repository.getCharacter(characterId)
            val plan = character?.let(levelUpPlanner::createPlan)
            uiState = if (character == null) {
                CharacterLevelUpUiState(isLoading = false, errorMessage = "Character not found.")
            } else if (plan == null) {
                CharacterLevelUpUiState(
                    isLoading = false,
                    originalCharacter = character,
                    errorMessage = "This character cannot level up further."
                )
            } else {
                CharacterLevelUpUiState(
                    isLoading = false,
                    plan = plan,
                    originalCharacter = character,
                    hitPointGainInput = plan.recommendedHitPointGain.toString()
                )
            }
        }
    }

    fun updateHitPointGain(value: String) {
        uiState = uiState.copy(
            hitPointGainInput = value.filter(Char::isDigit),
            errorMessage = null,
            completed = false
        )
    }

    fun applyLevelUp(onLeveledUp: () -> Unit) {
        if (uiState.completed) {
            navigate(onLeveledUp)
            return
        }
        if (!uiState.canApply) {
            uiState = uiState.copy(errorMessage = uiState.hitPointGainError ?: uiState.errorMessage)
            return
        }

        val character = checkNotNull(uiState.originalCharacter)
        val hitPointGain = checkNotNull(uiState.hitPointGainInput.toIntOrNull())
        uiState = uiState.copy(isSaving = true, errorMessage = null)
        launchBlock {
            try {
                levelUpCharacter(character, hitPointGain)
                uiState = uiState.copy(completed = true)
                navigate(onLeveledUp)
            } catch (_: IllegalArgumentException) {
                uiState = uiState.copy(errorMessage = "Unable to apply level up. Reopen the character and try again.")
            } finally {
                uiState = uiState.copy(isSaving = false)
            }
        }
    }

    private fun navigate(onLeveledUp: () -> Unit) {
        try {
            onLeveledUp()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            uiState = uiState.copy(errorMessage = "Level up applied, but navigation failed. Try again.")
        }
    }

    private fun launchBlock(block: suspend () -> Unit) {
        launchAsync?.invoke(block) ?: viewModelScope.launch { block() }
    }
}
