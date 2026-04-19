package com.vinni.dndcharacterlist.feature.character.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinni.dndcharacterlist.core.domain.model.CharacterRecord
import com.vinni.dndcharacterlist.core.domain.repository.CharacterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class CharacterListSortMode(val label: String) {
    UPDATED_AT("Last updated"),
    NAME("Name"),
    LEVEL("Level")
}

data class CharacterListItem(
    val id: Long,
    val name: String,
    val summary: String
)

data class CharacterListUiState(
    val isLoading: Boolean = true,
    val characters: List<CharacterListItem> = emptyList(),
    val hasSavedCharacters: Boolean = false,
    val searchQuery: String = "",
    val sortMode: CharacterListSortMode = CharacterListSortMode.UPDATED_AT,
    val errorMessage: String? = null
)

class CharacterListViewModel(
    repository: CharacterRepository,
    scope: CoroutineScope? = null
) : ViewModel() {
    private val stateScope = scope ?: viewModelScope
    private val searchQuery = MutableStateFlow("")
    private val sortMode = MutableStateFlow(CharacterListSortMode.UPDATED_AT)

    val uiState: StateFlow<CharacterListUiState> = combine(
        repository.observeCharacters(),
        searchQuery,
        sortMode
    ) { characters, query, activeSortMode ->
        val visibleCharacters = characters
            .filterByQuery(query)
            .sortedByMode(activeSortMode)
            CharacterListUiState(
                isLoading = false,
                characters = visibleCharacters.map(CharacterRecord::toListItem),
                hasSavedCharacters = characters.isNotEmpty(),
                searchQuery = query,
                sortMode = activeSortMode
            )
        }
        .catch {
            emit(
                CharacterListUiState(
                    isLoading = false,
                    searchQuery = searchQuery.value,
                    sortMode = sortMode.value,
                    errorMessage = "Couldn't load characters. Try again."
                )
            )
        }
        .stateIn(
            scope = stateScope,
            started = SharingStarted.Eagerly,
            initialValue = CharacterListUiState(isLoading = true)
        )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSortMode(mode: CharacterListSortMode) {
        sortMode.value = mode
    }
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

private fun List<CharacterRecord>.filterByQuery(query: String): List<CharacterRecord> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return this
    return filter { character ->
        character.name.contains(normalizedQuery, ignoreCase = true)
    }
}

private fun List<CharacterRecord>.sortedByMode(mode: CharacterListSortMode): List<CharacterRecord> {
    val comparator = when (mode) {
        CharacterListSortMode.UPDATED_AT -> compareByDescending<CharacterRecord> { it.updatedAt }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        CharacterListSortMode.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER, CharacterRecord::name)
            .thenByDescending { it.updatedAt }
        CharacterListSortMode.LEVEL -> compareByDescending<CharacterRecord> { it.level }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            .thenByDescending { it.updatedAt }
    }
    return sortedWith(comparator)
}

private fun String.toDisplayPart(): String? {
    val sanitized = trim()
    return sanitized.takeUnless {
        sanitized.isBlank() || sanitized.equals("null", ignoreCase = true)
    }
}

