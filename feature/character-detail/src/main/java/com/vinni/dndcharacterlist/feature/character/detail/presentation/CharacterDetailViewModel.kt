package com.vinni.dndcharacterlist.feature.character.detail.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class CharacterDetailUiState(
    val isLoading: Boolean = true,
    val character: CharacterDetailModel? = null,
    val isDuplicating: Boolean = false,
    val actionErrorMessage: String? = null
)

data class CharacterDetailModel(
    val id: Long,
    val name: String,
    val level: Int,
    val subtitle: String,
    val ruleset: String,
    val progressionDetails: List<String>,
    val alignment: String,
    val background: String,
    val armorClass: Int,
    val hitPoints: Int,
    val hitPointsMax: Int,
    val savingThrowProficiencies: List<String>,
    val skillProficiencies: List<String>,
    val stats: List<StatValue>,
    val notes: String,
    val canLevelUp: Boolean
)

data class StatValue(
    val label: String,
    val value: Int
)

class CharacterDetailViewModel(
    private val repository: CharacterRepository,
    private val characterId: Long
) : ViewModel() {
    var uiState by mutableStateOf(CharacterDetailUiState())
        private set

    init {
        viewModelScope.launch {
            repository.observeCharacter(characterId)
                .onStart { uiState = uiState.copy(isLoading = true) }
                .collect { character ->
                    uiState = uiState.copy(
                        isLoading = false,
                        character = character?.toDetailModel()
                    )
                }
        }
    }

    fun duplicate(onDuplicated: (Long) -> Unit) {
        if (uiState.isDuplicating) return

        viewModelScope.launch {
            uiState = uiState.copy(isDuplicating = true, actionErrorMessage = null)
            try {
                val sourceCharacter = repository.getCharacter(characterId)
                if (sourceCharacter == null) {
                    uiState = uiState.copy(
                        isDuplicating = false,
                        actionErrorMessage = "Character no longer exists. Reopen it from the list."
                    )
                    return@launch
                }

                val duplicatedId = repository.createCharacter(
                    sourceCharacter.duplicateAsDraft(timestamp = System.currentTimeMillis())
                )
                try {
                    onDuplicated(duplicatedId)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    uiState = uiState.copy(
                        actionErrorMessage = "Character duplicated, but navigation failed. Try again."
                    )
                }
            } finally {
                uiState = uiState.copy(isDuplicating = false)
            }
        }
    }
}

private fun CharacterRecord.toDetailModel(): CharacterDetailModel {
    return CharacterDetailModel(
        id = id,
        name = name,
        level = level,
        subtitle = buildList {
            add("Level $level")
            if (race.isNotBlank()) add(race)
            if (characterClass.isNotBlank()) add(characterClass)
            if (subclass.isNotBlank()) add(subclass)
        }.joinToString(" | "),
        ruleset = ruleset.toRulesetLabel(),
        progressionDetails = buildList {
            if (characterClass.isNotBlank()) add("Class: $characterClass")
            if (subclass.isNotBlank()) add("Subclass: $subclass")
            add("Level $level")
        },
        alignment = alignment,
        background = background,
        armorClass = armorClass,
        hitPoints = hitPoints,
        hitPointsMax = hitPointsMax,
        savingThrowProficiencies = savingThrowProficiencies.map(String::toDetailLabel),
        skillProficiencies = skillProficiencies.map(String::toDetailLabel),
        stats = listOf(
            StatValue("STR", strength),
            StatValue("DEX", dexterity),
            StatValue("CON", constitution),
            StatValue("INT", intelligence),
            StatValue("WIS", wisdom),
            StatValue("CHA", charisma)
        ),
        notes = notes,
        canLevelUp = level < 20
    )
}

private fun CharacterRecord.duplicateAsDraft(timestamp: Long): CharacterRecord {
    return copy(
        id = 0L,
        updatedAt = timestamp
    )
}

private fun String.toRulesetLabel(): String {
    return when (trim()) {
        "PHB_2014" -> "PHB 2014"
        else -> toDetailLabel()
    }
}

private fun String.toDetailLabel(): String {
    val normalized = trim()
        .replace('_', ' ')
        .replace('-', ' ')
    if (normalized.isBlank()) return normalized
    return normalized
        .split(' ')
        .filter(String::isNotBlank)
        .joinToString(" ") { part ->
            part.lowercase().replaceFirstChar { char -> char.titlecase() }
        }
}

