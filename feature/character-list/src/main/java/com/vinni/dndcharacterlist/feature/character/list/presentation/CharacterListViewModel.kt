package com.vinni.dndcharacterlist.feature.character.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CharacterListItem(
    val id: Long,
    val name: String,
    val summary: String
)

data class CharacterListUiState(
    val isLoading: Boolean = true,
    val characters: List<CharacterListItem> = emptyList(),
    val errorMessage: String? = null
)

class CharacterListViewModel(
    repository: CharacterRepository,
    scope: CoroutineScope? = null
) : ViewModel() {
    private val stateScope = scope ?: viewModelScope

    val uiState: StateFlow<CharacterListUiState> = repository.observeCharacters()
        .map { characters ->
            CharacterListUiState(
                isLoading = false,
                characters = characters.map(CharacterRecord::toListItem)
            )
        }
        .catch {
            emit(
                CharacterListUiState(
                    isLoading = false,
                    errorMessage = "Couldn't load characters. Try again."
                )
            )
        }
        .stateIn(
            scope = stateScope,
            started = SharingStarted.Eagerly,
            initialValue = CharacterListUiState(isLoading = true)
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

