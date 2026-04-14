package com.vinni.dndcharacterlist.feature.character.detail.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class CharacterDetailUiState(
    val isLoading: Boolean = true,
    val character: CharacterDetailModel? = null
)

data class CharacterDetailModel(
    val id: Long,
    val name: String,
    val level: Int,
    val subtitle: String,
    val alignment: String,
    val background: String,
    val armorClass: Int,
    val hitPoints: Int,
    val hitPointsMax: Int,
    val stats: List<StatValue>,
    val notes: String,
    val canLevelUp: Boolean
)

data class StatValue(
    val label: String,
    val value: Int
)

class CharacterDetailViewModel(
    repository: CharacterRepository,
    characterId: Long
) : ViewModel() {
    var uiState by mutableStateOf(CharacterDetailUiState())
        private set

    init {
        viewModelScope.launch {
            repository.observeCharacter(characterId)
                .onStart { uiState = uiState.copy(isLoading = true) }
                .collect { character ->
                    uiState = CharacterDetailUiState(
                        isLoading = false,
                        character = character?.toDetailModel()
                    )
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
        alignment = alignment,
        background = background,
        armorClass = armorClass,
        hitPoints = hitPoints,
        hitPointsMax = hitPointsMax,
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

