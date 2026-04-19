package com.vinni.dndcharacterlist.feature.character.creation.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import com.vinni.dndcharacterlist.core.rules.creation.mapper.CharacterCreationMapper
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityMethod
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityScores
import com.vinni.dndcharacterlist.core.rules.creation.model.AbilityType
import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationDraft
import com.vinni.dndcharacterlist.core.rules.creation.model.CharacterCreationStep
import com.vinni.dndcharacterlist.core.rules.creation.model.DerivedCharacterStats
import com.vinni.dndcharacterlist.core.rules.creation.model.Ruleset
import com.vinni.dndcharacterlist.core.rules.creation.repository.RulesRepository
import com.vinni.dndcharacterlist.core.rules.creation.rules.AbilityGenerationRules
import com.vinni.dndcharacterlist.core.rules.creation.rules.CharacterCreationRulesEngine
import com.vinni.dndcharacterlist.core.rules.creation.rules.RulesContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class CharacterCreationUiState(
    val currentStep: CharacterCreationStep = CharacterCreationStep.ORIGIN,
    val draft: CharacterCreationDraft = CharacterCreationDraft(),
    val derived: DerivedCharacterStats = DerivedCharacterStats(),
    val rulesContent: RulesContent,
    val isSubmitting: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isDiscardConfirmationVisible: Boolean = false,
    val stepError: String? = null,
    val createdCharacterId: Long? = null
)

class CharacterCreationViewModel(
    repository: RulesRepository,
    private val characterRepository: CharacterRepository,
    private val mapper: CharacterCreationMapper = CharacterCreationMapper(),
    private val rulesEngine: CharacterCreationRulesEngine = CharacterCreationRulesEngine(repository),
    private val launchCreate: ((block: suspend () -> Unit) -> Unit)? = null
) : ViewModel() {

    private val rulesContent = repository.getRuleset(Ruleset.PHB_2014)
    private var persistedDraft = CharacterCreationDraft()

    var uiState by mutableStateOf(
        CharacterCreationUiState(
            rulesContent = rulesContent
        ).recalculate(rulesEngine).withDirtyState()
    )
        private set

    fun updateName(value: String) = updateDraft { copy(name = value) }

    fun updateRace(raceId: String) = updateDraft {
        val race = uiState.rulesContent.races.firstOrNull { it.id == raceId }
        copy(
            raceId = raceId,
            subraceId = race?.subraces?.singleOrNull()?.id
        )
    }

    fun updateSubrace(subraceId: String?) = updateDraft { copy(subraceId = subraceId) }

    fun updateBackground(backgroundId: String) = updateDraft { copy(backgroundId = backgroundId) }

    fun updateClass(classId: String) = updateDraft {
        copy(
            classId = classId,
            subclassId = null,
            selectedClassSkills = emptySet(),
            selectedReplacementSkills = emptyMap()
        )
    }

    fun updateSubclass(subclassId: String?) = updateDraft { copy(subclassId = subclassId) }

    fun updateAbilityMethod(method: AbilityMethod) = updateDraft {
        copy(
            abilityMethod = method,
            baseAbilities = when (method) {
                AbilityMethod.MANUAL -> baseAbilities ?: AbilityGenerationRules.defaultScoresForMethod()
                AbilityMethod.STANDARD_ARRAY -> AbilityGenerationRules.standardArrayDefaultAssignment()
                AbilityMethod.POINT_BUY -> AbilityGenerationRules.defaultScoresForMethod()
                AbilityMethod.ROLL -> AbilityGenerationRules.rollSet()
            }
        )
    }

    fun updateBaseAbilities(scores: AbilityScores) = updateDraft { copy(baseAbilities = scores) }

    fun adjustPointBuyAbility(abilityType: AbilityType, delta: Int) {
        val currentScores = uiState.draft.baseAbilities ?: AbilityGenerationRules.defaultScoresForMethod()
        val currentValue = currentScores[abilityType]
        val nextValue = (currentValue + delta).coerceIn(8, 15)
        if (nextValue == currentValue) return

        val updatedScores = AbilityGenerationRules.updateAbility(currentScores, abilityType, nextValue)
        if (AbilityGenerationRules.pointBuyCost(updatedScores) > 27) return

        updateBaseAbilities(updatedScores)
    }

    fun rollAbilities() {
        if (uiState.draft.abilityMethod != AbilityMethod.ROLL) return
        updateBaseAbilities(AbilityGenerationRules.rollSet())
    }

    fun applyStandardArray() {
        if (uiState.draft.abilityMethod != AbilityMethod.STANDARD_ARRAY) return
        updateBaseAbilities(AbilityGenerationRules.standardArrayDefaultAssignment())
    }

    fun toggleClassSkill(skillId: String) {
        updateDraft {
            val selected = selectedClassSkills.toMutableSet()
            if (!selected.add(skillId)) {
                selected.remove(skillId)
            }
            copy(selectedClassSkills = selected)
        }
    }

    fun updateReplacementSkill(conflictingSkillId: String, replacementSkillId: String) {
        updateDraft {
            copy(
                selectedReplacementSkills = selectedReplacementSkills + (conflictingSkillId to replacementSkillId)
            )
        }
    }

    fun nextStep() {
        val validation = validateCurrentStep()
        if (validation != null) {
            uiState = uiState.copy(stepError = validation)
            return
        }
        uiState = uiState.copy(
            currentStep = uiState.currentStep.next(),
            stepError = null
        )
    }

    fun previousStep() {
        val previous = uiState.currentStep.previous() ?: return
        uiState = uiState.copy(currentStep = previous, stepError = null)
    }

    fun requestExit(onExit: () -> Unit) {
        when {
            uiState.isDiscardConfirmationVisible -> {
                uiState = uiState.copy(isDiscardConfirmationVisible = false)
            }

            uiState.isSubmitting -> Unit
            uiState.hasUnsavedChanges -> {
                uiState = uiState.copy(
                    isDiscardConfirmationVisible = true,
                    stepError = null
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

    fun createCharacter(onCreated: (Long) -> Unit) {
        if (uiState.currentStep != CharacterCreationStep.SUMMARY) return
        if (uiState.isSubmitting) return

        val existingCharacterId = uiState.createdCharacterId
        if (existingCharacterId != null) {
            try {
                onCreated(existingCharacterId)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                uiState = uiState.copy(stepError = "Character created, but navigation failed. Try again.")
            }
            return
        }

        uiState = uiState.copy(isSubmitting = true)
        val launcher = launchCreate ?: { block: suspend () -> Unit ->
            viewModelScope.launch { block() }
        }
        launcher {
            val characterId = try {
                characterRepository.createCharacter(
                    mapper.toCharacterRecord(
                        draft = uiState.draft,
                        derived = uiState.derived,
                        rulesContent = uiState.rulesContent
                    )
                )
            } catch (error: CancellationException) {
                uiState = uiState.copy(isSubmitting = false)
                throw error
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isSubmitting = false,
                    stepError = "Failed to create character. Try again."
                )
                return@launcher
            }

            persistedDraft = uiState.draft
            uiState = uiState.copy(
                isSubmitting = false,
                isDiscardConfirmationVisible = false,
                createdCharacterId = characterId,
                stepError = null
            ).withDirtyState()
            try {
                onCreated(characterId)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                uiState = uiState.copy(stepError = "Character created, but navigation failed. Try again.")
            }
        }
    }

    private fun updateDraft(update: CharacterCreationDraft.() -> CharacterCreationDraft) {
        uiState = uiState.copy(
            draft = uiState.draft.update(),
            isDiscardConfirmationVisible = false,
            stepError = null
        ).recalculate(rulesEngine).withDirtyState()
    }

    private fun validateCurrentStep(): String? {
        return when (uiState.currentStep) {
            CharacterCreationStep.ORIGIN -> when {
                uiState.draft.name.isBlank() -> "Name is required."
                uiState.draft.raceId == null -> "Choose a race."
                currentRaceRequiresSubrace() && uiState.draft.subraceId == null -> "Choose a subrace."
                uiState.draft.backgroundId == null -> "Choose a background."
                else -> null
            }

            CharacterCreationStep.CLASS -> when {
                uiState.draft.classId == null -> "Choose a class."
                subclassIsRequired() && uiState.draft.subclassId == null -> "Choose a subclass."
                else -> null
            }

            CharacterCreationStep.ABILITIES -> when {
                uiState.draft.abilityMethod == null -> "Choose an ability generation method."
                uiState.draft.baseAbilities == null -> "Enter the ability scores."
                uiState.derived.validationIssues.any { it.key.startsWith("abilities_") } ->
                    uiState.derived.validationIssues.first { it.key.startsWith("abilities_") }.message
                else -> null
            }

            CharacterCreationStep.SKILLS -> {
                uiState.derived.validationIssues.firstOrNull {
                    it.key == "class_skills_count" || it.key.startsWith("background_skill")
                }?.message
            }

            CharacterCreationStep.DERIVED,
            CharacterCreationStep.SUMMARY -> null
        }
    }

    private fun currentRaceRequiresSubrace(): Boolean {
        val raceId = uiState.draft.raceId ?: return false
        return uiState.rulesContent.races.firstOrNull { it.id == raceId }?.subraces?.isNotEmpty() == true
    }

    private fun subclassIsRequired(): Boolean {
        val classId = uiState.draft.classId ?: return false
        val classDefinition = uiState.rulesContent.classes.firstOrNull { it.id == classId } ?: return false
        return classDefinition.subclassLevel == 1
    }

    private fun CharacterCreationUiState.withDirtyState(): CharacterCreationUiState {
        return copy(
            hasUnsavedChanges = createdCharacterId == null && draft != persistedDraft
        )
    }
}

private fun CharacterCreationUiState.recalculate(
    rulesEngine: CharacterCreationRulesEngine
): CharacterCreationUiState {
    return copy(derived = rulesEngine.derive(draft))
}

private fun CharacterCreationStep.next(): CharacterCreationStep {
    val all = CharacterCreationStep.entries
    val index = all.indexOf(this)
    return all.getOrElse(index + 1) { this }
}

private fun CharacterCreationStep.previous(): CharacterCreationStep? {
    val all = CharacterCreationStep.entries
    val index = all.indexOf(this)
    return all.getOrNull(index - 1)
}

