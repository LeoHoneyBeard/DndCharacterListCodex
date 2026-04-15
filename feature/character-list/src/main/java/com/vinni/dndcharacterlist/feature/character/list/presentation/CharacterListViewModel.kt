package com.vinni.dndcharacterlist.feature.character.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CharacterListItem(
    val id: Long,
    val name: String,
    val summary: String
)

data class CharacterListUiState(
    val characters: List<CharacterListItem> = emptyList()
)

class CharacterListViewModel(
    repository: CharacterRepository
) : ViewModel() {
    val uiState: StateFlow<CharacterListUiState> = repository.observeCharacters()
        .map { characters -> CharacterListUiState(characters = characters.map(CharacterRecord::toListItem)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CharacterListUiState()
        )
}

internal fun CharacterRecord.toListItem(): CharacterListItem {
    val parts = listOfNotNull(
        "Lvl $level",
        race.toDisplayPart(),
        characterClass.toDisplayPart(),
        subclass.toDisplayPart(),
        "AC $armorClass",
        "HP $hitPoints"
    )
    return CharacterListItem(
        id = id,
        name = name,
        summary = parts.joinToString(" | ")
    )
}

private fun String.toDisplayPart(): String? {
    val sanitized = trim()
    return sanitized.takeUnless {
        sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)
    }
}

