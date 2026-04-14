package com.vinni.dndcharacterlist.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.data.CharacterEntity
import com.vinni.dndcharacterlist.data.CharacterRepository
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
        .map { characters -> CharacterListUiState(characters = characters.map(CharacterEntity::toListItem)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CharacterListUiState()
        )
}

private fun CharacterEntity.toListItem(): CharacterListItem {
    val parts = listOf(
        "Lvl $level",
        race.takeIf { it.isNotBlank() },
        characterClass.takeIf { it.isNotBlank() },
        subclass.takeIf { it.isNotBlank() },
        "AC $armorClass",
        "HP $hitPoints"
    )
    return CharacterListItem(
        id = id,
        name = name,
        summary = parts.joinToString(" | ")
    )
}
